package de.visualdigits.hybridxml.model.rss

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import de.visualdigits.hybridxml.model.html.Html.Companion.createHtmlNode
import de.visualdigits.hybridxml.model.hybrid.HybridRootNode
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import de.visualdigits.hybridxml.module.deserializer.PolymorphicNodeDeserializer.Companion.parseHtml
import org.jsoup.nodes.Element

class Rss(
    @field:JacksonXmlProperty(isAttribute = true) val version: String? = null,
    val channel: Channel? = null,
    val about: String? = null,
    val image: Image? = null,
    val items: List<Item> = listOf()
) : HybridRootNode<Rss>() {

    override fun postProcessXml() {
        channel?.items?.forEach { item ->
            item.encoded?.text?.also { html ->
                val tree = parseHtml(html = html, rootNodeName = "html") { label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String? ->
                    createHtmlNode(label, element = element, node = node, children = children.toMutableList(), text = text)
                }
                item.content = tree?.let { html -> HtmlContent(html) }
                ?.also { item.encoded = null } // ensure that we only null out when we could parse successfully
            }
        }
    }
}
