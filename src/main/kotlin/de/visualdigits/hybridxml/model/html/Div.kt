package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode

class Div(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: BaseNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf()
) : HtmlNode<Div>(
    label = "div",
    attributes = attributes,
    parent = parent,
    children = children
)
