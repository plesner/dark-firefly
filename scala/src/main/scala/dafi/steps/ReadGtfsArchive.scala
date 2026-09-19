package dafi.steps

import dafi.gtfs.GtfsArchive
import dafi.pipeline.{Pipeline, PipelineStep, PipelineStepObject}

object ReadGtfsArchive extends PipelineStepObject[GtfsArchive]:

  def prepare(pipeline: Pipeline): PipelineStep[GtfsArchive] =
    val options = pipeline.step[PipelineOptions]
    () => execute(options.get().gtfsPath)

  private def execute(path: String): GtfsArchive =
    GtfsArchive.open(path)
