package dafi.xport

import dafi.ctfs.CtfsBundle
import dafi.pipeline.{Pipeline, PipelineStep, PipelineStepObject}

object ExportBundle extends PipelineStepObject[CtfsBundle]:

  override def prepare(pipeline: Pipeline): PipelineStep[CtfsBundle] =
    val stops = pipeline.step[StopEntitySet]
    () => execute(stops.get())

  private def execute(stops: StopEntitySet): CtfsBundle =
    CtfsBundle(List(stops))
