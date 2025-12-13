package de.visualdigits.hybridxml.model.xslfo

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import de.visualdigits.hybridxml.model.objectnode.ObjectNode
import de.visualdigits.hybridxml.model.namespaces.XmlNamespace
import de.visualdigits.hybridxml.model.namespaces.XmlNamespaces
import de.visualdigits.hybridxml.model.namespaces.XmlSchema

@XmlNamespaces([XmlNamespace("fo", "http://www.w3.org/1999/XSL/Format")])
@XmlSchema("fo")
class Root(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "layout-master-set")
    val layoutMasterSet: List<LayoutMasterSet> = listOf(),

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "page-sequence")
    val pageSequence: List<PageSequence> = listOf()
) : ObjectNode<Root>()