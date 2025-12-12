package de.visualdigits.hybridxml.module.polymorphic.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase
import com.fasterxml.jackson.dataformat.xml.ser.XmlBeanSerializer
import de.visualdigits.hybridxml.model.html.Html.Companion.convertToDocument
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import org.jsoup.nodes.Document
import kotlin.math.max

/**
 * Dedicated serializer for polymorphic nodes which takes care on rendering the label as start object
 * or field name depending on the node type.
 */
class PolymorphicXmlNodeSerializer(
    src: BeanSerializerBase,
    private val indentAmount: Int = 2,
    private val writeHtmlDeclaration: Boolean = false
) : XmlBeanSerializer(src) {

    var needsPreprocessing: Boolean = false

    override fun serialize(
        bean: Any,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        val node = bean as PolymorphicNode<*>
        val rootElement = convertToDocument(node)
        rootElement?.also { elem ->
            val document = Document("")
            document.attr("xmlns", "http://www.w3.org/1999/xhtml")
            document.appendChild(elem)
            val outputSettings = document.outputSettings()
            outputSettings.indentAmount(indentAmount)
            outputSettings.syntax(Document.OutputSettings.Syntax.xml)
            val shouldDropParent = node.parent == null
            val indentOffset = if (shouldDropParent) 1 else 0
            val indent = " ".repeat(indentAmount * (node.level + indentOffset))
            val rawHtml = if (shouldDropParent) document.children().joinToString("\n") { elem -> elem.html() } else document.html()
            val html = rawHtml.split("\n").joinToString("\n") { line -> "$indent$line" }
                .let { h ->
                    if(writeHtmlDeclaration) {
                        h.replace("<html>", "<html xmlns=\"http://www.w3.org/1999/xhtml\">")
                    } else {
                        h
                    }
                }
            val indentAfter = " ".repeat(indentAmount * (indentOffset + max(node.level - 1, 0)))
            if (node.parent == null) {
                needsPreprocessing = true
                gen.writeString("\n$html\n$indentAfter")
            } else {
                gen.writeRaw("\n$html\n$indentAfter")
            }
        }
    }
}
