/**
Copyright [2014] Robby Pond

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
  */
package gander.extractors

import org.jsoup.nodes.Element

import scala.collection.JavaConversions._
import gander.opengraph.OpenGraphData
import org.joda.time.format.ISODateTimeFormat

import scala.util.Try

class OpenGraphDataExtractor extends Extractor[OpenGraphData] {

  private val dateParser = ISODateTimeFormat.dateTimeParser

  def extract(rootElement: Element): OpenGraphData = {
    val metas = rootElement.select("meta")
    def get(property: String): Option[String] =
      metas.find(_.attr("property") == property).map(_.attr("content"))
    OpenGraphData(
      title = get("og:title"),
      siteName = get("og:site_name"),
      url = get("og:url"),
      description = get("og:description"),
      image = get("og:image"),
      ogType = get("og:type"),
      locale = get("og:locale"),
      author = get("article:author"),
      publisher = get("article:publisher"),
      section = get("article:section"),
      tags = get("article:tag").toSeq.flatMap(_.split(',').map(_.trim)).toSet,
      publishedTime =
        get("article:published_time").flatMap(ds => Try(dateParser.parseDateTime(ds)).toOption),
      modifiedTime =
        get("article:modified_time").flatMap(ds => Try(dateParser.parseDateTime(ds)).toOption)
    )
  }
}
