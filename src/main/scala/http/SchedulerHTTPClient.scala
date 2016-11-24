package http

import com.twitter.finagle.Http
import com.twitter.finagle.http.Response
import com.twitter.io.Buf
import com.twitter.util.Future
import org.apache.mesos.v1.mesos.{FrameworkID, MasterInfo}
import org.apache.mesos.v1.scheduler.scheduler.Call
import org.apache.mesos.v1.scheduler.scheduler.Call.Type.TEARDOWN

/**
  * Created by karthik on 11/24/16.
  */
class SchedulerHTTPClient(val master: MasterInfo) extends Client {
  private val host = master.address.get.hostname.get + ":" + master.address.get.port




}
