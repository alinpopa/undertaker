package undertaker.workflow

import akka.actor.{Actor, Props}
import undertaker._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Execution(val workflow: Workflow,
                val aliveAfterFinished: FiniteDuration = 5.minutes)(implicit ec: ExecutionContext) extends Actor{
  private val scheduler = context.system.scheduler

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
  def props(workflow: Workflow, aliveAfterFinished: FiniteDuration)(implicit ec: ExecutionContext): Props =
    Props(new Execution(workflow, aliveAfterFinished))
}

