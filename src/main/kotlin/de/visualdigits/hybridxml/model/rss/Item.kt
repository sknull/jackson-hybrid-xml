package de.visualdigits.hybridxml.model.rss

import de.visualdigits.hybridxml.model.BaseNode

//@JsonIgnoreProperties("content")
class Item(
    val guid: Guid? = null,
    val identifier: String? = null,
    val id: String? = null,

    val itemDate: String? = null,
    val publishDate: String? = null,
    val pubDate: String? = null,

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
    val category: String? = null,
    val isPermaLink: Boolean? = null,
    val enclosure: Enclosure? = null,
    val images: List<Image> = listOf(),

    val comments: MutableList<Comment> = mutableListOf(),
) : BaseNode<Item>()
