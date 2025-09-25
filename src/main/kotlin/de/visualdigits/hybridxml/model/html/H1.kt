package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode

class H1(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: BaseNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf()
) : HtmlNode<H1>(
    label = "h1",
    attributes = attributes,
    parent = parent,
    children = children
)
