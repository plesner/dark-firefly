package dafi.pipeline

trait PipelineStep[T]:
  def execute(): T
