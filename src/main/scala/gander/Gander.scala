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

import gander.cleaners.StandardDocumentCleaner
import gander.outputformatters.StandardOutputFormatter

/**
  * Created by Jim Plush - Gravity.com
  * Date: 8/14/11
  */
class Gander(config: Configuration = Configuration.Default) {

  /**
    * Main method to extract an article object from a URL, pass in a url and get back a Article
    * @url The url that you want to extract
    */
  def extractArticleData(url: String, rawHTML: String): Option[Article] = {
    val cc      = new CrawlCandidate(config, url, rawHTML)
    val crawler = new Crawler(config)
    val article = crawler.crawl(cc)
    article
  }

  /**
    * Just extracts the text without doing all the heavy lifting like in extract article data
    * @param rawHtml
    * @return
    */
  def extractText(rawHtml: String): Option[String] = {
    for {
      doc <- Crawler.getDocument(rawHtml)
      cleanedDoc = Gander.docCleaner.clean(doc)
      topNode <- config.contentExtractor.calculateBestNodeBasedOnClustering(cleanedDoc)
    } yield StandardOutputFormatter.getFormattedText(topNode)
  }

}

object Gander {

  private val logPrefix  = "goose: "
  private val docCleaner = new StandardDocumentCleaner

}
