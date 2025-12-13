package de.visualdigits.hybridxml.model.xslfo

import de.visualdigits.hybridxml.module.common.JacksonHelper.readXmlValue
import org.junit.jupiter.api.Test
import java.io.File

class RootTest {

    @Test
    fun testReadModel() {
        val xml = File(ClassLoader.getSystemResource("namespaces/hello-world.xml").toURI()).readText()
        val root = readXmlValue<Root>(xml)
        println(root.writeXmlValue())
    }
}