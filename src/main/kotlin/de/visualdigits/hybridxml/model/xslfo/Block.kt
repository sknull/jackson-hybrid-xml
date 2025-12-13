package de.visualdigits.hybridxml.model.xslfo

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import de.visualdigits.hybridxml.model.objectnode.ObjectNode
import de.visualdigits.hybridxml.model.namespaces.XmlSchema

@XmlSchema("fo")
class Block() : ObjectNode<Block>() {

    @JacksonXmlText
    lateinit var text: String

    init {
        if (!::text.isInitialized) {
            this.text = ""
        }
    }
}