package de.visualdigits.hybridxml.model.xslfo

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import de.visualdigits.hybridxml.model.objectnode.ObjectNode
import de.visualdigits.hybridxml.model.namespaces.XmlSchema

@XmlSchema("fo")
class PageSequence(
    @JacksonXmlProperty(isAttribute = true, localName = "master-reference")
    val masterReference: String? = null,

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "flow")
    val flows: List<Flow> = listOf(),
) : ObjectNode<PageSequence>()