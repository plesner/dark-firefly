package dafi.ctfs

import com.google.flatbuffers.{FlatBufferBuilder}
import dafi.flat.{FlatOffset, SectionContentsOffset}
import dafi.flatbuf.{BundleSection}
import dafi.flat.Extensions.*

case class CtfsBundle(entitySets: List[CtfsEntitySet]):

  def createManifestSection(buf: FlatBufferBuilder): FlatOffset[BundleSection] =
    buf.createBundleSection(
      contents = SectionContentsOffset.manifest(
        buf.createBundleManifest(
          description = buf.createBundleDescription(
            entitySets =
              entitySets.map(s => s.createDescription(buf)).toFlatVector
          )
        )
      )
    )
