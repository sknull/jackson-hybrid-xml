package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode

class Br(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: BaseNode<*>? = null
) : HtmlNode<Br>(
    label = "br",
    attributes = attributes,
    parent = parent
)
