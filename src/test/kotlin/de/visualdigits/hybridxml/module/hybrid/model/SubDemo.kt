package de.visualdigits.hybridxml.module.hybrid.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import de.visualdigits.hybridxml.model.BaseNode

class SubDemo(
    @field:JacksonXmlProperty(isAttribute = true) val foo: String? = null,
    @field:JacksonXmlProperty(isAttribute = true) val bar: String? = null,
    @field:JacksonXmlProperty(isAttribute = true) val baz: String? = null,
    val description: Description? = null
) : BaseNode<SubDemo>()