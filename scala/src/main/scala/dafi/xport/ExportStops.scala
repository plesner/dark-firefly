package dafi.xport

import com.google.flatbuffers.FlatBufferBuilder
import dafi.ctfs.{
  CtfsEntityPackage,
  CtfsEntityPackageHeader,
  CtfsEntitySection,
  CtfsEntitySet,
  CtfsSection
}
import dafi.flat.{
  ColumnUnion,
  FlatOffset,
  FlatUnion,
  FlatVector,
  PackageHeaderUnion,
  SectionContentsUnion
}
import dafi.flat.Extensions.*
import dafi.flatbuf.{BundleSection, EntityType, StopAttrib}
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

  override def writeHeader(
      buf: FlatBufferBuilder
  ): FlatUnion[PackageHeaderUnion] =
    PackageHeaderUnion.stops(buf.writeStopPackageHeader(pack.quad))

case class StopEntityPackageSection(pack: StopPackage)
    extends CtfsEntitySection:

  override def path: String = f"stops-${pack.quad.toLong}.sec"

  override def attribMask: Int =
    CtfsSection.attribMask(StopAttrib.DESCENDANCY, StopAttrib.SHORT_NAME)

  override def writeSection(
      buf: FlatBufferBuilder
  ): FlatOffset[BundleSection] = {
    val descendancies =
      pack.stops.map(s => pack.quad.descendancy(s.normalStop.quad).toLong)
    val shortNames = pack.stops.map(s => s.normalStop.shortName)
    val secondaryNames = pack.stops.map(s => s.normalStop.shortName)
    buf.writeBundleSection(contents =
      SectionContentsUnion.stops(
        buf.writeStopsSection(
          columns = FlatVector(
            List(
              buf.writeAttribColumn(
                attrib = StopAttrib.DESCENDANCY,
                contents = ColumnUnion.longArray(
                  buf.writeLongArrayColumn(values = descendancies.toArray)
                )
              ),
              buf.writeAttribColumn(
                attrib = StopAttrib.SHORT_NAME,
                contents = ColumnUnion.stringArray(
                  buf.writeStringArrayColumn(values =
                    shortNames
                      .map(buf.writeString)
                      .toFlatVector
                  )
                )
              )
            )
          )
        )
      )
    )
  }

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
