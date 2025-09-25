package de.visualdigits.hybridxml.model.rss

import de.visualdigits.hybridxml.model.BaseNode

class Enclosure(
    val `type`: String? = null,
    val length: Int? = null,
    val url: String? = null
) : BaseNode()
