package undertaker.service

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest._
import undertaker.data.Messages._
import undertaker.data.Models._
import scala.concurrent.duration._

class ExecutionSupervisorTest extends TestKit(ActorSystem("testSystem"))
  with ImplicitSender with WordSpecLike with ShouldMatchers {

  implicit val ec = system.dispatcher

  "An execution supervisor " should {
    val registryReader = new RegistryReader[String, ActorRef] {
      override def lookup(key: String) = None
      override def size = 0
    }
    val supervisor = TestActorRef(new ExecutionSupervisor(registryReader))

    "create execution actors" in {
      val registryWriter = new RegistryWriter[String, ActorRef] {
        override def register(key: String, value: ActorRef): ActorRef = value
        override def unregister(key: String): Option[ActorRef] = None
      }
      supervisor ! Execution.props(Workflow("test-workflow", 7), registryWriter, 5.minutes)
      fishForMessage(max = 100.milliseconds){
        case ExecutionInfo(executionId, 0, false) => true
        case _ => false
      }
    }

    "not create more executions than the max limit" in {
      val registryWriter = new RegistryWriter[String, ActorRef] {
        override def register(key: String, value: ActorRef): ActorRef = value
        override def unregister(key: String): Option[ActorRef] = None
      }
      val supervisor = TestActorRef(new ExecutionSupervisor(new RegistryReader[String, ActorRef] {
        override def lookup(key: String) = None
        override def size = 2
      }, 2))
      supervisor ! Execution.props(Workflow("test-workflow", 7), registryWriter, 5.minutes)
      fishForMessage(max = 100.milliseconds){
        case ExecutionState.MaxExecutionsReached => true
        case _ => false
      }
    }
  }
}
