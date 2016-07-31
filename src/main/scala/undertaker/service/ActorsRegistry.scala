package undertaker.service

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._
import akka.actor.ActorRef

/**
  * Need a very simple way to register actors (executions) after
  * they're started. Due to its distributed nature, Akka doesn't support actor lookup based on the name,
  * except the actor selection, and in order to know if an actor is alive,
  * need to use the ask pattern (and that can timeout); and when ask timeouts,
  * turns out to be a bit misleading as you don't know if it was because the actor doesn't exist,
  * or if exists but is busy processing other messages from the mailbox.
  *
  * Internally, this class uses, again, a ConcurrentHashMap as it's good enough, performance wise,
  * for this job.
  *
  * Also, as part of this implementation, in order to get the total number of records,
  * we're basing on the internal size of the Map being used, and I'm not entirely convinced that this
  * is very performant (maybe an AtomicInt, or something similar would behave better, but having one more
  * reference to keep track of, maybe's too much for now, and maybe a distributed counter would do better?),
  * but for now it looks to be good enough, performance wise.
  */
class ActorsRegistry extends Registry[String, ActorRef]{
  private val registry = new ConcurrentHashMap[String, ActorRef]().asScala

  override def reader: RegistryReader[String, ActorRef] =
    new RegistryReader[String, ActorRef] {
      override def lookup(key: String): Option[ActorRef] =
        registry.get(key)

      override def size: Int = registry.size
    }

  override def writer: RegistryWriter[String, ActorRef] =
    new RegistryWriter[String, ActorRef] {
      override def register(key: String, value: ActorRef): ActorRef = {
        registry.put(key, value)
        value
      }

      override def unregister(key: String): Option[ActorRef] =
        registry.remove(key)
    }
}
