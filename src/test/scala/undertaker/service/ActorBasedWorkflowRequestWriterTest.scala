package undertaker.service

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{WordSpecLike, _}
import undertaker.data.Messages._
import undertaker.data.Models._
import scala.concurrent.duration._

class ActorBasedWorkflowRequestWriterTest extends TestKit(ActorSystem("testSystem"))
  with WordSpecLike with ShouldMatchers {

  implicit val ec = system.dispatcher

  "An actor based workflow request writer" should {
    val probe = TestProbe()
    val writer = new ActorBasedWorkflowRequestWriter(probe.ref)

    "use the underlying actor for writing a new workflow" in {
      writer.write(WorkflowRequest(3))
      probe.fishForMessage(500.milliseconds){
        case WorkflowAction.New(WorkflowRequest(3)) => true
        case _ => false
      }
    }
  }

}
