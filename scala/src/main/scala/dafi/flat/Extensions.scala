package dafi.flat

import com.google.flatbuffers.{FlatBufferBuilder, Table}
import dafi.flatbuf.{BundleDescription, BundleManifest, BundleSection, BundleSectionContents, EntityPackageDescription, EntityPackageHeader, EntitySectionDescription, EntitySetDescription, IntRange, StopPackageHeader}
import dafi.geo.ZQuad

object SectionContentsOffset:
  def manifest(offset: FlatOffset[BundleManifest]): SectionContentsOffset =
    SectionContentsOffset(BundleSectionContents.manifest, offset)

case class SectionContentsOffset(tag: Byte, offset: FlatOffset[Table])

object EntityPackageHeaderOffset:
  def stop(offset: FlatOffset[StopPackageHeader]): EntityPackageHeaderOffset =
    EntityPackageHeaderOffset(EntityPackageHeader.stop, offset)
  val empty: EntityPackageHeaderOffset =
    EntityPackageHeaderOffset(EntityPackageHeader.NONE, FlatOffset(0))

case class EntityPackageHeaderOffset(tag: Byte, offset: FlatOffset[Table])

object Extensions:

  extension [T](items: List[FlatOffset[T]])
    def toFlatVector: FlatVector[T] = FlatVector(items)

  extension (buf: FlatBufferBuilder)

    def createBundleSection(
        contents: SectionContentsOffset
    ): FlatOffset[BundleSection] =
      FlatOffset(
        BundleSection.createBundleSection(
          buf,
          contents.tag,
          contents.offset.toInt
        )
      )

    def createBundleManifest(
        description: FlatOffset[BundleDescription]
    ): FlatOffset[BundleManifest] =
      FlatOffset(BundleManifest.createBundleManifest(buf, description.toInt))

    def createBundleDescription(
        entitySets: FlatVector[EntitySetDescription]
    ): FlatOffset[BundleDescription] =
      FlatOffset(
        BundleDescription.createBundleDescription(
          buf,
          BundleDescription.createEntitySetsVector(buf, entitySets.toInts)
        )
      )

    def createEntitySetDescription(
        tag: Byte,
        packages: FlatVector[EntityPackageDescription]
    ): FlatOffset[EntitySetDescription] =
      FlatOffset(
        EntitySetDescription.createEntitySetDescription(
          buf,
          tag,
          EntitySetDescription.createPackagesVector(buf, packages.toInts)
        )
      )

    def createEntityPackageDescription(
        pid: Int,
        sections: FlatVector[EntitySectionDescription],
        gids: FlatOffset[IntRange],
        label: FlatOffset[String],
        header: EntityPackageHeaderOffset
    ): FlatOffset[EntityPackageDescription] =
      FlatOffset(
        EntityPackageDescription.createEntityPackageDescription(
          buf,
          pid,
          EntityPackageDescription.createSectionsVector(buf, sections.toInts),
          gids.toInt,
          label.toInt,
          header.tag,
          header.offset.toInt
        )
      )

    def createEntitySectionDescription(
        path: FlatOffset[String],
        attributes: Int
    ): FlatOffset[EntitySectionDescription] =
      FlatOffset(
        EntitySectionDescription.createEntitySectionDescription(
          buf,
          path.toInt,
          attributes
        )
      )

    def createStopPackageHeader(quad: ZQuad): FlatOffset[StopPackageHeader] =
      FlatOffset(
        StopPackageHeader.createStopPackageHeader(buf, quad.toLong)
      )

    def createIntRange(first: Int, limit: Int): FlatOffset[IntRange] =
      FlatOffset(IntRange.createIntRange(buf, first, limit))

    def createIntRange(range: (Int, Int)): FlatOffset[IntRange] =
      createIntRange(range._1, range._2)

    def createText(str: String): FlatOffset[String] =
      FlatOffset(buf.createString(str))

    def finishSection(root: FlatOffset[BundleSection]): Unit =
      buf.finish(root.toInt, "CTFS")
