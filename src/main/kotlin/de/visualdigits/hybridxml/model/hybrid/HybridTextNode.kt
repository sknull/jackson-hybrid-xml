package de.visualdigits.hybridxml.model.hybrid

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import de.visualdigits.hybridxml.model.objectnode.ObjectNode

/**
 * Attribute of a bean which is represented by a sub tag containing a text.
 */
abstract class HybridTextNode : ObjectNode<HybridTextNode>() {

    @JacksonXmlText
    lateinit var text: String

    init {
        if (!::text.isInitialized) {
            this.text = ""
        }
    }
}

