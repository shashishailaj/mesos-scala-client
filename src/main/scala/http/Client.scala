package http


import com.google.common.net.HostAndPort
import com.twitter.finagle.http.{Method, Request}
import com.twitter.io.Buf

/**
  * Created by karthik on 11/24/16.
  */

trait Client {
  def hostAndPort: HostAndPort
  def hostName: String
  def port: Int
}


trait MesosCallRequest {
  def uri: String

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

object SchedulerCallRequest extends MesosCallRequest {
  val uri = "/api/v1/scheduler"
}

object ExecutorCallRequest extends MesosCallRequest {
  val uri = "/api/v1/executor"
}

