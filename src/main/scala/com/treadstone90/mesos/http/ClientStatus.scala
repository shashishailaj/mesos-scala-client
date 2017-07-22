package com.treadstone90.mesos.http

/**
  * Created by karthik on 7/15/17.
  */
sealed trait ClientStatus

sealed trait ClientErrorStatus extends ClientStatus
sealed trait ClientRuntimeStatus extends ClientStatus

case object RuntimeError extends ClientErrorStatus
// only sent to Zk
case object HeartBeatFailure extends ClientErrorStatus
// Driver_ABORTED
case object SubscriptionNotFound extends ClientErrorStatus
/// Driver_aborted
case object ClientAborted extends ClientErrorStatus
// only ZK
case object LeaderChanged extends ClientRuntimeStatus
// Not started
case object LeaderNotFound extends ClientRuntimeStatus
//DRiVER_RUNNING
case object ClientRunning extends ClientRuntimeStatus
