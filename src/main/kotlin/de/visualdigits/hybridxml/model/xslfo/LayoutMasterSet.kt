package de.visualdigits.hybridxml.model.xslfo

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.namespaces.XmlSchema

@XmlSchema("fo")
class LayoutMasterSet(
    @JacksonXmlProperty(localName = "simple-page-master")
    val simplePageMaster: SimplePageMaster? = null
) : BaseNode<LayoutMasterSet>()