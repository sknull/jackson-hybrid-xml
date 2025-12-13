package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import de.visualdigits.hybridxml.model.polymorphic.Inline

class U(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: PolymorphicNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf()
) : HtmlNode<U>(
    label = "u",
    attributes = attributes,
    parent = parent,
    children = children
), Inline
