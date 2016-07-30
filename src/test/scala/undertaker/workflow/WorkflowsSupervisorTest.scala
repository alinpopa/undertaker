package undertaker.workflow

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{WordSpecLike, _}
import undertaker.service.WorkflowsWriter
import undertaker.{New, Workflow, WorkflowRequest}
import scala.concurrent.duration._

class WorkflowsSupervisorTest extends TestKit(ActorSystem("testSystem"))
  with ImplicitSender with WordSpecLike with ShouldMatchers {

  implicit val ec = system.dispatcher

  val noopWorkflowsWriter = new WorkflowsWriter {
    override def write(workflow: Workflow): Workflow = workflow
  }

  "A workflow supervisor " should {

    "persist a valid workflow" in {
      val supervisor = TestActorRef(new WorkflowsSupervisor(noopWorkflowsWriter))
      supervisor ! New(WorkflowRequest(10))
      fishForMessage(max = 100.milliseconds){
        case Workflow("workflow0", 10) => true
        case _ => false
      }
    }

    "persist multiple serial workflows" in {
      val supervisor = TestActorRef(new WorkflowsSupervisor(noopWorkflowsWriter))
      within(100.milliseconds){
        supervisor ! New(WorkflowRequest(10))
        fishForMessage(){
          case Workflow("workflow0", 10) => true
          case _ => false
        }
        supervisor ! New(WorkflowRequest(7))
        fishForMessage(){
          case Workflow("workflow1", 7) => true
          case _ => false
        }
      }
    }
  }
}
