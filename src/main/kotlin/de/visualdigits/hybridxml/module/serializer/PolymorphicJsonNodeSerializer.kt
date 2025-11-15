package de.visualdigits.hybridxml.module.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.visualdigits.hybridxml.model.html.Html.Companion.convertToDocument
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import de.visualdigits.hybridxml.model.polymorphic.TagName
import org.jsoup.nodes.CDataNode
import org.jsoup.nodes.Comment
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/**
 * Dedicated serializer for polymorphic nodes which takes care on rendering the label as start object
 * or field name depending on the node type.
 */
class PolymorphicJsonNodeSerializer(
    private val dropRootNode: Boolean = false
) : StdSerializer<PolymorphicNode<*>>(PolymorphicNode::class.java) {

    override fun serialize(node: PolymorphicNode<*>, gen: JsonGenerator, provider: SerializerProvider) {
        val polymorphicNode = (if (dropRootNode) node.children.first() else node) as PolymorphicNode<*>
        val rootElement = convertToDocument(polymorphicNode)
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
