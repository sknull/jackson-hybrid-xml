package de.visualdigits.hybridxml.model.namespaces

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class XmlSchema(
    val prefix: String
)
