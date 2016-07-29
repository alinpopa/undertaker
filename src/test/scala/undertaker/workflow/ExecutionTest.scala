package undertaker.workflow

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import org.scalatest._
import undertaker._

import scala.concurrent.duration._

class ExecutionTest extends TestKit(ActorSystem("testSystem"))
  with ImplicitSender with WordSpecLike with ShouldMatchers {
  implicit val timeout = Timeout(100.milliseconds)

  "An execution actor" should {
    val execution = TestActorRef(new Execution(Workflow("test-workflow", 3), 10.milliseconds))

    "have 0 as the initial state" in {
      execution ! GetState
      fishForMessage(){
        case Running(0) => true
        case _ => false
      }
    }

    "increment state by one with each run" in {
      execution ! Run
      execution ! GetState
      fishForMessage(){
        case Running(1) => true
        case _ => false
      }
    }

    "increment state by two when two Run messages are sent" in {
      execution ! Run
      execution ! Run
      execution ! GetState
      fishForMessage(){
        case Running(2) => true
        case _ => false
      }
    }

    "get into finished state when steps are finished" in {
      execution ! Run
      execution ! Run
      execution ! Run
      execution ! GetState
      fishForMessage(){
        case Finished(2) => true
        case _ => false
      }
    }

    "terminate after it finishes running" in {
      val probe = TestProbe()
      probe.watch(execution)
      execution ! Run
      execution ! Run
      execution ! Run
      probe.expectTerminated(execution)
    }
  }
}
