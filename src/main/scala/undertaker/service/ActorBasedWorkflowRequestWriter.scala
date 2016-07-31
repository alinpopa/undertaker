package undertaker.service

import akka.actor.ActorRef
import scala.concurrent.{ExecutionContext, Future}
import akka.pattern.ask
import akka.util.Timeout
import undertaker.data.Messages._
import undertaker.data.Models.{WorkflowRequest, Workflow}
import scala.concurrent.duration._

class ActorBasedWorkflowRequestWriter(val supervisor: ActorRef)(implicit ec: ExecutionContext) extends WorkflowRequestWriter{

  override def write(workflowRequest: WorkflowRequest): Future[Option[Workflow]] = {
    implicit val timeout = Timeout(500.milliseconds)
    (supervisor ? WorkflowAction.New(workflowRequest)).map {
      case WorkflowState.Created(w) => Some(w)
      case WorkflowState.MaxWorkflowsReached => None
    }
  }

}
