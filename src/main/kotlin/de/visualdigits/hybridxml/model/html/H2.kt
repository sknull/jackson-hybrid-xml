package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode

class H2(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: BaseNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf()
) : HtmlNode<H2>(
    label = "h2",
    attributes = attributes,
    parent = parent,
    children = children
)
