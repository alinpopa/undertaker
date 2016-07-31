package undertaker.service

import akka.actor.{Actor, ActorRef, Props}
import undertaker.data.Models._
import undertaker.data.Messages._
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
    case ExecutionAction.Run =>
      sender ! ExecutionState.Running(currentStep)
      if (isNotFinished(currentStep)) {
        context.become(running(currentStep + 1))
      } else {
        scheduler.scheduleOnce(delay = aliveAfterFinished, receiver = self, message = ExecutionAction.Suicide)
        context.become(finished(currentStep))
      }
    case _ =>
      sender ! ExecutionState.Running(currentStep)
  }

  private def finished(currentStep: Int): Receive = {
    case ExecutionAction.Suicide =>
      context.stop(self)
    case _ =>
      sender ! ExecutionState.Finished(currentStep)
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

