package dafi.flat

import com.google.flatbuffers.Table

object FlatUnion:
  def apply[T](tag: Byte, offset: FlatOffset[Table]): FlatUnion[T] =
    new FlatUnion(tag, offset)

case class FlatUnion[T](tag: Byte, offset: FlatOffset[Table])
