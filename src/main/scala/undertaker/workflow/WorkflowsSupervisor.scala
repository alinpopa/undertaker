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
      val workflow = workflowsWriter.write(Workflow(workflowId, workflowRequest.steps))
      val requester = sender
      requester ! workflow
      context.become(ready(counter + 1))
    }
  }

}
