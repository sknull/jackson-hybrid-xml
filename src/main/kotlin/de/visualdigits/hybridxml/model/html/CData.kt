package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.polymorphic.TagName

class CData(
    text: String? = null,
    parent: BaseNode<*>? = null
) : HtmlNode<CData>(
    label = TagName.CDATA.label,
    parent = parent,
    text = text
) {

    override fun toString(): String = "<![CDATA[$text]]>"
}

