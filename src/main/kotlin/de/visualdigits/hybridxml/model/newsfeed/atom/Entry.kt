package de.visualdigits.hybridxml.model.newsfeed.atom


import de.visualdigits.hybridxml.model.BaseNode
import java.time.OffsetDateTime

class Entry(
    val title: Text? = null,
    val id: String? = null,
    val updated: OffsetDateTime? = null,
    val published: OffsetDateTime? = null,
    val link: Link? = null,
    val author: Author? = null,
    val tags: String? = null,
    val keywords: List<String>? = tags?.split(",")?.map { t -> t.trim() }?.filter { t -> t.isNotEmpty() },
    val summary: Text? = null,
    val content: Text? = null
) : BaseNode<Entry>()