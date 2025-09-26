package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.polymorphic.TagName

class Text(
    text: String? = null,
    parent: BaseNode<*>? = null
) : HtmlNode<Text>(
    label = TagName.TEXT.label,
    parent = parent,
    text = text
) {

    override fun toString(): String = text?:""
}
