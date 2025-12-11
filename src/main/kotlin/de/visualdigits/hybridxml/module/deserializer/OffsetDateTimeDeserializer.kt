package de.visualdigits.hybridxml.module.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.TemporalQueries
import java.time.temporal.WeekFields
import java.util.Locale

class OffsetDateTimeDeserializer() : JsonDeserializer<OffsetDateTime>() {

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): OffsetDateTime {
        val text = ctxt.readValue(p, String::class.java)
        return parseDateTimeWithWeekday(text)
            ?: parseOffsetDateTime(text)
            ?: parseZonedDateTime(text)
            ?: error("Could not parse fiven string '$text'")
    }

    private fun parseDateTimeWithWeekday(text: String): OffsetDateTime? {
        return try {
            val temporal = DateTimeFormatter.ofPattern("EEE, d MMM YYYY HH:mm:ss Z", Locale.US).parse(text)
            val offset = ZoneOffset.from(temporal)
            val localTime = temporal.query(TemporalQueries.localTime())
            val year = temporal[WeekFields.SUNDAY_START.weekBasedYear()]
            val month = temporal[ChronoField.MONTH_OF_YEAR]
            val day = temporal[ChronoField.DAY_OF_MONTH]
            OffsetDateTime.of(LocalDate.of(year, month, day), localTime, offset)
        } catch (_: Exception) {
            null // by means
        }
    }

    private fun parseOffsetDateTime(text: String): OffsetDateTime? {
        return try {
            OffsetDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
        } catch (_: Exception) {
            null // by means
        }
    }

    private fun parseZonedDateTime(text: String): OffsetDateTime? {
        return try {
            ZonedDateTime.parse(text, DateTimeFormatter.RFC_1123_DATE_TIME).toOffsetDateTime()
        } catch (_: Exception) {
            null // by means
        }
    }
}