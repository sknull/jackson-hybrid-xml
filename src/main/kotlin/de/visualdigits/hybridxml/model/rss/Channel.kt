package de.visualdigits.hybridxml.model.rss

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import de.visualdigits.hybridxml.model.BaseNode

class Channel(
    val version: String? = null,

    val title: String? = null,
    val link: String? = null,
    val description: String? = null,

    val source: String? = null,
    val publisher: String? = null,
    val rights: String? = null,
    val date: String? = null,
    val updatePeriod: String? = null,
    val updateFrequency: String? = null,
    val updateBase: String? = null,
    val broadcasting: String? = null,

    val image: Image? = null,
    val language: String? = null,
    val copyright: String? = null,
    val lastBuildDate: String? = null,
    val docs: String? = null,
    val ttl: Int? = null,
    val itemRefs: List<String> = listOf(),
    @field:JacksonXmlElementWrapper(useWrapping = false) @JsonProperty("item") val items: List<Item> = listOf()
) : BaseNode()
