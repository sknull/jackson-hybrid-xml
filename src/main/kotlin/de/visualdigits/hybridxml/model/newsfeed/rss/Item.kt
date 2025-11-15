package de.visualdigits.hybridxml.model.newsfeed.rss

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.newsfeed.atom.Text
import java.time.OffsetDateTime

class Item(
    val guid: Guid? = null,
    val identifier: String? = null,
    val id: String? = null,

    val date: OffsetDateTime? = null, // first publish date time
    val pubDate: OffsetDateTime? = null, // update date time or first publish date time when date is empty

    val about: String? = null,
    val type: String? = null,
    val format: String? = null,
    val source: String? = null,
    val language: String? = null,
    val publisher: String? = null,
    val rights: String? = null,
    val subject: String? = null,
    val audience: String? = null,
    val isFormatOf: String? = null,
    var encoded: Content? = null,
    var content: HtmlContent? = null,
    val topline: String? = null,
    val states: String? = null,

    val title: String? = null,
    val link: String? = null,
    val description: String? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false) @JacksonXmlProperty(localName = "category") val categories: List<Text> = listOf(),
    val isPermaLink: Boolean? = null,
    val enclosure: Enclosure? = null,
    val images: List<Image> = listOf(),

    val comments: MutableList<Comment> = mutableListOf(),
) : BaseNode<Item>()
