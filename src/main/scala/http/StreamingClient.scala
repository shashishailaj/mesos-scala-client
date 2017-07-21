package http

import com.twitter.finagle.http.Response
import com.twitter.util.Future
import org.apache.mesos.v1.scheduler.scheduler.Call

/**
  * Created by karthik on 11/24/16.
  */

trait StreamingClient {
  def subscribe(): Future[ClientStatus]
  def shutdown(status: ClientStatus): Future[ClientStatus]
  def call(call: Call): Future[ClientStatus]
}

case class MesosStreamSubscription(mesosStreamId: String)