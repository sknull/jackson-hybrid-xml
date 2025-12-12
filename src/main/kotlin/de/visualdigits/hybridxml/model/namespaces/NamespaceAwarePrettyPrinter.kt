package de.visualdigits.hybridxml.model.namespaces

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter

class NamespaceAwarePrettyPrinter(): DefaultXmlPrettyPrinter() {

    fun writeStartObject(gen: JsonGenerator, schemaPrefix: String?) {
        if (!_objectIndenter.isInline) {
            if (_nesting > 0) {
                _objectIndenter.writeIndentation(gen, _nesting)
            }
            ++_nesting
        }
        _justHadStartElement = true
        (gen as ToXmlGenerator).handleStartObject(schemaPrefix)
    }
}