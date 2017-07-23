package com.treadstone90.mesos.http

import com.treadstone90.mesos.scheduler.{Scheduler, SchedulerDriver}
import org.apache.mesos.v1.mesos.{AgentID, ExecutorID, OfferID, TaskStatus}
import org.apache.mesos.v1.scheduler.scheduler.Event.{Failure, Message, Offers, Subscribed}

/**
  * Created by karthik on 7/22/17.
  */
class ZkWrappingScheduler(scheduler: Scheduler, isSubscribed: => Boolean) extends Scheduler {
  def disconnected(schedulerDriver: SchedulerDriver): Unit =
    scheduler.disconnected(schedulerDriver)

  def offerRescinded(schedulerDriver: SchedulerDriver, offerId: OfferID): Unit =
    scheduler.offerRescinded(schedulerDriver, offerId)

  def failure(schedulerDriver: SchedulerDriver, failure: Failure): Unit =
    scheduler.failure(schedulerDriver, failure)

  def reregistered(schedulerDriver: SchedulerDriver, subscribed: Subscribed): Unit =
    scheduler.reregistered(schedulerDriver, subscribed)

  def agentLost(schedulerDriver: SchedulerDriver, agentID: AgentID): Unit =
    scheduler.agentLost(schedulerDriver, agentID)

  def error(schedulerDriver: SchedulerDriver, message: String): Unit =
    scheduler.error(schedulerDriver, message)

  def statusUpdate(schedulerDriver: SchedulerDriver, status: TaskStatus): Unit =
    scheduler.statusUpdate(schedulerDriver, status)

  def frameworkMessage(schedulerDriver: SchedulerDriver, message: Message): Unit =
    scheduler.frameworkMessage(schedulerDriver, message)

  def resourceOffers(schedulerDriver: SchedulerDriver, offers: Offers): Unit =
    scheduler.resourceOffers(schedulerDriver, offers)

  def registered(schedulerDriver: SchedulerDriver, subscribed: Subscribed): Unit = {
    if(isSubscribed) {
      scheduler.reregistered(schedulerDriver, subscribed)
    } else {
      scheduler.reregistered(schedulerDriver, subscribed)
    }
  }

  def executorLost(schedulerDriver: SchedulerDriver, executorID: ExecutorID, agentID: AgentID): Unit = {
    scheduler.executorLost(schedulerDriver, executorID, agentID)
  }
}
