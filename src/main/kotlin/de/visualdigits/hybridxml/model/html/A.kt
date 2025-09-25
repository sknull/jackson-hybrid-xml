package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.polymorphic.Inline

open class A(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: BaseNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf()
) : HtmlNode<A>(
    label = "a",
    attributes = attributes,
    parent = parent,
    children = children
), Inline
