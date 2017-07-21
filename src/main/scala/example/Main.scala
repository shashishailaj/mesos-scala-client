package example

/**
  * Created by karthik on 11/15/16.
  */
import com.treadstone90.mesos.scheduler.SchedulerDriver
import org.apache.mesos.v1.mesos.FrameworkInfo
/**
  * This client connects to a Streaming HTTP service, prints 1000 messages, then
  * disconnects.  If you start two or more of these clients simultaneously, you
  * will notice that this is also a PubSub example.
  */
object HttpStreamingClient {
  def main(args: Array[String]): Unit = {
    val master = "zk://localhost:2181/mesos"

    val eventHandler = new PrintingEventHandler

    val driver = new SchedulerDriver(eventHandler, FrameworkInfo("foo", "bar"), master)

    driver.run()
    println("Already set why are we not exiting.")
  }
}


