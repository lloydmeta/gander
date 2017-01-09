package gander

import gander.images.Image
import gander.opengraph.OpenGraphData
import org.joda.time.DateTime
import org.jsoup.nodes.{Document, Element}

/**
  * Created by Lloyd on 1/9/17.
  *
  * Copyright 2017
  */
/**
  * An article
  *
  * @param title of the article
  * @param cleanedArticleText stores the lovely, pure text from the article, stripped of html,
  *                           formatting, etcjust raw text with paragraphs separated by
  *                           newlines. This is probably what you want to use.
  * @param metaDescription description field in HTML source
  * @param metaKeywords field in the HTML source
  * @param canonicalLink of this article if found in the meta data
  * @param domain of this article we're parsing
  * @param topNode holds the top Element we think is a candidate for the main
  *                body of the article
  * @param topImage holds the top Image object that we think represents this article
  * @param tags holds a set of tags that may have been in the article, these are not meta
  *             keywords
  * @param movies holds a list of any movies we found on the page like youtube, vimeo
  * @param finalUrl tores the final URL that we're going to try and fetch content against,
  *                 this would be expanded if any escaped fragments were found in the
  *                 starting url
  * @param linkHash stores the MD5 hash of the url to use for various identification tasks
  * @param rawHtml stores the RAW HTML straight from the network connection
  * @param doc the JSoup Document object
  * @param rawDoc this is the original JSoup document that contains a pure object from the
  *               original HTML without any cleaning options done on it
  * @param publishDate Sometimes useful to try and know when the publish date of an article was
  * @param additionalData A property bucket for consumers of goose to store custom data
  *                       extractions. This is populated by an implementation of
  *                       {@link goose.extractors.AdditionalDataExtractor} which is executed
  *                       before document cleansing within {@link goose.CrawlingActor#crawl}
  * @param openGraphData Facebook Open Graph data that that is found in Article Meta tags
  */
final case class Article(title: String,
                         cleanedArticleText: Option[String],
                         metaDescription: String,
                         metaKeywords: String,
                         canonicalLink: String,
                         domain: String,
                         topNode: Option[Element],
                         topImage: Option[Image],
                         tags: Set[String],
                         movies: List[Element],
                         finalUrl: String,
                         linkHash: String,
                         rawHtml: String,
                         doc: Document,
                         rawDoc: Document,
                         publishDate: Option[DateTime],
                         additionalData: Map[String, String],
                         openGraphData: OpenGraphData)
