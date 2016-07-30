package undertaker.service

trait Workflows {
  def writer: WorkflowsWriter
  def reader: WorkflowReader
}
