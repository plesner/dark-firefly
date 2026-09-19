package dafi.pipeline

import scala.reflect.{ClassTag, classTag}

class StepRef[T](pipeline: Pipeline, step: PipelineStep[T]):

  private var value: Option[T] = None

  def get(): T =
    if value.isEmpty then
      value = Some(step.execute())
    value.get


class Pipeline(builder: PipelineBuilder):

  private var steps: Map[String, StepRef[?]] = Map.empty

  def step[T: ClassTag]: StepRef[T] =
    val tagName = classTag[T].runtimeClass.getCanonicalName
    steps.get(tagName) match
      case Some(value) =>
        value.asInstanceOf[StepRef[T]]
      case None =>
        val result = createStep[T](tagName)
        steps = steps + (tagName -> result)
        result

  private def createStep[T](tagName: String): StepRef[T] =
    builder.steps.get(tagName) match
      case Some(value) =>
        instantiateStep[T](value)
      case None =>
        throw new IllegalArgumentException(f"Unknown step of type $tagName")

  private def instantiateStep[T](factory: Pipeline => PipelineStep[?]): StepRef[T] =
    val step = factory(this)
    new StepRef[T](this, step.asInstanceOf[PipelineStep[T]])
