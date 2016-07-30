package undertaker.workflow

import akka.actor.Actor
import undertaker.{New, Workflow}
import undertaker.service.WorkflowsWriter
import akka.pattern.pipe
import scala.concurrent.ExecutionContext

class WorkflowsSupervisor(val workflowsWriter: WorkflowsWriter)(implicit ec: ExecutionContext) extends Actor {
  override def receive = ready(0)

  private def ready(counter: Int): Receive = {
    case New(workflowRequest) => {
      val workflowId = s"workflow$counter"
      val future = workflowsWriter.write(Workflow(workflowId, workflowRequest.steps))
      val requester = sender
      future pipeTo requester
      context.become(ready(counter + 1))
    }
  }

}
