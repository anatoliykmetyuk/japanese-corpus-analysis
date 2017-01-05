package analysis

import scala.collection.JavaConverters._

import fs2._

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}


object Main {
  implicit val strategy = Strategy.fromFixedDaemonPool(8)
  implicit class StreamableSeq[A](x: Seq[A]) { def asStream[F[_]]: Stream[F, A] = Stream[F, A](x: _*) }
  implicit class FilterableStream[F[_], A](x: Stream[F, A]) { def withFilter(f: A => Boolean): Stream[F, A] = x.filter(f) }

  val indexUrl = "http://stage48.net/studio48/lyricsindex.html"
  val urlResolutionAttempts = 10

  def get(url: String): Stream[Task, Document] = {
    @annotation.tailrec def loop(i: Int): Either[Throwable, Document] =
      try Right { Jsoup.connect(url).get() }
      catch { case t: Throwable => println(s"Exception occurred: $t.getMessage"); if (i > 0) loop(i - 1) else Left(t) }
    
    Stream.eval(Task { loop(urlResolutionAttempts) }).flatMap {
      case Right(doc) => Stream.emit(doc)
      case Left (_  ) => Stream.empty[Task, Document]
    }
  }
  
  val links: Pipe[Task, Document, (String, String)] = s => for {
    index     <- s
    container <- index    .getElementsByClass("two-columns").asScala.asStream
    link      <- container.getElementsByTag  ("a").asScala.asStream

    relativePath = link.attr("href")
    title        = link.text
    if relativePath.nonEmpty && title.nonEmpty
    fullPath     = s"http://stage48.net/studio48/$relativePath"    
  } yield (fullPath, title)

  val songs: Pipe[Task, (String, String), Song] = s => for {
    (path, title) <- s
    songPage      <- get(path)
    container     <- songPage .getElementsByClass("two-columns").asScala.asStream
    columns        = container.getElementsByClass("column").asScala
  } yield {
    println(s"Done processing $title")

    def processColumn(c: Element): List[String] = c.getElementsByTag("p").asScala.map(_.text).toList
    val kanjiVerses       = processColumn(columns(0))
    val romajiVerses      = processColumn(columns(1))
    val translationVerses = processColumn(columns(2))

    Song(title, kanjiVerses, romajiVerses, translationVerses)
  }

  val songsStream: Stream[Task, Song] = get(indexUrl)
    .through(links)
    .through(songs)
}

case class Song(
  title       : String
, kanji       : List[String]
, romaji      : List[String]
, translation : List[String]
)
