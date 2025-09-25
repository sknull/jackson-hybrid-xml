package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.polymorphic.Inline
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode

open class A(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: PolymorphicNode<*>? = null,
    children: MutableList<PolymorphicNode<*>> = mutableListOf()
) : HtmlNode<A>(
    label = "a",
    attributes = attributes,
    parent = parent,
    children = children
), Inline
