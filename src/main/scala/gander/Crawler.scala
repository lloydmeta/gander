/**
  * Licensed to Gravity.com under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  Gravity.com licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package gander

import cleaners.{DocumentCleaner, StandardDocumentCleaner}
import extractors.ContentExtractor
import images.{ImageExtractor, DefaultImageExtractor}
import org.jsoup.nodes.Document
import org.jsoup.Jsoup

import utils.{Logging, URLHelper}
import gander.outputformatters.{OutputFormatter, StandardOutputFormatter}

import scala.util.Try

/**
  * Created by Jim Plush
  * User: jim
  * Date: 8/18/11
  */
case class CrawlCandidate(config: Configuration, url: String, rawHTML: String = null)

class Crawler(config: Configuration) {

  import Crawler._

  def crawl(crawlCandidate: CrawlCandidate): Option[Article] = {
    for {
      parseCandidate <- URLHelper.getCleanedUrl(crawlCandidate.url)
      rawHtml = crawlCandidate.rawHTML
      doc <- getDocument(parseCandidate.url.toString, rawHtml)
    } yield {
      trace("Crawling url: " + parseCandidate.url)

      val extractor       = getExtractor
      val docCleaner      = getDocCleaner
      val outputFormatter = getOutputFormatter

      val finalUrl = parseCandidate.url.toString
      val domain   = parseCandidate.url.getHost
      val linkHash = parseCandidate.linkhash

      val rawDoc = doc.clone()

      val title           = extractor.getTitle(doc)
      val publishDate     = config.publishDateExtractor.extract(doc)
      val additionalData  = config.additionalDataExtractor.extract(doc)
      val metaDescription = extractor.getMetaDescription(doc)
      val metaKeywords    = extractor.getMetaKeywords(doc)
      val canonicalLink   = extractor.getCanonicalLink(doc, finalUrl)
      val tags            = extractor.extractTags(doc)
      val openGraphData   = config.openGraphDataExtractor.extract(doc)
      // before we do any calcs on the body itself let's clean up the document
      val cleanedDoc = docCleaner.clean(doc)

      val topNode = extractor.calculateBestNodeBasedOnClustering(cleanedDoc)
      val movies  = topNode.map(extractor.extractVideos).toList.flatten
      val topImage = {
        val imageExtractor =
          getImageExtractor(linkHash = linkHash, targetUrl = finalUrl, rawDoc = rawDoc)
        for {
          node <- topNode
          tryImg = Try(imageExtractor.getBestImage(rawDoc, node))
          img <- tryImg.toOption
        } yield img
      }
      val cleanedTopNode     = topNode.map(extractor.postExtractionCleanup)
      val cleanedArticleText = topNode.map(outputFormatter.getFormattedText)

      Article(
        title = title,
        cleanedArticleText = cleanedArticleText,
        metaDescription = metaDescription,
        canonicalLink = canonicalLink,
        metaKeywords = metaKeywords,
        domain = domain,
        topNode = cleanedTopNode,
        topImage = topImage,
        tags = tags,
        movies = movies,
        finalUrl = finalUrl,
        linkHash = linkHash,
        rawHtml = rawHtml,
        doc = cleanedDoc,
        rawDoc = rawDoc,
        publishDate = publishDate,
        additionalData = additionalData,
        openGraphData = openGraphData
      )
    }
  }

  def getImageExtractor(linkHash: String, targetUrl: String, rawDoc: Document): ImageExtractor = {
    new DefaultImageExtractor(linkHash = linkHash,
                              targetUrl = targetUrl,
                              rawDoc = rawDoc,
                              config = config)
  }

  def getOutputFormatter: OutputFormatter = {
    StandardOutputFormatter
  }

  def getDocCleaner: DocumentCleaner = {
    new StandardDocumentCleaner
  }

  def getDocument(url: String, rawlHtml: String): Option[Document] = {

    try {
      Some(Jsoup.parse(rawlHtml))
    } catch {
      case e: Exception => {
        trace("Unable to parse " + url + " properly into JSoup Doc")
        None
      }
    }
  }

  def getExtractor: ContentExtractor = {
    config.contentExtractor
  }

}

object Crawler extends Logging {
  val logPrefix = "crawler: "
}
