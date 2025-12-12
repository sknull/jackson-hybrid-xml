package de.visualdigits.hybridxml.model.namespaces

import com.fasterxml.jackson.core.PrettyPrinter
import com.fasterxml.jackson.core.io.IOContext
import com.fasterxml.jackson.dataformat.xml.XmlFactory
import java.io.Writer

/**
 * Override visibility of ToXmlGenerator#nextIsAttribute()
 */
class XmlFactory(
    private val prettyPrinter: PrettyPrinter
) : XmlFactory() {

  private var ctxt: IOContext? = null

  override fun createGenerator(out: Writer): ToXmlGenerator {
    ctxt = _createContext(_createContentReference(out), false)
      val toXmlGenerator = ToXmlGenerator(
          ctxt,
          _generatorFeatures,
          _xmlGeneratorFeatures,
          _objectCodec,
          _createXmlWriter(ctxt, out),
          _nameProcessor
      )
      toXmlGenerator.setPrettyPrinter(prettyPrinter)
      return toXmlGenerator
  }
}