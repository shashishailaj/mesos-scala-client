package mesos

import java.net.URI

import com.google.common.net.HostAndPort
import com.twitter.logging.Logger
import com.twitter.util.{Await, Future}
import http.{ClientAborted, _}
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.mesos.v1.mesos.Offer.Operation
import org.apache.mesos.v1.mesos.Offer.Operation.Launch
import org.apache.mesos.v1.mesos.Status.{DRIVER_ABORTED, DRIVER_NOT_STARTED, DRIVER_RUNNING, DRIVER_STOPPED}
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

  private val log = Logger.get(getClass)
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

  def run(): Status = {
    completeFuture {
      subscribe()
    }
  }

  def abort() = {
    completeFuture {
      schedulerStreamingClient.shutdown(ClientAborted)
    }
  }

  def acceptOffers(accept: Accept) = {
    completeFuture {
      schedulerStreamingClient.call(Call(`type` = Some(ACCEPT), accept = Some(accept)))
    }
  }

  def teardown() = {
    completeFuture {
      schedulerStreamingClient.call(Call(`type` = Some(TEARDOWN)))
    }
  }

  def declineOffer(offerIds: Seq[OfferID], filters: Option[Filters]): Status = {
    completeFuture {
      schedulerStreamingClient.call(Call(`type` = Some(DECLINE), decline = Some(Decline(offerIds, filters))))
    }
  }

  def reviveOffers(): Status = {
    completeFuture {
      schedulerStreamingClient.call(Call(`type` = Some(REVIVE)))
    }
  }

  def killTask(kill: Kill): Status = {
    completeFuture {
      schedulerStreamingClient.call(Call(`type` = Some(KILL), kill = Some(kill)))
    }
  }

  def shutdown(shutdown: Call.Shutdown): Status = {
    completeFuture {
      schedulerStreamingClient.call(Call(`type` = Some(Call.Type.SHUTDOWN), shutdown = Some(shutdown)))
    }
  }

  def acknowledgeStatusUpdate(acknowledge: Acknowledge): Status = {
    completeFuture {
      schedulerStreamingClient.call(Call(`type` = Some(ACKNOWLEDGE), acknowledge = Some(acknowledge)))
    }
  }

  def reconcileTasks(reconcile: Reconcile): Status = {
    completeFuture {
      schedulerStreamingClient.call(Call(`type` = Some(RECONCILE), reconcile = Some(reconcile)))
    }
  }

  def sendFrameworkMessage(message: Message): Status = {
    completeFuture {
      schedulerStreamingClient.call(Call(`type` = Some(MESSAGE), message = Some(message)))
    }
  }

  def requestResources(requests: Call.Request): Status = {
    completeFuture {
      schedulerStreamingClient.call(Call(`type` = Some(REQUEST), request = Some(requests)))
    }
  }

  def launchTasks(offerIds: Seq[OfferID], tasks: Seq[TaskInfo]): Status = {
    val operation = Operation(`type` = Some(Operation.Type.LAUNCH), launch = Some(Launch(tasks)))
    acceptOffers(Accept(offerIds, Seq(operation)))
  }

  def launchTasks(offerIds: Seq[OfferID], tasks: Seq[TaskInfo], filters: Filters): Status = {
    val operation = Operation(`type` = Some(Operation.Type.LAUNCH),
      launch = Some(Launch(tasks)))
    acceptOffers(Accept(offerIds, Seq(operation), filters = Some(filters)))
  }

  def stop(failover: Boolean): Status = {
    completeFuture {
      if (failover) {
        schedulerStreamingClient.shutdown(ClientAborted)
      } else {
        schedulerStreamingClient.call(Call(`type` = Some(TEARDOWN)))
          .flatMap(_ => schedulerStreamingClient.shutdown(ClientAborted))
      }
    }
    DRIVER_STOPPED
  }

  def suppressOffers(): Status = {
    completeFuture {
      schedulerStreamingClient.call(Call(`type` = Some(SUPPRESS)))
    }
  }

  private def subscribe(): Future[ClientStatus] = {
    schedulerStreamingClient.subscribe()
  }

  private def completeFuture(f: => Future[ClientStatus]): Status = {
    try {
      Await.result(f) match {
        case SubscriptionNotFound => DRIVER_NOT_STARTED
        case ClientAborted => DRIVER_ABORTED
        case x: ClientErrorStatus => DRIVER_ABORTED
        case x: ClientRuntimeStatus => DRIVER_RUNNING
      }
    } catch {
      case e: Exception => {
        log.error(e, "Error returned from client")
        DRIVER_ABORTED
      }
    }
  }
}
