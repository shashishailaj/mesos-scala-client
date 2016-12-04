package mesos

import com.twitter.util.Future
import http.{MesosStreamSubscription, SchedulerStreamingClient}
import org.apache.mesos.v1.mesos._
import org.apache.mesos.v1.scheduler.scheduler.Call
import org.apache.mesos.v1.scheduler.scheduler.Call.Type.{ACKNOWLEDGE, DECLINE, KILL, MESSAGE, RECONCILE, REQUEST, REVIVE, TEARDOWN}
import org.apache.mesos.v1.scheduler.scheduler.Call.{Accept, Acknowledge, Decline, Kill, Message, Reconcile}

/**
  * Created by karthik on 12/1/16.
  */
class SchedulerDriver(eventHandler: MesosEventHandler,
                      frameworkInfo: FrameworkInfo,
                      masterInfo: MasterInfo,
                      schedulerStreamingClient: SchedulerStreamingClient) extends Driver {

  val host: String = masterInfo.address.get.hostname + ":" + masterInfo.address.get.port

  def subscribe(): Future[Option[MesosStreamSubscription]]= {
    schedulerStreamingClient.subscribe(frameworkInfo, eventHandler, this)
  }

  def accept(frameworkID: FrameworkID, accept: Accept) = {
    schedulerStreamingClient.call(Call(`type` = Some(TEARDOWN),
      frameworkId = Some(frameworkID), accept = Some(accept)))
  }

  def teardown(frameworkID: FrameworkID) = {
    schedulerStreamingClient.call(Call(`type` = Some(TEARDOWN),
      frameworkId = Some(frameworkID)))
  }

  def decline(frameworkID: FrameworkID, offerIds: Seq[OfferID], filters: Option[Filters]): Unit = {
    schedulerStreamingClient.call(Call(`type` = Some(DECLINE),
      frameworkId = Some(frameworkID),
      decline = Some(Decline(offerIds, filters))))
  }

  def revive(frameworkID: FrameworkID) = {
    schedulerStreamingClient.call(Call(`type` = Some(REVIVE),
      frameworkId = Some(frameworkID)))
  }

  def kill(frameworkID: FrameworkID, kill: Kill) = {
    schedulerStreamingClient.call(Call(`type` = Some(KILL),
      frameworkId = Some(frameworkID), kill = Some(kill)))
  }

  def shutdown(frameworkID: FrameworkID, shutdown: Call.Shutdown): Unit = {
    schedulerStreamingClient.call(Call(`type` = Some(Call.Type.SHUTDOWN),
      frameworkId = Some(frameworkID), shutdown = Some(shutdown)))
  }

  def acknowledge(frameworkID: FrameworkID, acknowledge: Acknowledge): Unit = {
    schedulerStreamingClient.call(Call(`type` = Some(ACKNOWLEDGE),
      frameworkId = Some(frameworkID), acknowledge = Some(acknowledge)))
  }

  def reconcile(frameworkID: FrameworkID, reconcile: Reconcile): Unit = {
    schedulerStreamingClient.call(Call(`type` = Some(RECONCILE),
      frameworkId = Some(frameworkID),
      reconcile = Some(reconcile)))
  }

  def message(frameworkID: FrameworkID, message: Message): Unit = {
    schedulerStreamingClient.call(Call(`type` = Some(MESSAGE), frameworkId = Some(frameworkID),
      message = Some(message)))
  }

  def request(frameworkID: FrameworkID, requests: Call.Request): Unit = {
    schedulerStreamingClient.call(Call(`type` = Some(REQUEST), frameworkId = Some(frameworkID),
      request = Some(requests)))
  }

  def exit = {
    schedulerStreamingClient.shutdownClient()
  }
}
