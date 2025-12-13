package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import de.visualdigits.hybridxml.model.polymorphic.TagName
import org.jsoup.Jsoup
import org.jsoup.nodes.*
import org.jsoup.nodes.Comment
import org.jsoup.parser.Parser

class Html(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: PolymorphicNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf()
) : HtmlNode<Html>(
    label = "html",
    attributes = attributes,
    parent = parent,
    children = children
) {

    companion object {

        /**
         * Converts this node tree back to a jsoup tree
         */
        fun convertToDocument(node: PolymorphicNode<*>): Node? {

            // first dive into the tree to make sure we have processed all children for the given node
            val children = node.children.mapNotNull { child -> convertToDocument(child) }

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
                    }?:createPolymorphicNode(label, jsoupNode, children = children.toMutableList())
                }

                is CDataNode -> { // DO NOT MOVE DOWN - CDataNode is a subclass of TextNode
                    val text = jsoupNode.text()
                    if (text.isNotBlank()) {
                        PolymorphicNode(label = TagName.CDATA.label, text = text.trimLineBreaks())
                    } else {
                        null
                    }
                }

                is TextNode -> {
                    val text = jsoupNode.text()
                    if (text.isNotBlank()) {
                        PolymorphicNode(label = TagName.TEXT.label, text = text.trimLineBreaks())
                    } else {
                        null
                    }
                }

                is Comment -> {
                    val text = jsoupNode.data
                    if (text.isNotBlank()) {
                        PolymorphicNode(label = TagName.COMMENT.label, text = text.trimLineBreaks())
                    } else {
                        null
                    }
                }

                else -> {
                    // jsoup also knows about other nodes but for now I keep it simple
                    error("Unsupported node type '${jsoupNode::class}'")
                }
            }
        }

        /**
         * Default node generator method which creates a PolymorphicNode having the desired label.
         */
        fun createPolymorphicNode(
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

        /**
         * Convenience replacement for the default polymorphic node generator method
         * which creates concrete HTML objects.
         */
        fun createHtmlNode(
            label: String? = null,
            element: Element? = null,
            node: PolymorphicNode<*>? = null,
            children: MutableList<PolymorphicNode<*>> = mutableListOf(),
            text: String? = null
        ): PolymorphicNode<*>? {
            val label = label?:node?.label?:""
            return when (label.lowercase()) {
                "a" -> A()
                "b" -> B()
                "body" -> Body()
                "br" -> Br()
                "div" -> Div()
                "h1" -> H1()
                "h2" -> H2()
                "h3" -> H3()
                "head" -> Head()
                "html" -> Html()
                "i" -> I()
                "li" -> Li()
                "meta" -> Meta()
                "ol" -> Ol()
                "p" -> P()
                "pre" -> Pre()
                "span" -> Span()
                "table" -> Table()
                "title" -> Title()
                "td" -> Td()
                "tr" -> Tr()
                "u" -> U()
                "ul" -> Ul()
                else -> if (label.isNotBlank()) {
                    HtmlNode(label)
                } else {
                    null
                }
            }?.also { h ->
                val attributes = element
                    ?.attributes()
                    ?.associate { attribute -> Pair(attribute.key, attribute.value) }
                    ?.toMutableMap()
                    ?: node?.attributes?.toMutableMap()
                    ?: mutableMapOf()
                h.attributes.putAll(attributes)
                h.withChildren(*children.toTypedArray())
                h.children.forEach { c -> c.parent = h }
                h.text = text
            }
        }

        private fun String.trimLineBreaks(): String {
            return this
                .replace("\r\n", "")
                .replace("\r", "")
                .replace("\n", "")
                .replace("\\r\\n", "")
                .replace("\\r", "")
                .replace("\\n", "")
        }
    }
}
