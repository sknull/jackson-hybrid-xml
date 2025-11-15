package de.visualdigits.hybridxml.model.newsfeed.unified

import de.visualdigits.hybridxml.model.BaseNode
import java.time.OffsetDateTime

class NewsItem(
    val identifier: String? = null,

    val published: OffsetDateTime? = null,
    val updated: OffsetDateTime? = null,

    val link: String? = null,

    val title: String? = null,
    val summary: String? = null,
    val keywords: List<String>? = null,
    val image: String? = null,
    val imageTitle: String? = null,
    val imageCaption: String? = null,
    val content: String? = null,
) : BaseNode<NewsItem>()