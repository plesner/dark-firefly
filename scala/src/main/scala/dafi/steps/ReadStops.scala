package dafi.steps

import dafi.geo.ZQuad
import dafi.gtfs.{GtfsArchive, GtfsStop}
import dafi.pipeline.{Pipeline, PipelineStep, PipelineStepObject}

import java.util.regex.Pattern

case class ReadStopsOptions(
    idRegex: String = "^(?<id>.*)$",
    nameRegex: String = "^(?<short>[^(]*)\\((?<secondary>[^)]*)\\)"
)

case class NormalStop(
    normalId: String,
    quad: ZQuad,
    name: String,
    shortName: String,
    secondaryName: String
)

object ReadStops extends PipelineStepObject[Stops]:

  def prepare(pipeline: Pipeline): PipelineStep[Stops] =
    val arch = pipeline.step[GtfsArchive]
    val options = pipeline.step[PipelineOptions]
    () => execute(arch.get(), options.get().readStops)

  private def execute(arch: GtfsArchive, options: ReadStopsOptions): Stops =
    val gtfsStops = arch.stops()
    val idRegex = Pattern.compile(options.idRegex)
    val nameRegex = Pattern.compile(options.nameRegex)
    val groupedStops = gtfsStops.groupBy(stop =>
      val matcher = idRegex.matcher(stop.id)
      assert(matcher.find())
      matcher.group("id")
    )

    def createNormalStop(nid: String, stops: List[GtfsStop]): NormalStop =
      val primary = stops.head
      val nameMatcher = nameRegex.matcher(primary.name)
      val (shortName, secondaryName) = if nameMatcher.find()
        then (nameMatcher.group("short"), nameMatcher.group("secondary"))
        else (primary.name, "")
      NormalStop(
        normalId = nid,
        quad = primary.quad,
        name = primary.name,
        shortName = shortName.trim,
        secondaryName = secondaryName.trim
      )

    val normalStops =
      groupedStops.map((nid, gtfss) => createNormalStop(nid, gtfss))
    Stops(normalStops.toList, gtfsStops)

case class Stops(normalStops: List[NormalStop], gtfsStops: List[GtfsStop])
