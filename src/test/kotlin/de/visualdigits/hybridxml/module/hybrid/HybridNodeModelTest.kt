package de.visualdigits.hybridxml.module.hybrid

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.html.B
import de.visualdigits.hybridxml.model.html.Body
import de.visualdigits.hybridxml.model.html.CData
import de.visualdigits.hybridxml.model.html.Comment
import de.visualdigits.hybridxml.model.html.Div
import de.visualdigits.hybridxml.model.html.Head
import de.visualdigits.hybridxml.model.html.Html
import de.visualdigits.hybridxml.model.html.Html.Companion.createHtmlNode
import de.visualdigits.hybridxml.model.html.Li
import de.visualdigits.hybridxml.model.html.Text
import de.visualdigits.hybridxml.model.html.Title
import de.visualdigits.hybridxml.model.hybrid.HybridRootNode
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import org.jsoup.nodes.Element
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class HybridNodeModelTest {

    private val demo = Demo(
        name = "Hello World!",
        subDemo = SubDemo(
            foo = "foo",
            bar = "bar",
            baz = "baz",
            description = Description(Html(children = mutableListOf(
                Head(children = mutableListOf(
                    Title(children = mutableListOf(Text(text = "Hello World!")))
                )),
                Body(children = mutableListOf(
                    Comment(text = "this is a comment"),
                    Div(attributes = mutableMapOf(
                        "id" to "the-div",
                        "class" to "foo"
                    ),
                        children = mutableListOf(Text(text = "Hello World!")))
                )),
                CData(text = "data data and mor data")
            )))
        )
    )

    @Test
    fun testWriteXml() {
        val subDemo = demo.subDemo!!
        val description = subDemo.description!!
        val html = description.html!!
        val body1 = html.firstChild<Body>()!!
        val body2 = html.lastChild<Body>()!!
        val head = body1.previousSibling()!!
        val cdata = body1.nextSibling()!!

        assertEquals("foo", subDemo.foo)
        assertEquals("bar", subDemo.bar)
        assertEquals("baz", subDemo.baz)

        assertEquals(listOf(demo, subDemo, description), description.rootLine())
        assertEquals(listOf(head, cdata), body1.siblings())
        assertEquals(1, body1.indexOfInParent())
        assertEquals(demo, description.rootNode())

        assertTrue(subDemo.isChildOf(demo))
        assertFalse(description.isChildOf(demo))

        assertTrue(description.isInRootlineOf(demo))

        assertFalse(description.isInRootlineOf(head))

        assertEquals(body1, body2)

        assertNotNull(html.firstChild<Head>())
        assertNull(html.firstChild<B>())

        assertNotNull(html.firstChild<CData>())
        assertNull(html.firstChild<Li>())

        assertEquals(Head::class.java, head.javaClass)
        assertEquals(CData::class.java, cdata.javaClass)

        assertTrue(head.isFirstChild())
        assertFalse(body1.isFirstChild())

        assertTrue(cdata.isLastChild())
        assertFalse(body1.isLastChild())

        assertTrue(head.hasChildren())
        assertFalse(cdata.hasChildren())

        assertTrue(head.hasSiblings())
        assertFalse(demo.hasSiblings())

        val expected = File(ClassLoader.getSystemResource("hybridxml/expected_xml.txt").toURI()).readText()
        val actual = demo.writeValueAsString()
        assertEquals(expected, actual)
    }

    /**
     * Tests that we can read xml and serialize it back to either xml and json.
     */
    @Test
    fun testReadXml() {
        val expectedXml = File(ClassLoader.getSystemResource("hybridxml/expected_xml.txt").toURI()).readText()
        val expectedJson = File(ClassLoader.getSystemResource("hybridxml/expected_json.txt").toURI()).readText()
        val demo = Demo.readValue(expectedXml)

        val actualXml = demo.writeValueAsString()
        assertEquals(expectedXml, actualXml)

        val actualJson = demo.writeValueAsJsonString()
        assertEquals(expectedJson, actualJson)
    }

    /**
     * Tests that we can read json and serialize it back to either json and xml.
     */
    @Test
    fun testReadJson() {
        val expectedXml = File(ClassLoader.getSystemResource("hybridxml/expected_xml.txt").toURI()).readText()
        val expectedJson = File(ClassLoader.getSystemResource("hybridxml/expected_json.txt").toURI()).readText()
        val demo = Demo.readJsonValue(expectedJson)

        val actualJson = demo.writeValueAsJsonString()
        assertEquals(expectedJson, actualJson)

        val actualXml = demo.writeValueAsString()
        assertEquals(expectedXml, actualXml)
    }
}

@JacksonXmlRootElement(localName = "demo")
class Demo(
    @field:JacksonXmlProperty(isAttribute = true) val name: String? = null,
    val subDemo: SubDemo? = null
) : HybridRootNode<Demo>() {

    init {
        indent() // do not do this when your tree is read with the deserializers as this would be to early
                 // leading to improper indenting.
    }

    companion object {

        fun readValue(rawXml: String): Demo {
            return readValue<Demo>(rawXml) { label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String? ->
                createHtmlNode(label = label, element = element, node = node, children = children.toMutableList(), text = text)
            }
        }

        fun readJsonValue(rawXml: String): Demo {
            return readJsonValue<Demo>(rawXml) { label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String? ->
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
