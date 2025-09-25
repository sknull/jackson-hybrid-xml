package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.polymorphic.TagName

class Comment(
    text: String? = null,
    parent: BaseNode<*>? = null
) : HtmlNode<Comment>(
    label = TagName.COMMENT.label,
    parent = parent,
    text = text
)
