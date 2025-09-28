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
    parent: BaseNode<*>? = null,
    @field:JsonIgnore(false) children: MutableList<PolymorphicNode<*>> = mutableListOf(),
    var text: String? = null
) : BaseNode<T>(
    parent = parent,
    children = children as MutableList<BaseNode<*>>
) {

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
    override fun clone(
    ): T {
        val clonedChildren = this.children.map { c -> c.clone() }
        return PolymorphicNode(
            label = label,
            attributes = attributes.toMutableMap(),
            text = text
        ).withChildren(*clonedChildren.toTypedArray<BaseNode<*>>()) as T
    }

    /**
     * Returns a deep copy of this polymorphic node.
     */
    fun clone(
        createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
    ): T {
        val clonedChildren = this.children.mapNotNull { c -> (c as? PolymorphicNode)?.clone() }

        val node = createNodeFunction?.let { createNode ->
            createNode(label, null, this, children as MutableList<PolymorphicNode<*>>, text)
        }?:createPolymorphicNode(label = label, node = this, children = (children as MutableList<PolymorphicNode<*>>).toMutableList(), text = text)

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
        val text = (text() ?: "") + children.joinToString("") { (it as? PolymorphicNode)?.rawText()?:"" }
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
            (label?.let { l -> l == (child as PolymorphicNode<*>).label }?:true) &&
                    (attributes?.all { att -> attributes.keys.contains(att.key) && att.value?.let { v -> v == attributes[att.key] }?:true }?:true)
        } as List<PolymorphicNode<*>>
    }

    override fun indent(parent: BaseNode<*>?, level: Int) {
        this.level = level
        children.forEach { child ->
            child.parent = this
            child.indent(this, level + 1)
        }
    }

    override fun rootLine(rootPath: MutableList<BaseNode<*>>): List<BaseNode<*>> {
        // make sure we do not look up bbeyond the polymorphic boundary here and sonsider any nonpolymorphic node as terminator
        if (parent != null && parent?.javaClass?.let { jc -> PolymorphicNode::class.java.isAssignableFrom(jc) }?:false) {
            parent!!.rootLine(rootPath)
        }
        rootPath.add(this)
        return rootPath
    }}
