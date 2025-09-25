package de.visualdigits.hybridxml.model.rss

import de.visualdigits.hybridxml.model.BaseNode

class Comment(
    val submitted: String? = null,
    val title: String? = null,
    val content: String? = null
) : BaseNode()
