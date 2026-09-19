package dafi.steps

import dafi.geo.ZQuad
import dafi.gtfs.{GtfsArchive, GtfsStop}
import dafi.pipeline.{Pipeline, PipelineStep, PipelineStepObject}

import java.util.regex.Pattern

case class ReadStopsOptions(
    idRegex: String = "^(?<id>.*)$"
)

case class NormalStop(
    normalId: String,
    primaryGtfs: GtfsStop,
    secondaryGtfs: List[GtfsStop]
):

  def quad: ZQuad = primaryGtfs.quad

object ReadStops extends PipelineStepObject[Stops]:

  def prepare(pipeline: Pipeline): PipelineStep[Stops] =
    val arch = pipeline.step[GtfsArchive]
    val options = pipeline.step[PipelineOptions]
    () => execute(arch.get(), options.get().readStops)

  private def execute(arch: GtfsArchive, options: ReadStopsOptions): Stops =
    val gtfsStops = arch.stops()
    val idRegex = Pattern.compile(options.idRegex)
    val groupedStops = gtfsStops.groupBy(stop =>
      val matcher = idRegex.matcher(stop.id)
      assert(matcher.find())
      matcher.group("id")
    )
    val normalStops =
      groupedStops.map((nid, gtfss) => NormalStop(nid, gtfss.head, gtfss.tail))
    Stops(normalStops.toList, gtfsStops)

case class Stops(normalStops: List[NormalStop], gtfsStops: List[GtfsStop])
