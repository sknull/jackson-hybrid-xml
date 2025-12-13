package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import de.visualdigits.hybridxml.model.polymorphic.TagName

class Text(
    text: String? = null,
    parent: PolymorphicNode<*>? = null
) : HtmlNode<Text>(
    label = TagName.TEXT.label,
    parent = parent,
    text = text
) {

    override fun toString(): String = text?:""
}
