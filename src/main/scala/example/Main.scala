package example

/**
  * Created by karthik on 11/15/16.
  */
import com.treadstone90.mesos.scheduler.MesosSchedulerDriver
import org.apache.mesos.v1.mesos.FrameworkInfo

object ZkHAClient {
  def main(args: Array[String]): Unit = {
    val master = "zk://localhost:2181/mesos"

    val eventHandler = new PrintingScheduler

    val driver = new MesosSchedulerDriver(eventHandler, FrameworkInfo("foo", "bar"), master)
    driver.run()
  }
}


object StandAloneClient {
  def main(args: Array[String]): Unit = {
    val master = "localhost:5050"

    val eventHandler = new PrintingScheduler

    val driver = new MesosSchedulerDriver(eventHandler, FrameworkInfo("foo", "bar"), master)
    driver.run()
  }
}


