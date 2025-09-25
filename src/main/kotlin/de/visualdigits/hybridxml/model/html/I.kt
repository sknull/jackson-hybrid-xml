package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.polymorphic.Inline

class I(
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: BaseNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf()
) : HtmlNode<I>(
    label = "i",
    attributes = attributes,
    parent = parent,
    children = children
), Inline
