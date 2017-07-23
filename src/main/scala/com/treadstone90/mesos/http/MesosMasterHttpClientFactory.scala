package com.treadstone90.mesos.http

import com.google.common.net.HostAndPort
import com.treadstone90.mesos.scheduler.{Scheduler, SchedulerDriver}
import org.apache.mesos.v1.mesos.{FrameworkInfo, MasterInfo}

/**
  * Created by karthik on 7/15/17.
  */
class MesosMasterHttpClientFactory(frameworkInfo: FrameworkInfo,
                                   mesosDriver: SchedulerDriver) {

  def newClient(hostAndPort: HostAndPort, scheduler: Scheduler): MesosMasterHTTPClient = {
    new MesosMasterHTTPClient(hostAndPort, frameworkInfo, scheduler, mesosDriver)
  }

  def newClient(masterInfo: MasterInfo, scheduler: Scheduler): MesosMasterHTTPClient = {
    new MesosMasterHTTPClient(HostAndPort.fromParts(masterInfo.address.get.hostname.get,
      masterInfo.address.get.port),
      frameworkInfo, scheduler, mesosDriver)
  }
}
