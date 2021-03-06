package undertaker.http

import undertaker.service._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives
import undertaker.data.Models._
import scala.concurrent.ExecutionContext

class Api(val workflowsReader: WorkflowReader,
          val workflowRequestWriter: WorkflowRequestWriter,
          val executions: Executions[Workflow, ExecutionInfo])(implicit ec: ExecutionContext) extends Directives
  with JsonSupport {

  val routes =
    pathPrefix("workflows") {
      createWorkflow ~ createExecution ~ manageExecution
    }

  def createWorkflow =
    pathEndOrSingleSlash {
      post {
        entity(as[WorkflowRequest]) { w =>
          complete(workflowRequestWriter.write(w).map { workflowOpt =>
            workflowOpt.map { workflow =>
              Created -> Some(WorkflowResponse(workflow.workflowId))
            } getOrElse {
              BadRequest -> None
            }
          })
        }
      }
    }

  def createExecution =
    path(Segment / "executions") { workflowId =>
      post {
        workflowsReader.read(workflowId).map { workflow =>
          complete(executions.create(workflow).map { infoOpt =>
            infoOpt.map { info =>
              Created -> Some(WorkflowExecutionId(info.executionId))
            } getOrElse {
              BadRequest -> None
            }
          })
        } getOrElse {
          complete(NotFound -> None)
        }
      }
    }

  def manageExecution =
    path(Segment / "executions" / Segment) { (workflowId, executionId) =>
      runExecution(workflowId, executionId) ~ readExecution(workflowId, executionId)
    }

  def runExecution(workflowId: String, executionId: String) =
    put {
      workflowsReader.read(workflowId).map { workflow =>
        complete {
          executions.run(executionId).map { optExecutionInfo =>
            optExecutionInfo.map {
              case ExecutionInfo(_, _, false) => NoContent -> None
              case ExecutionInfo(_, _, true) => BadRequest -> None
            } getOrElse {
              NotFound -> None
            }
          }
        }
      } getOrElse {
        complete(NotFound -> None)
      }
    }

  def readExecution(workflowId: String, executionId: String) =
    get {
      workflowsReader.read(workflowId).map { workflow =>
        complete {
          executions.get(executionId).map { optExecutionInfo =>
            optExecutionInfo.map { info =>
              OK -> Some(FinishedInfo(info.finished))
            } getOrElse {
              NotFound -> None
            }
          }
        }
      } getOrElse {
        complete(NotFound -> None)
      }
    }
}
