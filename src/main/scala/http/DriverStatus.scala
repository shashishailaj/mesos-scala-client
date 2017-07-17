package http

/**
  * Created by karthik on 7/15/17.
  */
sealed trait DriverStatus
case object HeartBeatFailure extends DriverStatus
case object SubscriptionNotFound extends DriverStatus
case object DriverAborted extends DriverStatus
case object LeaderChanged extends DriverStatus
