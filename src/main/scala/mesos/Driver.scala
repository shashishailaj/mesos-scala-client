package mesos

import org.apache.mesos.v1.mesos.{Filters, OfferID, TaskInfo}
import org.apache.mesos.v1.scheduler.scheduler.Call
import org.apache.mesos.v1.scheduler.scheduler.Call.{Accept, Acknowledge, Kill, Message, Reconcile}

/**
  * Created by karthik on 11/30/16.
  */
trait Driver {
  def abort(): Unit

  def acceptOffers(accept: Accept): Unit

  def acknowledgeStatusUpdate(acknowledge: Acknowledge): Unit

  def declineOffer(offerIds: Seq[OfferID], filters: Option[Filters]): Unit

  def killTask(kill: Kill): Unit

  def shutdown(shutdown: Call.Shutdown): Unit

  def launchTasks(offerIds: Seq[OfferID], tasks: Seq[TaskInfo]): Unit

  def launchTasks(offerIds: Seq[OfferID], tasks: Seq[TaskInfo], filters: Filters): Unit

  def reconcileTasks(reconcile: Reconcile): Unit

  def requestResources(requests: Call.Request): Unit

  def reviveOffers(): Unit

  def run(): Unit

  def sendFrameworkMessage(message: Message): Unit

  def stop(failover: Boolean): Unit

  def suppressOffers(): Unit
}
