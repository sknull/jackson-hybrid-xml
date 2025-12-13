package de.visualdigits.hybridxml.module.polymorphic.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.visualdigits.hybridxml.model.html.Html
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import org.jsoup.nodes.Document

/**
 * Dedicated serializer for polymorphic nodes which takes care on rendering the label as start object
 * or field name depending on the node type.
 */
class PolymorphicJsonNodeSerializer(
    private val dropRootNode: Boolean = false
) : StdSerializer<PolymorphicNode<*>>(PolymorphicNode::class.java) {

    override fun serialize(node: PolymorphicNode<*>, gen: JsonGenerator, provider: SerializerProvider) {
        val polymorphicNode = if (dropRootNode) node.children.first() else node
        val rootElement = Html.convertToDocument(polymorphicNode)
        rootElement?.also { elem ->
            val document = Document("")
            document.appendChild(elem)
            val outputSettings = document.outputSettings()
            outputSettings.prettyPrint(false)
            outputSettings.syntax(Document.OutputSettings.Syntax.xml)
            val html = document.html()
            gen.writeString(html)
        }
    }
}