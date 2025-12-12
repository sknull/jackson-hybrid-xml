package de.visualdigits.hybridxml.module.common

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.XmlBeanSerializer

/**
 * Injects CollectionAttributeXmlSerializer as base serializer into JsonSerializer.
 */
class JsonSerializerWrapper<T>(
    private val baseSerializer: XmlBeanSerializer
) : JsonSerializer<T>() {

    override fun serialize(value: T, gen: JsonGenerator, serializers: SerializerProvider) {
        baseSerializer.serialize(value, gen, serializers)
    }
}