package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode

/**
 * Base class for HTML like polymorphic nodes.
 */
open class HtmlNode<T : HtmlNode<T>>(
    label: String,
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: PolymorphicNode<*>? = null,
    children: MutableList<PolymorphicNode<*>> = mutableListOf()
) : PolymorphicNode<T>(
    label, attributes, parent, children
)
