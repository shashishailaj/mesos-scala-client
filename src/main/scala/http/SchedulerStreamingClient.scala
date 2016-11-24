package http

import java.util.concurrent.Executors

import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.io.{Buf, Reader}
import com.twitter.util.{Await, Future, FuturePool}
import org.apache.mesos.v1.mesos._
import org.apache.mesos.v1.scheduler.scheduler.Call.Type._
import org.apache.mesos.v1.scheduler.scheduler.{Call, Event}
import org.apache.mesos.v1.scheduler.scheduler.Call.{Accept, Acknowledge, Decline, Kill, Message, Reconcile, Subscribe}

/**
  * Created by karthik on 11/24/16.
  */

trait StreamingClient extends Client {
  def register(eventHandler: MesosEventHandler)
  def register(eventHandler: Seq[MesosEventHandler])
  def subscribe(frameworkInfo: FrameworkInfo): Unit
}

case class MesosStreamSubscription(mesosStreamId: String)

class SchedulerStreamingClient(val master: MasterInfo)
  extends Client {
  private val host = master.address.get.hostname.get + ":" + master.address.get.port
  private val streamClient = Http.client.withStreaming(enabled = true).newService(host)

  private val callClient = Http.client.newService(host)
  private var eventHandlers: List[MesosEventHandler] = Nil

  private var mesosStreamId: Option[MesosStreamSubscription] = None

  val eventCallbackExecutor = Executors.newSingleThreadExecutor()

  def accept(frameworkID: FrameworkID, accept: Accept) = {
    call(Call(`type` = Some(TEARDOWN), frameworkId = Some(frameworkID), accept = Some(accept)))
  }

  def teardown(frameworkID: FrameworkID) = {
    call(Call(`type` = Some(TEARDOWN), frameworkId = Some(frameworkID)))
  }

  def decline(frameworkID: FrameworkID, offerIds: Seq[OfferID], filters: Option[Filters]): Unit = {
    call(Call(`type` = Some(DECLINE), frameworkId = Some(frameworkID),
      decline = Some(Decline(offerIds, filters))))
  }

  def revive(frameworkID: FrameworkID) = {
    call(Call(`type` = Some(REVIVE), frameworkId = Some(frameworkID)))
  }

  def kill(frameworkID: FrameworkID, kill: Kill) = {
    call(Call(`type` = Some(KILL), frameworkId = Some(frameworkID), kill = Some(kill)))
  }

  def shutdown(frameworkID: FrameworkID, shutdown: Call.Shutdown): Unit = {
    call(Call(`type` = Some(Call.Type.SHUTDOWN),
      frameworkId = Some(frameworkID), shutdown = Some(shutdown)))
  }

  def acknowledge(frameworkID: FrameworkID, acknowledge: Acknowledge): Unit = {
    call(Call(`type` = Some(ACKNOWLEDGE), frameworkId = Some(frameworkID), acknowledge = Some(acknowledge)))
  }

  def reconcile(frameworkID: FrameworkID, reconcile: Reconcile): Unit = {
    call(Call(`type` = Some(RECONCILE), frameworkId = Some(frameworkID),
      reconcile = Some(reconcile)))
  }

  def message(frameworkID: FrameworkID, message: Message): Unit = {
    call(Call(`type` = Some(MESSAGE), frameworkId = Some(frameworkID),
      message = Some(message)))
  }

  def request(frameworkID: FrameworkID, requests: Call.Request): Unit = {
    call(Call(`type` = Some(REQUEST), frameworkId = Some(frameworkID),
      request = Some(requests)))
  }

  def subscribe(frameworkInfo: FrameworkInfo, eventHandler: MesosEventHandler): Future[Option[MesosStreamSubscription]] = {
    register(eventHandler)
    val subscription = Subscribe(frameworkInfo)
    val callRequest: Call = Call(`type` = Some(SUBSCRIBE),
      subscribe = Some(subscription))

    val request = SchedulerCallRequest(Buf.ByteArray(callRequest.toByteArray: _*),
      host)

    FuturePool.interruptible(eventCallbackExecutor) {
      doRequest(request)
    }.flatten
  }

  def register(eventHandler: MesosEventHandler): Unit = {
    eventHandlers = eventHandlers.::(eventHandler)
  }

  def shutdownClient(): Unit = {
    eventCallbackExecutor.shutdown()
  }

  private def call(call: Call): Future[Response] = {
    val request = SchedulerCallRequest(Buf.ByteArray(call.toByteArray: _*), host)
    mesosStreamId.foreach(s => request.headerMap.add("Mesos-Stream-Id", s.mesosStreamId))
    callClient(request)
  }

  private def doRequest(request: Request): Future[Option[MesosStreamSubscription]] = {
    streamClient(request).map {
      case response if response.status != Status.Ok =>  {
        streamClient.close()
        None
      }
      case response => {
        handleResponse(response)
        val streamId = response.headerMap.get("Mesos-Stream-Id")
        streamId.map(MesosStreamSubscription)
      }
    }
  }

  private def handleResponse(response: Response) = {
    fromReader(response.reader).foreach {
      case buf => {
        val byteBuf = Buf.ByteBuffer.Owned.extract(buf)
        val array = removeLF(byteBuf.array())
        // add exception handling here
        val event = Event.parseFrom(array)

        event.`type` match {
          case Some(Event.Type.SUBSCRIBED) => {
            println("Got stream Id")
            val id = response.headerMap.get("Mesos-Stream-Id").map(MesosStreamSubscription)
            if(id.isEmpty) {

            } else {
              mesosStreamId = id
            }
          }
        }
        handleEvent(event)
      }
      case _ => {
        streamClient.close()
      }
    }
  }

  private def handleEvent(event: Event): Unit = eventHandlers.foreach(_.handleEvent(event))


  private def removeLF(bytes: Array[Byte]): Array[Byte] =  {
    bytes.dropWhile(_ != 10.toByte).tail
  }

  private def readLength(reader: Reader): Future[Buf] = {
    reader.read(1).flatMap {
      case Some(buf) => {
        if(buf.equals(Buf.ByteArray(10.toByte))) {
          Future(Buf.Empty)
        } else {
          readLength(reader).flatMap { next =>
            Future(buf.concat(next))
          }
        }
      }
      case None => Future(Buf.Empty)
    }
  }

  private def readBytes(reader: Reader): Future[Option[Buf]] = {
    readLength(reader).flatMap {
      case Buf.Empty => Future(None)
      case Buf.UsAscii(len) => {
        // The mesos RecordIO format is not accurate and contains the length of the byte stream and a new line operator
        // So I need to read 1 + len(size) bytes.
        reader.read(len.toInt + len.size + 1)
      }
    }
  }

  private def fromReader(reader: Reader): AsyncStream[Buf] =
    AsyncStream.fromFuture(readBytes(reader)).flatMap {
      case None => AsyncStream.empty
      case Some(a) => {
        a +:: fromReader(reader)
      }
    }
}
