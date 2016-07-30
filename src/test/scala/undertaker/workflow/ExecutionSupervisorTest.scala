package undertaker.workflow

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest._
import undertaker.{ExecutionInfo, Workflow}
import scala.concurrent.duration._

class ExecutionSupervisorTest extends TestKit(ActorSystem("testSystem"))
  with ImplicitSender with WordSpecLike with ShouldMatchers {

  implicit val ec = system.dispatcher

  "An execution supervisor " should {
    val supervisor = TestActorRef[ExecutionSupervisor]

    "create execution actors" in {
      supervisor ! Execution.props(Workflow("test-workflow", 7), 5.minutes)
      fishForMessage(max = 100.milliseconds){
        case ExecutionInfo(executionId, 0, false) => true
        case _ => false
      }
    }
  }
}
