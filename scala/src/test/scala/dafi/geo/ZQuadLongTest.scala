package dafi.geo

class ZQuadLongTest extends munit.FunSuite:

  test("zoomLevel"):
    def check(expected: Int, quad: Long): Unit =
      assert(clue(expected) == clue(ZQuadLong.fromLong(quad).zoomLevel))
    check(0, 0)
    check(1, 1)
    check(1, 4)
    check(2, 5)
    check(2, 20)
    check(3, 21)
    check(3, 84)
    check(4, 85)
    check(4, 340)
    check(5, 341)
    check(5, 1364)
    check(6, 1365)
    check(30, 1537228672809129300)
    check(31, 1537228672809129301)

  test("spreadAndCompact"):
    def check(expected: Long, input: Int): Unit =
      val spread = ZQuadLong.spreadInt(input)
      assert(clue(expected) == clue(spread))
      val compacted = ZQuadLong.compactLong(spread)
      assert(clue(input) == clue(compacted))
    check(0, 0)
    check(0b01, 0b1)
    check(0b0101, 0b11)
    check(0b0101010101010101, 0b11111111)
    check(0x5555555555555555L, 0xFFFFFFFF)
