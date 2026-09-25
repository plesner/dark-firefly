package dafi.flat

import com.google.flatbuffers.{FlatBufferBuilder, Table}
import dafi.flatbuf.*
import dafi.geo.ZQuad

object Extensions:

  extension [T](items: List[FlatOffset[T]])
    def toFlatVector: FlatVector[T] = FlatVector(items)

  extension (buf: FlatBufferBuilder)

    def writeBundleSection(
        contents: FlatUnion[SectionContentsUnion]
    ): FlatOffset[BundleSection] =
      FlatOffset(
        BundleSection.createBundleSection(
          buf,
          contents.tag,
          contents.offset.toInt
        )
      )

    def writeBundleManifest(
        description: FlatOffset[BundleDescription]
    ): FlatOffset[BundleManifest] =
      FlatOffset(BundleManifest.createBundleManifest(buf, description.toInt))

    def writeBundleDescription(
        entitySets: FlatVector[EntitySetDescription]
    ): FlatOffset[BundleDescription] =
      FlatOffset(
        BundleDescription.createBundleDescription(
          buf,
          BundleDescription.createEntitySetsVector(buf, entitySets.toInts)
        )
      )

    def writeEntitySetDescription(
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

    def writeEntityPackageDescription(
        pid: Int,
        sections: FlatVector[EntitySectionDescription],
        gids: FlatOffset[IntRange],
        label: FlatOffset[String],
        header: FlatUnion[PackageHeaderUnion]
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

    def writeEntitySectionDescription(
        path: FlatOffset[String],
        attribMask: Int
    ): FlatOffset[EntitySectionDescription] =
      FlatOffset(
        EntitySectionDescription.createEntitySectionDescription(
          buf,
          path.toInt,
          attribMask
        )
      )

    def writeStopPackageHeader(quad: ZQuad): FlatOffset[StopPackageHeader] =
      FlatOffset(
        StopPackageHeader.createStopPackageHeader(buf, quad.toLong)
      )

    def writeStopsSection(
        columns: FlatVector[AttribColumn]
    ): FlatOffset[StopsSection] =
      FlatOffset(
        StopsSection.createStopsSection(
          buf,
          StopsSection.createColumnsVector(buf, columns.toInts)
        )
      )

    def writeAttribColumn(
        attrib: Int,
        contents: FlatUnion[ColumnUnion]
    ): FlatOffset[AttribColumn] =
      FlatOffset(
        AttribColumn.createAttribColumn(
          buf,
          attrib,
          contents.tag,
          contents.offset.toInt
        )
      )

    def writeLongArrayColumn(values: Array[Long]): FlatOffset[LongArrayColumn] =
      FlatOffset(
        LongArrayColumn.createLongArrayColumn(
          buf,
          LongArrayColumn.createValuesVector(buf, values)
        )
      )

    def writeStringArrayColumn(values: FlatVector[String]): FlatOffset[StringArrayColumn] =
      FlatOffset(
        StringArrayColumn.createStringArrayColumn(
          buf,
          StringArrayColumn.createValuesVector(buf, values.toInts)
        )
      )

    def writeIntRange(first: Int, limit: Int): FlatOffset[IntRange] =
      FlatOffset(IntRange.createIntRange(buf, first, limit))

    def writeIntRange(range: (Int, Int)): FlatOffset[IntRange] =
      writeIntRange(range._1, range._2)

    def writeString(str: String): FlatOffset[String] =
      FlatOffset(buf.createString(str))

    def finishSection(root: FlatOffset[BundleSection]): Unit =
      buf.finish(root.toInt, "CTFS")
