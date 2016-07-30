package undertaker.service

import org.scalatest._
import undertaker.Workflow
import scala.util.Success

class InMemoryWorkflowsTest extends WordSpecLike with ShouldMatchers{
  "An in memory workflow manager" should {
    val workflows = new InMemoryWorkflows

    "persist any newly created workflow using the writer" in {
      val workflow = workflows.writer.write(Workflow("workflow-0", 7))

      workflow.workflowId should be ("workflow-0")
      workflow.steps should be (7)
    }

    "be able to find existing workflow using the reader" in {
      val workflow = workflows.writer.write(Workflow("workflow-0", 7))

      workflows.reader.read("workflow-0") should be(Some(Workflow(workflow.workflowId, workflow.steps)))
    }
  }
}
