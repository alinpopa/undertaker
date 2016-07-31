package undertaker.service

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{WordSpecLike, _}
import undertaker.data.Messages._
import undertaker.data.Models._
import scala.concurrent.duration._

class ActorBasedExecutionsTest extends TestKit(ActorSystem("testSystem"))
  with WordSpecLike with ShouldMatchers {

  implicit val ec = system.dispatcher

  "An actor based executions" should {
    val probe = TestProbe()
    val registryReader = new RegistryReader[String, ActorRef] {
      override def lookup(key: String) = None
      override def size = 0
    }
    val registryWriter = new RegistryWriter[String, ActorRef] {
      override def register(key: String, value: ActorRef) = value
      override def unregister(key: String) = None
    }
    val registry = new Registry[String, ActorRef] {
      override def reader: RegistryReader[String, ActorRef] = registryReader
      override def writer: RegistryWriter[String, ActorRef] = registryWriter
    }

    "use the execution actor when creating a new execution" in {
      val executions = new ActorBasedExecutions(probe.ref, registry)
      executions.create(Workflow("workflow-test-01", 10))

      probe.fishForMessage(500.milliseconds){
        case _:Props => true
        case _ => false
      }
    }

    "use the existing execution actor when doing a fetch" in {
      val registry = new Registry[String, ActorRef] {
        override def reader: RegistryReader[String, ActorRef] = new RegistryReader[String, ActorRef] {
          override def lookup(key: String) = Some(probe.ref)
          override def size = 0
        }
        override def writer: RegistryWriter[String, ActorRef] = registryWriter
      }
      val executions = new ActorBasedExecutions(probe.ref, registry)
      executions.get("test-execution-0")

      probe.fishForMessage(500.milliseconds){
        case ExecutionAction.GetState => true
        case _ => false
      }
    }

    "send a run message to the existing execution" in {
      val registry = new Registry[String, ActorRef] {
        override def reader: RegistryReader[String, ActorRef] = new RegistryReader[String, ActorRef] {
          override def lookup(key: String) = Some(probe.ref)
          override def size = 0
        }
        override def writer: RegistryWriter[String, ActorRef] = registryWriter
      }
      val executions = new ActorBasedExecutions(probe.ref, registry)
      executions.run("test-execution-0")

      probe.fishForMessage(500.milliseconds){
        case ExecutionAction.Run => true
        case _ => false
      }
    }
  }

}
