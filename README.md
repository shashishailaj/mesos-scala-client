# mesos-scala-client
[![Circle CI](https://circleci.com/gh/treadstone90/mesos-scala-client.png?circle-token=:circle-token)](https://circleci.com/gh/treadstone90/mesos-scala-client)

## Scheduler HTTP API

This library provides a scala client for the [mesos scheduler API](http://mesos.apache.org/documentation/latest/scheduler-http-api).

This library doesn't depend on libmesos so it is scala all the way through.

The protobufs are generated using the awesome [ScalaPb project](https://scalapb.github.io/) which
converts mesos protobuf messages to case classes.

The client also supports talking to a mesos master setup in HA mode. So you
can pass a Zookeeper path and the client will automatically route requests to
the current mesos master in the event of a master failover.

## Configuration

Add the Sonatype.org Releases repo as a resolver in your `build.sbt` or `Build.scala` as appropriate.

```scala
resolvers += "Sonatype.org Releases" at "https://oss.sonatype.org/content/repositories/releases/"
```

Add **mesos-scala-client** as a dependency in your `build.sbt` or `Build.scala` as appropriate.

```scala
libraryDependencies ++= Seq(
  // Other dependencies ...
  "com.treadstone90" %% "mesos-scala-client" % "0.0.1" % "compile"
)
```

## Scala Versions

This project is compiled, tested, and published only against 2.11.x. Working on supporting 2.12.x.

## Examples

1. Scheduler client with mesos master in HA mode.

This assumes that mesos master is running locally at port 5050 and zookeeper is running at port 2181.

```scala

import com.treadstone90.mesos.scheduler.{MesosSchedulerDriver, Scheduler, SchedulerDriver}
import org.apache.mesos.v1.mesos._
import org.apache.mesos.v1.scheduler.scheduler.Event.{Failure, Message, Offers, Subscribed}

object HttpStreamingClient {
  def main(args: Array[String]): Unit = {
    val master = "zk://localhost:2181/mesos"

    val eventHandler = new PrintingScheduler

    val driver = new MesosSchedulerDriver(eventHandler, FrameworkInfo("foo", "bar"), master)
    driver.run()
  }
}

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
```

2. Scheduler client connecting directly to a mesos master

This assumes that mesos master is running locally at port 5050.

```scala

package example

import com.treadstone90.mesos.scheduler.SchedulerDriver
import org.apache.mesos.v1.mesos.FrameworkInfo

object HttpStreamingClient {
  def main(args: Array[String]): Unit = {
    val master = "http://localhost:5050"

    val eventHandler = new PrintingScheduler

    val driver = new MesosSchedulerDriver(eventHandler, FrameworkInfo("foo", "bar"), master)
    driver.run()
  }
 }
 
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
```

## Wishlist

Below is a list of features we would like to one day include in this project

1. Support the [Executor HTTP API](http://mesos.apache.org/documentation/latest/executor-http-api/).
2. Support Scala 2.12.x
3. Better documentation.


## License

*mesos-scala-client* is licensed under [APL 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Copyright 2013 com.treadstone90

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
