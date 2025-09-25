package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.polymorphic.Inline
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode

class B(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: PolymorphicNode<*>? = null,
    children: MutableList<PolymorphicNode<*>> = mutableListOf()
) : HtmlNode<B>(
    label = "b",
    attributes = attributes,
    parent = parent,
    children = children
), Inline
