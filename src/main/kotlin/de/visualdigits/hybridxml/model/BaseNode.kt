package de.visualdigits.hybridxml.model

import com.fasterxml.jackson.databind.module.SimpleModule
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import de.visualdigits.hybridxml.module.common.JacksonHelper.jsonMapperBuilder
import de.visualdigits.hybridxml.module.common.JacksonHelper.printer
import de.visualdigits.hybridxml.module.common.JacksonHelper.xmlMapperBuilder
import de.visualdigits.hybridxml.module.common.XmlNodeSerializerModifier
import de.visualdigits.hybridxml.module.polymorphic.serializer.ConfigurableSpacesIndenter
import de.visualdigits.hybridxml.module.polymorphic.serializer.PolymorphicJsonNodeSerializer
import org.apache.commons.text.StringEscapeUtils
import java.io.File
import java.io.OutputStream

abstract class BaseNode {

    /**
     * Serializes this hybrid xml instance as a String.
     *
     * @param file The file to write to.
     * @param indentOutput Determines whether to indent the output or not.
     * @param writeXmlDeclaration Determines whether to write the xml declaration or not.
     *
     * @return The serialized instance as a string.
     */
    fun writeXmlValue(
        outs: OutputStream,
        indentOutput: Boolean = true,
        indentAmount: Int = 2,
        writeXmlDeclaration: Boolean = true,
        writeHtmlDeclaration: Boolean = false
    ): String {
        val xml = writeXmlValue(indentOutput, indentAmount, writeXmlDeclaration, writeHtmlDeclaration)
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
    fun writeXmlValue(
        file: File,
        indentOutput: Boolean = true,
        indentAmount: Int = 2,
        writeXmlDeclaration: Boolean = true,
        writeHtmlDeclaration: Boolean = false
    ): String {
        val xml = writeXmlValue(indentOutput, indentAmount, writeXmlDeclaration, writeHtmlDeclaration)
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
    open fun writeXmlValue(
        indentOutput: Boolean = true,
        indentAmount: Int = 2,
        writeXmlDeclaration: Boolean = true,
        writeHtmlDeclaration: Boolean = false
    ): String {
        val indenter = ConfigurableSpacesIndenter(indentAmount)
        printer.indentObjectsWith(indenter)
        indent()

        val modifier = XmlNodeSerializerModifier(indentAmount, writeHtmlDeclaration)
        val xml = xmlMapperBuilder(indentOutput, writeXmlDeclaration)
            .addModule(SimpleModule().setSerializerModifier(modifier))
            .build()
            .writeValueAsString(this)
            .replace("\r\n", "\n")
            .replace("\r", "\n")
        return if (modifier.polymorphicXmlNodeSerializer?.needsPreprocessing == true) StringEscapeUtils.unescapeXml(xml) else xml
    }

    /**
     * Serializes this hybrid xml instance as a String.
     *
     * @param file The file to write to.
     *
     * @return The serialized instance as a string.
     */
    fun writeJsonValue(
        outs: OutputStream,
        indentOutput: Boolean = true
    ): String {
        val json = writeJsonValue(indentOutput)
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
    fun writeJsonValue(
        file: File,
        indentOutput: Boolean = true
    ): String {
        val json = writeJsonValue(indentOutput)
        file.writeText(json)

        return json
    }

    /**
     * Serializes this hybrid xml instance as a String.
     *
     * @return The serialized instance as a string.
     */
    open fun writeJsonValue(
        indentOutput: Boolean = true,
        dropRootNode: Boolean = false
    ): String {
        indent()

        return jsonMapperBuilder(indentOutput)
            .addModule(
                SimpleModule()
                    .addSerializer(
                        PolymorphicNode::class.java,
                        PolymorphicJsonNodeSerializer(dropRootNode)
                    )
            )
            .build()
            .writeValueAsString(this)
            .replace("\r\n", "\n")
            .replace("\r", "\n")
    }

    abstract fun indent(parent: BaseNode? = null, level: Int = 0)

    open fun postProcessTree() {
        // nothing to do
    }
}