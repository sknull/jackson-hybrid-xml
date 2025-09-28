package de.visualdigits.hybridxml.module.rss

import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.rss.Rss
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class RssModelTest {

    @Test
    fun readXmlTest() {
        val rss = BaseNode.readValue<Rss>(File(ClassLoader.getSystemResource("rdf/heise.xml").toURI()))
        val actual = rss.writeValueAsString()
        val expected = File(ClassLoader.getSystemResource("rdf/heise_expected.xml.txt").toURI()).readText()
        assertEquals(expected, actual)
    }
}
