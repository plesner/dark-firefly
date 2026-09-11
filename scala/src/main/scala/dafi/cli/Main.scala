package dafi.cli

import dafi.geo.ZQuadTree
import dafi.gtfs.GtfsArchive
import scopt.OParser

case class Options(
    input: String = "",
    handler: Option[Options => Unit] = None
)

class Main(args: Array[String]):

  private val builder = OParser.builder[Options]
  private val argParser =
    import builder._
    OParser.sequence(
      programName("dafi"),
      cmd("gtfs")
        .action((_, c) => c.copy(handler = Some(handle_gtfs)))
        .children(
          opt[String]("input").action((v, options) => options.copy(input = v))
        )
    )

  def main(): Unit =
    OParser.parse(argParser, args, Options()) match
      case Some(options) =>
        options.handler.foreach(_.apply(options))
      case _ =>
        System.exit(1)

  private def handle_gtfs(opts: Options): Unit =
    val arch = GtfsArchive.open(opts.input)
    val stops = arch.stops()
    val q = ZQuadTree.from(stops.map(s => s.quad -> s), 256)
    println(stops.size)
    println(q.leafCount)
    println(q.branchCount)
    println(q.maxDepth)

def main(args: Array[String]): Unit =
  new Main(args).main()
