package de.visualdigits.hybridxml.module.hybrid

import de.visualdigits.hybridxml.model.html.B
import de.visualdigits.hybridxml.model.html.Body
import de.visualdigits.hybridxml.model.html.CData
import de.visualdigits.hybridxml.model.html.Comment
import de.visualdigits.hybridxml.model.html.Div
import de.visualdigits.hybridxml.model.html.Head
import de.visualdigits.hybridxml.model.html.Html
import de.visualdigits.hybridxml.model.html.Li
import de.visualdigits.hybridxml.model.html.Text
import de.visualdigits.hybridxml.model.html.Title
import de.visualdigits.hybridxml.module.hybrid.model.Demo
import de.visualdigits.hybridxml.module.hybrid.model.Description
import de.visualdigits.hybridxml.module.hybrid.model.SubDemo
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
            description = Description(
                Html(
                    children = mutableListOf(
                        Head(
                            children = mutableListOf(
                                Title(children = mutableListOf(Text(text = "Hello World!")))
                            )
                        ),
                        Body(
                            children = mutableListOf(
                                Comment(text = "this is a comment"),
                                Div(
                                    attributes = mutableMapOf(
                                        "id" to "the-div",
                                        "class" to "foo"
                                    ),
                                    children = mutableListOf(Text(text = "Hello World!"))
                                )
                            )
                        ),
                        CData(text = "data data and mor data")
                    )
                )
            )
        )
    )

    private val subDemo = demo.subDemo!!
    private val description = subDemo.description!!
    private val html = description.html!!
    private val body1 = html.firstChild<Body>()!!
    private val body2 = html.lastChild<Body>()!!
    private val head = body1.previousSibling()!!
    private val cdata = body1.nextSibling()!!

    @Test
    fun testBasicManipulations() {
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
    }

    @Test
    fun testWriteXml() {
        val expected = File(ClassLoader.getSystemResource("hybridxml/expected_xml.txt").toURI()).readText()
        val actual = demo.writeValueAsString()
        assertEquals(expected, actual)
    }

    @Test
    fun testClone() {
        val expected = File(ClassLoader.getSystemResource("hybridxml/expected_html.txt").toURI()).readText()
        val clone = description.clone()
        val actual = clone.writeValueAsString()
        assertEquals(expected, actual)
    }

    @Test
    fun testWriteHtml() {
        val expected = File(ClassLoader.getSystemResource("hybridxml/expected_html.txt").toURI()).readText()
        val actual = html.writeValueAsString()
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

