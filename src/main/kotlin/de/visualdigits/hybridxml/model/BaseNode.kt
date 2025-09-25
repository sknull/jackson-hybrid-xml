package de.visualdigits.hybridxml.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.lang.reflect.ParameterizedType
import java.lang.reflect.WildcardType
import java.util.List

/**
 * Base node for all nodes to be handled with this jackson module.
 * It takes care about calculating indent levels after the complete tree is read.
 * We need this to properly indent any polymorphic stuff within bean objects.
 */
@JsonIgnoreProperties("level")
abstract class BaseNode {

    protected var level: Int = 0

    /**
     * Calculate indent levels for all nodes not being polymorphic
     * in a reflective manner.
     */
    open fun indent(level: Int = 0) {
        this.level = level

        // process all fields which we can directly determine
        val baseNodes = javaClass.declaredFields
            .filter { field ->
                BaseNode::class.java.isAssignableFrom(field.type)
            }
            .mapNotNull { field ->
                field.isAccessible = true
                (field[this] as? BaseNode)
            }.toMutableList()

        // process lists
        baseNodes.addAll(javaClass.declaredFields
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
                    (elem as? BaseNode)
                }
            }.flatten())
        baseNodes.forEach { bn -> bn.indent(level + 1) }
    }

    fun currentLevel(): Int = level

    open fun postProcessXml() {
        // nothing to do here
    }
}
