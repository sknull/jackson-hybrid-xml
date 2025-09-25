package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode

class H3(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: BaseNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf()
) : HtmlNode<H3>(
    label = "h3",
    attributes = attributes,
    parent = parent,
    children = children
)
