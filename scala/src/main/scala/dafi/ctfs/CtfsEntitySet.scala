package dafi.ctfs

import com.google.flatbuffers.{FlatBufferBuilder, Table}
import dafi.flat.FlatOffset
import dafi.flatbuf.{
  EntityPackageDescription,
  EntityPackageHeader,
  EntitySectionDescription,
  EntitySetDescription,
  EntityType
}
import dafi.flat.*
import dafi.flat.Extensions.*

trait CtfsEntitySection extends CtfsSection:

  def attribMask: Int

  def createDescription(
      buf: FlatBufferBuilder
  ): FlatOffset[EntitySectionDescription] =
    buf.writeEntitySectionDescription(
      path = buf.writeString(path),
      attribMask = attribMask
    )

trait CtfsEntityPackageHeader:

  def writeHeader(buf: FlatBufferBuilder): FlatUnion[PackageHeaderUnion]


trait CtfsEntityPackage:

  def pid: Int

  def sections: List[CtfsEntitySection]

  def gids: (Int, Int)

  def label: String

  def header: Option[CtfsEntityPackageHeader]

  def createDescription(
      buf: FlatBufferBuilder
  ): FlatOffset[EntityPackageDescription] =
    val headerOffset = header.map(_.writeHeader(buf)).getOrElse(PackageHeaderUnion.empty)
    buf.writeEntityPackageDescription(
      pid = pid,
      sections = sections.map(s => s.createDescription(buf)).toFlatVector,
      gids = buf.writeIntRange(gids),
      label = buf.writeString(label),
      header = headerOffset
    )

trait CtfsEntitySet:

  def entityType: Byte

  def packages: List[CtfsEntityPackage]
  
  def sections: List[CtfsSection] =
    packages.flatMap(p => p.sections)

  def writeDescription(
      buf: FlatBufferBuilder
  ): FlatOffset[EntitySetDescription] =
    buf.writeEntitySetDescription(
      tag = entityType,
      packages = packages.map(p => p.createDescription(buf)).toFlatVector
    )
