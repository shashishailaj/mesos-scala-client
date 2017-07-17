package http

import java.util.concurrent.locks.ReentrantReadWriteLock

import com.twitter.finagle.http.Response
import com.twitter.util.{Future, Promise}
import io.circe.generic.auto._
import io.circe.parser._
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode
import org.apache.curator.framework.recipes.cache.{ChildData, PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.apache.mesos.v1.mesos.MasterInfo
import org.apache.mesos.v1.scheduler.scheduler.Call
import com.twitter.logging.Logger

import scala.collection.JavaConverters._

/**
  * Created by karthik on 7/15/17.
  */
class ZkAwareHttpClient(curatorFramework: CuratorFramework,
                        path: String,
                        mesosMasterHttpClientFactory: MesosMasterHttpClientFactory) extends StreamingClient {


  private var isSubscribed = false
  private val log = Logger.get(getClass)

  private var mesosMasterHTTPClient: Option[MesosMasterHTTPClient] = None

  private val zkExitFuture = Promise[DriverStatus]()
  private val clientLock = new ReentrantReadWriteLock()

  private val pathCache = new PathChildrenCache(curatorFramework, path, true)

  private val listener = new PathChildrenCacheListener {
    private def isMesosMasterPath(event: PathChildrenCacheEvent): Boolean = {
      event.getData.getPath.startsWith("/mesos/json")
    }

    def isCurrentLeader(childPath: String): Boolean = currentLeader.contains(childPath)

    def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
      event.getType match {
        case PathChildrenCacheEvent.Type.CHILD_ADDED if isMesosMasterPath(event) => {
          val masterInfo = decodeMasterInfo(event.getData)
          if(currentLeader.isEmpty || event.getData.getPath < currentLeader.get) {
            currentLeader = Some(event.getData.getPath)
            handleLeaderChange(masterInfo)
          }
        }
        case PathChildrenCacheEvent.Type.CHILD_REMOVED if isMesosMasterPath(event) => {
          if(isCurrentLeader(event.getData.getPath)) {
            currentLeader = initializeMesosClient()
          }
        }
        case PathChildrenCacheEvent.Type.CONNECTION_LOST =>
        case PathChildrenCacheEvent.Type.CONNECTION_RECONNECTED =>
        case PathChildrenCacheEvent.Type.CONNECTION_SUSPENDED =>
        case _ =>
      }
    }
  }

  pathCache.start(StartMode.BUILD_INITIAL_CACHE)
  private var currentLeader = initializeMesosClient()

  pathCache.getListenable.addListener(listener)

  def handleLeaderChange(masterInfo: MasterInfo): Unit = {
    val writeLock = clientLock.writeLock()
    try {
      writeLock.lock()
      log.info(s"Triggering change in leader from ${mesosMasterHTTPClient.map(_.hostAndPort)} to ${masterInfo}")
      mesosMasterHTTPClient.foreach(_.shutdown(LeaderChanged))
      mesosMasterHTTPClient = Some(mesosMasterHttpClientFactory.newClient(masterInfo))
      if (isSubscribed) {
        subscribeAndRegister()
      } else {
        log.warning("Driver has not subscribed to event streams. Skipping..")
      }
    } finally {
      writeLock.unlock()
    }
  }

  def decodeMasterInfo(event: ChildData): MasterInfo = {
    val masterInfoJson = new String(event.getData)
    parse(masterInfoJson) match {
      case Left(failure) => throw new RuntimeException(s"Unable to decode $masterInfoJson as JSON")
      case Right(json) => json.as[MasterInfo] match {
        case Left(failure) => throw new RuntimeException(s"Unable to decode $masterInfoJson as MasterInfo")
        case Right(masterInfo) => masterInfo
      }
    }
  }

  def subscribe(): Future[DriverStatus] = {
    if(mesosMasterHTTPClient.isEmpty) {
      throw new RuntimeException("Unable to subscribe. Either subscribe is called twice.")
    } else {
      isSubscribed = true
      subscribeAndRegister()
      zkExitFuture
    }
  }

  private def subscribeAndRegister(): Future[DriverStatus] = {
    val exitStatusFuture = mesosMasterHTTPClient.get.subscribe()
    exitStatusFuture.onSuccess {
      case driverStatus @(HeartBeatFailure | SubscriptionNotFound) =>
        zkExitFuture.setValue(driverStatus)
      case LeaderChanged =>
        log.info("Leader has changed not propagating error back to driver.")
    }
  }

  private def initializeMesosClient(): Option[String] = {
    val children = pathCache.getCurrentData.asScala
    if(children.nonEmpty) {
      val leader = children.minBy { child =>
        child.getPath
      }
      val masterInfo = decodeMasterInfo(leader)
      handleLeaderChange(masterInfo)
      Some(leader.getPath)
    } else {
      None
    }
  }


  def shutdown(status: DriverStatus): Future[Unit] = {
    mesosMasterHTTPClient.get.shutdown(status)
      .onSuccess(_ => zkExitFuture.setValue(status))
  }

  def call(call: Call): Future[Response] = {
    val readLock = clientLock.readLock
    try {
      readLock.lock()
      mesosMasterHTTPClient.get.call(call)
    } finally {
      readLock.unlock()
    }
  }
}
