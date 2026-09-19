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

trait CtfsEntitySection:

  def path: String

  def attributes: Int

  def createDescription(
      buf: FlatBufferBuilder
  ): FlatOffset[EntitySectionDescription] =
    buf.createEntitySectionDescription(
      path = buf.createText(path),
      attributes = attributes
    )

trait CtfsEntityPackageHeader:

  def createHeader(buf: FlatBufferBuilder): EntityPackageHeaderOffset


trait CtfsEntityPackage:

  def pid: Int

  def sections: List[CtfsEntitySection]

  def gids: (Int, Int)

  def label: String

  def header: Option[CtfsEntityPackageHeader]

  def createDescription(
      buf: FlatBufferBuilder
  ): FlatOffset[EntityPackageDescription] =
    val headerOffset = header.map(_.createHeader(buf)).getOrElse(EntityPackageHeaderOffset.empty)
    buf.createEntityPackageDescription(
      pid = pid,
      sections = sections.map(s => s.createDescription(buf)).toFlatVector,
      gids = buf.createIntRange(gids),
      label = buf.createText(label),
      header = headerOffset
    )

trait CtfsEntitySet:

  def entityType: Byte

  def packages: List[CtfsEntityPackage]

  def createDescription(
      buf: FlatBufferBuilder
  ): FlatOffset[EntitySetDescription] =
    buf.createEntitySetDescription(
      tag = entityType,
      packages = packages.map(p => p.createDescription(buf)).toFlatVector
    )
