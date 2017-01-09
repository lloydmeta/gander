package com.gravity.goose.images

import com.gravity.goose.{Configuration, Article}
import org.jsoup.nodes.{Element, Document}
import java.util.regex.{Pattern, Matcher}
import com.gravity.goose.text.string
import java.net.{MalformedURLException, URL}
import org.jsoup.select.Elements
import scala.collection.JavaConversions._
import java.util.ArrayList
import collection.mutable.ListBuffer
import io.Source

/**
* Created by Jim Plush
* User: jim
* Date: 9/22/11
*/

class UpgradedImageIExtractor(article: Article, config: Configuration) extends ImageExtractor {

  import UpgradedImageIExtractor._

  /**
  * What's the minimum bytes for an image we'd accept is
  */
  private val minBytesForImages: Int = 4000

  /**
  * the webpage url that we're extracting content from
  */
  val targetUrl = article.finalUrl
  /**
  * stores a hash of our url for reference and image processing
  */
  val linkhash = article.linkhash

  /**
  * this lists all the known bad button names that we have
  */
  val matchBadImageNames: Matcher = {
    val sb = new StringBuilder
    // create negative elements
    sb.append(".html|.gif|.ico|button|twitter.jpg|facebook.jpg|ap_buy_photo|digg.jpg|digg.png|delicious.png|facebook.png|reddit.jpg|doubleclick|diggthis|diggThis|adserver|/ads/|ec.atdmt.com")
    sb.append("|mediaplex.com|adsatt|view.atdmt")
    Pattern.compile(sb.toString()).matcher(string.empty)
  }

  def getBestImage(doc: Document, topNode: Element): Image = {
    trace("Starting to Look for the Most Relavent Image")
    checkForKnownElements() match {
      case Some(image) => return image
      case None => {
        trace("No known images found")
      }
    }

    checkForMetaTag match {
      case Some(image) => return image
      case None => trace("No Meta Tag Images found")
    }

    new Image
  }

  private def checkForMetaTag: Option[Image] = {
    checkForLinkTag match {
      case Some(image) => return Some(image)
      case None => trace("No known images found")
    }

    checkForOpenGraphTag match {
      case Some(image) => return Some(image)
      case None => trace("No known images found")
    }

    None
  }

  def getDepthLevel(node: Element, parentDepth: Int, siblingDepth: Int): Option[DepthTraversal] = {
    if (node == null) return None

    val MAX_PARENT_DEPTH = 2
    if (parentDepth > MAX_PARENT_DEPTH) {
      trace("ParentDepth is greater than " + MAX_PARENT_DEPTH + ", aborting depth traversal")
      None
    } else {
      val siblingNode = node.previousElementSibling()
      if (siblingNode == null) {
        Some(DepthTraversal(node.parent, parentDepth + 1, 0))
      } else {
        Some(DepthTraversal(siblingNode, parentDepth, siblingDepth + 1))
      }
    }
  }

  def getAllImages: ArrayList[Element] = {
    null
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
      val diff: Float = (width.asInstanceOf[Float] / height.asInstanceOf[Float])
      if (diff > 5) {
        return true
      }
    }
    if (height > width) {
      val diff: Float = height.asInstanceOf[Float] / width.asInstanceOf[Float]
      if (diff > 5) {
        return true
      }
    }
    false
  }

  def getImagesFromNode(node: Element): Option[Elements] = {
    val images: Elements = node.select("img")

    if (images == null || images.isEmpty) {
      None
    } else {
      Some(images)
    }
  }

  /**
  * takes a list of image elements and filters out the ones with bad names
  *
  * @param images
  * @return
  */
  private def filterBadNames(images: Elements): Option[ArrayList[Element]] = {
    val goodImages: ArrayList[Element] = new ArrayList[Element]
    for (image <- images) {
      if (this.isOkImageFileName(image)) {
        goodImages.add(image)
      }
      else {
        image.remove()
      }
    }
    if (goodImages == null || goodImages.isEmpty) None else Some(goodImages)
  }

  /**
  * will check the image src against a list of bad image files we know of like buttons, etc...
  *
  * @return
  */
  private def isOkImageFileName(imageNode: Element): Boolean = {
    val imgSrc: String = imageNode.attr("src")
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

  def getImageCandidates(node: Element): Option[ArrayList[Element]] = {

    for {
      n <- getNode(node)
      images <- getImagesFromNode(node)
      filteredImages <- filterBadNames(images)
    } {
      return Some(filteredImages)
    }
    None

  }


  def getNode(node: Element): Option[Element] = {
    if (node == null) None else Some(node)
  }

  /**
  * checks to see if we were able to find open graph tags on this page
  *
  * @return
  */
  private def checkForLinkTag: Option[Image] = {
    if (article.rawDoc == null) return None

    try {
      val meta: Elements = article.rawDoc.select("link[rel~=image_src]")
      for (item <- meta) {
        val href = item.attr("href")
        if (href.isEmpty) {
          return None
        }
        val mainImage = new Image
        mainImage.imageSrc = buildImagePath(href)
        mainImage.imageExtractionType = "linktag"
        mainImage.confidenceScore = 100

        trace("link tag found, using it")

        return Some(mainImage)
      }
      None
    }
    catch {
      case e: Exception => {
        warn("Unexpected exception caught in checkForLinkTag. Handled by returning None.", e)
        None
      }
    }

  }

  /**
  * checks to see if we were able to find open graph tags on this page
  *
  * @return
  */
  private def checkForOpenGraphTag: Option[Image] = {
    try {
      val meta: Elements = article.rawDoc.select("meta[property~=og:image]")

      for (item <- meta) {
        if (item.attr("content").length < 1) {
          return None
        }
        val imagePath: String = this.buildImagePath(item.attr("content"))
        val mainImage = new Image
        mainImage.imageSrc = imagePath
        mainImage.imageExtractionType = "opengraph"
        mainImage.confidenceScore = 100
        trace("open graph tag found, using it: %s".format(imagePath))
        return Some(mainImage)
      }
      None
    }
    catch {
      case e: Exception => {
        warn(e, e.toString)
        None
      }
    }
  }



  def getCleanDomain = {
    // just grab the very end of the domain
    dotRegex.split(article.domain).takeRight(2).mkString(".")
  }

  /**
  * in here we check for known image contains from sites we've checked out like yahoo, techcrunch, etc... that have
  * known  places to look for good images.
  * //todo enable this to use a series of settings files so people can define what the image ids/classes are on specific sites
  */
  def checkForKnownElements(): Option[Image] = {
    if (article.rawDoc == null) return None

    val domain = getCleanDomain
    customSiteMapping.get(domain).foreach(classes => {
      subDelimRegex.split(classes).foreach(c => KNOWN_IMG_DOM_NAMES += c)
    })

    var knownImage: Element = null
    trace("Checking for known images from large sites")

    for (knownName <- KNOWN_IMG_DOM_NAMES; if (knownImage == null)) {
      var known: Element = article.rawDoc.getElementById(knownName)
      if (known == null) {
        known = article.rawDoc.getElementsByClass(knownName).first
      }
      if (known != null) {
        val mainImage: Element = known.getElementsByTag("img").first
        if (mainImage != null) {
          knownImage = mainImage
          trace("Got Known Image: " + mainImage.attr("src"))
        }
      }
    }

    if (knownImage == null) return None

    val knownImgSrc: String = knownImage.attr("src")
    val mainImage = new Image
    mainImage.imageSrc = buildImagePath(knownImgSrc)
    mainImage.imageExtractionType = "known"
    mainImage.confidenceScore = 90

    Some(mainImage)
  }

  /**
  * This method will take an image path and build out the absolute path to that image
  * using the initial url we crawled so we can find a link to the image if they use relative urls like ../myimage.jpg
  *
  * @param imageSrc
  * @return
  */
  private def buildImagePath(imageSrc: String): String = {

    try {
      val pageURL = new URL(this.targetUrl)
      return new URL(pageURL, ImageUtils.cleanImageSrcString(imageSrc)).toString
    }
    catch {
      case e: MalformedURLException => {
        warn("Unable to get Image Path: " + imageSrc)
      }
    }

    imageSrc
  }


}

object UpgradedImageIExtractor {
  val delimRegex = """\^""".r
  val dotRegex = """\.""".r
  val subDelimRegex = """\|""".r

  // custom site mapping is for major sites that we know what they generally
  // place images into, allows for higher accuracy of image extraction
  lazy val customSiteMapping = {
    val lines = Source.fromInputStream(getClass.getResourceAsStream("/com/gravity/goose/images/known-image-css.txt")).getLines()
    (for (line <- lines) yield {
      val Array(domain, css) = delimRegex.split(line)
      domain -> css
    }).toMap
  }

  val KNOWN_IMG_DOM_NAMES = ListBuffer("yn-story-related-media", "cnn_strylccimg300cntr", "big_photo", "ap-smallphoto-a")

}