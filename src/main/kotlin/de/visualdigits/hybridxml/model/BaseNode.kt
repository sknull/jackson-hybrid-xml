package de.visualdigits.hybridxml.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.kotlinModule
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import de.visualdigits.hybridxml.module.deserializer.OffsetDateTimeDeserializer
import de.visualdigits.hybridxml.module.deserializer.PolymorphicJsonNodeDeserializer
import de.visualdigits.hybridxml.module.deserializer.PolymorphicXmlNodeDeserializer
import de.visualdigits.hybridxml.module.serializer.ConfigurableSpacesIndenter
import de.visualdigits.hybridxml.module.serializer.PolymorphicJsonNodeSerializer
import de.visualdigits.hybridxml.module.serializer.PolymorphicXmlNodeSerializer
import org.jsoup.nodes.Element
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.ParameterizedType
import java.lang.reflect.WildcardType
import java.net.URI
import java.time.OffsetDateTime

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

    companion object {

        /**
         * Internal method to create the builder.
         * Must be public to be usable within public inline methods below.
         */
        fun xmlMapperBuilder(indentOutput: Boolean = true, writeXmlDeclaration: Boolean = true): XmlMapper.Builder {
            // The builder must be created on every call as otherwise the hybrid module would be not replaced
            // on subsequent calls.
            val xmlMapperBuilder = XmlMapper.builder()
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // ISODate
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .serializationInclusion(JsonInclude.Include.NON_EMPTY)
                .addModule(kotlinModule())
                .addModule(JavaTimeModule().addDeserializer(OffsetDateTime::class.java, OffsetDateTimeDeserializer()))
                .disable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
                .defaultUseWrapper(false)

            if (indentOutput) {
                xmlMapperBuilder.enable(SerializationFeature.INDENT_OUTPUT)
            } else {
                xmlMapperBuilder.disable(SerializationFeature.INDENT_OUTPUT)
            }

            if (writeXmlDeclaration) {
                xmlMapperBuilder.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
            } else {
                xmlMapperBuilder.disable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
            }

            return xmlMapperBuilder
        }

        /**
         * Internal method to create the builder.
         * Must be public to be usable within public inline methods below.
         */
        fun jsonMapperBuilder(indentOutput: Boolean = true): JsonMapper.Builder {
            // The builder must be created on every call as otherwise the hybrid module would be not replaced
            // on subsequent calls.
            val jsonMapperBuilder = jacksonMapperBuilder()
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // ISODate
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .serializationInclusion(JsonInclude.Include.NON_EMPTY)
                .addModule(kotlinModule())
                .addModule(JavaTimeModule())

            if (indentOutput) {
                jsonMapperBuilder.enable(SerializationFeature.INDENT_OUTPUT)
            } else {
                jsonMapperBuilder.disable(SerializationFeature.INDENT_OUTPUT)
            }

            return jsonMapperBuilder
        }

        /**
         * Deserializes the given input stream to an instance of the desired type.
         */
        inline fun <reified T : BaseNode<T>> readValue(
            ins: InputStream,
            noinline createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
        ): T {
            return ins.use { i ->
                readValue(String(i.readAllBytes()), createNodeFunction)
            }
        }

        /**
         * Deserializes the given file contents to an instance of the desired type.
         */
        inline fun <reified T : BaseNode<T>> readValue(
            file: File,
            noinline createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
        ): T {
            return readValue(file.readText(), createNodeFunction)
        }

        /**
         * Deserializes the given url contents to an instance of the desired type.
         */
        inline fun <reified T : BaseNode<T>> readValue(
            uri: URI,
            noinline createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
        ): T {
            val rss = uri.toURL().readText()
            return readValue(rss, createNodeFunction)
        }

        /**
         * Deserializes the given raw xml to an instance of the desired type.
         */
        inline fun <reified T : BaseNode<T>> readValue(
            xml: String,
            noinline createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
        ): T {
            val tree = xmlMapperBuilder()
                .addModule(
                    SimpleModule()
                        .addDeserializer(
                            PolymorphicNode::class.java,
                            PolymorphicXmlNodeDeserializer(xml, createNodeFunction)
                        )
                )
                .build()
                .readValue<T>(xml, T::class.java)
                .also { node -> node.postProcessXml()}
            tree.indent()

            return tree
        }

        /**
         * Deserializes the given input stream to an instance of the desired type.
         */
        inline fun <reified T : BaseNode<T>> readJsonValue(
            ins: InputStream,
            noinline createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
        ): T {
            return ins.use { i ->
                readJsonValue(String(i.readAllBytes()), createNodeFunction)
            }
        }

        /**
         * Deserializes the given file contents to an instance of the desired type.
         */
        inline fun <reified T : BaseNode<T>> readJsonValue(
            file: File,
            noinline createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
        ): T {
            return readJsonValue(file.readText(), createNodeFunction)
        }

        /**
         * Deserializes the given raw xml to an instance of the desired type.
         */
        inline fun <reified T : BaseNode<T>> readJsonValue(
            json: String,
            noinline createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
        ): T {
            val tree = jsonMapperBuilder()
                .addModule(
                    SimpleModule()
                        .addDeserializer(
                            PolymorphicNode::class.java,
                            PolymorphicJsonNodeDeserializer(createNodeFunction)
                        )
                )
                .build()
                .readValue<T>(json, T::class.java)
                .also { node -> node.postProcessXml()}
            tree.indent()

            return tree
        }
    }

    override fun toString(): String {
        return "${"  ".repeat(level)}${javaClass.simpleName}"
    }

    /**
     * Serializes this hybrid xml instance as a String.
     *
     * @param file The file to write to.
     * @param indentOutput Determines whether to indent the output or not.
     * @param writeXmlDeclaration Determines whether to write the xml declaration or not.
     *
     * @return The serialized instance as a string.
     */
    fun writeValue(
        outs: OutputStream,
        indentOutput: Boolean = true,
        indentAmount: Int = 2,
        writeXmlDeclaration: Boolean = true,
        writeHtmlDeclaration: Boolean = false
    ): String {
        val xml = writeValueAsString(indentOutput, indentAmount, writeXmlDeclaration, writeHtmlDeclaration)
        outs.use { o -> o.write(xml.toByteArray())}

        return xml
    }

    /**
     * Serializes this hybrid xml instance as a String.
     *
     * @param file The file to write to.
     * @param indentOutput Determines whether to indent the output or not.
     * @param writeXmlDeclaration Determines whether to write the xml declaration or not.
     *
     * @return The serialized instance as a string.
     */
    fun writeValue(
        file: File,
        indentOutput: Boolean = true,
        indentAmount: Int = 2,
        writeXmlDeclaration: Boolean = true,
        writeHtmlDeclaration: Boolean = false
    ): String {
        val xml = writeValueAsString(indentOutput, indentAmount, writeXmlDeclaration, writeHtmlDeclaration)
        file.writeText(xml)

        return xml
    }

    /**
     * Serializes this hybrid xml instance as a String.
     *
     * @param indentOutput Determines whether to indent the output or not.
     * @param writeXmlDeclaration Determines whether to write the xml declaration or not.
     *
     * @return The serialized instance as a string.
     */
    open fun writeValueAsString(
        indentOutput: Boolean = true,
        indentAmount: Int = 2,
        writeXmlDeclaration: Boolean = true,
        writeHtmlDeclaration: Boolean = false
    ): String {
        val printer = DefaultXmlPrettyPrinter()
        val indenter = ConfigurableSpacesIndenter(indentAmount)
        printer.indentObjectsWith(indenter)
        indent()

        return xmlMapperBuilder(indentOutput, writeXmlDeclaration)
            .addModule(SimpleModule()
                .addSerializer(
                    PolymorphicNode::class.java,
                    PolymorphicXmlNodeSerializer(indentAmount, writeHtmlDeclaration)
                )
            )
            .build()
            .writer(printer)
            .writeValueAsString(this)
            .replace("\r\n", "\n")
            .replace("\r", "\n")
    }

    /**
     * Serializes this hybrid xml instance as a String.
     *
     * @param file The file to write to.
     *
     * @return The serialized instance as a string.
     */
    fun writeValueAsJson(
        outs: OutputStream,
        indentOutput: Boolean = true
    ): String {
        val json = writeValueAsJsonString(indentOutput)
        outs.use { o -> o.write(json.toByteArray())}

        return json
    }

    /**
     * Serializes this hybrid xml instance as a String.
     *
     * @param file The file to write to.
     *
     * @return The serialized instance as a string.
     */
    fun writeValueAsJson(
        file: File,
        indentOutput: Boolean = true
    ): String {
        val json = writeValueAsJsonString(indentOutput)
        file.writeText(json)

        return json
    }

    /**
     * Serializes this hybrid xml instance as a String.
     *
     * @return The serialized instance as a string.
     */
    open fun writeValueAsJsonString(
        indentOutput: Boolean = true,
    ): String {
        indent()

        return jsonMapperBuilder(indentOutput)
            .addModule(SimpleModule()
                .addSerializer(
                    PolymorphicNode::class.java,
                    PolymorphicJsonNodeSerializer()
                )
            )
            .build()
            .writeValueAsString(this)
            .replace("\r\n", "\n")
            .replace("\r", "\n")
    }

    /**
     * Calculate indent levels for all nodes not being polymorphic
     * in a reflective manner.
     */
    open fun indent(parent: BaseNode<*>? = null, level: Int = 0) {
        this.level = level
        this.parent = parent

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
            bn.indent(this, level + 1)
        }
        this.children.addAll(baseNodeChildren)
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
    open fun clone(): T {
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
