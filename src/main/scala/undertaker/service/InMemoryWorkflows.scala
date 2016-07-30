package undertaker.service

import undertaker.Workflow
import scala.collection.mutable

/**
  * This is not thread-safe; it'll be used, for writing, only from within
  * an actor; meaning that, all writes will be serialised to that actor's mailbox.
  * Therefore, no need to use something like a ConcurrentHashMap, and an ordinary HashMap will suffice.
  */
class InMemoryWorkflows extends Workflows{
  private val store = new mutable.HashMap[String, Workflow]()

  override def writer: WorkflowsWriter = new WorkflowsWriter {
    override def write(workflow: Workflow): Workflow = {
      store.put(workflow.workflowId, workflow)
      workflow
    }
  }

  override def reader: WorkflowReader = new WorkflowReader {
    override def read(workflowId: String): Option[Workflow] =
      store.get(workflowId)
  }
}
