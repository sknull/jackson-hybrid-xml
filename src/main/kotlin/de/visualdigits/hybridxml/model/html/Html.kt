package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import org.jsoup.nodes.Element

class Html(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: PolymorphicNode<*>? = null,
    children: MutableList<PolymorphicNode<*>> = mutableListOf()
) : HtmlNode<Html>(
    label = "html",
    attributes = attributes,
    parent = parent,
    children = children
) {

    companion object {

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
                    PolymorphicNode(label)
                } else {
                    null
                }
            }?.also { h ->
                h.attributes.putAll(createAttributes(element, node))
                h.withChildren(*children.toTypedArray())
                h.children.forEach { c -> c.parent = h }
                h.text = text
            }
        }

        private fun createAttributes(element: Element?, node: PolymorphicNode<*>?): MutableMap<String, String?> {
            return element
                ?.attributes()
                ?.associate { attribute -> Pair(attribute.key, attribute.value) }
                ?.toMutableMap()
                ?: node?.attributes
                ?: mutableMapOf()
        }
    }
}
