package http

import org.apache.mesos.v1.mesos.FrameworkID
import org.apache.mesos.v1.scheduler.scheduler.Event
import org.apache.mesos.v1.scheduler.scheduler.Event.Type.{ERROR, FAILURE, HEARTBEAT, MESSAGE, OFFERS, RESCIND, SUBSCRIBED, UNKNOWN, UPDATE}

/**
  * Created by karthik on 11/24/16.
  */
trait MesosEventHandler {
  def handleEvent(event: Event): Unit = {
    event.`type` match {
      case Some(HEARTBEAT) =>  handleHeartbeat(event)
      case Some(OFFERS) => handleOffers(event)
      case Some(RESCIND) => handleRescind(event)
      case Some(UPDATE) => handleUpdate(event)
      case Some(MESSAGE) => handleMessage(event)
      case Some(FAILURE) => handleFailure(event)
      case Some(ERROR) => handleError(event)
      case Some(SUBSCRIBED) => handleSubscribed(event)
      case Some(UNKNOWN) => handleUnknown(event)
      case _ => println("Unknown mesage cannot handle")
    }
  }

  def handleSubscribed(event: Event)
  def handleHeartbeat(event: Event)
  def handleOffers(event: Event)
  def handleRescind(event: Event)
  def handleUpdate(event: Event)
  def handleMessage(event: Event)
  def handleFailure(event: Event)
  def handleError(event: Event)
  def handleUnknown(event: Event)
}

class PrintingMesosEventHandler extends MesosEventHandler {
  private var frameworkID: Option[FrameworkID] = None
  def handleSubscribed(event: Event): Unit = {
    println(s"Handling ${event.`type`}")
    frameworkID = event.subscribed.map(_.frameworkId)
  }

  def handleFailure(event: Event): Unit = println(s"Handling ${event.`type`}")

  def handleOffers(event: Event): Unit = println(s"Handling ${event.`type`}")

  def handleUpdate(event: Event): Unit = println(s"Handling ${event.`type`}")

  def handleRescind(event: Event): Unit = println(s"Handling ${event.`type`}")

  def handleMessage(event: Event): Unit = println(s"Handling ${event.`type`}")

  def handleError(event: Event): Unit = println(s"Handling ${event.`type`}")

  def handleHeartbeat(event: Event): Unit = println(s"Handling ${event.`type`}")

  def handleUnknown(event: Event): Unit = println(s"Handling ${event.`type`}")

  def getFrameworkId(): Option[FrameworkID] = frameworkID
}
