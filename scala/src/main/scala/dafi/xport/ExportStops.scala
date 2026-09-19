package dafi.xport

import com.google.flatbuffers.{FlatBufferBuilder}
import dafi.ctfs.{
  CtfsEntityPackage,
  CtfsEntityPackageHeader,
  CtfsEntitySection,
  CtfsEntitySet
}
import dafi.flat.{EntityPackageHeaderOffset}
import dafi.flat.Extensions.*
import dafi.flatbuf.{EntityType}
import dafi.pipeline.{Pipeline, PipelineStep, PipelineStepObject}
import dafi.steps.{StopPackage, StopPackages}

object ExportStops extends PipelineStepObject[StopEntitySet]:

  override def prepare(pipeline: Pipeline): PipelineStep[StopEntitySet] =
    val packs = pipeline.step[StopPackages]
    () => execute(packs.get())

  private def execute(packs: StopPackages): StopEntitySet =
    StopEntitySet(packs.packages.map((pid, pack) => StopEntityPackage(pack)))

case class StopEntitySet(packages: List[StopEntityPackage])
    extends CtfsEntitySet:
  override def entityType: Byte = EntityType.STOP

case class StopEntityPackageHeader(pack: StopPackage)
    extends CtfsEntityPackageHeader:

  override def createHeader(buf: FlatBufferBuilder): EntityPackageHeaderOffset =
    EntityPackageHeaderOffset.stop(buf.createStopPackageHeader(pack.quad))

case class StopEntityPackageSection(pack: StopPackage)
    extends CtfsEntitySection:

  override def path: String = f"stops_${pack.quad.toLong}.fb"

  override def attributes: Int = 0

case class StopEntityPackage(pack: StopPackage) extends CtfsEntityPackage:

  override def label: String = pack.quad.toString

  override def pid: Int = pack.pid

  override def gids: (Int, Int) =
    (pack.firstGid, pack.firstGid + pack.stops.length)

  override def sections: List[CtfsEntitySection] = List(
    StopEntityPackageSection(pack)
  )

  override def header: Option[CtfsEntityPackageHeader] = Some(
    StopEntityPackageHeader(pack)
  )
