package de.visualdigits.hybridxml.model.namespaces

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase
import de.visualdigits.hybridxml.model.BaseNode

class XmlNamespacesSerializerModifier : BeanSerializerModifier() {

    override fun modifySerializer(
        config: SerializationConfig,
        beanDesc: BeanDescription,
        serializer: JsonSerializer<*>
    ): JsonSerializer<*> {
        // for unknowm reasons the CollectionAttributeXmlSerializer only works when wrapped by a JsonSerializer
        return if (BaseNode::class.java.isAssignableFrom(beanDesc.beanClass)) {
            JsonSerializerWrapper(
                XmlNamespacesSerializer(
                    serializer as BeanSerializerBase
                )
            )
        } else {
            serializer
        }
    }
}