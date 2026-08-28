package dafi.geo

class ZQuadTest extends munit.FunSuite:

  test("zoomBias"):
    def check(expected: Long, zoomLevel: Int): Unit =
      assert(clue(expected) == clue(ZQuad.zoomBias(zoomLevel)))
    check(0, 0)
    check(0b1, 1)
    check(0b101, 2)
    check(0b10101, 3)
    check(0b1010101, 4)
    check(0b101010101, 5)
    check(0b10101010101, 6)
    check(0b1010101010101, 7)
    check(0x555555555555555L, 30)

  test("fromUnit"):
    def check(x: Double, y: Double, zoomLevel: Int, quad: Long): Unit =
      var zq = ZQuad.fromUnit(x, y).toZoom(zoomLevel)
      assert(clue(quad) == clue(zq.quad.toLong))
    check(0.5, 0.5, 0, 0)
    check(0.25, 0.25, 1, 1)
    check(0.75, 0.25, 1, 2)
    check(0.25, 0.75, 1, 3)
    check(0.75, 0.75, 1, 4)
    check(0, 0, 1, 1)
    check(0, 0, 4, 85)
    check(0, 0, 8, 21845)
    check(0, 0, 12, 5592405)
    check(0, 0, 16, 1431655765)
    check(0, 0, 20, 366503875925)
    check(0, 0, 24, 93824992236885)
    check(0.999999, 0.999999, 1, 4)
    check(0.999999, 0.999999, 4, 340)
    check(0.999999, 0.999999, 8, 87380)
    check(0.999999, 0.999999, 12, 22369620)
    check(0.999999, 0.999999, 16, 5726623060)
    check(0.999999, 0.999999, 20, 1466015503697)
    check(0.999999, 0.999999, 24, 375299968946772)
    check(0.333333, 0.666667, 1, 3)
    check(0.333333, 0.666667, 2, 14)
    check(0.333333, 0.666667, 3, 59)
    check(0.333333, 0.666667, 4, 238)
    check(0.333333, 0.666667, 5, 955)
    check(0.333333, 0.666667, 6, 3822)
    check(0.333333, 0.666667, 7, 15291)

  test("unitCenter"):
    def check(quad: Long, x: Double, y: Double): Unit =
      var zq = ZQuad.fromLong(quad)
      var (cx, cy) = zq.unitCenter
      assert(clue(cx) == clue(x))
      assert(clue(cy) == clue(y))
    check(0, 0.5, 0.5)
    check(1, 0.25, 0.25)
    check(2, 0.75, 0.25)
    check(3, 0.25, 0.75)
    check(4, 0.75, 0.75)
    check(9, 0.625, 0.125)
    check(39, 0.5625, 0.1875)
    check(159, 0.53125, 0.21875)
    check(40810, 0.529296875, 0.189453125)

  test("fromWgs84"):
    def check(lat: Double, lon: Double, zoom: Int, quad: Long): Unit =
      var zq = ZQuad.fromWgs84(lat, lon).toZoom(zoom)
      assert(clue(zq.quad.toLong) == clue(quad))

    // Toronto
    check(43.7000, -79.4000, 1, 1)
    // Kyoto
    check(35.0116, 135.7683, 1, 2)
    // Buenos Aires
    check(-34.6033, -58.3816, 1, 3)
    // Sydney
    check(-33.8650, 151.2094, 1, 4)

    check(56.1676, 10.2062, 1, 2)
    check(56.1676, 10.2062, 2, 9)
    check(56.1676, 10.2062, 3, 39)
    check(56.1676, 10.2062, 4, 159)
    check(56.1676, 10.2062, 8, 40810)
    check(56.1676, 10.2062, 16, 2674550782)
    check(56.1676, 10.2062, 20, 684685000362)
    check(56.1676, 10.2062, 24, 175279360092826)
    check(56.1676, 10.2062, 28, 44871516183763571)
    check(56.1676, 10.2062, 30, 717944258940217152)

  test("wgs84Center"):
    def check(quad: Long, lat: Double, lon: Double): Unit =
      var zq = ZQuad.fromLong(quad)
      var (la, lo) = zq.wgs84Center
      assertEqualsDouble(clue(lat), clue(la), 1e-6)
      assertEqualsDouble(clue(lon), clue(lo), 1e-6)

    check(2, 45, 90)
    check(9, 67.5, 45)
    check(39, 56.25, 22.5)
    check(159, 50.625, 11.25)
    check(40810, 55.8984375, 10.546875)
    check(2674550782, 56.166229, 10.203552)
    check(684685000362, 56.167517, 10.206127)
    check(175279360092826, 56.167597, 10.206202)
    check(44871516183763568, 56.167600, 10.206198)
    check(717944258940217152, 56.167600, 10.206199)

  test("leastCommonAncestor"):
    def check(expected: Long, a: Long, b: Long): Unit =
      assert(expected == ZQuad.fromLong(a).leastCommonAncestor(ZQuad.fromLong(b)).toLong)

    check(0, 0, 1)
    check(0, 1, 2)
    check(0, 2, 3)
    check(0, 3, 4)
    check(2, 9, 12)
    check(0, 16, 17)
    check(15, 63, 62)
    check(3, 64, 57)
    check(0, 64, 72)
    check(15, 256, 62)
    check(15, 62, 256)
    check(0, 1108, 1109)
    check(3, 1044, 917)
    check(232, 931, 930)
