package undertaker.service

import undertaker.data.Models.{Workflow, WorkflowRequest}
import scala.concurrent.Future

trait WorkflowRequestWriter {
  def write(workflowRequest: WorkflowRequest): Future[Option[Workflow]]
}
