package dafi.flat

opaque type FlatOffset[+T] = Int

object FlatOffset:
  def apply[T](offset: Int): FlatOffset[T] = offset

extension [T](offset: FlatOffset[T])
  def toInt: Int = offset

opaque type FlatVector[T] = Iterable[FlatOffset[T]]

object FlatVector:
  def apply[T](offsets: Iterable[FlatOffset[T]]): FlatVector[T] = offsets

extension[T] (offsets: FlatVector[T])
  def toInts: Array[Int] = offsets.toArray
