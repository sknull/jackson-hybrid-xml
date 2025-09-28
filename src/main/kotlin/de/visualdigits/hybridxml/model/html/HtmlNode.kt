package de.visualdigits.hybridxml.model.html

import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode

/**
 * Base class for HTML like polymorphic nodes.
 */
@Suppress("UNCHECKED_CAST")
open class HtmlNode<T : HtmlNode<T>>(
    label: String,
    attributes: MutableMap<String, String?> = mutableMapOf(),
    parent: BaseNode<*>? = null,
    children: MutableList<HtmlNode<*>> = mutableListOf(),
    text: String? = null
) : PolymorphicNode<T>(
    label = label,
    attributes = attributes,
    parent = parent,
    children = children as MutableList<PolymorphicNode<*>>,
    text = text
) {

    /**
     * Returns a deep copy of this polymorphic node.
     */
    override fun clone(
    ): T {
        val clonedChildren = this.children.map { c -> c.clone() }
        return HtmlNode(
            label = label,
            attributes = attributes.toMutableMap(),
            text = text
        ).withChildren(*clonedChildren.toTypedArray<BaseNode<*>>()) as T
    }

}
