package de.visualdigits.hybridxml.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.lang.reflect.ParameterizedType
import java.lang.reflect.WildcardType

/**
 * Base node for all nodes to be handled with this jackson module.
 * It takes care about calculating indent levels after the complete tree is read.
 * We need this to properly indent any polymorphic stuff within bean objects.
 */
@Suppress("UNCHECKED_CAST")
open class BaseNode<T : BaseNode<T>>(
    @JsonIgnore var parent: BaseNode<*>? = null,
    @JsonIgnore val children: MutableList<BaseNode<*>> = mutableListOf()
) {

    @JsonIgnore var level: Int = 0

    @JsonIgnore private var indented: Boolean = false

    override fun toString(): String {
        return "${"  ".repeat(level)}${javaClass.simpleName}"
    }

    /**
     * Calculate indent levels for all nodes not being polymorphic
     * in a reflective manner.
     */
    open fun indent(parent: BaseNode<*>? = null, level: Int = 0) {
        if (indented) {
            return
        }

        this.level = level

        // process all fields which we can directly determine
        val baseNodeChildren = javaClass.declaredFields
            .filter { field ->
                BaseNode::class.java.isAssignableFrom(field.type)
            }
            .mapNotNull { field ->
                field.isAccessible = true
                (field[this] as? BaseNode<*>)
            }.toMutableList()

        // process lists
        baseNodeChildren.addAll(javaClass.declaredFields
            .filter { field ->
                val isCandidate = (field.genericType as? ParameterizedType)?.let { pt ->
                    pt.actualTypeArguments.any { ata ->
                        if (ata::class.java == Class::class.java) {
                            BaseNode::class.java.isAssignableFrom(ata as Class<*>)
                        } else if (WildcardType::class.java.isAssignableFrom(ata::class.java)) {
                            ((ata as WildcardType).upperBounds.any { ub -> (ub as? Class<*>)?.let { c -> BaseNode::class.java.isAssignableFrom(c) }?:false  })
                        } else {
                            false
                        }
                    }
                } ?: false
                List::class.java.isAssignableFrom(field.type) && isCandidate
            }
            .mapNotNull { field ->
                field.isAccessible = true
                val list = field[this] as? List<*>
                list?.mapNotNull { elem ->
                    (elem as? BaseNode<*>)
                }
            }.flatten())

        baseNodeChildren.forEach { bn ->
            bn.parent = this
            bn.indent(this, level + 1)
        }
        this.children.addAll(baseNodeChildren)
        indented = true
    }

    open fun postProcessXml() {
        // nothing to do here
    }

    /**
     * Sets the parent node in a fluent manner.
     */
    fun withParent(parent: BaseNode<*>?): T {
        if (parent != null) {
            this.parent?.removeChild(this)
            parent.withChild(this)
        }
        return this as T
    }

    /**
     * Remove this node from its parent node (if any).
     */
    fun removeFromParent(): T {
        parent?.removeChild(this)
        return this as T
    }

    /**
     * Removes the given child node and nulls out the childs parent attribute.
     */
    fun removeChild(child: BaseNode<*>?): T {
        if (child != null) {
            child.parent = null
            children.remove(child)
        }
        return this as T
    }

    /**
     * Moves this node to another porent.
     */
    fun moveTo(newParent: BaseNode<*>): T {
        parent?.removeChild(this)
        newParent.withChild(this)
        return this as T
    }

    /**
     * Moves this node to its parent (if any).
     * The node will bne placed as last child.
     */
    fun moveUp(): T {
        parent?.also { p ->
            p.removeChild(this)
            children.forEach { c -> c.parent = p }
            children.clear()
        }

        return this as T
    }

    /**
     * Returns all children of the parent node (if any) except the node itself.
     */
    fun siblings(): List<BaseNode<*>> {
        val polymorphicXmlNodes = parent?.children?.filterNot { it == this } ?: listOf()
        return polymorphicXmlNodes
    }

    /**
     * Returns the index of this node in the parents children list (if any) or -1
     */
    fun indexOfInParent(): Int = parent?.children?.indexOf(this) ?: -1

    /**
     * Adds a child to this node at the given index.
     * When the index is omitted the node will be added at the end.
     * Also takes care on the children eventual parent.
     */
    fun withChild(child: BaseNode<*>?, index: Int? = null): T {
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
    fun withChildren(vararg children: BaseNode<*>): T {
        children.forEach { child -> withChild(child) }
        return this as T
    }

    /**
     * Determines if the node has children.
     */
    @JsonIgnore
    fun hasChildren() = children.isNotEmpty()

    /**
     * Determines if the node has siblings.
     */
    @JsonIgnore
    fun hasSiblings() = (parent?.children?.size ?: 0) > 1

    /**
     * Determines if the node is the first child of its parent.
     */
    @JsonIgnore
    fun isFirstChild() = parent?.children?.firstOrNull() == this

    /**
     * Determines if the node is the last child of its parent.
     */
    @JsonIgnore
    fun isLastChild() = parent?.children?.lastOrNull() == this

    /**
     * Returns the previous sibling of this node or null if this node has no previous sibling.
     */
    @JsonIgnore
    fun previousSibling(): BaseNode<*>? {
        return parent?.children?.getOrNull((parent?.children?.indexOf(this) ?: -1) - 1)
    }

    /**
     * Returns the next sibling of this node or null if this node has no next sibling.
     */
    @JsonIgnore
    fun nextSibling(): BaseNode<*>? {
        return parent?.children?.getOrNull((parent?.children?.indexOf(this) ?: -1) + 1)
    }

    /**
     * Returns the first child with the given tag name (if any).
     */
    inline fun <reified T : BaseNode<T>> firstChild(): BaseNode<*>? {
        return children<T>().firstOrNull()
    }

    /**
     * Returns the last child with the given tag name (if any).
     */
    inline fun <reified T : BaseNode<T>> lastChild(): BaseNode<*>? {
        return children<T>().lastOrNull()
    }

    /**
     * Returns all children with the given tag name and given attributes.
     * When the given attribute key is associated with null it is only checked for existence of the key.
     */
    inline fun <reified T : BaseNode<T>> children(): List<BaseNode<*>> {
        return children.filter { child -> T::class.java.isAssignableFrom(child::class.java)}
    }

    /**
     * Returns true if this node is a child of the given node.
     */
    @JsonIgnore
    fun isChildOf(node: BaseNode<*>): Boolean {
        return parent == node
    }

    /**
     * Returns true if this node is beneath the rootline of the given node.
     */
    @JsonIgnore
    fun isInRootlineOf(node: BaseNode<*>): Boolean {
        return rootLine().any { n -> n == node }
    }

    /**
     * Returns a deep copy of this polymorphic node.
     */
    fun clone(): T {
        val clonedChildren = this.children.map { c -> c.clone() }
        return BaseNode().withChildren(*clonedChildren.toTypedArray<BaseNode<*>>()) as T
    }

    fun rootNode(): BaseNode<*>? = rootLine().firstOrNull()

    open fun rootLine(rootPath: MutableList<BaseNode<*>> = mutableListOf()): List<BaseNode<*>> {
        parent?.rootLine(rootPath)
        rootPath.add(this)
        return rootPath
    }
}
