package com.treadstone90.mesos.scheduler

import org.apache.mesos.v1.mesos.{Filters, OfferID, Status, TaskInfo}
import org.apache.mesos.v1.scheduler.scheduler.Call
import org.apache.mesos.v1.scheduler.scheduler.Call.{Accept, Acknowledge, Kill, Message, Reconcile}

/**
  * Created by karthik on 11/30/16.
  */
trait Driver {
  def abort(): Unit

  def acceptOffers(accept: Accept): Status

  def acknowledgeStatusUpdate(acknowledge: Acknowledge): Status

  def declineOffer(offerIds: Seq[OfferID], filters: Option[Filters]): Status

  def killTask(kill: Kill): Status

  def shutdown(shutdown: Call.Shutdown): Status

  def launchTasks(offerIds: Seq[OfferID], tasks: Seq[TaskInfo]): Status

  def launchTasks(offerIds: Seq[OfferID], tasks: Seq[TaskInfo], filters: Filters): Status

  def reconcileTasks(reconcile: Reconcile): Status

  def requestResources(requests: Call.Request): Status

  def reviveOffers(): Status

  def run(): Status

  def sendFrameworkMessage(message: Message): Status

  def stop(failover: Boolean): Status

  def suppressOffers(): Status
}
