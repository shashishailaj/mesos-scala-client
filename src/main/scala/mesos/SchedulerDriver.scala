package mesos

import java.net.URI

import com.google.common.net.HostAndPort
import com.twitter.util.{Await, Future}
import http._
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.mesos.v1.mesos.Offer.Operation
import org.apache.mesos.v1.mesos.Offer.Operation.Launch
import org.apache.mesos.v1.mesos._
import org.apache.mesos.v1.scheduler.scheduler.Call
import org.apache.mesos.v1.scheduler.scheduler.Call.Type.{ACCEPT, ACKNOWLEDGE, DECLINE, KILL, MESSAGE, RECONCILE, REQUEST, REVIVE, SUPPRESS, TEARDOWN}
import org.apache.mesos.v1.scheduler.scheduler.Call.{Accept, Acknowledge, Decline, Kill, Message, Reconcile}

/**
  * Created by karthik on 12/1/16.
  */
class SchedulerDriver(eventHandler: Scheduler,
                      frameworkInfo: FrameworkInfo,
                      masterInfo: String) extends Driver {

  private val clientFactory = new MesosMasterHttpClientFactory(frameworkInfo, eventHandler, this)
  private val schedulerStreamingClient: StreamingClient = if(masterInfo.startsWith("zk")) {
    val zkConnectString =  new URI(masterInfo)
    val curatorFramework =
      CuratorFrameworkFactory.newClient(zkConnectString.getAuthority, new ExponentialBackoffRetry(1000, 3))
    curatorFramework.start()
    new ZkAwareHttpClient(curatorFramework, zkConnectString.getPath, clientFactory)
  } else {
    val hostAndPort = HostAndPort.fromString(masterInfo)
    clientFactory.newClient(hostAndPort)
  }

  def run(): Unit = {
    val driverFuture = subscribe()
    Await.result(driverFuture)
  }

  def abort() = {
    schedulerStreamingClient.shutdown(DriverAborted)
  }

  def acceptOffers(accept: Accept) = {
    schedulerStreamingClient.call(Call(`type` = Some(ACCEPT), accept = Some(accept)))
  }

  def teardown() = {
    schedulerStreamingClient.call(Call(`type` = Some(TEARDOWN)))
  }

  def declineOffer(offerIds: Seq[OfferID], filters: Option[Filters]): Unit = {
    schedulerStreamingClient.call(Call(`type` = Some(DECLINE), decline = Some(Decline(offerIds, filters))))
  }

  def reviveOffers() = {
    schedulerStreamingClient.call(Call(`type` = Some(REVIVE)))
  }

  def killTask(kill: Kill) = {
    schedulerStreamingClient.call(Call(`type` = Some(KILL), kill = Some(kill)))
  }

  def shutdown(shutdown: Call.Shutdown): Unit = {
    schedulerStreamingClient.call(Call(`type` = Some(Call.Type.SHUTDOWN), shutdown = Some(shutdown)))
  }

  def acknowledgeStatusUpdate(acknowledge: Acknowledge): Unit = {
    schedulerStreamingClient.call(Call(`type` = Some(ACKNOWLEDGE), acknowledge = Some(acknowledge)))
  }

  def reconcileTasks(reconcile: Reconcile): Unit = {
    schedulerStreamingClient.call(Call(`type` = Some(RECONCILE), reconcile = Some(reconcile)))
  }

  def sendFrameworkMessage(message: Message): Unit = {
    schedulerStreamingClient.call(Call(`type` = Some(MESSAGE), message = Some(message)))
  }

  def requestResources(requests: Call.Request): Unit = {
    schedulerStreamingClient.call(Call(`type` = Some(REQUEST), request = Some(requests)))
  }

  def launchTasks(offerIds: Seq[OfferID], tasks: Seq[TaskInfo]): Unit = {
    val operation = Operation(`type` = Some(Operation.Type.LAUNCH), launch = Some(Launch(tasks)))
    acceptOffers(Accept(offerIds, Seq(operation)))
  }

  def launchTasks(offerIds: Seq[OfferID], tasks: Seq[TaskInfo], filters: Filters): Unit = {
    val operation = Operation(`type` = Some(Operation.Type.LAUNCH),
      launch = Some(Launch(tasks)))
    acceptOffers(Accept(offerIds, Seq(operation), filters = Some(filters)))
  }

  def stop(failover: Boolean): Unit = {
    if(!failover) {
      teardown()
      abort()
    } else {
      abort()
    }
  }

  def suppressOffers(): Unit = {
    schedulerStreamingClient.call(Call(`type` = Some(SUPPRESS)))
  }

  private def subscribe(): Future[DriverStatus] = {
    schedulerStreamingClient.subscribe()
  }
}
