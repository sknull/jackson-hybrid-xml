package de.visualdigits.hybridxml.module.polymorphic.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter
import org.codehaus.stax2.XMLStreamWriter2

/**
 * XML indenter which uses system-specific linefeeds and 4 spaces for indentation per level.
 */
class ConfigurableSpacesIndenter(
    private val indentAmount: Int = 2
) : DefaultXmlPrettyPrinter.Indenter {

    private val newLine = System.lineSeparator()

    override fun writeIndentation(g: JsonGenerator, level: Int) {
        g.writeRaw(newLine)
        g.writeRaw(" ".repeat(indentAmount * level))
    }

    override fun writeIndentation(sw: XMLStreamWriter2, level: Int) {
        sw.writeRaw(newLine)
        sw.writeRaw(" ".repeat(indentAmount * level))
    }

    override fun isInline(): Boolean {
        return false
    }
}
