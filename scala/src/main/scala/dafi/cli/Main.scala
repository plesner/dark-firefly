package dafi.cli

import com.google.flatbuffers.FlatBufferBuilder
import dafi.geo.ZQuadTree
import dafi.gtfs.GtfsArchive
import scopt.OParser

import java.nio.file.{Files, Paths}

case class Options(
    input: Option[String] = None,
    output: Option[String] = None,
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
          opt[String]("input").action((v, options) => options.copy(input = Some(v))),
          opt[String]("output").action((v, options) => options.copy(output = Some(v)))
        )
    )

  def main(): Unit =
    OParser.parse(argParser, args, Options()) match
      case Some(options) =>
        options.handler.foreach(_.apply(options))
      case _ =>
        System.exit(1)

  private def handle_gtfs(opts: Options): Unit =
    val arch = GtfsArchive.open(opts.input.get)
    val stops = arch.stops()
    val q = ZQuadTree.from(stops.map(s => s.quad -> s), 256)
    val buf = new FlatBufferBuilder()
    val root = q.writeFlatBuf(buf, s => s.hashCode())
    buf.finish(root)
    Files.write(Paths.get(opts.output.get), buf.sizedByteArray())

def main(args: Array[String]): Unit =
  new Main(args).main()
