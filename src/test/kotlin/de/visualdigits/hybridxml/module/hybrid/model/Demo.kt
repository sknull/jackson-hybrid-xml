package de.visualdigits.hybridxml.module.hybrid.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import de.visualdigits.hybridxml.model.objectnode.ObjectNode
import de.visualdigits.hybridxml.model.html.Html
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import de.visualdigits.hybridxml.module.common.JacksonHelper.readJsonValue
import de.visualdigits.hybridxml.module.common.JacksonHelper.readXmlValue
import org.jsoup.nodes.Element

@JacksonXmlRootElement(localName = "demo")
class Demo(
    @field:JacksonXmlProperty(isAttribute = true) val name: String? = null,
    val subDemo: SubDemo? = null
) : ObjectNode<Demo>() {

    init {
        indent() // do not do this when your tree is read with the deserializers as this would be to early
                 // leading to improper indenting.
    }

    companion object {

        fun readXmlValue(rawXml: String): Demo {
            return readXmlValue<Demo>(rawXml) { label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String? ->
                Html.createHtmlNode(
                    label = label,
                    element = element,
                    node = node,
                    children = children.toMutableList(),
                    text = text
                )
            }
        }

        fun readJsonValue(rawXml: String): Demo {
            return readJsonValue<Demo>(rawXml) { label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String? ->
                Html.createHtmlNode(
                    label = label,
                    element = element,
                    node = node,
                    children = children.toMutableList(),
                    text = text
                )
            }
        }
    }
}