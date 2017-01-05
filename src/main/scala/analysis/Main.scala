package analysis

import scala.collection.JavaConversions._

import org.jsoup.Jsoup

object Main {
  lazy val indexUrl = "http://stage48.net/studio48/lyricsindex.html"
  lazy val index = Jsoup.connect(indexUrl).get()
  
  lazy val links = for {
    container <- index    .getElementsByClass("two-columns")
    link      <- container.getElementsByTag  ("a")

    relativePath = link.attr("href")
    text         = link.text

    if relativePath.nonEmpty && text.nonEmpty
  } yield Url(relativePath, text)
}

case class Url(link: String, text: String)
