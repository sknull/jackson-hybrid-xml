package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode

class H2(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: PolymorphicNode<*>? = null,
    children: MutableList<PolymorphicNode<*>> = mutableListOf()
) : HtmlNode<H2>(
    label = "h2",
    attributes = attributes,
    parent = parent,
    children = children
)
