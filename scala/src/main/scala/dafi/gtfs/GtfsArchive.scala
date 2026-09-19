package dafi.gtfs

import com.github.tototoshi.csv.CSVReader
import dafi.geo.ZQuad

import java.util.zip.ZipFile
import scala.io.Source


object GtfsArchive:

  def open(path: String): GtfsArchive =
    new GtfsArchive(new ZipFile(path))


class GtfsStop(row: Map[String, String]):

  def id: String =
    row("stop_id")

  def name: String =
    row("stop_name")

  def latLon: (Double, Double) =
    (row("stop_lat").toDouble, row("stop_lon").toDouble)

  def quad: ZQuad =
    val (lat, lon) = latLon
    ZQuad.fromWgs84(lat, lon)


class GtfsArchive(archive: ZipFile):

  def stops(): List[GtfsStop] =
    val entry = archive.getEntry("stops.txt")
    val reader = CSVReader.open(Source.fromInputStream(archive.getInputStream(entry)))
    reader.allWithHeaders().map(new GtfsStop(_))
