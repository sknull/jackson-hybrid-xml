package de.visualdigits.hybridxml.model.namespaces

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

/**
 * Injects CollectionAttributeXmlSerializer as base serializer into JsonSerializer.
 */
class JsonSerializerWrapper(
    private val baseSerializer: XmlNamespacesSerializer
) : JsonSerializer<Any>() {

    override fun serialize(value: Any, gen: JsonGenerator, serializers: SerializerProvider) {
        baseSerializer.serialize(value, gen, serializers)
    }
}