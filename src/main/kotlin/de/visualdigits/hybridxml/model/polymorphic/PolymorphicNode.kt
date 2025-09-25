package de.visualdigits.hybridxml.model.polymorphic

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.polymorphic.text.PolymorphicTextNode
import de.visualdigits.hybridxml.module.deserializer.PolymorphicNodeDeserializer.Companion.createNode
import org.jsoup.nodes.Element

/**
 * Basic node which can have one parent and multiple children.
 * It has also a label (the tag name) and can have attributes.
 * Whereas in XML we can distinguish attributes of a tag from
 * sub elements which only contain one text element.
 * Jackson XML would consider both as the same because the underlying
 * JSON logic cannot distinguish the two (and in JSON it is in fact the same).
 */
@JsonIgnoreProperties("parent", "label", "attributes")
@Suppress("UNCHECKED_CAST")
open class PolymorphicNode<T : PolymorphicNode<T>>(
    var label: String,
    val attributes: MutableMap<String, String?> = mutableMapOf(),
    var parent: PolymorphicNode<*>? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false) val children: MutableList<PolymorphicNode<*>> = mutableListOf(),
    var text: String? = null
) : BaseNode() {

    override fun toString(): String {
        return label
    }

    /**
     * Returns a deep copy of this polymorphic node.
     */
    fun clone(
        createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
    ): T {
        val clonedChildren = this.children.map { c -> c.clone() }

        val node = createNodeFunction?.let { createNode ->
            createNode(label, null, this, children, text)
        }?:createNode(label = label, node = this, children = children.toMutableList(), text = text)

        if (node is PolymorphicTextNode) {
            node.text = text()
        }
        node.withChildren(*clonedChildren.toTypedArray())
        return node as T
    }

    fun text(): String? {
        return if (this is PolymorphicTextNode) text else null
    }

    /**
     * Returns the raw text of this node and its children recursively.
     */
    fun rawText(): String? {
        val text = (text() ?: "") + children.joinToString("") { it.rawText()?:"" }
        return if (text.isNotBlank()) text else null
    }

    /**
     * Adds a child to this node at the given index.
     * When the index is omitted the node will be added at the end.
     * Also takes care on the children eventual parent.
     */
    fun withChild(child: PolymorphicNode<*>?, index: Int? = null): T {
        child?.parent?.children?.remove(child)
        child?.parent = this
        child?.let { c ->
            index?.let {
                children.add(index, c)
            } ?: children.add(c)

        }
        return this as T
    }

    /**
     * Adds the given children to this node at the end.
     * Also takes care on the children eventual parent.
     */
    open fun withChildren(vararg children: PolymorphicNode<*>): T {
        children.forEach { child -> withChild(child) }
        return this as T
    }

    override fun indent(level: Int) {
        this.level = level
        children.forEach { child -> child.indent((level + 1)) }
    }
}
