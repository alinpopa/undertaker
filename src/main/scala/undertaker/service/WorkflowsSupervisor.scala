package undertaker.service

import akka.actor.Actor
import undertaker.data.Messages._
import undertaker.data.Models._
import scala.concurrent.ExecutionContext

class WorkflowsSupervisor(val workflowsWriter: WorkflowsWriter,
                          val maxWorkflowsAlive: Int = 0)(implicit ec: ExecutionContext) extends Actor {
  override def receive = {
    if (maxWorkflowsAlive <= 0) ready(0)
    else limitedReady(0, maxWorkflowsAlive)
  }

  private def ready(counter: Int): Receive = {
    case WorkflowAction.New(workflowRequest) =>
      val workflowId = s"workflow$counter"
      val workflow = workflowsWriter.write(Workflow(workflowId, workflowRequest.steps))
      val requester = sender
      requester ! WorkflowState.Created(workflow)
      context.become(ready(counter + 1))
  }

  private def limitedReady(counter: Int, maxWorkflowsAlive: Int): Receive = {
    case WorkflowAction.New(workflowRequest) if counter >= maxWorkflowsAlive =>
      sender ! WorkflowState.MaxWorkflowsReached
      context.become(paused())
    case WorkflowAction.New(workflowRequest) =>
      val workflowId = s"workflow$counter"
      val workflow = workflowsWriter.write(Workflow(workflowId, workflowRequest.steps))
      val requester = sender
      requester ! WorkflowState.Created(workflow)
      context.become(limitedReady(counter + 1, maxWorkflowsAlive))
  }

  private def paused(): Receive = {
    case _ =>
      sender ! WorkflowState.MaxWorkflowsReached
  }
}
