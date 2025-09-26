package de.visualdigits.hybridxml.module.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import de.visualdigits.hybridxml.model.polymorphic.TagName
import org.jsoup.nodes.CDataNode
import org.jsoup.nodes.Comment
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import kotlin.math.max

/**
 * Dedicated serializer for polymorphic nodes which takes care on rendering the label as start object
 * or field name depending on the node type.
 */
class PolymorphicXmlNodeSerializer(
    private val indentAmount: Int = 2,
    private val writeHtmlDeclaration: Boolean = false
) : StdSerializer<PolymorphicNode<*>>(PolymorphicNode::class.java) {

    override fun serialize(node: PolymorphicNode<*>, gen: JsonGenerator, provider: SerializerProvider) {
        val rootElement = convertToDocument(node)
        rootElement?.also { elem ->
            val document = Document("")
            document.attr("xmlns", "http://www.w3.org/1999/xhtml")
            document.appendChild(elem)
            val outputSettings = document.outputSettings()
            outputSettings.indentAmount(indentAmount)
            outputSettings.syntax(Document.OutputSettings.Syntax.xml)
            val indent = " ".repeat(indentAmount * node.level)
            val html = document.html()
                .split("\n").joinToString("\n") { line -> "$indent$line" }
                .let { h ->
                    if(writeHtmlDeclaration) {
                        h.replace("<html>", "<html xmlns=\"http://www.w3.org/1999/xhtml\">")
                    } else {
                        h
                    }
                }
            val indentAfter = " ".repeat(indentAmount * (max(node.level - 1, 0)))
            gen.writeRaw("\n$html\n$indentAfter")
        }
    }

    /**
     * Converts this node tree back to a jsoup tree
     */
    private fun convertToDocument(node: PolymorphicNode<*>): Node? {

        // first dive into the tree to make sure we have processed all children for the given node
        val children = node.children.mapNotNull { child -> convertToDocument(child as PolymorphicNode) }

        // now process the given node using the children obtained above - this is the branch up from the recursion
        return when (node.label) {
            TagName.CDATA.label -> node.text?.let{ t -> CDataNode(t) }
            TagName.TEXT.label -> node.text?.let{ t -> TextNode(t) }
            TagName.COMMENT.label -> node.text?.let{ t -> Comment(t) }
            else -> Element(node.label).appendChildren(children)
        }?.also { elem ->
            node.attributes.forEach { (k, v) -> elem.attr(k, v?:"") }
        }
    }
}
