package undertaker.service

import akka.actor.{Actor, ActorSystem}
import akka.testkit.TestActorRef
import org.scalatest.{WordSpecLike, _}

class ActorsRegistryTest extends WordSpecLike with ShouldMatchers {
  implicit val system = ActorSystem("testSystem")

  "An actors registry" should {
    val registry = new ActorsRegistry
    "register and return the given actor" in {
      val actor1 = TestActorRef(new Actor {
        override def receive: Receive = {
          case "test" => sender ! "ok"
        }
      })
      registry.writer.register("test1", actor1) should be (actor1)
    }

    "be able to lookup registered actors" in {
      val actor1 = TestActorRef(new Actor {
        override def receive: Receive = {
          case "test" => sender ! "ok"
        }
      })
      registry.writer.register("test1", actor1) should be (actor1)
      registry.reader.lookup("test1") should be (Some(actor1))
    }

    "be able to unregister registered actor" in {
      val actor1 = TestActorRef(new Actor {
        override def receive: Receive = {
          case "test" => sender ! "ok"
        }
      })
      registry.writer.register("test1", actor1) should be (actor1)
      registry.writer.unregister("test1") should be (Some(actor1))
      registry.reader.lookup("test1") should be (None)
    }
  }

}
