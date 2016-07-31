package undertaker.service

import akka.actor.ActorRef
import akka.util.Timeout
import akka.pattern.{AskTimeoutException, ask}
import undertaker.data.Models._
import undertaker.data.Messages._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class ActorBasedExecutions(val executionsSupervisor: ActorRef,
                           val registry: Registry[String, ActorRef],
                           val aliveAfterFinished: FiniteDuration = 5.minutes)(implicit ec: ExecutionContext)
  extends Executions[Workflow, ExecutionInfo]{

  override def create(from: Workflow): Future[Option[ExecutionInfo]] = {
    implicit val timeout = Timeout(500.milliseconds)
    (executionsSupervisor ? Execution.props(from, registry.writer, aliveAfterFinished)).map {
      case e:ExecutionInfo => Some(e)
      case ExecutionState.MaxExecutionsReached => None
    }
  }

  override def get(executionId: String): Future[Option[ExecutionInfo]] = {
    implicit val timeout = Timeout(100.milliseconds)
    registry.reader.lookup(executionId).map { execution =>
      (execution ? ExecutionAction.GetState).map {
        case ExecutionState.Running(currentStep) => Some(ExecutionInfo(executionId, currentStep))
        case ExecutionState.Finished(currentStep) => Some(ExecutionInfo(executionId, currentStep, finished = true))
        case _ => None
      } recover {
        case _:AskTimeoutException => None
      }
    } getOrElse {
      Future.successful(None)
    }
  }

  override def run(executionId: String): Future[Option[ExecutionInfo]] = {
    implicit val timeout = Timeout(100.milliseconds)
    registry.reader.lookup(executionId).map { execution =>
      (execution ? ExecutionAction.Run).map {
        case ExecutionState.Running(currentStep) => Some(ExecutionInfo(executionId, currentStep))
        case ExecutionState.Finished(currentStep) => Some(ExecutionInfo(executionId, currentStep, finished = true))
        case _ => None
      }.recover {
        case _:AskTimeoutException => None
      }
    } getOrElse {
      Future.successful(None)
    }
  }
}
