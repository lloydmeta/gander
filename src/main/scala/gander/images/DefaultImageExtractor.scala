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
package gander.images

import org.jsoup.select.Elements
import org.jsoup.nodes.{Document, Element}
import gander.Configuration
import java.util.ArrayList

import scala.collection.JavaConversions._
import gander.text.string
import java.net.{MalformedURLException, URL}
import java.util
import java.util.regex.Pattern

import scala.util.Try
import scala.util.control.NonFatal

/**
  * Created by Jim Plush
  * User: jim
  * Date: 8/18/11
  */
final case class DepthTraversal(node: Element, parentDepth: Int, siblingDepth: Int)

/**
  * This image extractor will attempt to find the best image nearest the article.
  * Unfortunately this is a slow process since we're actually downloading the image itself
  * to inspect it's actual height/width and area metrics since most of the time these aren't
  * in the image tags themselves or can be falsified.
  * We'll weight the images in descending order depending on how high up they are compared to the top node content
  *
  * //todo this is a straight java to scala conversion, need to add the nicities of scala, all these null checks make me dizzy
  */
class DefaultImageExtractor(targetUrl: String, protected val doc: Document, config: Configuration)
    extends ImageExtractor {

  /**
    * this lists all the known bad button names that we have
    */
  private val NODE_ID_FORMAT: String = "tag: %s class: %s ID: %s"
  private val KNOWN_IMG_DOM_NAMES    = "yn-story-related-media" :: "cnn_strylccimg300cntr" :: "big_photo" :: "ap-smallphoto-a" :: Nil

  private val matchBadImageNames = {
    val s =
      ".html|.gif|.ico|button|twitter.jpg|facebook.jpg|ap_buy_photo|digg.jpg|digg.png|delicious.png|facebook.png|reddit.jpg|doubleclick|diggthis|diggThis|adserver|/ads/|ec.atdmt.com|mediaplex.com|adsatt|view.atdmt"
    Pattern.compile(s).matcher(string.empty)
  }

  override def getBestImage(): Option[Image] = {
    if (logger.isDebugEnabled) {
      logger.debug("Starting to Look for the Most Relavent Image")
    }
    checkForKnownElements.orElse(checkForMetaTag)
  }

  private def checkForMetaTag: Option[Image] = {
    checkForLinkTag.orElse(checkForOpenGraphTag)
  }

  /**
    * checks to see if we were able to find open graph tags on this page
    *
    * @return
    */
  private def checkForOpenGraphTag: Option[Image] = {
    try {
      val meta: Elements = doc.select("meta[property~=og:image]")
      import scala.collection.JavaConversions._
      for (item <- meta) {
        if (item.attr("content").length < 1) {
          return None
        }
        val imagePath: String = this.buildImagePath(item.attr("content"))
        val r = Some(
          Image(
            imageSrc = imagePath,
            confidenceScore = 100d,
            imageExtractionType = Some("opengraph")
          )
        )
        trace(logPrefix + "open graph tag found, using it")
        return r
      }
      None
    } catch {
      case e: Exception => {
        e.printStackTrace()
        None
      }
    }
  }

  /**
    * checks to see if we were able to find open graph tags on this page
    *
    * @return
    */
  private def checkForLinkTag: Option[Image] = {
    try {
      import scala.collection.JavaConversions._
      val meta: Elements = doc.select("link[rel~=image_src]")
      for (item <- meta) {
        if (item.attr("href").length < 1) {
          return None
        }
        val r = Some(
          Image(
            imageSrc = this.buildImagePath(item.attr("href")),
            confidenceScore = 100d,
            imageExtractionType = Some("linktag")
          )
        )
        trace(logPrefix + "link tag found, using it")
        return r
      }
      None
    } catch {
      case e: Exception => {
        logger.error(e.toString, e)
        None
      }
    }
  }

  def getAllImages: util.ArrayList[Element] = {
    new ArrayList
  }

  def getImagesFromNode(node: Element): Option[Elements] = {
    val images: Elements = node.select("img")

    if (images == null || images.size < 1) {
      None
    } else {
      Some(images)
    }
  }

  def getImageCandidates(node: Element): Option[util.ArrayList[Element]] = {

    for {
      n              <- getNode(node)
      images         <- getImagesFromNode(node)
      filteredImages <- filterBadNames(images)
    } yield filteredImages

  }

  def getDepthLevel(node: Element, parentDepth: Int, siblingDepth: Int): Option[DepthTraversal] = {
    val MAX_PARENT_DEPTH = 2
    if (parentDepth > MAX_PARENT_DEPTH) {
      trace(
        logPrefix + "ParentDepth is greater than %d, aborting depth traversal".format(
          MAX_PARENT_DEPTH))
      None
    } else {
      try {
        val siblingNode = node.previousElementSibling()
        if (siblingNode == null) throw new NullPointerException
        Some(DepthTraversal(siblingNode, parentDepth, siblingDepth + 1))
      } catch {
        case e: NullPointerException => {
          if (node != null) {
            Some(DepthTraversal(node.parent, parentDepth + 1, 0))
          } else {
            None
          }

        }
      }
    }
  }

  def getNode(node: Element): Option[Element] = Option(node)

  /**
    * takes a list of image elements and filters out the ones with bad names
    *
    * @param images
    * @return
    */
  private def filterBadNames(images: Elements): Option[util.ArrayList[Element]] = {
    val goodImages: util.ArrayList[Element] = new util.ArrayList[Element]
    for (image <- images) {
      if (this.isOkImageFileName(image)) {
        goodImages.add(image)
      } else {
        image.remove()
      }
    }
    if (goodImages != null && goodImages.size > 0) Some(goodImages) else None
  }

  /**
    * will check the image src against a list of bad image files we know of like buttons, etc...
    *
    * @return
    */
  private def isOkImageFileName(imageNode: Element): Boolean = {
    var imgSrc: String = imageNode.attr("src")
    if (string.isNullOrEmpty(imgSrc)) {
      return false
    }
    matchBadImageNames.reset(imgSrc)
    if (matchBadImageNames.find) {
      if (logger.isDebugEnabled) {
        logger.debug("Found bad filename for image: " + imgSrc)
      }
      return false
    }
    true
  }

  /**
    * in here we check for known image contains from sites we've checked out like yahoo, techcrunch, etc... that have
    * known  places to look for good images.
    * //todo enable this to use a series of settings files so people can define what the image ids/classes are on specific sites
    */
  def checkForKnownElements: Option[Image] = {

    var knownImage: Element = null
    trace(logPrefix + "Checking for known images from large sites")

    for (knownName <- KNOWN_IMG_DOM_NAMES) {

      try {
        var known: Element = doc.getElementById(knownName)
        if (known == null) {
          known = doc.getElementsByClass(knownName).first
        }
        if (known != null) {
          val mainImage: Element = known.getElementsByTag("img").first
          if (mainImage != null) {
            knownImage = mainImage
            if (logger.isDebugEnabled) {
              logger.debug("Got Image: " + mainImage.attr("src"))
            }
          }
        }

      } catch {
        case NonFatal(e) => {
          if (logger.isDebugEnabled) {
            logger.debug(e.toString, e)
          }
        }
      }
    }
    if (knownImage != null) {
      Try {
        val knownImgSrc: String = knownImage.attr("src")
        Image(imageSrc = this.buildImagePath(knownImgSrc),
              imageExtractionType = Some("known"),
              confidenceScore = 90d)
      }.toOption
    } else {
      if (logger.isDebugEnabled) {
        logger.debug("No known images found")
      }
      None
    }
  }

  /**
    * This method will take an image path and build out the absolute path to that image
    * using the initial url we crawled so we can find a link to the image if they use relative urls like ../myimage.jpg
    *
    * @param image
    * @return
    */
  private def buildImagePath(image: String): String = {
    try {
      val pageURL  = new URL(this.targetUrl)
      val imageURL = new URL(pageURL, image)
      imageURL.toString
    } catch {
      case e: MalformedURLException => {
        logger.error("Unable to get Image Path: " + image)
        image.replace(" ", "%20")
      }
    }
  }

  /**
    * returns true if we think this is kind of a bannery dimension
    * like 600 / 100 = 6 may be a fishy dimension for a good image
    *
    * @param width
    * @param height
    */
  private def isBannerDimensions(width: Int, height: Int): Boolean = {
    if (width == height) {
      return false
    }
    if (width > height) {
      val diff: Float = width.toFloat / height.toFloat
      if (diff > 5) {
        return true
      }
    }
    if (height > width) {
      val diff: Float = height.toFloat / width.toFloat
      if (diff > 5) {
        return true
      }
    }
    false
  }

}
