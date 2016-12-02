package mesos

import fastparse.core.Frame
import org.apache.mesos.v1.mesos.{FrameworkID, OfferID, Status, TaskStatus}
import org.apache.mesos.v1.scheduler.scheduler.{Call, Event}
import org.apache.mesos.v1.scheduler.scheduler.Event.{Failure, Message, Offers, Subscribed}

/**
  * Created by karthik on 12/1/16.
  */
trait MesosEventHandler {

  def subscribed(schedulerDriver: Driver, subscribed: Subscribed)

  def disconnected(schedulerDriver: Driver)

  def resourceOffers(schedulerDriver: Driver, offers: Offers)

  def rescind(schedulerDriver: Driver, offerId: OfferID)

  def update(schedulerDriver: Driver, status: TaskStatus)

  def message(schedulerDriver: Driver, message: Message)

  def failure(schedulerDriver: Driver, failure: Failure)

  def error(schedulerDriver: Driver, message: String)

  def heartbeat(schedulerDriver: Driver)
}

class PrintingEventHandler extends MesosEventHandler {
  var frameworkId: Option[FrameworkID] = None

  def subscribed(schedulerDriver: Driver, subscribed: Subscribed) = {
    println(subscribed)
    frameworkId = Some(subscribed.frameworkId)
  }

  def disconnected(schedulerDriver: Driver): Unit = {
    println("Disconnected from Mesos Master")
  }

  def failure(schedulerDriver: Driver, failure: Failure): Unit = println(failure)

  def update(schedulerDriver: Driver, status: TaskStatus): Unit = println(status)

  def rescind(schedulerDriver: Driver, offerId: OfferID): Unit = println(offerId)

  def error(schedulerDriver: Driver, message: String): Unit = println(s"error $message")

  def resourceOffers(schedulerDriver: Driver, offers: Offers): Unit = print(offers)

  def message(schedulerDriver: Driver, message: Message): Unit = println(message)

  def heartbeat(schedulerDriver: Driver): Unit = println("heartbeat")
}