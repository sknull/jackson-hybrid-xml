package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import de.visualdigits.hybridxml.model.polymorphic.TagName

class Comment(
    text: String? = null,
    parent: PolymorphicNode<*>? = null
) : HtmlNode<Comment>(
    label = TagName.COMMENT.label,
    parent = parent,
    text = text
) {

    override fun toString(): String = "<!--$text-->"
}

