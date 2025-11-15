package de.visualdigits.hybridxml.model.newsfeed.unified

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import de.visualdigits.hybridxml.model.BaseNode
import de.visualdigits.hybridxml.model.newsfeed.atom.Entry
import de.visualdigits.hybridxml.model.newsfeed.atom.Feed
import de.visualdigits.hybridxml.model.newsfeed.rss.Item
import de.visualdigits.hybridxml.model.newsfeed.rss.Rss
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.File
import java.net.URI
import java.time.OffsetDateTime

class NewsFeed(
    val title: String? = null,
    val description: String? = null,
    val link: String? = null,
    val image: String? = null,
    val imageTitle: String? = null,
    val imageCaption: String? = null,
    val updated: OffsetDateTime? = null,
    val rights: String? = null,
    val language: String? = null,
    val keywords: List<String>? = null,

    @JacksonXmlElementWrapper(useWrapping = false) @JacksonXmlProperty(localName = "item") val items: List<NewsItem> = listOf()
) : BaseNode<NewsFeed>() {

    companion object {

        fun readValue(uri: URI): NewsFeed {
            return readValue(uri.toURL().readText())
        }

        fun readValue(file: File): NewsFeed {
            return readValue(file.readText())
        }

        private fun readValue(xml: String): NewsFeed {
            val feedType = Jsoup
                .parse(xml, "", Parser.xmlParser())
                .root()
                .select("> *")
                .firstOrNull()
                ?.tagName()
                ?.split(":")
                ?.firstOrNull()
            return when (feedType) {
                "rss", "rdf" -> {
                    val rss = readValue<Rss>(xml)
                    fromRss(rss)
                }
                "feed" -> {
                    val feed = readValue<Feed>(xml)
                    fromFeed(feed)
                }
                else -> error("Unsupported feed type '$feedType'")
            }
        }

        private fun fromRss(rss: Rss): NewsFeed {
            val newsFeed = NewsFeed(
                title = rss.channel?.title,
                description = rss.channel?.description,
                link = rss.channel?.link,
                image = rss.channel?.image?.url,
                imageTitle = rss.channel?.image?.title,
                imageCaption = rss.channel?.image?.caption,
                updated = rss.channel?.lastBuildDate,
                rights = rss.channel?.rights,
                language = rss.channel?.language,
                items = rss.items?.map { item -> processItem(item) }
                    ?: rss.channel?.items?.map { item -> processItem(item) }
                    ?: listOf()
            )

            return newsFeed
        }

        private fun processItem(item: Item): NewsItem {
            val content = item.content?.html
                ?.writeValueAsString(
                    indentOutput = false,
                    writeXmlDeclaration = false
                )
                ?.replace("Html", "html")
                ?.let { html -> StringEscapeUtils.unescapeHtml4(html).trim() }

            var (image, imageTitle, imageCaption) = content?.let { c -> extractImage(c) }?:Triple(null, null, null)
            if (image == null) {
                image = item.enclosure?.url
            }
            val newsItem = NewsItem(
                identifier = item.identifier ?: item.id,
                published = item.date ?: item.pubDate,
                updated = item.pubDate,
                link = item.link,
                title = item.title,
                summary = item.description,
                keywords = item.categories.mapNotNull { category ->  category.text }.filter { c -> c.isNotEmpty() },
                image = image,
                imageTitle = imageTitle,
                imageCaption = imageCaption,
//                content = content // should be the main text
            )
            return newsItem
        }

        private fun fromFeed(feed: Feed): NewsFeed {
            val newsFeed = NewsFeed(
                title = feed.title?.text,
                description = feed.subtitle?.text,
                link = feed.links?.firstOrNull()?.href,
                updated = feed.updated,
                rights = feed.rights,
                keywords = feed.keywords,
                items = feed.entries?.map { entry -> processEntry(entry) } ?: listOf()
            )

            return newsFeed
        }

        private fun processEntry(entry: Entry): NewsItem {
            val content = entry.content?.text ?: ""
            val (image, imageTitle, imageCaption) = extractImage(content)
            return NewsItem(
                identifier = entry.id,
                published = entry.published,
                updated = entry.updated,
                link = entry.link?.href,
                title = entry.title?.text,
                summary = entry.summary?.text,
                keywords = entry.keywords,
                image = image,
                imageTitle = imageTitle,
                imageCaption = imageCaption,
        //                content = content // should be the main text
            )
        }

        private fun extractImage(content: String): Triple<String?, String?, String?> {
            val document = Jsoup.parse(content)
            val image = document.select("img").firstOrNull()
            val url = image?.attr("src")
            val title = image?.attr("title")
            var caption = image?.attr("alt")
            if (caption?.isEmpty() == true) {
                caption = document.select("body").firstOrNull()?.wholeText()?.trim()
                if (caption?.isEmpty() == true) {
                    caption = url?.let { u -> File(URI(u).path).nameWithoutExtension.replace("-", " ") }
                }
            }

            return Triple(url, title, caption)
        }
    }
}

