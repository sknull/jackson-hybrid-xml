package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode

class P(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: BaseNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf()
) : HtmlNode<P>(
    label = "p",
    attributes = attributes,
    parent = parent,
    children = children
)
