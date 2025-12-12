package de.visualdigits.hybridxml.module.common

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase
import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import de.visualdigits.hybridxml.module.namespaces.serializer.XmlNamespacesSerializer
import de.visualdigits.hybridxml.module.polymorphic.serializer.PolymorphicXmlNodeSerializer

class XmlNodeSerializerModifier(
    val indentAmount: Int = 2,
    val writeHtmlDeclaration: Boolean = false
) : BeanSerializerModifier() {

    var polymorphicXmlNodeSerializer: PolymorphicXmlNodeSerializer? = null

    override fun modifySerializer(
        config: SerializationConfig,
        beanDesc: BeanDescription,
        serializer: JsonSerializer<*>
    ): JsonSerializer<*> {
        // for unknowm reasons the CollectionAttributeXmlSerializer only works when wrapped by a JsonSerializer
        return if (PolymorphicNode::class.java.isAssignableFrom(beanDesc.beanClass)) {
            this.polymorphicXmlNodeSerializer = PolymorphicXmlNodeSerializer(
                serializer as BeanSerializerBase,
                indentAmount,
                writeHtmlDeclaration
            )
            JsonSerializerWrapper<Any>(this.polymorphicXmlNodeSerializer!!)
        } else if (BaseNode::class.java.isAssignableFrom(beanDesc.beanClass)) {
            JsonSerializerWrapper<Any>(
                XmlNamespacesSerializer(
                    serializer as BeanSerializerBase
                )
            )
        } else {
            serializer
        }
    }
}