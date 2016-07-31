package undertaker

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import configs.Configs
import undertaker.http.Api
import undertaker.service._
import scala.concurrent.duration._

object Main {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val httpConfig = config.getConfig("http")
    val httpHost = httpConfig.getString("host")
    val httpPort = httpConfig.getInt("port")
    val undertakerConf = config.getConfig("undertaker")
    val maxWorkflowsAlive = undertakerConf.getInt("workflows.maxWorkflowsAlive")
    val maxExecutionsAlive = undertakerConf.getInt("executions.maxExecutionsAlive")
    val executionAliveAfterFinished = Configs[FiniteDuration]
      .get(undertakerConf, "executions.aliveAfterFinished")
      .valueOrElse(5.minutes)

    implicit val actorSystem = ActorSystem()
    implicit val ec = actorSystem.dispatcher
    implicit val materializer = ActorMaterializer()

    val workflows = new InMemoryWorkflows
    val executionsRegistry = new ActorsRegistry
    val workflowsSupervisor = actorSystem.actorOf(Props(new WorkflowsSupervisor(workflows.writer, maxWorkflowsAlive)))
    val executionsSupervisor = actorSystem.actorOf(Props(new ExecutionSupervisor(executionsRegistry.reader, maxExecutionsAlive)))
    val workflowRequestWriter = new ActorBasedWorkflowRequestWriter(workflowsSupervisor)
    val executions = new ActorBasedExecutions(executionsSupervisor, executionsRegistry, executionAliveAfterFinished)

    Http().bindAndHandle(new Api(workflows.reader, workflowRequestWriter, executions).routes, httpHost, httpPort)
  }

}
