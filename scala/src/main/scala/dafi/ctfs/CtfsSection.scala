package dafi.ctfs

import com.google.flatbuffers.FlatBufferBuilder
import dafi.flat.Extensions.finishSection
import dafi.flat.FlatOffset
import dafi.flatbuf.BundleSection

object CtfsSection:

  def attribMask(attribs: Int*): Int =
    attribs.map(1 << _).sum

trait CtfsSection:

  def path: String

  def writeSection(buf: FlatBufferBuilder): FlatOffset[BundleSection]

  def toByteArray: Array[Byte] =
    val buf = new FlatBufferBuilder()
    buf.finishSection(writeSection(buf))
    buf.sizedByteArray()
