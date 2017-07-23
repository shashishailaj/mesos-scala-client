package example

import com.treadstone90.mesos.scheduler.{Scheduler, SchedulerDriver}
import org.apache.mesos.v1.mesos._
import org.apache.mesos.v1.scheduler.scheduler.Event.{Failure, Message, Offers, Subscribed}



class PrintingScheduler extends Scheduler {
  var frameworkId: Option[FrameworkID] = None

  def registered(schedulerDriver: SchedulerDriver, subscribed: Subscribed) = {
    println(subscribed)
    frameworkId = Some(subscribed.frameworkId)
  }

  def disconnected(schedulerDriver: SchedulerDriver): Unit = println("Disconnected from Mesos Master")

  def failure(schedulerDriver: SchedulerDriver, failure: Failure): Unit = println(failure)

  def statusUpdate(schedulerDriver: SchedulerDriver, status: TaskStatus): Unit = println(status)

  def offerRescinded(schedulerDriver: SchedulerDriver, offerId: OfferID): Unit = println(offerId)

  def error(schedulerDriver: SchedulerDriver, message: String): Unit = println(s"error $message")

  def resourceOffers(schedulerDriver: SchedulerDriver, offers: Offers): Unit = {
    println(offers)
    schedulerDriver.declineOffer(offers.offers.map(_.id), None)
  }

  def frameworkMessage(schedulerDriver: SchedulerDriver, message: Message): Unit = println(message)

  def executorLost(schedulerDriver: SchedulerDriver, executorID: ExecutorID, agentID: AgentID): Unit = {
    println(s"executor Lost $executorID")
  }

  def reregistered(schedulerDriver: SchedulerDriver, subscribed: Subscribed): Unit = {
    println(s"reregistered")
  }

  def agentLost(schedulerDriver: SchedulerDriver, agentID: AgentID): Unit = {
    println(s"agent Lost $agentID")
  }
}