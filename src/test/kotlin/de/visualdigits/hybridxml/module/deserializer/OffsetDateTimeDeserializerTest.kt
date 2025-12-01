package de.visualdigits.hybridxml.module.deserializer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.format.DateTimeFormatter
import java.util.Locale

class OffsetDateTimeDeserializerTest {

    @Test
    fun testParseDate() {
        val text = "Mon, 01 Dec 2025 15:00:58 +0100"
        val temporal = DateTimeFormatter.ofPattern("EEE, d MMM YYYY HH:mm:ss Z", Locale.US).parse(text)
    }
}