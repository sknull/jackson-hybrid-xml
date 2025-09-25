package de.visualdigits.hybridxml.model.hybrid

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import de.visualdigits.hybridxml.model.BaseNode

/**
 * Attribute of a bean which is represented by a sub tag containing a text.
 */
abstract class HybridTextNode : BaseNode() {

    @JacksonXmlText
    lateinit var text: String

    init {
        if (!::text.isInitialized) {
            this.text = ""
        }
    }
}

