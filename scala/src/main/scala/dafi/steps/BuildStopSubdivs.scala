package dafi.steps

import dafi.geo.ZQuad
import dafi.gtfs.GtfsStop
import dafi.pipeline.{Pipeline, PipelineStep, PipelineStepObject}

case class BuildStopSubdivsOptions(
    minPackageSize: Int = 512,
    highestZoomLevel: Int = 0,
    lowestZoomLevel: Int = 13
)

case class StopSubdiv(quad: ZQuad, stopCount: Int)

object BuildStopSubdivs extends PipelineStepObject[StopSubdivs]:

  def prepare(pipeline: Pipeline): PipelineStep[StopSubdivs] =
    val gtfsStops = pipeline.step[Stops]
    val options = pipeline.step[PipelineOptions]
    () => execute(gtfsStops.get().normalStops, options.get().buildStopSubdivs)

  private def execute(
      stops: List[NormalStop],
      options: BuildStopSubdivsOptions
  ): StopSubdivs =
    // Starting from the highest granularity, divide the stops into blocks and
    // take out those blocks that are large enough. For those that aren't, go
    // to the next higher zoom level and try again until all stops have been
    // assigned to a package.
    var remainingStops = stops
    var packages: Iterable[StopSubdiv] = List.empty
    for zoomLevel <- options.lowestZoomLevel.to(options.highestZoomLevel, -1) do
      val groups = remainingStops.groupBy(stop => stop.quad.toZoom(zoomLevel))
      val largePackages = groups.filter((k, v) => v.length >= options.minPackageSize)
      val smallPackages = groups.filterNot((k, v) => v.length >= options.minPackageSize)
      remainingStops = smallPackages.values.flatten.toList
      val newSubdivs = largePackages.map((k, v) => StopSubdiv(k, v.length))
      packages = packages ++ newSubdivs
    if remainingStops.nonEmpty then
      packages = packages ++ List(StopSubdiv(ZQuad.Everything, remainingStops.length))
    assert(packages.map(_.stopCount).sum == stops.length)
    StopSubdivs(packages.toList.sortBy(-_.quad.toLong))

case class StopSubdivs(packages: List[StopSubdiv])
