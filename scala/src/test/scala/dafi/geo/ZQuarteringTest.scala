package dafi.geo


object QuarteringTestCase:

  def create[V](items: Seq[(Long, V)], defaultValue: Option[V]): QuarteringTestCase[V] =
    QuarteringTestCase(items.map((l, v) => ZQuad.fromLong(l) -> v), defaultValue)


case class QuarteringTestCase[V](items: Seq[(ZQuad, V)], defaultValue: Option[V]):

  def quarter(): ZQuartering[V] = ZQuartering(items, defaultValue)

  def get(input: ZQuad): Option[V] =
    var bestItem: Option[(ZQuad, V)] = None
    for item <- items do
      val (quad, value) = item
      if quad.isAncestor(input) && !bestItem.exists(_._1.zoomLevel > quad.zoomLevel) then
        bestItem = Some(item)
    bestItem.map(_._2).orElse(defaultValue)


class ZQuarteringTest extends munit.FunSuite:

  def assertBranching[V](
    input: QuarteringTestCase[V],
    expectedAncestor: Long,
    expectedOuterDefault: Option[V],
    expectedInnerDefault: Option[V],
    expectedNw: Seq[(Long, V)],
    expectedNe: Seq[(Long, V)],
    expectedSw: Seq[(Long, V)],
    expectedSe: Seq[(Long, V)]
  ): Unit =
    val quartering = input.quarter()
    assertEquivalent(input, quartering)
    def toBranch(items: Seq[(Long, V)]): Branch[V] =
      Branch(items.map((k, v) => ZQuad.fromLong(k) -> v))
    quartering match
      case BranchingQuartering(ancestor, outerDefault, innerDefault, branches) =>
        assert(ZQuad.fromLong(expectedAncestor) == ancestor)
        assert(clue(expectedOuterDefault) == clue(outerDefault))
        assert(clue(expectedInnerDefault) == clue(innerDefault))
        assert(clue(toBranch(expectedNw)) == clue(branches(0)))
        assert(clue(toBranch(expectedNe)) == clue(branches(1)))
        assert(clue(toBranch(expectedSw)) == clue(branches(2)))
        assert(clue(toBranch(expectedSe)) == clue(branches(3)))
      case _ =>
        assert(false)

  def assertLeaf[V](
    input: QuarteringTestCase[V],
    expectedValue: V
  ): Unit =
    val quartering = input.quarter()
    assertEquivalent(input, quartering)
    quartering match
      case LeafQuartering(v) =>
        assert(clue(expectedValue) == clue(v))
      case _ =>
        assert(false)

  def assertEquivalent[V](
    input: QuarteringTestCase[V],
    q: ZQuartering[V]
  ): Unit =
    for value <- 0 to ZQuad.zoomBias(6).toInt do
      val quad = ZQuad.fromLong(value)
      input.get(quad) match
        case inputResult@Some(_) =>
          assert(clue(inputResult) == clue(q.get(clue(quad))))
        case None =>
          ()


  test("branch0"):
    val input = QuarteringTestCase.create(List(69L -> 0, 76L -> 1, 72L -> 2), None)
    assertBranching(
      input,
      4L,
      None,
      None,
      Seq(4L -> 2, 1L -> 0),
      Seq(4L -> 1),
      Seq(),
      Seq())

  test("defaultLeaf"):
    val input =  QuarteringTestCase.create(List(69L -> 0, 76L -> 1, 72L -> 2), Some(1))
    assertBranching(
      input,
      4L,
      Some(1),
      Some(1),
      Seq(4L -> 2, 1L -> 0),
      Seq(4L -> 1),
      Seq(),
      Seq())

  test("branch1"):
    val input =  QuarteringTestCase.create(List(2L -> 1, 11L -> 2, 12L -> 3), None)
    assertBranching(
      input,
      2L,
      None,
      Some(1),
      Seq(),
      Seq(),
      Seq(0L -> 2),
      Seq(0L -> 3))

  test("branch2"):
    val input = QuarteringTestCase.create(
      List(136L -> 1, 138L -> 2, 552L -> 3, 545L -> 4, 568L -> 5, 582L -> 6, 580L -> 7, 594L -> 8),
      None)
    assertBranching(
      input,
      8L,
      None,
      None,
      Seq(17L -> 4, 4L -> 1),
      Seq(8L -> 3, 2L -> 2),
      Seq(20L -> 7, 8L -> 5),
      Seq(18L -> 8, 6L -> 6))

  test("simpleLeaf"):
    val input = QuarteringTestCase.create(
      List(69L -> 0, 76L -> 0, 72L -> 0),
      None)
    assertLeaf(input, 0)

  test("noDefaultSingleton"):
    val input = QuarteringTestCase.create(
      List(0L -> 1),
      None)
    assertLeaf(input, 1)

  test("defaultSingleton"):
    val input = QuarteringTestCase.create(
      List(0L -> 1),
      Some(2))
    assertLeaf(input, 1)

  test("defaultSingletonWithChild"):
    val input = QuarteringTestCase.create(
      List(0L -> 1, 70L -> 1),
      Some(2))
    assertLeaf(input, 1)
