package de.visualdigits.hybridxml.model.polymorphic.text

import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode

/**
 * Special node only containing a text, which can have only a parent but no children
 * representing a text element.
 * This includes also text elements found between tags.
 * The deserializer avoid to create text nodes with empty text.
 */
class PolymorphicTextNode(
    text: String? = null,
    parent: PolymorphicNode<*>? = null
) : PolymorphicNode<PolymorphicTextNode>(label = TagName.TEXT.label, parent = parent, text = text)
