package de.visualdigits.hybridxml.model.namespaces

@Target(AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.RUNTIME)
annotation class XmlNamespace(
    val prefix: String,
    val namespaceUri: String
)
