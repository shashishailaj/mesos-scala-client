package http

import java.util.concurrent.Executors

import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.io.{Buf, Reader}
import com.twitter.util._
import mesos.{Driver, MesosEventHandler}
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

class SchedulerStreamingClient(val master: MasterInfo) extends Client {
  private val host = master.address.get.hostname.get + ":" + master.address.get.port
  private val streamClient = Http.client.withStreaming(enabled = true).newService(host)

  private val callClient = Http.client.newService(host)
  private val mesosStreamIdHeader = "Mesos-Stream-Id"

  val eventCallbackExecutor = Executors.newSingleThreadExecutor()

  private var eventHandlers: List[MesosEventHandler] = Nil
  private var mesosStreamSubscrption: Option[MesosStreamSubscription] = None
  private var mesosDriver: Option[Driver] = None


  def subscribe(frameworkInfo: FrameworkInfo,
                eventHandler: MesosEventHandler, driver: Driver): Future[Option[MesosStreamSubscription]] = {
    register(eventHandler)
    mesosDriver = Some(driver)

    val subscription = Subscribe(frameworkInfo)
    val callRequest: Call = Call(`type` = Some(SUBSCRIBE),
      subscribe = Some(subscription))

    val request = SchedulerCallRequest(Buf.ByteArray(callRequest.toByteArray: _*),
      host)

    val streamSubscription = FuturePool.interruptible(eventCallbackExecutor) {
      doRequest(request)
    }.flatten

    streamSubscription.onSuccess { sub =>
      mesosStreamSubscrption = sub
    }
  }

  private def register(eventHandler: MesosEventHandler): Unit = {
    eventHandlers = eventHandlers.::(eventHandler)
  }

  def shutdownClient(): Unit = {
    callClient.close()
    streamClient.close()
    eventCallbackExecutor.shutdown()
  }

  def call(call: Call): Future[Response] = {
    val request = SchedulerCallRequest(Buf.ByteArray(call.toByteArray: _*), host)
    mesosStreamSubscrption.foreach(s => request.headerMap.add(mesosStreamIdHeader, s.mesosStreamId))
    callClient(request)
  }

  private def doRequest(request: Request): Future[Option[MesosStreamSubscription]] = {
    streamClient(request).map {
      case response if response.status != Status.Ok => {
        streamClient.close()
        None
      }
      case response => {
        handleResponse(response)
        val streamId = response.headerMap.get(mesosStreamIdHeader)
        streamId.map(MesosStreamSubscription)
      }
    }
  }

  private def handleResponse(response: Response) = {
    fromReader(response.reader).foreach { buf =>
      val result = handleSafely {
        val byteBuf = Buf.ByteBuffer.Owned.extract(buf)
        val array = removeLF(byteBuf.array())
        // add exception handling here
        val event = Event.parseFrom(array)
        handleEvent(event)
      }
      if(result.isThrow) {
        println(result.throwable)
      }
    }
  }

  private def handleSafely[T](f: => T): Try[T] = Try(f)

  private def handleEvent(event: Event): Unit = {
    if(mesosDriver.isEmpty) {
      println("Mesos Driver not initiazled. Cannot send events to callback")
    } else {
      event.`type` match {
        case Some(Event.Type.SUBSCRIBED) => {
          eventHandlers.foreach { handler =>
            handler.subscribed(mesosDriver.get, event.subscribed.get)
          }
        }
        case Some(Event.Type.OFFERS) =>  {
          eventHandlers.foreach { handler =>
            handler.resourceOffers(mesosDriver.get, event.offers.get)
          }
        }
        case Some(Event.Type.RESCIND) => {
          eventHandlers.foreach { handler =>
            handler.rescind(mesosDriver.get, event.rescind.get.offerId)
          }
        }
        case Some(Event.Type.UPDATE) => {
          eventHandlers.foreach { handler =>
            handler.update(mesosDriver.get, event.update.get.status)
          }
        }
        case Some(Event.Type.MESSAGE) => {
          eventHandlers.foreach { handler =>
            handler.message(mesosDriver.get, event.message.get)
          }
        }
        case Some(Event.Type.FAILURE) => {
          eventHandlers.foreach { handler =>
            handler.failure(mesosDriver.get, event.failure.get)
          }
        }
        case Some(Event.Type.ERROR) => {
          eventHandlers.foreach { handler =>
            handler.error(mesosDriver.get, event.error.get.message)
          }
        }
        case Some(Event.Type.HEARTBEAT) => {
          eventHandlers.foreach { handler =>
            handler.heartbeat(mesosDriver.get)
          }
        }
        case Some(Event.Type.UNKNOWN) => {
          println("Received unknown type not known to mesos ! ")
        }
        case Some(Event.Type.INVERSE_OFFERS) => ???

        case Some(Event.Type.RESCIND_INVERSE_OFFER) => ???
        case Some(_) => println("Unknown event type")
        case None => println("Event type not specified")
      }
    }

  }




  private def removeLF(bytes: Array[Byte]): Array[Byte] = {
    bytes.dropWhile(_ != 10.toByte).tail
  }

  private def readLength(reader: Reader): Future[Buf] = {
    reader.read(1).flatMap {
      case Some(buf) => {
        if (buf.equals(Buf.ByteArray(10.toByte))) {
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
