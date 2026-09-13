package dafi.geo

import com.google.flatbuffers.FlatBufferBuilder

import dafi.flatbuf

import scala.collection.mutable.ListBuffer

object ZQuadTree:

  def from[V](items: Iterable[(ZQuad, V)], maxLeafSize: Int): ZQuadTree[V] =
    if items.size <= maxLeafSize then fromLeaves(items)
    else
      val Quartering(commonAncestor, leaves, branches) = quarterQuads(items)
      val children =
        branches.map((q, bs) => Branch(q, ZQuadTree.from(bs, maxLeafSize)))
      ZQuadTree(
        commonAncestor = commonAncestor,
        leaves = leaves,
        branches = children.toSeq
      )

  private def fromLeaves[V](items: Iterable[(ZQuad, V)]): ZQuadTree[V] =
    val commonAncestor = ZQuad.leastCommonAncestor(items.map(_._1))
    val leaves =
      items.map((quad, value) => Leaf(commonAncestor.descendancy(quad), value))
    ZQuadTree(
      commonAncestor = commonAncestor,
      leaves = leaves.toSeq,
      branches = List.empty
    )

  private def quarterQuads[V](items: Iterable[(ZQuad, V)]): Quartering[V] =
    val commonAncestor = ZQuad.leastCommonAncestor(items.map(_._1))
    var leaves = List.empty[Leaf[V]]
    val branches = Array.fill[(ZQuad, ListBuffer[(ZQuad, V)])](4)(null)
    for (quad, value) <- items do
      val descendancy = commonAncestor.descendancy(quad)
      if descendancy.isEverything then
        // Quads that aren't within the ancestor but that *are* the ancestor
        // have to go in the leaves because the branches require at least one
        // child.
        leaves = Leaf(descendancy, value) :: leaves
      else
        // Discard the ancestry of the quad above the common ancestor -- from
        // now on that will be implicit.
        val quarter = descendancy.toZoom(1)
        val quarterIndex = (quarter.toLong - 1).toInt
        if branches(quarterIndex) eq null then
          branches(quarterIndex) = (quarter, ListBuffer())
        branches(quarterIndex)._2.addOne(
          quarter.descendancy(descendancy) -> value
        )
    Quartering(commonAncestor, leaves, branches.filter(e => !(e eq null)))

  case class Leaf[V](descendancy: ZQuad, value: V):
    def writeFlatBuf(buf: FlatBufferBuilder, valueToInt: V => Int): Int =
      flatbuf.ZQuadTreeLeaf.createZQuadTreeLeaf(
        buf,
        descendancy.toLong,
        valueToInt(value)
      )

  case class Branch[V](quarter: ZQuad, subtree: ZQuadTree[V]):
    def writeFlatBuf(buf: FlatBufferBuilder, valueToInt: V => Int): Int =
      val subtreeIndex = subtree.writeFlatBuf(buf, valueToInt)
      flatbuf.ZQuadTreeBranch.createZQuadTreeBranch(
        buf,
        quarter.toLong.toByte,
        subtreeIndex
      )

  private case class Quartering[V](
      commonAncestor: ZQuad,
      leaves: Seq[Leaf[V]],
      branches: Iterable[(ZQuad, Iterable[(ZQuad, V)])]
  )

case class ZQuadTree[V](
    commonAncestor: ZQuad,
    leaves: Seq[ZQuadTree.Leaf[V]],
    branches: Seq[ZQuadTree.Branch[V]]
):

  def leafCount: Int = leaves.size + branches.map(_.subtree.leafCount).sum

  def branchCount: Int = branches.size + branches.map(_.subtree.branchCount).sum

  def maxDepth: Int =
    1 + branches.map(_.subtree.maxDepth).maxOption.getOrElse(0)

  def writeFlatBuf(buf: FlatBufferBuilder, valueToInt: V => Int): Int =
    flatbuf.ZQuadTree.startLeavesVector(buf, leaves.size)
    for leaf <- leaves do leaf.writeFlatBuf(buf, valueToInt)
    val leavesIndex = buf.endVector()
    val branchIndexes = branches.map(b => b.writeFlatBuf(buf, valueToInt))
    val branchesIndex = flatbuf.ZQuadTree.createBranchesVector(buf, branchIndexes.toArray)
    flatbuf.ZQuadTree.createZQuadTree(
      buf,
      commonAncestor.toLong,
      leavesIndex,
      branchesIndex
    )

  override def toString: String =
    f"ZQuadTree[$commonAncestor: ${leaves.size}](${branches.mkString(", ")})"
