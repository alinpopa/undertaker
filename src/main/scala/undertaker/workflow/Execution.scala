package undertaker.workflow

import akka.actor.{Actor, ActorRef, Props}
import undertaker._
import undertaker.service.RegistryWriter

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Execution(val workflow: Workflow,
                val registryWriter: RegistryWriter[String, ActorRef],
                val aliveAfterFinished: FiniteDuration = 5.minutes)(implicit ec: ExecutionContext) extends Actor{
  private val scheduler = context.system.scheduler

  override def preStart(): Unit = {
    registryWriter.register(self.path.name, self)
  }

  override def postStop(): Unit = {
    registryWriter.unregister(self.path.name)
  }

  override def receive =
    if (workflow.steps > 0)
      running(0)
    else
      finished(0)

  private def running(currentStep: Int): Receive = {
    case Run =>
      sender ! Running(currentStep)
      if (isNotFinished(currentStep)) {
        context.become(running(currentStep + 1))
      } else {
        scheduler.scheduleOnce(delay = aliveAfterFinished, receiver = self, message = Suicide)
        context.become(finished(currentStep))
      }
    case _ =>
      sender ! Running(currentStep)
  }

  private def finished(currentStep: Int): Receive = {
    case Suicide =>
      context.stop(self)
    case _ =>
      sender ! Finished(currentStep)
  }

  private def isNotFinished(currentStep: Int) =
    currentStep < (workflow.steps - 1)
}

object Execution {
  def props(workflow: Workflow,
            registryWriter: RegistryWriter[String, ActorRef],
            aliveAfterFinished: FiniteDuration)(implicit ec: ExecutionContext): Props =
    Props(new Execution(workflow, registryWriter, aliveAfterFinished))
}

