package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode

class H3(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: PolymorphicNode<*>? = null,
    children: MutableList<PolymorphicNode<*>> = mutableListOf()
) : HtmlNode<H3>(
    label = "h3",
    attributes = attributes,
    parent = parent,
    children = children
)
