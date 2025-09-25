package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode

class Ul(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: BaseNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf()
) : HtmlNode<Ul>(
    label = "ul",
    attributes = attributes,
    parent = parent,
    children = children
)
