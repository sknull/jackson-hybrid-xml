package de.visualdigits.hybridxml.module.polymorphic.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import de.visualdigits.hybridxml.model.html.Html
import de.visualdigits.hybridxml.model.html.Html.Companion.parseHtml
import de.visualdigits.hybridxml.model.polymorphic.PolymorphicNode
import org.jsoup.nodes.Element

/**
 * Jackson deserializer which can deserialie hybrid xml which consists of
 * predefined beans and also contains embedded HTML markup which is not
 * provided as CData.
 *
 * As an example for formats doing this I mention News ML G2 which is a format
 * widely used within the news industry.
 *
 * Problem with this kind of XML is that jackson is under the hood still just a
 * json library and also the jackson-xml module does not know to differentiate between attributes
 * and sub objects. Also it is not possible with jackson-xml to serialize the polymorphic nodes
 * back properly rendering the label as the tag name (there is still no strategy for that within the JsonTypeInfo annotaion).
 *
 * So the idea was to use somehow jsoup to parse the polymorphic part.
 * Another problem with this is that a deserializer has no access to the original xml as it a stream reading api.
 * To work around this we have to inject the raw xml in the deserializer and calculate the snippet to parse with jsoup.
 * As the deserializer context has a mechanism which provides the current location within the raw xml we can use this
 * and take a snapshot before and a second snapshot after consuming the respective subtree utilizing the contexts readTree() method.
 * The calculated snippet obtained from the raw xml can then parsed with jsoup and we end up with a dom like node tree which
 * we can transform afterwards into a desired html node structure.
 *
 * To serialize this structure back we need to make use of dedicated serializers which take care on rendering the label as start object or field name
 * depending on the node type.
 */
class PolymorphicJsonNodeDeserializer(
    private val createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
) : JsonDeserializer<PolymorphicNode<*>>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PolymorphicNode<*>? {
        // obtain the label of the root node which is the current name
        val rootNodeName = p.currentName()

        // obtain snippet from given raw xml
        val snippet = ctxt.readValue(p, String::class.java)

        // now transform jsoups dom tree to the desired html node hierarchy recursively and return the root node
        return when (val html = parseHtml(snippet, rootNodeName, createNodeFunction)) {
            is Html -> html.children.firstOrNull() as PolymorphicNode<*>
            is PolymorphicNode<*> -> html
            else -> null
        }
    }
}
