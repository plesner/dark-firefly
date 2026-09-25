package dafi.steps

import dafi.ctfs.CtfsBundle
import dafi.gtfs.GtfsArchive
import dafi.pipeline.PipelineBuilder
import dafi.xport.{ExportBundle, ExportStops, StopEntitySet}

case class PipelineOptions(
    gtfsPath: String,
    buildStopSubdivs: BuildStopSubdivsOptions = BuildStopSubdivsOptions(),
    readStops: ReadStopsOptions = ReadStopsOptions()
)

object Steps:

  def builder(options: PipelineOptions): PipelineBuilder =
    PipelineBuilder()
      .addValue(options)
      .addStep[GtfsArchive](ReadGtfsArchive)
      .addStep[Stops](ReadStops)
      .addStep[StopSubdivs](BuildStopSubdivs)
      .addStep[StopPackages](BuildStopPackages)
      .addStep[CtfsBundle](ExportBundle)
      .addStep[StopEntitySet](ExportStops)
