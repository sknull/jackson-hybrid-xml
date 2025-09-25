package de.visualdigits.hybridxml.model.rss

import de.visualdigits.hybridxml.model.BaseNode

class Image(
    val about: String? = null,
    val title: String? = null,
    val link: String? = null,
    val url: String? = null,
    val alt: String? = null,
    val caption: String? = null,
    val source: String? = null,
    val data: String? = null,
    val width: Int? = null,
    val height: Int? = null
) : BaseNode<Image>()
