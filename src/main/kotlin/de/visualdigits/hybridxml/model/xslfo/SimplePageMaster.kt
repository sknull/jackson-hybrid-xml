package de.visualdigits.hybridxml.model.xslfo

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.namespaces.XmlSchema

@XmlSchema("fo")
class SimplePageMaster(
    @JacksonXmlProperty(isAttribute = true, localName = "master-name")
    val masterName: String? = null,

    @JacksonXmlProperty(isAttribute = true, localName = "region-body")
    val regionBody: RegionBody? = null
) : BaseNode<SimplePageMaster>()