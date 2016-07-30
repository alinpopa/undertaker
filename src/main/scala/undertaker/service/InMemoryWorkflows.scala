package undertaker.service

import undertaker.Workflow
import scala.collection.mutable
import scala.concurrent.Future

/**
  * This is not thread-safe; it'll be used, for writing, only from within
  * an actor; meaning that, all writes will be serialised to that actor's mailbox.
  * Therefore, no need to use something like a ConcurrentHashMap, and an ordinary HashMap will suffice.
  */
class InMemoryWorkflows extends Workflows{
  private val store = new mutable.HashMap[String, Workflow]()

  override def writer: WorkflowsWriter = new WorkflowsWriter {
    override def write(workflow: Workflow): Future[Workflow] = {
      store.put(workflow.workflowId, workflow)
      Future.successful(workflow)
    }
  }

  override def reader: WorkflowReader = new WorkflowReader {
    override def read(workflowId: String): Option[Workflow] =
      store.get(workflowId)
  }
}
