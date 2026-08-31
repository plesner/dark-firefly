package dafi.geo

import dafi.geo.ZQuad.zoomBias

/** Utilities related to working with z-quads. A z-quad is a 64-bit quantity that
  * identifies a regular square sub-division of a square space. Here's an
  * illustration of the first 3 zoom levels,
  *
  *       zoom = 0            zoom = 1            zoom = 2
  *   +---------------+   +-------+-------+   +---+---+---+---+
  *   |               |   |       |       |   | 5 | 6 | 9 | 10|
  *   |               |   |   1   |   2   |   +---+---+---+---+
  *   |               |   |       |       |   | 7 | 8 | 11| 12|
  *   |       0       |   +-------+-------+   +---+---+---+---+
  *   |               |   |       |       |   | 13| 14| 17| 18|
  *   |               |   |   3   |   4   |   +---+---+---+---+
  *   |               |   |       |       |   | 15| 16| 19| 20|
  *   +---------------+   +-------+-------+   +---+---+---+---+
  *
  * So 0 represents the whole space, 1 represents the top left corner of the
  * space, and so on. The greater the value the smaller and more accurate the
  * space it represents. Note that at zoom level 2 the indices don't move
  * straight across, they move in zig-zag finishing each quad from the previous
  * zoom level before moving on to the next one, so 5-8 at zoom 2 cover 1 from
  * zoom 1, and so forth.
  *
  * A bit of terminology used in the implementation. The relations between quads
  * are the following.
  *
  *  * Given a quad its _parent_ is the containing quad at the zoom level above.
  *    So the parent of 14 is 3, the parent of 3 is 0.
  *  * If B is a parent of A then A is a _child_ of B. So 3 is a child of 0 and
  *    14 is a child of 3.
  *  * A quad A is an _ancestor_ of another quad B if A and B are equal or A is
  *    an ancestor of B's parent. So 0, 3, and 14 are all ancestors of 14 but
  *    only 3 is the parent of 14.
  *  * B is a _descendant_ of A if A is an ancestor of B. So 0, 3, and 14 are
  *    all descendants of 0.
  *  * Given a quad B and an ancestor A, the _descendancy_ between A and B is
  *    the quad that indicates the location of B within A. For instance, the
  *    descendancy of 14 within 3 is 2 because if you look only at 3 then 14 is
  *    at its quad 2.
  *
  * Terminology around the implementation,
  *
  *  * For a given zoom level the _bias_ is the value of the first quad at that
  *    level. The 0th bias is 0, the 1st is 1, the 2nd is 5, the 3rd is 21,
  *    etc.
  *  * The _scalar_ value of a quad is the quad's value minus the bias for that
  *    quad's zoom level. For example, values at zoom level 2 are 5-20, the
  *    scalars are 0-15. There are 4^z scalars at zoom level z.
  *
  * The name, z-quad, comes from the fact that going between a quad and its
  * descendants and ancestors ("zooming in and out") is inexpensive, and the
  * zig-zag pattern of numbering.
  *
  * For a lot of operations you need to know the quad's zoom level explicitly.
  * It is not super expensive to calculate but it's not trivial either and often
  * you'll already know the zoom so it's a waste to recalculate it, so for
  * those operations two functions exist: one that calculates the zoom itself
  * and one that takes it as an argument.
  */
object ZQuad:

  val MaxZoom: Int = 30

  val Everything: ZQuad = ZQuad(ZQuadLong.Everything, 0)

  private def calcBiases(): Array[Long] =
    val result = Array.fill[Long](MaxZoom + 1)(0)
    var current = 0L
    for (i <- 0 to MaxZoom) do
      result(i) = current
      current = (current << 2) + 1L
    result

  private val Biases: Array[Long] = calcBiases()

  def zoomBias(zoomLevel: Int): Long = Biases(zoomLevel)

  def fromLong(l: Long): ZQuad =
    val q = ZQuadLong.fromLong(l)
    new ZQuad(q, q.zoomLevel)

  def fromUnit(unitX: Double, unitY: Double): ZQuad =
    new ZQuad(ZQuadLong.fromUnitAtMaxZoom(unitX, unitY), MaxZoom)

  /** If no precision is given returns the zoom-30 wgs84 quad that contains the
    * given coordinates.
    *
    * If a precision is given returns a quad such that when you get the result's
    * wgs84 center and round it to the given precision the result will be the
    * same as rounding the given coordinates to the same precision.
    */
  def fromWgs84(geoLat: Double, geoLon: Double): ZQuad =
    val unitLat = (90.0 - geoLat) / 180.0
    val unitLon = (180.0 + geoLon) / 360.0
    fromUnit(unitLon, unitLat)


case class ZQuad(quad: ZQuadLong, zoomLevel: Int):

  /** Returns the quad at the given zoom level that contains this quad. If the
    * requested level is more accurate than this quad's accuracy we return this
    * same quad.
    */
  def toZoom(newZoomLevel: Int): ZQuad =
    if newZoomLevel >= zoomLevel
    then this
    else this.ancestor(zoomLevel - newZoomLevel)

  def isEverything: Boolean = quad.isEverything

  /** Returns this quad's ancestor n levels above. A quad's 0'th ancestor is
    * itself.
    */
  def ancestor(n: Int): ZQuad =
    val newQuad = quad.ancestor(n)
    val newZoom = (zoomLevel - n).max(0)
    return new ZQuad(newQuad, newZoom)

  def isAncestor(that: ZQuad): Boolean =
    zoomLevel <= that.zoomLevel && that.quad.ancestor(that.zoomLevel - zoomLevel) == this.quad

  def leastCommonAncestor(that: ZQuad): ZQuad =
    var a = this
    var b = that

    // Normalize a and b so they're both at the same zoom level.
    if a.zoomLevel < b.zoomLevel then
      b = b.ancestor(b.zoomLevel - a.zoomLevel)
    else
      a = a.ancestor(a.zoomLevel - b.zoomLevel)

    // Get their respective scalars.
    val aScalar = a.scalar
    val bScalar = b.scalar

    // Find most significant bit of difference between the two scalars. Because
    // of the recursive zig-zag way the quad indices are constructed this gives
    // the highest zoom level where there is a difference.
    val allDifferences = aScalar ^ bScalar
    val highestDifference = ZQuadLong.highestOneBit(allDifferences)

    // The amount to zoom out such that the most significant difference will be
    // discarded.
    val ancestorDeltaZoom = (highestDifference + 1) >> 1

    // Zoom out by that amount.
    a.ancestor(ancestorDeltaZoom)

  /** Given a quad within this one, returns the quad that locates the given quad
   * within this one. That is, if you made this quad everything what would the
   * given quad be? Or, to express it differently, this returns the quad that
   * you can pass to 'descendant' from this one to get the argument. Cheap.
   */
  def descendancy(that: ZQuad): ZQuad =
    val innerZoom = that.zoomLevel - this.zoomLevel
    val innerBias = zoomBias(innerZoom)
    // This is equivalent to getting that's scalar and masking out just the
    // part that falls within this quad. However since we're masking the top
    // bits away it doesn't matter if they're biased or not so we just remove
    // the bias of the lower bits.
    val innerQuad = ((that.toLong - innerBias) & ((1L << (innerZoom << 1)) - 1)) + innerBias
    new ZQuad(ZQuadLong.fromLong(innerQuad), innerZoom)

  def descendant(that: ZQuad): ZQuad =
    val newQuad = (toLong << (that.zoomLevel << 1)) + that.toLong
    val newZoom = zoomLevel + that.zoomLevel
    new ZQuad(ZQuadLong.fromLong(newQuad), newZoom);

  def unitCenter: (Double, Double) =
    val s = this.scalar
    var x = ZQuadLong.compactLong(s)
    var y = ZQuadLong.compactLong(s >> 1)
    var z = zoomLevel
    if z < ZQuad.MaxZoom then
      x = (x << 1) + 1
      y = (y << 1) + 1
      z += 1
    (x.toDouble / (1 << z), y.toDouble / (1 << z))

  def wgs84Center: (Double, Double) =
    val (ux, uy) = this.unitCenter
    (-uy * 180 + 90, ux * 360 - 180)

  def parent: ZQuad = ancestor(1)

  def scalar: Long = quad.toLong - ZQuad.zoomBias(zoomLevel)

  def toLong: Long = quad.toLong

  private val Quadigits: String = "◰◳◱◲"

  override def toString: String = f"ZQuad(${quad.toLong}: $toQuadigits)"

  def toQuadigits: String =
    var result = ""
    var currentZoom = zoomLevel
    var currentScalar = scalar
    while currentZoom > 0 do
      result = f"${Quadigits((currentScalar % 4).toInt)}$result"
      currentScalar = currentScalar / 4
      currentZoom = currentZoom - 1
    result
