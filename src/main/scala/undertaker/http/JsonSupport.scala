package undertaker.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}
import undertaker.data.Models._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val finishedFormat = jsonFormat1(FinishedInfo)

  implicit object WorkflowResponseJsonFormat extends RootJsonFormat[WorkflowResponse] {
    override def write(workflowResponse: WorkflowResponse): JsValue = JsObject(
      "workflow_id" -> JsString(workflowResponse.workflowId)
    )

    override def read(json: JsValue): WorkflowResponse = {
      json.asJsObject.getFields("workflow_id") match {
        case Seq(JsString(workflowId)) =>
          WorkflowResponse(workflowId)
        case _ => throw DeserializationException("'workflow_id' expected.")
      }
    }
  }

  implicit object WorkflowRequestJsonFormat extends RootJsonFormat[WorkflowRequest] {
    override def write(workflowRequest: WorkflowRequest): JsValue = JsObject(
      "number_of_steps" -> JsNumber(workflowRequest.steps)
    )

    override def read(json: JsValue): WorkflowRequest = {
      json.asJsObject.getFields("number_of_steps") match {
        case Seq(JsNumber(steps)) =>
          WorkflowRequest(steps.toInt)
        case _ => throw DeserializationException("'number_of_steps' expected.")
      }
    }
  }

  implicit object WorkflowExecutionIdJsonFormat extends RootJsonFormat[WorkflowExecutionId] {
    override def write(executionId: WorkflowExecutionId): JsValue = JsObject(
      "workflow_execution_id" -> JsString(executionId.executionId)
    )

    override def read(json: JsValue): WorkflowExecutionId = {
      json.asJsObject.getFields("workflow_execution_id") match {
        case Seq(JsString(executionId)) =>
          WorkflowExecutionId(executionId)
        case _ => throw DeserializationException("'workflow_execution_id' expected.")
      }
    }
  }
}

