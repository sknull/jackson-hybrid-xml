package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.polymorphic.Inline
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode

class U(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: PolymorphicNode<*>? = null,
    children: MutableList<PolymorphicNode<*>> = mutableListOf()
) : HtmlNode<U>(
    label = "u",
    attributes = attributes,
    parent = parent,
    children = children
), Inline
