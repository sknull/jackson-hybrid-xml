package de.visualdigits.hybridxml.model.xslfo

import de.visualdigits.hybridxml.model.BaseNode
import org.junit.jupiter.api.Test
import java.io.File

class RootTest {

    @Test
    fun testReadModel() {
        val xml = File(ClassLoader.getSystemResource("namespaces/hello-world.xml").toURI()).readText()
        val root = BaseNode.readValue<Root>(xml)
        println(root.writeValueAsString())
    }
}