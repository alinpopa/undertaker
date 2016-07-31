package undertaker.service

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{WordSpecLike, _}
import undertaker.data.Models._
import undertaker.data.Messages._
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
      supervisor ! WorkflowAction.New(WorkflowRequest(10))
      fishForMessage(max = 100.milliseconds){
        case WorkflowState.Created(Workflow("workflow0", 10)) => true
        case _ => false
      }
    }

    "persist multiple serial workflows" in {
      val supervisor = TestActorRef(new WorkflowsSupervisor(noopWorkflowsWriter))
      within(100.milliseconds){
        supervisor ! WorkflowAction.New(WorkflowRequest(10))
        fishForMessage(){
          case WorkflowState.Created(Workflow("workflow0", 10)) => true
          case _ => false
        }
        supervisor ! WorkflowAction.New(WorkflowRequest(7))
        fishForMessage(){
          case WorkflowState.Created(Workflow("workflow1", 7)) => true
          case _ => false
        }
      }
    }

    "not be able to create more workflows than maximum" in {
      val supervisor = TestActorRef(new WorkflowsSupervisor(noopWorkflowsWriter, maxWorkflowsAlive = 2))
      within(100.milliseconds){
        supervisor ! WorkflowAction.New(WorkflowRequest(10))
        fishForMessage(){
          case WorkflowState.Created(Workflow("workflow0", 10)) => true
          case _ => false
        }
        supervisor ! WorkflowAction.New(WorkflowRequest(7))
        fishForMessage(){
          case WorkflowState.Created(Workflow("workflow1", 7)) => true
          case _ => false
        }
        supervisor ! WorkflowAction.New(WorkflowRequest(8))
        fishForMessage(){
          case WorkflowState.MaxWorkflowsReached => true
          case _ => false
        }
      }
    }
  }
}
