package de.visualdigits.hybridxml.module.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.kotlinModule
import de.visualdigits.hybridxml.model.objectnode.ObjectNode
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import de.visualdigits.hybridxml.module.namespaces.serializer.NamespaceAwarePrettyPrinter
import de.visualdigits.hybridxml.module.polymorphic.deserializer.OffsetDateTimeDeserializer
import de.visualdigits.hybridxml.module.polymorphic.deserializer.PolymorphicJsonNodeDeserializer
import de.visualdigits.hybridxml.module.polymorphic.deserializer.PolymorphicXmlNodeDeserializer
import org.jsoup.nodes.Element
import java.io.File
import java.io.InputStream
import java.net.URI
import java.time.OffsetDateTime

object JacksonHelper {

    val printer = NamespaceAwarePrettyPrinter()

    /**
     * Internal method to create the builder.
     * Must be public to be usable within public inline methods below.
     */
    fun xmlMapperBuilder(indentOutput: Boolean = true, writeXmlDeclaration: Boolean = true): XmlMapper.Builder {
        // The builder must be created on every call as otherwise the hybrid module would be not replaced
        // on subsequent calls.
        val xmlMapperBuilder = XmlMapper.builder(XmlFactory(printer))
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // ISODate
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .defaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY))
            .addModule(kotlinModule())
            .addModule(JavaTimeModule().addDeserializer(OffsetDateTime::class.java, OffsetDateTimeDeserializer()))
            .disable(com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
            .defaultUseWrapper(false)

        if (indentOutput) {
            xmlMapperBuilder.enable(SerializationFeature.INDENT_OUTPUT)
        } else {
            xmlMapperBuilder.disable(SerializationFeature.INDENT_OUTPUT)
        }

        if (writeXmlDeclaration) {
            xmlMapperBuilder.enable(com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
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
            .defaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY))
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
    inline fun <reified T : ObjectNode<T>> readXmlValue(
        ins: InputStream,
        noinline createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
    ): T {
        return ins.use { i ->
            readXmlValue(String(i.readAllBytes()), createNodeFunction)
        }
    }

    /**
     * Deserializes the given file contents to an instance of the desired type.
     */
    inline fun <reified T : ObjectNode<T>> readXmlValue(
        file: File,
        noinline createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
    ): T {
        return readXmlValue(file.readText(), createNodeFunction)
    }

    /**
     * Deserializes the given url contents to an instance of the desired type.
     */
    inline fun <reified T : ObjectNode<T>> readXmlValue(
        uri: URI,
        noinline createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
    ): T {
        val rss = uri.toURL().readText()
        return readXmlValue(rss, createNodeFunction)
    }

    /**
     * Deserializes the given raw xml to an instance of the desired type.
     */
    inline fun <reified T : ObjectNode<T>> readXmlValue(
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
            .also { node -> node.postProcessTree()}
        tree.indent()

        return tree
    }

    /**
     * Deserializes the given input stream to an instance of the desired type.
     */
    inline fun <reified T : ObjectNode<T>> readJsonValue(
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
    inline fun <reified T : ObjectNode<T>> readJsonValue(
        file: File,
        noinline createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
    ): T {
        return readJsonValue(file.readText(), createNodeFunction)
    }

    /**
     * Deserializes the given raw xml to an instance of the desired type.
     */
    inline fun <reified T : ObjectNode<T>> readJsonValue(
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
            .also { node -> node.postProcessTree()}
        tree.indent()

        return tree
    }
}