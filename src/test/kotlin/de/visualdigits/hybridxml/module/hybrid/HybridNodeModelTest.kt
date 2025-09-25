package de.visualdigits.hybridxml.module.hybrid

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.html.Body
import de.visualdigits.hybridxml.model.html.Div
import de.visualdigits.hybridxml.model.html.Head
import de.visualdigits.hybridxml.model.html.Html
import de.visualdigits.hybridxml.model.html.Html.Companion.createHtmlNode
import de.visualdigits.hybridxml.model.html.Title
import de.visualdigits.hybridxml.model.hybrid.HybridRootNode
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import de.visualdigits.hybridxml.model.polymorphic.text.PolymorphicCDataNode
import de.visualdigits.hybridxml.model.polymorphic.text.PolymorphicCommentNode
import de.visualdigits.hybridxml.model.polymorphic.text.PolymorphicTextNode
import org.jsoup.nodes.Element
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class HybridNodeModelTest {

    @Test
    fun testWriteXml() {
        val demo = Demo(
            name = "Hello World!",
            subDemo = SubDemo(
                foo = "foo",
                bar = "bar",
                baz = "baz",
                description = Description(Html(children = mutableListOf(
                    Head(children = mutableListOf(
                        Title(children = mutableListOf(PolymorphicTextNode(text = "Hello World!")))
                        )),
                        Body(children = mutableListOf(
                            PolymorphicCommentNode(text = "this is a comment"),
                            Div(attributes = mutableMapOf(
                                    "id" to "the-div",
                                    "class" to "foo"
                                ),
                                children = mutableListOf(PolymorphicTextNode(text = "Hello World!")))
                        )),
                    PolymorphicCDataNode(text = "data data and mor data")
                    ))
                )
            )
        )

        val expected = File(ClassLoader.getSystemResource("hybridxml/expected_html.txt").toURI()).readText()
        val actual = demo.writeValueAsString()
        assertEquals(expected, actual)
    }

    @Test
    fun testReadXml() {
        val expected = File(ClassLoader.getSystemResource("hybridxml/expected_html.txt").toURI()).readText()
        val demo = Demo.readValue(expected)
        val actual = demo.writeValueAsString()
        assertEquals(expected, actual)
    }
}

@JacksonXmlRootElement(localName = "demo")
class Demo(
    @field:JacksonXmlProperty(isAttribute = true) val name: String? = null,
    val subDemo: SubDemo? = null
) : HybridRootNode<Demo>() {

    companion object {

        fun readValue(rawXml: String): Demo {
            return readValue<Demo>(rawXml) { label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String? ->
                createHtmlNode(label = label, element = element, node = node, children = children.toMutableList(), text = text)
            }
        }
    }
}

class Description(
    @field:JacksonXmlProperty(localName = "html") val html: PolymorphicNode<*>? = null
) : BaseNode<Description>()

class SubDemo(
    @field:JacksonXmlProperty(isAttribute = true) val foo: String? = null,
    @field:JacksonXmlProperty(isAttribute = true) val bar: String? = null,
    @field:JacksonXmlProperty(isAttribute = true) val baz: String? = null,
    val description: Description? = null
) : BaseNode<SubDemo>()
