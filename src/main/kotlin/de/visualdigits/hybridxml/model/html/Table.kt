package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode

class Table(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: BaseNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf()
) : HtmlNode<Table>(
    label = "table",
    attributes = attributes,
    parent = parent,
    children = children
)
