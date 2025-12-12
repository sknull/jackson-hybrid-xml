package de.visualdigits.hybridxml.model.xslfo

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.namespaces.XmlSchema

@XmlSchema("fo")
class Block() : BaseNode<Block>() {

    @JacksonXmlText
    lateinit var text: String

    init {
        if (!::text.isInitialized) {
            this.text = ""
        }
    }
}