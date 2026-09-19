package dafi.cli

import com.google.flatbuffers.FlatBufferBuilder
import dafi.ctfs.CtfsBundle
import dafi.flat.Extensions.*
import dafi.steps.{PipelineOptions, ReadStopsOptions, Steps}
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
    import builder.*
    OParser.sequence(
      programName("dafi"),
      cmd("gtfs")
        .action((_, c) => c.copy(handler = Some(handle_gtfs)))
        .children(
          opt[String]("input")
            .action((v, options) => options.copy(input = Some(v))),
          opt[String]("output")
            .action((v, options) => options.copy(output = Some(v)))
        )
    )

  def main(): Unit =
    OParser.parse(argParser, args, Options()) match
      case Some(options) =>
        options.handler.foreach(_.apply(options))
      case _ =>
        System.exit(1)

  private def handle_gtfs(opts: Options): Unit =
    val stepsOptions =
      PipelineOptions(
        gtfsPath = opts.input.get,
        readStops =
          ReadStopsOptions().copy(idRegex = "^0*(?<id>[1-9][0-9]*)G*$")
      )
    val pipeline = Steps.builder(stepsOptions).newPipeline
    val bundle = pipeline.step[CtfsBundle].get()
    val buf = new FlatBufferBuilder()
    buf.finishSection(bundle.createManifestSection(buf))
    Files.write(Paths.get(opts.output.get), buf.sizedByteArray())

def main(args: Array[String]): Unit =
  new Main(args).main()
