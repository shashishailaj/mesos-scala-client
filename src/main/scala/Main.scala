/**
  * Created by karthik on 11/15/16.
  */
import com.google.common.util.concurrent.ServiceManager
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.inject.annotations.Lifecycle
import http.SchedulerStreamingClient
import mesos.{PrintingEventHandler, SchedulerDriver}
import org.apache.mesos.v1.mesos.{Address, FrameworkInfo, MasterInfo}
/**
  * This client connects to a Streaming HTTP service, prints 1000 messages, then
  * disconnects.  If you start two or more of these clients simultaneously, you
  * will notice that this is also a PubSub example.
  */
object HttpStreamingClient {
  def main(args: Array[String]): Unit = {
    val address = Address(hostname = Some("localhost"), ip = Some("127.0.0.1"), port = 5050)
    val master = MasterInfo("localhost", 0, 5050, address = Some(address))

    val eventHandler = new PrintingEventHandler

    val driver = new SchedulerDriver(eventHandler, FrameworkInfo("foo", "bar"), master,
      new SchedulerStreamingClient(master))

    val mesosSubscription = driver.subscribe()

    Thread.sleep(30000)
    driver.teardown(eventHandler.frameworkId.get)
    Thread.sleep(5000)

    driver.exit
    
  }
}


