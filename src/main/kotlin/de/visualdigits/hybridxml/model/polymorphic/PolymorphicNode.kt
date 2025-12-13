package de.visualdigits.hybridxml.model.polymorphic

import com.fasterxml.jackson.annotation.JsonIgnore
import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.html.Html.Companion.createPolymorphicNode
import org.jsoup.nodes.Element

/**
 * Basic node which can have one parent and multiple children.
 * It has also a label (the tag name) and can have attributes.
 * Whereas in XML we can distinguish attributes of a tag from
 * sub elements which only contain one text element.
 * Jackson XML would consider both as the same because the underlying
 * JSON logic cannot distinguish the two (and in JSON it is in fact the same).
 */
@Suppress("UNCHECKED_CAST")
open class PolymorphicNode<T : PolymorphicNode<T>>(
    @JsonIgnore var label: String,
    @JsonIgnore val attributes: MutableMap<String, String?> = mutableMapOf(),
    @JsonIgnore var parent: BaseNode? = null,
    val children: MutableList<PolymorphicNode<*>> = mutableListOf(),
    var text: String? = null
) : BaseNode() {

    @JsonIgnore var level: Int = 0

    override fun toString(): String {
        return when (label) {
            TagName.TEXT.label -> text
            TagName.CDATA.label -> "<![CDATA[$text]]>"
            TagName.COMMENT.label -> "<!--$text-->"
            else -> {
                "<$label ${attributes.map { att -> "${att.key}${att.value?.let{ v -> "\"$v\""}?:""}}"}.joinToString(" ")}>"
            }
        }?:label
    }

    /**
     * Returns a deep copy of this polymorphic node.
     */
    open fun clone(): T {
        val clonedChildren = this.children.map { c -> c.clone() }
        return PolymorphicNode(
            label = label,
            attributes = attributes.toMutableMap(),
            text = text
        ).withChildren(*clonedChildren.toTypedArray<PolymorphicNode<*>>()) as T
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
        }?:createPolymorphicNode(label = label, node = this, children = children.toMutableList(), text = text)

        if (TagName.TEXT.label == label || TagName.CDATA.label == label || TagName.COMMENT.label == label) {
            node.text = text()
        }
        node.withChildren(*clonedChildren.toTypedArray())
        return node as T
    }

    fun text(): String? {
        return if (TagName.TEXT.label == label || TagName.CDATA.label == label || TagName.COMMENT.label == label) text else null
    }

    /**
     * Returns the raw text of this node and its children recursively.
     */
    fun rawText(): String? {
        val text = (text() ?: "") + children.joinToString("") { it.rawText() ?:"" }
        return text.ifBlank { null }
    }

    /**
     * Sets the parent node in a fluent manner.
     */
    fun withParent(parent: PolymorphicNode<*>?): T {
        if (parent == null) {
            (this.parent as? PolymorphicNode<*>)?.removeChild(this)
            parent?.withChild(this)
        }
        return this as T
    }

    /**
     * Moves this node to another porent.
     */
    fun moveTo(newParent: PolymorphicNode<*>): T {
        (this.parent as? PolymorphicNode<*>)?.removeChild(this)
        newParent.withChild(this)
        return this as T
    }

    /**
     * Moves this node to its parent (if any).
     * The node will bne placed as last child.
     */
    fun moveUp(): T {
        parent?.also { p ->
            (p as? PolymorphicNode<*>)?.removeChild(this)
            children.forEach { c -> c.parent = p }
            children.clear()
        }

        return this as T
    }

    /**
     * Remove this node from its parent node (if any).
     */
    fun removeFromParent(): T {
        (this.parent as? PolymorphicNode<*>)?.removeChild(this)
        return this as T
    }

    /**
     * Removes the given child node and nulls out the childs parent attribute.
     */
    fun removeChild(child: PolymorphicNode<*>?): T {
        if (child != null) {
            child.parent = null
            children.remove(child)
        }
        return this as T
    }

    /**
     * Adds a child to this node at the given index.
     * When the index is omitted the node will be added at the end.
     * Also takes care on the children eventual parent.
     */
    fun withChild(child: PolymorphicNode<*>?, index: Int? = null): T {
        (child?.parent as? PolymorphicNode<*>)?.children?.remove(child)
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

    /**
     * Returns the first child with the given tag name (if any).
     */
    fun firstChild(label: String? = null, attributes: Map<String, String?>? = null): PolymorphicNode<*>? {
        return children(label, attributes).firstOrNull()
    }

    /**
     * Returns the last child with the given tag name (if any).
     */
    fun lastChild(label: String? = null, attributes: Map<String, String?>? = null): PolymorphicNode<*>? {
        return children(label, attributes).lastOrNull()
    }

    fun children(label: String? = null, attributes: Map<String, String?>? = null): List<PolymorphicNode<*>> {
        return children.filter { child ->
            (label?.let { l -> l == child.label }?:true) &&
                    (attributes?.all { att -> attributes.keys.contains(att.key) && att.value?.let { v -> v == attributes[att.key] }?:true }?:true)
        }
    }

    /**
     * Returns the index of this node in the parents children list (if any) or -1
     */
    fun indexOfInParent(): Int = (this.parent as? PolymorphicNode<*>)?.children?.indexOf(this) ?: -1

    /**
     * Determines if the node has children.
     */
    @JsonIgnore
    fun hasChildren() = children.isNotEmpty()

    /**
     * Returns all children of the parent node (if any) except the node itself.
     */
    fun siblings(): List<PolymorphicNode<*>> {
        return (this.parent as? PolymorphicNode<*>)?.children?.filterNot { it == this } ?: listOf()
    }

    /**
     * Determines if the node has siblings.
     */
    @JsonIgnore
    fun hasSiblings() = ((this.parent as? PolymorphicNode<*>)?.children?.size ?: 0) > 1

    /**
     * Determines if the node is the first child of its parent.
     */
    @JsonIgnore
    fun isFirstChild() = (this.parent as? PolymorphicNode<*>)?.children?.firstOrNull() == this

    /**
     * Determines if the node is the last child of its parent.
     */
    @JsonIgnore
    fun isLastChild() = (this.parent as? PolymorphicNode<*>)?.children?.lastOrNull() == this

    /**
     * Returns true if this node is a child of the given node.
     */
    @JsonIgnore
    fun isChildOf(node: PolymorphicNode<*>): Boolean {
        return parent == node
    }

    /**
     * Returns true if this node is beneath the rootline of the given node.
     */
    @JsonIgnore
    fun isInRootlineOf(node: PolymorphicNode<*>): Boolean {
        return rootLine().any { n -> n == node }
    }

    fun rootNode(): PolymorphicNode<*>? = rootLine().firstOrNull()

    /**
     * Returns the previous sibling of this node or null if this node has no previous sibling.
     */
    @JsonIgnore
    fun previousSibling(): PolymorphicNode<*>? {
        return (this.parent as? PolymorphicNode<*>)?.children?.getOrNull(((this.parent as? PolymorphicNode<*>)?.children?.indexOf(this) ?: -1) - 1)
    }

    /**
     * Returns the next sibling of this node or null if this node has no next sibling.
     */
    @JsonIgnore
    fun nextSibling(): PolymorphicNode<*>? {
        return (this.parent as? PolymorphicNode<*>)?.children?.getOrNull(((this.parent as? PolymorphicNode<*>)?.children?.indexOf(this) ?: -1) + 1)
    }

    /**
     * Returns the first child with the given tag name (if any).
     */
    inline fun <reified T : PolymorphicNode<T>> firstChild(): PolymorphicNode<*>? {
        return children<T>().firstOrNull()
    }

    /**
     * Returns the last child with the given tag name (if any).
     */
    inline fun <reified T : PolymorphicNode<T>> lastChild(): PolymorphicNode<*>? {
        return children<T>().lastOrNull()
    }

    /**
     * Returns all children with the given tag name and given attributes.
     * When the given attribute key is associated with null it is only checked for existence of the key.
     */
    inline fun <reified T : PolymorphicNode<T>> children(): List<PolymorphicNode<*>> {
        return children.filter { child -> T::class.java.isAssignableFrom(child::class.java)}
    }

    override fun indent(parent: BaseNode?, level: Int) {
        this.level = level
        this.parent = parent
        children.forEach { child ->
            child.indent(this, level + 1)
        }
    }

    fun rootLine(rootPath: MutableList<PolymorphicNode<*>> = mutableListOf()): List<PolymorphicNode<*>> {
        if (parent != null) {
            (this.parent as? PolymorphicNode<*>)!!.rootLine(rootPath)
        }
        rootPath.add(this)
        return rootPath
    }
}
