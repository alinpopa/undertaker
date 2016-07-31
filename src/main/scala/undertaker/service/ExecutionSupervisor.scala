package undertaker.service

import akka.actor.{Actor, ActorRef, Props}
import undertaker.data.Models.ExecutionInfo
import undertaker.data.Messages._

class ExecutionSupervisor(val registryReader: RegistryReader[String, ActorRef],
                          val maxExecutionsAlive: Int = 0) extends Actor{
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  import scala.concurrent.duration._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 3.seconds) {
      case _: Exception => Resume
    }

  override def receive = {
    if(maxExecutionsAlive <= 0) started(0)
    else startedLimited(0, maxExecutionsAlive)
  }

  private def started(counter: Int): Receive = {
    case p: Props =>
      val executionId = s"execution-$counter"
      context.actorOf(p, executionId)
      sender ! ExecutionInfo(executionId, 0)
      context.become(started(counter + 1))
  }

  private def startedLimited(counter: Int, maxExecutionsSize: Int): Receive = {
    case p: Props if registryReader.size >= maxExecutionsSize =>
      sender ! ExecutionState.MaxExecutionsReached
      context.become(startedLimited(counter, maxExecutionsSize))
    case p: Props =>
      val executionId = s"execution-$counter"
      context.actorOf(p, executionId)
      sender ! ExecutionInfo(executionId, 0)
      context.become(startedLimited(counter + 1, maxExecutionsSize))
  }
}
