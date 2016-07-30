package undertaker.service

import scala.concurrent.Future

trait Executions[T, R] {
  def create(from: T): Future[R]
  def get(executionId: String): Future[Option[R]]
  def run(executionId: String): Future[Option[R]]
}
