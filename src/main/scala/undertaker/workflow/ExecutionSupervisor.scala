package undertaker.workflow

import akka.actor.{Actor, Props}
import undertaker.ExecutionInfo

class ExecutionSupervisor extends Actor{
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  import scala.concurrent.duration._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 3.seconds) {
      case _: Exception => Resume
    }

  override def receive = started(0)

  private def started(counter: Int): Receive = {
    case p: Props => {
      val requester = sender
      val executionId = "execution-" + counter
      val execution = context.actorOf(p, executionId)
      requester ! ExecutionInfo(executionId, 0)
      context.become(started(counter + 1))
    }
  }
}

