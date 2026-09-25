package dafi.flat

import dafi.flatbuf.{ColumnContents, LongArrayColumn, StringArrayColumn}

trait ColumnUnion

object ColumnUnion:

  def longArray(o: FlatOffset[LongArrayColumn]): FlatUnion[ColumnUnion] =
    FlatUnion(ColumnContents.LongArrayColumn, o)

  def stringArray(o: FlatOffset[StringArrayColumn]): FlatUnion[ColumnUnion] =
    FlatUnion(ColumnContents.StringArrayColumn, o)
