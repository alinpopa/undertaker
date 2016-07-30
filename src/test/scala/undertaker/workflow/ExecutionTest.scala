package undertaker.workflow

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import undertaker._
import undertaker.service.RegistryWriter

import scala.concurrent.duration._

class ExecutionTest extends TestKit(ActorSystem("testSystem"))
  with ImplicitSender with WordSpecLike with ShouldMatchers with MockFactory {
  implicit val timeout = Timeout(100.milliseconds)
  implicit val ec = system.dispatcher

  "An execution actor" should {
    val registryWriter = new RegistryWriter[String, ActorRef] {
      override def register(key: String, value: ActorRef): ActorRef = value
      override def unregister(key: String): Option[ActorRef] = None
    }
    val execution = TestActorRef(new Execution(Workflow("test-workflow", 3), registryWriter, 10.milliseconds))

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

    "register itself when starting" in {
      val registryWriter = stub[RegistryWriter[String, ActorRef]]
      val execution = TestActorRef(new Execution(Workflow("test-workflow", 3), registryWriter, 10.milliseconds))
      (registryWriter.register _).verify(execution.path.name, execution)
    }

    "unregister itself when finished" in {
      val registryWriter = stub[RegistryWriter[String, ActorRef]]
      val execution = TestActorRef(new Execution(Workflow("test-workflow", 3), registryWriter, 10.milliseconds))
      val probe = TestProbe()
      probe.watch(execution)
      execution ! Run
      execution ! Run
      execution ! Run
      within(100.milliseconds) {
        probe.expectTerminated(execution)
      }
      (registryWriter.unregister _).verify(execution.path.name)
    }
  }
}
