package de.visualdigits.hybridxml.module.rss

import de.visualdigits.hybridxml.model.newsfeed.unified.NewsFeed
import org.junit.jupiter.api.Test
import java.io.File

class RssModelTest {

    @Test
    fun readTagesschau() {
//        val newsFeed = NewsFeed.readValue(URI("https://www.tagesschau.de/infoservices/alle-meldungen-100~rss2.xml"))
        val newsFeed = NewsFeed.readValue(File(ClassLoader.getSystemResource("rdf/tagesschau.xml").toURI()))
        println(newsFeed.writeValueAsJsonString())
    }

    @Test
    fun readNtv() {
//        val newsFeed = NewsFeed.readValue(URI("https://www.n-tv.de/rss"))
        val newsFeed = NewsFeed.readValue(File(ClassLoader.getSystemResource("rdf/ntv.xml").toURI()))
        println(newsFeed.writeValueAsJsonString())
    }

    @Test
    fun readNdr() {
//        val newsFeed = NewsFeed.readValue(URI("https://www.ndr.de/nachrichten/hamburg/index~rdf.xml"))
        val newsFeed = NewsFeed.readValue(File(ClassLoader.getSystemResource("rdf/ndr.xml").toURI()))
        println(newsFeed.writeValueAsJsonString())
    }

    @Test
    fun readWdr() {
//        val newsFeed = NewsFeed.readValue(URI("view-source:https://www1.wdr.de/nachrichten/ruhrgebiet/uebersicht-ruhrgebiet-100.feed"))
        val newsFeed = NewsFeed.readValue(File(ClassLoader.getSystemResource("rdf/wdr.xml").toURI()))
        println(newsFeed.writeValueAsJsonString())
    }

    @Test
    fun readHeise() {
//        val newsFeed = NewsFeed.readValue(URI("https://www.heise.de/rss/heise-atom.xml"))
        val newsFeed = NewsFeed.readValue(File(ClassLoader.getSystemResource("rdf/heise.xml").toURI()))
        println(newsFeed.writeValueAsJsonString())
    }

    @Test
    fun readT3n() {
//        val newsFeed = NewsFeed.readValue(URI("https://t3n.de/rss.xml"))
        val newsFeed = NewsFeed.readValue(File(ClassLoader.getSystemResource("rdf/t3n.xml").toURI()))
        println(newsFeed.writeValueAsJsonString())
    }
}
