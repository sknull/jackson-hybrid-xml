package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.polymorphic.Inline
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode

class Span(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: PolymorphicNode<*>? = null,
    children: MutableList<PolymorphicNode<*>> = mutableListOf()
) : HtmlNode<Span>(
    label = "span",
    attributes = attributes,
    parent = parent,
    children = children
), Inline
