package undertaker.service

import akka.actor.ActorRef
import undertaker.{New, Workflow, WorkflowRequest}
import scala.concurrent.{ExecutionContext, Future}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

class ActorBasedWorkflowsWriter(val supervisor: ActorRef)(implicit ec: ExecutionContext) extends WorkflowRequestWriter{

  override def write(workflowRequest: WorkflowRequest): Future[Workflow] = {
    implicit val timeout = Timeout(500.milliseconds)
    (supervisor ? New(workflowRequest)).map {
      case w:Workflow => w
    }
  }

}
