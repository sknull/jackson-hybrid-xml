package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode

class Head(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: PolymorphicNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf()
) : HtmlNode<Head>(
    label = "head",
    attributes = attributes,
    parent = parent,
    children = children
)
