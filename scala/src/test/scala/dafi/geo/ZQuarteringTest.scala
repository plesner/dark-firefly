package dafi.geo

import dafi.geo.ZQuarteringTest.toEntries

object ZQuarteringTest:

  def toEntries[V](items: (Long, V)*): Seq[(ZQuad, V)] =
    items.map((l, v) => ZQuad.fromLong(l) -> v)
    

class ZQuarteringTest extends munit.FunSuite:

  def assertBranching[V](
    q: ZQuartering[V], 
    expectedAncestor: Long,
    expectedDefaultValue: Option[V],
    expectedNw: Seq[(Long, V)],
    expectedNe: Seq[(Long, V)],
    expectedSw: Seq[(Long, V)],
    expectedSe: Seq[(Long, V)]
  ): Unit =
    def toBranch(items: Seq[(Long, V)]): Branch[V] =
      Branch(items.map((k, v) => ZQuad.fromLong(k) -> v))
    q match
      case BranchingQuartering(ancestor, defaultValue, branches) =>
        assert(ZQuad.fromLong(expectedAncestor) == ancestor)
        assert(expectedDefaultValue == defaultValue)
        assert(clue(toBranch(expectedNw)) == clue(branches(0)))
        assert(clue(toBranch(expectedNe)) == clue(branches(1)))
        assert(clue(toBranch(expectedSw)) == clue(branches(2)))
        assert(clue(toBranch(expectedSe)) == clue(branches(3)))
      case _ => 
        assert(false)

  test("branch0"):
    val entries =  toEntries(69L -> 0, 76L -> 1, 72L -> 2)
    val quartering = ZQuartering.quarterQuads(entries, None)
    assertBranching(
      quartering, 
      4L,
      None, 
      Seq(4L -> 2, 1L -> 0),
      Seq(4L -> 1),
      Seq(),
      Seq())
  