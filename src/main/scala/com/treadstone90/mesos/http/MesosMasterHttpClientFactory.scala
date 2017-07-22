package com.treadstone90.mesos.http

import com.google.common.net.HostAndPort
import com.treadstone90.mesos.scheduler.Driver
import example.MesosEventHandler
import org.apache.mesos.v1.mesos.{FrameworkInfo, MasterInfo}

/**
  * Created by karthik on 7/15/17.
  */
class MesosMasterHttpClientFactory(frameworkInfo: FrameworkInfo,
                                   eventHandler: MesosEventHandler,
                                   mesosDriver: Driver) {

  def newClient(hostAndPort: HostAndPort): MesosMasterHTTPClient = {
    new MesosMasterHTTPClient(hostAndPort, frameworkInfo, eventHandler, mesosDriver)
  }

  def newClient(masterInfo: MasterInfo): MesosMasterHTTPClient = {
    new MesosMasterHTTPClient(HostAndPort.fromParts(masterInfo.address.get.hostname.get,
      masterInfo.address.get.port),
      frameworkInfo, eventHandler, mesosDriver)
  }
}
