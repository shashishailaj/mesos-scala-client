/**
  * Created by karthik on 11/15/16.
  */
import http.{MesosEventHandler, PrintingMesosEventHandler, SchedulerHTTPClient, SchedulerStreamingClient}
import org.apache.mesos.v1.mesos.{Address, FrameworkInfo, MasterInfo}
import org.apache.mesos.v1.scheduler.scheduler.{Call, Event}
import org.apache.mesos.v1.scheduler.scheduler.Call.Subscribe
import org.apache.mesos.v1.scheduler.scheduler.Event.Type.{ERROR, FAILURE, HEARTBEAT, MESSAGE, OFFERS, RESCIND, SUBSCRIBED, UPDATE}

/**
  * This client connects to a Streaming HTTP service, prints 1000 messages, then
  * disconnects.  If you start two or more of these clients simultaneously, you
  * will notice that this is also a PubSub example.
  */
object HttpStreamingClient {
  def main(args: Array[String]): Unit = {
    val address = Address(hostname = Some("localhost"), ip = Some("127.0.0.1"), port = 5050)
    val master = MasterInfo("localhost", 0, 5050, address = Some(address))
    val eventHandler = new PrintingMesosEventHandler

    val client = new SchedulerStreamingClient(master)
    client.register(eventHandler)
    client.subscribe(FrameworkInfo("foo", "bar"), eventHandler)

    Thread.sleep(10000)
    println("Done sleeping")
    println(s"The framework id is ${eventHandler.getFrameworkId()}")
    client.teardown(eventHandler.getFrameworkId().orElse(throw new IllegalArgumentException).get)
    client.shutdownClient()
  }
}
