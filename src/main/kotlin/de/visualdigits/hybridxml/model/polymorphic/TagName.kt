package de.visualdigits.hybridxml.model.polymorphic

enum class TagName(
    val label: String
) {

    TEXT("#text"),
    CDATA("#cdata"),
    COMMENT("#comment")
}
