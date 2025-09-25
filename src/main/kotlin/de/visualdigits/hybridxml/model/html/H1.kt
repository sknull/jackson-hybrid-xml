package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode

class H1(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: PolymorphicNode<*>? = null,
    children: MutableList<PolymorphicNode<*>> = mutableListOf()
) : HtmlNode<H1>(
    label = "h1",
    attributes = attributes,
    parent = parent,
    children = children
)
