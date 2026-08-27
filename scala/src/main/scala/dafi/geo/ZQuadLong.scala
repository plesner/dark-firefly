package dafi.geo

import dafi.{geo => Returns}
import Returns.ZQuad.zoomBias

opaque type ZQuadLong = Long

object ZQuadLong:

  private val MaxZoomLimit: Int = 1 << ZQuad.MaxZoom
  private val MaxZoomBias: Long = 0x555555555555555L

  def fromLong(quad: Long): ZQuadLong = quad

  /** Returns the zoom-30 quad that contains the given pair of coordinates on
    * the unit square.
    */
  def fromUnitAtMaxZoom(unitX: Double, unitY: Double): ZQuadLong =
    val ix = (unitX * MaxZoomLimit).toInt
    val iy = (unitY * MaxZoomLimit).toInt
    val sx = spreadInt(ix)
    val sy = spreadInt(iy)
    val scalar = sx | (sy << 1)
    val bias = ZQuad.zoomBias(ZQuad.MaxZoom)
    return scalar + bias

  /** Given a long where there are only bits set in the lower half, returns a
    * long where those bits have been spread evenly across the whole word. That
    * is, given this input,
    *
    * 00000000000000000000000000000000abcdefghijklmnopqrstuvwxyzABCDEF
    *
    * returns
    *
    * 0a0b0c0d0e0f0g0h0i0j0k0l0m0n0o0p0q0r0s0t0u0v0w0x0y0z0A0B0C0D0E0F
    */
  def spreadInt(n: Int): Long =
    var r: Long = n
    // 00000000000000000000000000000000abcdefghijklmnopqrstuvwxyzABCDEF
    r = ((r << 16) & 0x0000ffff00000000L) | (r & 0x000000000000ffffL)
    // 0000000000000000abcdefghijklmnop0000000000000000qrstuvwxyzABCDEF
    r = ((r << 8) & 0x00ff000000ff0000L) | (r & 0x000000ff000000ffL)
    // 00000000abcdefgh00000000ijklmnop00000000qrstuvwx00000000yzABCDEF
    r = ((r << 4) & 0x0f000f000f000f00L) | (r & 0x000f000f000f000fL)
    // 0000abcd0000efgh0000ijkl0000mnop0000qrst0000uvwx0000yzAB0000CDEF
    r = ((r << 2) & 0x3030303030303030L) | (r & 0x0303030303030303L)
    // 00ab00cd00ef00gh00ij00kl00mn00op00qr00st00uv00wx00yz00AB00CD00EF
    r = ((r << 1) & 0x4444444444444444L) | (r & 0x1111111111111111L)
    // 0a0b0c0d0e0f0g0h0i0j0k0l0m0n0o0p0q0r0s0t0u0v0w0x0y0z0A0B0C0D0E0F
    r

  /** Given a long where only even-offset bits are set, returns a value where
    * the odd-offset bits have been discarded and the even-offset bits have been
    * packed together in the lower half. That is, given this input
    *
    * 0a0b0c0d0e0f0g0h0i0j0k0l0m0n0o0p0q0r0s0t0u0v0w0x0y0z0A0B0C0D0E0F
    *
    * returns
    *
    * 00000000000000000000000000000000abcdefghijklmnopqrstuvwxyzABCDEF
    */
  def compactLong(n: Long): Int =
    var r: Long = n
    // 0a0b0c0d0e0f0g0h0i0j0k0l0m0n0o0p0q0r0s0t0u0v0w0x0y0z0A0B0C0D0E0F
    r = ((r >> 1) & 0x2222222222222222L) | (r & 0x1111111111111111L)
    // 00ab00cd00ef00gh00ij00kl00mn00op00qr00st00uv00wx00yz00AB00CD00EF
    r = ((r >> 2) & 0x0c0c0c0c0c0c0c0cL) | (r & 0x0303030303030303L)
    // 0000abcd0000efgh0000ijkl0000mnop0000qrst0000uvwx0000yzAB0000CDEF
    r = ((r >> 4) & 0x00f000f000f000f0L) | (r & 0x000f000f000f000fL)
    // 00000000abcdefgh00000000ijklmnop00000000qrstuvwx00000000yzABCDEF
    r = ((r >> 8) & 0x0000ff000000ff00L) | (r & 0x000000ff000000ffL)
    // 0000000000000000abcdefghijklmnop0000000000000000qrstuvwxyzABCDEF
    r = ((r >> 16) & 0x00000000ffff0000L) | (r & 0x000000000000ffffL)
    // 00000000000000000000000000000000abcdefghijklmnopqrstuvwxyzABCDEF
    r.toInt

extension (quad: ZQuadLong)

  def toLong: Long = quad

  def zoomLevel: Int =
    // Determine which is the highest one-bit. Each zoom level uses exactly 2
    // bits of state but are offset by ~33%, hence the zoom level is ln2(n)/2
    // adjusted for the offset.
    val highestOneBit = 64 - java.lang.Long.numberOfLeadingZeros(quad)
    // Calculate the zoom level bias that separates the coarser zoom level that
    // can have this highest bit set, and the finer one.
    val zoomBias = ((1L << highestOneBit) - 1L) & 0x5555555555555555L
    // Calculate difference between the value and the boundary. If the value is
    // below then we're looking at a value belonging to the coarser zoom level
    // and the delta becomes non-negative. If it is greater than then it
    // belongs to the finer zoom level and the value becomes negative.
    val delta = (zoomBias - 1) - quad
    // Extract the sign bit as an integer. If the value belongs to the finer
    // zoom level the delta is negative and this value is 1, otherwise it is 0
    val isFineBonus = ((delta >> 63) & 1).toInt
    // Adjust for the fineness and then shift down to account for each zoom
    // level spanning 2 bits.
    return (highestOneBit + isFineBonus) >> 1

  def ancestor(n: Int): ZQuadLong = (quad - ZQuad.zoomBias(n)) >> (n << 1)
