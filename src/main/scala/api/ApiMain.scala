package api

import com.twitter.util.Future
import gen.mine.api._
import gen.mine.api.ReadOnlyScheduler._
import com.twitter.finatra.thrift._
import com.twitter.finatra.thrift.filters.LoggingMDCFilter
import com.twitter.finatra.thrift.routing.ThriftRouter

/**
 * Created by kpadmanabhan on 12/4/16.
 */


object ExampleServerMain extends ExampleServer

class ExampleServer extends ThriftServer {
  override protected def configureThrift(router: ThriftRouter): Unit = {
    router
      .filter[LoggingMDCFilter]
      .add[ExampleController]
  }
}



class ExampleController extends Controller with ReadOnlyScheduler.BaseServiceIface {
  val getJobSummary = handle(GetJobSummary) { args: GetJobSummary.Args =>
    info(s"Responding to role Summary for ${args.role}")
    Future.value(Response(ResponseCode(0), ServerInfo("blah", "stats")))
  }

  val getRoleSummary = handle(GetRoleSummary) { args: GetRoleSummary.Args =>
    info(s"Responding to role Summary for ${args}")
    Future.value(Response(ResponseCode(0), ServerInfo("blah", "stats")))
  }

  val getTasksStatus = handle(GetTasksStatus) { args: GetTasksStatus.Args =>
    info(s"Responding to role Summary for ${args}")
    Future.value(Response(ResponseCode(0), ServerInfo("blah", "stats")))
  }

  val getTasksWithoutConfigs = handle(GetTasksWithoutConfigs) { args: GetTasksWithoutConfigs.Args =>
    info(s"Responding to role Summary for ${args}")
    Future.value(Response(ResponseCode(0), ServerInfo("blah", "stats")))
  }

  val getPendingReason = handle(GetPendingReason) { args: GetPendingReason.Args =>
    info(s"Responding to role Summary for ${args}")
    Future.value(Response(ResponseCode(0), ServerInfo("blah", "stats")))
  }

  val getConfigSummary = handle(GetConfigSummary) { args: GetConfigSummary.Args =>
    info(s"Responding to role Summary for ${args}")
    Future.value(Response(ResponseCode(0), ServerInfo("blah", "stats")))
  }

  val getJobs = handle(GetJobs) { args: GetJobs.Args =>
    info(s"Responding to role Summary for ${args}")
    Future.value(Response(ResponseCode(0), ServerInfo("blah", "stats")))
  }

  val getQuota = handle(GetQuota) { args: GetQuota.Args =>
    info(s"Responding to role Summary for ${args}")
    Future.value(Response(ResponseCode(0), ServerInfo("blah", "stats")))
  }

  val populateJobConfig = handle(PopulateJobConfig) { args: PopulateJobConfig.Args =>
    info(s"Responding to role Summary for ${args}")
    Future.value(Response(ResponseCode(0), ServerInfo("blah", "stats")))
  }

  val getJobUpdateSummaries = handle(GetJobUpdateSummaries) { args: GetJobUpdateSummaries.Args =>
    info(s"Responding to role Summary for ${args}")
    Future.value(Response(ResponseCode(0), ServerInfo("blah", "stats")))
  }

  val getJobUpdateDetails = handle(GetJobUpdateDetails) { args: GetJobUpdateDetails.Args =>
    info(s"Responding to role Summary for ${args}")
    Future.value(Response(ResponseCode(0), ServerInfo("blah", "stats")))
  }

  val getJobUpdateDiff = handle(GetJobUpdateDiff) { args: GetJobUpdateDiff.Args =>
    info(s"Responding to role Summary for ${args}")
    Future.value(Response(ResponseCode(0), ServerInfo("blah", "stats")))
  }

  val getTierConfigs = handle(GetTierConfigs) { args: GetTierConfigs.Args =>
    info(s"Responding to role Summary for ${args}")
    Future.value(Response(ResponseCode(0), ServerInfo("blah", "stats")))
  }
}