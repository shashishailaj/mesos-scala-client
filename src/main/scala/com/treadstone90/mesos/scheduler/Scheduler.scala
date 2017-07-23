package com.treadstone90.mesos.scheduler

import org.apache.mesos.v1.mesos.{AgentID, ExecutorID, OfferID, TaskStatus}
import org.apache.mesos.v1.scheduler.scheduler.Event.{Failure, Message, Offers, Subscribed}

/**
  * Created by karthik on 12/1/16.
  */
trait Scheduler {

  def disconnected(schedulerDriver: SchedulerDriver)

  def error(schedulerDriver: SchedulerDriver, message: String)

  def executorLost(schedulerDriver: SchedulerDriver, executorID: ExecutorID, agentID: AgentID)

  def frameworkMessage(schedulerDriver: SchedulerDriver, message: Message)

  def offerRescinded(schedulerDriver: SchedulerDriver, offerId: OfferID)

  def registered(schedulerDriver: SchedulerDriver, subscribed: Subscribed)

  def reregistered(schedulerDriver: SchedulerDriver, subscribed: Subscribed)

  def resourceOffers(schedulerDriver: SchedulerDriver, offers: Offers)

  def agentLost(schedulerDriver: SchedulerDriver, agentID: AgentID)

  def statusUpdate(schedulerDriver: SchedulerDriver, status: TaskStatus)

  def failure(schedulerDriver: SchedulerDriver, failure: Failure)
}
