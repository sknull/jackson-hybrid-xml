package de.visualdigits.hybridxml.module.hybrid.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode

class Description(
    @field:JacksonXmlProperty(localName = "html") val html: PolymorphicNode<*>? = null
) : BaseNode<Description>() {

    override fun clone(): Description {
        return Description(
            html = html?.clone()
        )
    }
}