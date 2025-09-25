package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode

class Meta(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: BaseNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf()
) : HtmlNode<Meta>(
    label = "meta",
    attributes = attributes,
    parent = parent,
    children = children
)
