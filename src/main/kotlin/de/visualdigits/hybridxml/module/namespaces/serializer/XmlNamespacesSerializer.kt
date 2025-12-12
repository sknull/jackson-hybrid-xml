package de.visualdigits.hybridxml.module.namespaces.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase
import com.fasterxml.jackson.dataformat.xml.ser.XmlBeanSerializer
import de.visualdigits.hybridxml.model.namespaces.XmlNamespaces
import de.visualdigits.hybridxml.model.namespaces.XmlSchema
import de.visualdigits.hybridxml.module.common.ToXmlGenerator
import kotlin.reflect.full.findAnnotations

class XmlNamespacesSerializer(
    src: BeanSerializerBase
) : XmlBeanSerializer(src) {

    override fun serialize(
        bean: Any,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        val xmlGen: ToXmlGenerator = gen as ToXmlGenerator

        val xmlNamespaces = bean::class.findAnnotations(XmlNamespaces::class).firstOrNull()?.nameSpaces?.associate { nameSpace ->
                Pair(
                    nameSpace.prefix,
                    nameSpace.namespaceUri
                )
            } ?: mapOf()
        val schemaPrefix = bean::class.findAnnotations(XmlSchema::class).firstOrNull()?.prefix

        if (_objectIdWriter != null) {
            _serializeWithObjectId(bean, xmlGen, provider, true)
            return
        }
        xmlGen.writeStartObjectWithSchemaPrefix(schemaPrefix)
        if (_propertyFilterId != null) {
            serializeFieldsFiltered(bean, xmlGen, provider)
        } else {
            serializeFields(bean, xmlGen, provider, xmlNamespaces)
        }
        xmlGen.writeEndObject()
    }

    private fun serializeFields(bean: Any, gen: JsonGenerator, provider: SerializerProvider, xmlNamespaces: Map<String, String>) {
        val xmlGen = gen as ToXmlGenerator

        if (_attributeCount > 0) {
            xmlGen.setNextIsAttribute(true)
        }

        getPropertyWriters(provider).forEachIndexed { i, prop ->
            try {
                setParameters(i, xmlGen)
                xmlGen.setNextName(_xmlNames[i])
                serializeField(i, xmlGen, prop, bean, provider, xmlNamespaces)
                if (i == _textPropertyIndex) {
                    xmlGen.setNextIsUnwrapped(false)
                }
            } catch (e: Exception) {
                wrapAndThrow(provider, e, bean, prop.name)
            } catch (_: StackOverflowError) { // Bit tricky, can't do more calls as stack is full; so:
                val mapE = JsonMappingException.from(
                    xmlGen,
                    "Infinite recursion (StackOverflowError)"
                )
                mapE.prependPath(JsonMappingException.Reference(bean, prop.name))
                throw mapE
            }
        }
    }

    private fun serializeField(
        i: Int,
        xmlGen: ToXmlGenerator,
        prop: BeanPropertyWriter,
        bean: Any,
        provider: SerializerProvider,
        xmlNamespaces: Map<String, String>
    ) {
        if ((_cdata != null) && _cdata[i]) {
            xmlGen.setNextIsCData(true)
            prop.serializeAsField(bean, xmlGen, provider)
            xmlGen.setNextIsCData(false)
        } else {
            serializeNamespaces(i, xmlGen, xmlNamespaces)
            prop.serializeAsField(bean, xmlGen, provider)
        }
    }

    private fun setParameters(i: Int, xmlGen: ToXmlGenerator) {
        if (i == _attributeCount && !(xmlGen.nextIsAttribute() && isUnwrappingSerializer)) {
            xmlGen.setNextIsAttribute(false)
        }
        if (i == _textPropertyIndex) {
            xmlGen.setNextIsUnwrapped(true)
        }
    }

    private fun getPropertyWriters(provider: SerializerProvider): Array<out BeanPropertyWriter> {
        return if (_filteredProps != null && provider.activeView != null) {
            _filteredProps
        } else {
            _props
        }
    }

    private fun serializeNamespaces(
        i: Int,
        xmlGen: ToXmlGenerator,
        xmlNamespaces: Map<String, String>
    ) {
        if ((_attributeCount > 0 && i == _attributeCount) || (_attributeCount == 0 && i == 0)) {
            xmlNamespaces.forEach { nameSpace ->
                xmlGen.setNextIsAttribute(true)
                xmlGen.writeFieldName("xmlns:${nameSpace.key}")
                xmlGen.writeString(nameSpace.value)
                xmlGen.setNextIsAttribute(xmlGen.nextIsAttribute())
            }
        }
    }
}