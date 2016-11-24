package http


import com.twitter.finagle.Http
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.io.Buf
import com.twitter.util.Future
import org.apache.mesos.v1.mesos.MasterInfo
import org.apache.mesos.v1.scheduler.scheduler.Call

/**
  * Created by karthik on 11/24/16.
  */

trait Client {
  def master: MasterInfo
}

object SchedulerCallRequest {
  val uri = "/api/v1/scheduler"

  def apply(content: Buf, host: String): Request = {
    val request = Request(Method.Post, "/api/v1/scheduler")
    request.headerMap.add("User-Agent", "Finagle 0.0")
    request.contentType_=("application/x-protobuf")
    request.accept_=("application/x-protobuf")
    request.headerMap.add("Connection", "close")
    request.host_=(host)
    request.content_=(content)
    request
  }

}

