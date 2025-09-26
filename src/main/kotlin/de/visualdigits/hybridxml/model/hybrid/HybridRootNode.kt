package de.visualdigits.hybridxml.model.hybrid

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
import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import de.visualdigits.hybridxml.module.deserializer.PolymorphicJsonNodeDeserializer
import de.visualdigits.hybridxml.module.deserializer.PolymorphicXmlNodeDeserializer
import de.visualdigits.hybridxml.module.serializer.ConfigurableSpacesIndenter
import de.visualdigits.hybridxml.module.serializer.PolymorphicJsonNodeSerializer
import de.visualdigits.hybridxml.module.serializer.PolymorphicXmlNodeSerializer
import org.jsoup.nodes.Element
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Base class for top level hybrid nodes which should have
 * serializer methods.
 */
abstract class HybridRootNode<T : HybridRootNode<T>> : BaseNode<T>() {

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
                .addModule(JavaTimeModule())
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
}

