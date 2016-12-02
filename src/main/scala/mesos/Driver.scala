package mesos

import org.apache.mesos.v1.mesos.{Filters, FrameworkID, OfferID}
import org.apache.mesos.v1.scheduler.scheduler.Call
import org.apache.mesos.v1.scheduler.scheduler.Call.{Accept, Acknowledge, Kill, Message, Reconcile}

/**
  * Created by karthik on 11/30/16.
  */
trait Driver {
  def teardown(frameworkID: FrameworkID)

  def accept(frameworkID: FrameworkID, accept: Accept)

  def decline(frameworkID: FrameworkID, offerIds: Seq[OfferID], filters: Option[Filters])

  def revive(frameworkID: FrameworkID)

  def kill(frameworkID: FrameworkID, kill: Kill)

  def shutdown(frameworkID: FrameworkID, shutdown: Call.Shutdown)

  def acknowledge(frameworkID: FrameworkID, acknowledge: Acknowledge)

  def reconcile(frameworkID: FrameworkID, reconcile: Reconcile)

  def message(frameworkID: FrameworkID, message: Message)

  def request(frameworkID: FrameworkID, requests: Call.Request)
}
