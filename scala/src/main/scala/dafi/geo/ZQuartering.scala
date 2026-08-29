package dafi.geo

import scala.compiletime.ops.double

object ZQuartering:
  
  def quarterQuads[V](quads: Iterable[(ZQuad, V)], defaultValue: Option[V]): ZQuartering[V] =
    val iter = quads.iterator
    val (firstQuad, firstValue) = iter.next()
    
    // The common ancestor of all the entries. Initialized to an arbitrary value
    // which the result is guaranteed to be an ancestor of.
    var commonAncestor = firstQuad
    
    // If the common ancestor is explicitly given a value then this variable will
    // hold it at the end.
    var commonAncestorValue: Option[V] = Some(firstValue)
    
    // If all the entries have the same value then this will hold that value at
    // the end.
    var commonEntryValue: Option[V] = Some(firstValue)

    // Scan through and determine the global properties of the entries.
    while iter.hasNext do
      val (nextQuad, nextValue) = iter.next()
      val nextCommonAncestor = commonAncestor.leastCommonAncestor(nextQuad)
      if nextCommonAncestor != commonAncestor then
        // If we're changing the common ancestor whatever value we're recorded for
        // it is no longer valid so we clear it.
        commonAncestor = nextCommonAncestor
        commonAncestorValue = None
      if nextQuad == commonAncestor then
        commonAncestorValue = Some(nextValue)
      if commonEntryValue.exists(_ != nextValue) then
        // This entry's value is different from the common value so there is none.
        commonEntryValue = None
    
    var overrideDefaultValue = defaultValue
    if commonAncestor.isEverything then
      // As a special case, if there is an entry that covers everything then it
      // shadows the explicitly given default value, if there is one, so we make
      // sure it gets discarded. In the following we need to be able to assume
      // that this has happened.
      if commonAncestorValue.isDefined then
        overrideDefaultValue = commonAncestorValue
    else if commonAncestor.zoomLevel > 1 then
      // If the common ancestor is not a direct child but a descendant further
      // down, then if there is a default value we may have to zoom the descendant
      // out until it's a direct child. For instance, say we have a case like this
      // where 'x' is the common ancestor and '2' is the default value.
      //
      //   nw                 ne
      //     +---+---+---+---+
      //     | 2 | 2 | 2 | 2 |
      //     +---+---+---+---+
      //     | 2 | 2 | 2 | 2 |
      //     +---+---+---+---+
      //     | 2 | x | 2 | 2 |
      //     +---+---+---+---+
      //     | 2 | 2 | 2 | 2 |
      //     +---+---+---+---+
      //   sw                 se
      //
      // In the resulting tree we really want this to be represented as
      //
      //   nw                 ne
      //     +-------+-------+
      //     |       |       |
      //     |   2   |   2   |
      //     |       |       |
      //     +---+---+-------+
      //     | 2 | x |       |
      //     +---+---+   2   |
      //     | 2 | 2 |       |
      //     +---+---+-------+
      //   sw                 se
      //
      // which means that we want to ensure that the sw quad gets split into four,
      // which won't happen if we go directly to 'x'. If we go directly to 'x' the
      // result will be
      //
      //   nw                 ne
      //     +-------+-------+
      //     |       |       |
      //     |   2   |   2   |
      //     |       |       |
      //     +---+---+-------+
      //     |   | x |       |
      //     |   +---+   2   |
      //     |       |       |
      //     +-------+-------+
      //   sw                 se
      //
      // where the quads outside 'x' in sw don't have a value -- which is fine if
      // there is no default because then we don't care what the value of the
      // other quads are as long as 'x' is correct, but if there is a default they
      // must have that value. So, to force the split to happen when there is a
      // default we force the common ancestor to be the entire child,
      //
      //   nw                 ne
      //     +-------+-------+
      //     |       |       |
      //     |   2   |   2   |
      //     |       |       |
      //     +---+---+-------+
      //     |       |       |
      //     |   x   |   2   |
      //     | def 2 |       |
      //     +-------+-------+
      //   sw                 se
      //
      // with the same default. Then in the next step when we quarter sw it will
      // be split as expected.
      //
      // In the following we'll assume this has happened. There is a special case
      // where there is a default value but it happens to be the same as the value
      // of 'x', in which case we don't actually need to do this, but that gets
      // taken care of just below and will result in generating a leaf so this has
      // no effect.
      if defaultValue.isDefined then
        commonAncestor = commonAncestor.toZoom(1)
        commonAncestorValue = None

    // If all the entries have the same value, unless we have a default value and
    // which is different from that common value, this can be a leaf.
    //
    // This is where we use the assumption that if there is a quad that covers
    // everything then its value shadows the given default value. Otherwise if
    // there is a default value and it's different we won't be able to generate
    // a leaf in that case even though obviously we should be able to.
    commonEntryValue match
      case Some(cev) => 
        if !overrideDefaultValue.exists(_ != cev) then
          return LeafQuartering(cev)
      case _ => ()
    
    // The entries have different values so we have to split them into the four
    // branches. The default value to use here is a little tricky. By default we
    // pass through the override default value we computed above since that's the
    // one that was requested.
    val entryBuf = Array.fill[List[(ZQuad, V)]](4)(List.empty[(ZQuad, V)])
    for (quad, value) <- quads do
      val descendancy = commonAncestor.descendancy(quad)
      if !descendancy.isEverything then
        // If this quad is the same as the common ancestor we don't represent
        // it explicitly, rather we use the branch default value to pass its value
        // through to the children. If it's a child then we put it in the
        // appropriate branch.
        val childQuad = descendancy.toZoom(1)
        val childIndex = (childQuad.toLong - 1).toInt
        val entry = childQuad.descendancy(descendancy) -> value
        entryBuf(childIndex) = entry :: entryBuf(childIndex)
    BranchingQuartering(
      commonAncestor,
      overrideDefaultValue,
      entryBuf.map(b => Branch(b)))


sealed trait ZQuartering[V]

case class LeafQuartering[V](value: V) extends ZQuartering[V]

/**
 * An individual branch from a branching quartering.
 */
case class Branch[V](entries: Seq[(ZQuad, V)])

case class BranchingQuartering[V](
  /**
    * The common ancestor of all the branches.
    */
  commonAncestor: ZQuad, 
  
  /** The default value to use for quads within this branch that aren't
    * explicitly mentioned in the children lists.
    */
  defaultValue: Option[V],
  
  /**The array of branches; the branch at index i (0 <= i < 4) represents the
    * child branch at zoom level 1 with scalar i.
    */
  branches: IndexedSeq[Branch[V]]
) extends ZQuartering[V]