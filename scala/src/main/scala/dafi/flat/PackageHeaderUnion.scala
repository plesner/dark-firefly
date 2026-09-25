package dafi.flat

import dafi.flatbuf.{EntityPackageHeader, StopPackageHeader}

trait PackageHeaderUnion

object PackageHeaderUnion:

  val empty: FlatUnion[PackageHeaderUnion] =
    FlatUnion(EntityPackageHeader.NONE, FlatOffset(0))

  def stops(o: FlatOffset[StopPackageHeader]): FlatUnion[PackageHeaderUnion] =
    FlatUnion(EntityPackageHeader.stops, o)
