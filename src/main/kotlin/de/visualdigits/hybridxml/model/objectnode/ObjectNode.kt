package de.visualdigits.hybridxml.model.objectnode

import com.fasterxml.jackson.annotation.JsonIgnore
import de.visualdigits.hybridxml.model.BaseNode
import java.lang.reflect.ParameterizedType
import java.lang.reflect.WildcardType

/**
 * Base node for all nodes to be handled with this jackson module.
 * It takes care about calculating indent levels after the complete tree is read.
 * We need this to properly indent any polymorphic stuff within bean objects.
 */
@Suppress("UNCHECKED_CAST")
open class ObjectNode<T : ObjectNode<T>>(
    @JsonIgnore var parent: ObjectNode<*>? = null,
    @JsonIgnore val children: MutableList<ObjectNode<*>> = mutableListOf()
) : BaseNode() {

    @JsonIgnore
    var level: Int = 0

    override fun toString(): String {
        return "${"  ".repeat(level)}${javaClass.simpleName}"
    }

    /**
     * Calculate indent levels for all nodes not being polymorphic
     * in a reflective manner.
     */
    override fun indent(parent: BaseNode?, level: Int) {
        this.level = level
        this.parent = parent as? ObjectNode<*>

        // process all fields which we can directly determine
        val declaredChildren = javaClass.declaredFields
            .filter { field ->
                BaseNode::class.java.isAssignableFrom(field.type)
            }
            .mapNotNull { field ->
                field.isAccessible = true
                (field[this] as? BaseNode)
            }.toMutableList()

        // process lists
        declaredChildren.addAll(javaClass.declaredFields
            .filter { field ->
                val isCandidate = (field.genericType as? ParameterizedType)?.let { pt ->
                    pt.actualTypeArguments.any { ata ->
                        if (ata::class.java == Class::class.java) {
                            ObjectNode::class.java.isAssignableFrom(ata as Class<*>)
                        } else if (WildcardType::class.java.isAssignableFrom(ata::class.java)) {
                            ((ata as WildcardType).upperBounds.any { ub -> (ub as? Class<*>)?.let { c -> ObjectNode::class.java.isAssignableFrom(c) }?:false  })
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
                    (elem as? ObjectNode<*>)
                }
            }.flatten())

        declaredChildren.forEach { bn ->
            bn.indent(this, level + 1)
        }
        this.children.addAll(declaredChildren.filterIsInstance<ObjectNode<*>>())
    }
}