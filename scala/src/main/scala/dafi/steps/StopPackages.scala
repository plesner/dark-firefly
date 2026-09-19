package dafi.steps

import dafi.ctfs.CtfsStop
import dafi.geo.ZQuad
import dafi.pipeline.{Pipeline, PipelineStep, PipelineStepObject}

case class StopPackage(
    quad: ZQuad,
    pid: Int,
    firstGid: Int,
    stops: List[CtfsStop]
)

object CreateStopPackages extends PipelineStepObject[StopPackages]:

  override def prepare(pipeline: Pipeline): PipelineStep[StopPackages] =
    val stops = pipeline.step[Stops]
    val subdivs = pipeline.step[StopSubdivs]
    () => execute(stops.get(), subdivs.get())

  private def execute(stops: Stops, subdivs: StopSubdivs): StopPackages =
    val indexedPackages = subdivs.packages.zipWithIndex
    def pidForQuad(quad: ZQuad): Int =
      indexedPackages.find((subdiv, pid) => subdiv.quad.isAncestor(quad)).get._2
    val groupedStops = stops.normalStops.groupBy(s => pidForQuad(s.quad))
    var nextFirstGid = 0
    val packageList = indexedPackages.map((subdiv, pid) =>
      val stops = groupedStops(pid)
        .sortBy(s => s.normalId)
        .zipWithIndex
        .map((stop, eid) =>
          CtfsStop(
            pid = pid,
            eid = eid,
            gid = nextFirstGid + eid,
            normalStop = stop
          )
        )
      val pack = StopPackage(
        quad = subdiv.quad,
        pid = pid,
        firstGid = nextFirstGid,
        stops = stops
      )
      nextFirstGid += stops.size
      pid -> pack
    )
    StopPackages(packageList)

case class StopPackages(packages: List[(Int, StopPackage)])
