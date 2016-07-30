package undertaker.workflow

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{WordSpecLike, _}
import undertaker.service.WorkflowsWriter
import undertaker.{ExecutionInfo, New, Workflow, WorkflowRequest}

import scala.concurrent.Future
import scala.concurrent.duration._

class WorkflowsSupervisorTest extends TestKit(ActorSystem("testSystem"))
  with ImplicitSender with WordSpecLike with ShouldMatchers {

  implicit val ec = system.dispatcher

  val noopWorkflowsWriter = new WorkflowsWriter {
    override def write(workflow: Workflow): Future[Workflow] = Future.successful(workflow)
  }

  "An execution supervisor " should {
    val supervisor = TestActorRef(new WorkflowsSupervisor(noopWorkflowsWriter))

    "create execution actors" in {
      supervisor ! New(WorkflowRequest(10))
      fishForMessage(max = 100.milliseconds){
        case Workflow("workflow0", 10) => true
        case _ => false
      }
    }
  }
}
