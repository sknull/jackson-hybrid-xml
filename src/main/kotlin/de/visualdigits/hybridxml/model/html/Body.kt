package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode

class Body(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: BaseNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf()
) : HtmlNode<Body>(
    label = "body",
    attributes = attributes,
    parent = parent,
    children = children
)
