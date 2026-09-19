package dafi.pipeline

import scala.reflect.{ClassTag, classTag}

trait PipelineStepObject[T]:
  def prepare(pipeline: Pipeline): PipelineStep[T]

case class PipelineBuilder(steps: Map[String, Pipeline => PipelineStep[?]] = Map.empty):

  def addFactory[T: ClassTag](factory: Pipeline => PipelineStep[T]): PipelineBuilder =
    copy(steps = steps + (classTag[T].runtimeClass.getCanonicalName -> factory))

  def addStep[T: ClassTag](obj: PipelineStepObject[T]): PipelineBuilder =
    addFactory[T](obj.prepare)

  def addValue[T: ClassTag](v: T): PipelineBuilder =
    val obj: PipelineStepObject[T] = pipeline => () => v
    addStep[T](obj)

  def newPipeline: Pipeline = new Pipeline(this)
