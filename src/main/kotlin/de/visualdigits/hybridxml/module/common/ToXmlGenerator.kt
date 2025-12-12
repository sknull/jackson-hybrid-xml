package de.visualdigits.hybridxml.module.common

import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.io.IOContext
import com.fasterxml.jackson.dataformat.xml.XmlNameProcessor
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil
import de.visualdigits.hybridxml.module.namespaces.serializer.NamespaceAwarePrettyPrinter
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamWriter

/**
 * Override visibility of nextIsAttribute()
 */
class ToXmlGenerator(
    ctxt: IOContext?,
    stdFeatures: Int,
    xmlFeatures: Int,
    codec: ObjectCodec,
    sw: XMLStreamWriter,
    nameProcessor: XmlNameProcessor?
) : ToXmlGenerator(ctxt, stdFeatures, xmlFeatures, codec, sw, nameProcessor) {

  fun nextIsAttribute(): Boolean = _nextIsAttribute

    fun writeStartObjectWithSchemaPrefix(schemaPrefix: String?) {
        _verifyValueWrite("start an object")
        _writeContext = _writeContext.createChildObjectContext()
        streamWriteConstraints().validateNestingDepth(_writeContext.nestingDepth)
        if (_cfgPrettyPrinter != null) {
            (_cfgPrettyPrinter as NamespaceAwarePrettyPrinter).writeStartObject(this, schemaPrefix)
        } else {
            handleStartObject(schemaPrefix)
        }
    }

    // note: public just because pretty printer needs to make a callback
    fun handleStartObject(schemaPrefix: String?) {
        if (_nextName == null) {
            handleMissingName()
        }
        // Need to keep track of names to make Lists work correctly
        _elementNameStack.addLast(_nextName)
        try {
            _xmlWriter.writeStartElement(_nextName.namespaceURI, "${schemaPrefix?.let{ sp->"$sp:"}?:""}${_nextName.localPart}")
        } catch (e: XMLStreamException) {
            StaxUtil.throwAsGenerationException<Any?>(e, this)
        }
    }
}