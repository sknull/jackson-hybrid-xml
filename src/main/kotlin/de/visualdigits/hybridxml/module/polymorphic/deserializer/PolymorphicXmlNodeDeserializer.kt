package de.visualdigits.hybridxml.module.polymorphic.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
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
class PolymorphicXmlNodeDeserializer(
    private val xml: String,
    private val createNodeFunction: ((label: String?, element: Element?, node: PolymorphicNode<*>?, children: List<PolymorphicNode<*>>, text: String?) -> PolymorphicNode<*>?)? = null
) : JsonDeserializer<PolymorphicNode<*>>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PolymorphicNode<*>? {
        // find the last opening char in the part of the raw xml up to the current location
        // which marks the beginning of the snipet to parse with jsoup
        val start = xml.take(p.currentLocation().charOffset.toInt()).indexOfLast { c -> c == '<' }

        // obtain the label of the root node which is the current name
        val rootNodeName = p.currentName()

        ctxt.readTree(p) // Just consume the complete subtree for this node to obtain parser location.
                         // With this we also avoid jackson having access to this part of the tree
                         // as we want to process it ourselves using jsoup.

        // the end location is just the current location after having consumed the subtree
        val end = p.currentLocation().charOffset.toInt()

        // obtain snippet from given raw xml
        val snippet = xml.substring(start, end)

        // now transform jsoups dom tree to the desired html node hierarchy recursively and return the root node
        return parseHtml(snippet, rootNodeName, createNodeFunction)
    }
}
