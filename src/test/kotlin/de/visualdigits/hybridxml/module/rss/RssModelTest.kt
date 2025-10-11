package de.visualdigits.hybridxml.module.rss

import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.rss.Rss
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.TemporalQueries
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.zip.GZIPInputStream

class RssModelTest {

    @Test
    fun readXmlTest() {
        val rss = BaseNode.readValue<Rss>(File(ClassLoader.getSystemResource("rdf/heise.xml").toURI()))
        val actual = rss.writeValueAsString()
        val expected = File(ClassLoader.getSystemResource("rdf/heise_expected.xml.txt").toURI()).readText()
        assertEquals(expected, actual)
    }

    @Test
    fun readRssStream() {
//        val rss = BaseNode.readValue<Rss>(URI("https://www.ndr.de/nachrichten/hamburg/index~rdf.xml"))
        val rss = BaseNode.readValue<Rss>(File(ClassLoader.getSystemResource("rdf/ndr2.xml").toURI()))
        println(rss.writeValueAsString())
    }

    @Test
    fun testParseDate() {
        val text = "2025-10-08T11:54:16+00:00"
        val date = OffsetDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
        println(date)
    }

    @Test
    fun readUrl() {
        val rss = URI("https://www.ndr.de/nachrichten/hamburg/index~rdf.xml").get(
            headers = mapOf(
                "Accept" to "application/xml",
                "Accept-Encoding" to "gzip"
            )
        )
        println(rss)
    }
}

fun URI.get(
    headers: Map<String, String> = mapOf()
): String {
    val connection = createConnection("GET", headers)
    val response =
        (if (connection.contentEncoding == "gzip") GZIPInputStream(connection.inputStream) else connection.inputStream)
            .use { ins ->
                ins.readAllBytes()
            }
    return String(response)
}

private fun URI.createConnection(
    method: String,
    headers: Map<String, String>,
    doOutput: Boolean = false
): HttpURLConnection {
    val connection = (toURL().openConnection() as HttpURLConnection)
    connection.requestMethod = method
    connection.connectTimeout = 5000
    headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
    if (doOutput) {
        connection.doOutput = true
    }
    return connection
}
