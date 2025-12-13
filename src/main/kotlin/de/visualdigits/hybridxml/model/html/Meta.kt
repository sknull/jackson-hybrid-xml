package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode

class Meta(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: PolymorphicNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf()
) : HtmlNode<Meta>(
    label = "meta",
    attributes = attributes,
    parent = parent,
    children = children
)
