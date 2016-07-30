package undertaker

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import undertaker.http.Api
import undertaker.service.{ActorBasedExecutions, ActorBasedWorkflowsWriter, ActorsRegistry, InMemoryWorkflows}
import undertaker.workflow.{ExecutionSupervisor, WorkflowsSupervisor}

object Main {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val httpConfig = config.getConfig("http")
    val httpHost = httpConfig.getString("host")
    val httpPort = httpConfig.getInt("port")

    implicit val actorSystem = ActorSystem()
    implicit val ec = actorSystem.dispatcher
    implicit val materializer = ActorMaterializer()

    val workflows = new InMemoryWorkflows
    val workflowsSupervisor = actorSystem.actorOf(Props(new WorkflowsSupervisor(workflows.writer)))
    val executionsSupervisor = actorSystem.actorOf(Props[ExecutionSupervisor])
    val workflowRequestWriter = new ActorBasedWorkflowsWriter(workflowsSupervisor)
    val executionsRegistry = new ActorsRegistry

    val executions = new ActorBasedExecutions(executionsSupervisor, executionsRegistry)

    Http().bindAndHandle(new Api(workflows.reader, workflowRequestWriter, executions).routes, httpHost, httpPort)
  }

}
