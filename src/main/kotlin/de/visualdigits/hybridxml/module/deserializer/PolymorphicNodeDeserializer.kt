package de.visualdigits.hybridxml.module.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import de.visualdigits.hybridxml.model.polymorphic.TagName
import org.jsoup.Jsoup
import org.jsoup.nodes.CDataNode
import org.jsoup.nodes.Comment
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Parser

/**
 * Jackson deserializer which can deserialie hybrid xml which consists of
 * predefined beans and also contains embedded HTML markup which is not
 * provided as CData.
 *
 * As an example for formats doing this I mention News ML G2 which is a format
 * widely used within the news industry.
 *
 * Problem with this kind of XML is that jackson is under the hood still just a
 * json library and also the jackson-xml module does not know to differentiate between attributes
 * and sub objects. Also it is not possible with jackson-xml to serialize the polymorphic nodes
 * back properly rendering the label as the tag name (there is still no strategy for that within the JsonTypeInfo annotaion).
 *
 * So the idea was to use somehow jsoup to parse the polymorphic part.
 * Another problem with this is that a deserializer has no access to the original xml as it a stream reading api.
 * To work around this we have to inject the raw xml in the deserializer and calculate the snippet to parse with jsoup.
 * As the deserializer context has a mechanism which provides the current location within the raw xml we can use this
 * and take a snapshot before and a second snapshot after consuming the respective subtree utilizing the contexts readTree() method.
 * The calculated snippet obtained from the raw xml can then parsed with jsoup and we end up with a dom like node tree which
 * we can transform afterwards into a desired html node structure.
 *
 * To serialize this structure back we need to make use of dedicated serializers which take care on rendering the label as start object or field name
 * depending on the node type.
 */
class PolymorphicNodeDeserializer(
    private val rawXml: String,
    private val createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
) : JsonDeserializer<PolymorphicNode<*>>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PolymorphicNode<*>? {
        // find the last opening char in the part of the raw xml up to the current location
        // which marks the beginning of the snipet to parse with jsoup
        val start = rawXml.take(p.currentLocation().charOffset.toInt()).indexOfLast { c -> c == '<' }

        // obtain the label of the root node which is the current name
        val rootNodeName = p.currentName()

        ctxt.readTree(p) // Just consume the complete subtree for this node to obtain parser location.
                         // With this we also avoid jackson having access to this part of the tree
                         // as we want to process it ourselves using jsoup.

        // the end location is just the current location after having consumed the subtree
        val end = p.currentLocation().charOffset.toInt()

        // obtain snippet from given raw xml
        val snippet = rawXml.substring(start, end)

        // now transform jsoups dom tree to the desired html node hierarchy recursively and return the root node
        return parseHtml(snippet, rootNodeName, createNodeFunction)
    }

    companion object {

        fun parseHtml(
            html: String,
            rootNodeName: String? = null,
            createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
        ): PolymorphicNode<*>? {
            // parse by jsoup using the xml parser as we can assume we deal with xhtml
            val jsoupNode = Jsoup.parse(html, "", Parser.xmlParser())
            return parseHtml(jsoupNode, rootNodeName, createNodeFunction)
        }

        private fun parseHtml(
            jsoupNode: Node,
            rootNodeName: String? = null,
            createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
        ): PolymorphicNode<*>? {
            // first dive into the tree to make sure we have processed all children for the given node
            val children = jsoupNode.childNodes().mapNotNull { child -> parseHtml(child) }

            // now process the given node using the children obtained above - this is the branch up from the recursion
            return when (jsoupNode) {
                is Element -> {
                    var label = jsoupNode.tag().name.trimLineBreaks()
                    // jsoup gives back the root node with special label, so replace it with the given rootNodeName (if any)
                    if (label == "#root") {
                        label = rootNodeName?:label
                    }
                    createNodeFunction?.let { createNode ->
                        createNode(label, jsoupNode, null, children, null)
                    }?:createNode(label, jsoupNode, children = children.toMutableList())
                }

                is CDataNode -> { // DO NOT MOVE DOWN - CDataNode is a subclass of TextNode
                    PolymorphicNode(label = TagName.CDATA.label, text = jsoupNode.text())
                }

                is TextNode -> {
                    PolymorphicNode(label = TagName.TEXT.label, text = jsoupNode.text())
                }

                is Comment -> {
                    PolymorphicNode(label = TagName.COMMENT.label, text = jsoupNode.data)
                }

                else -> {
                    // jsoup also knows about other nodes but for now I keep it simple
                    error("Unsupported node type '${jsoupNode::class}'")
                }
            }
        }

        fun createNode(
            label: String? = null,
            element: Element? = null,
            node: PolymorphicNode<*>? = null,
            children: MutableList<PolymorphicNode<*>> = mutableListOf(),
            text: String? = null
        ): PolymorphicNode<*> {
            return PolymorphicNode(
                label = label?:error("No label given"),
                attributes = element?.attributes()
                    ?.associate { attribute -> Pair(attribute.key, attribute.value) }
                    ?.toMutableMap()
                    ?:node?.attributes
                        ?.toMutableMap()
                    ?:mutableMapOf(),
                children = children,
                text = text
            ).also { h -> h.children.forEach { c -> c.parent = h } }
        }

        private fun String.trimLineBreaks(): String {
            return this
                .replace("\r", "")
                .replace("\n", "")
                .replace("\r\n", "")
                .replace("\\r", "")
                .replace("\\n", "")
                .replace("\\r\\n", "")
                .trim()
        }
    }
}
