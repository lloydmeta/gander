package gander

import images.Image
import junit.framework.Assert._
import gander.extractors.AdditionalDataExtractor
import gander.utils.FileHelper
import org.jsoup.nodes.Element

/**
  * Created by Jim Plush
  * User: jim
  * Date: 8/19/11
  */
object TestUtils {

  val staticHtmlDir = "/gander/statichtml/"
  private val NL    = '\n';
  private val TAB   = "\t\t";
  val articleReport = new StringBuilder(
    "=======================::. ARTICLE REPORT .::======================\n");

  val DEFAULT_CONFIG: Configuration  = Configuration.Default
  val NO_IMAGE_CONFIG: Configuration = Configuration.Default

  object additionalExt extends AdditionalDataExtractor {
    override def extract(rootElement: Element) = {
      println()
      println("ADDITIONAL DATA EXTRACTOR CALLED")
      println()
      Map("test" -> "success")
    }
  }

  val ADDITIONAL_DATA_CONFIG = Configuration.Default.copy(additionalDataExtractor = additionalExt)

  /**
    * returns an article object from a crawl
    */
  def getArticle(url: String, rawHTML: String = null)(implicit config: Configuration): Article = {
    val goose = new Gander(config)
    goose.extractArticleData(url, rawHTML).get
  }

  def runArticleAssertions(article: Article,
                           expectedTitle: String = null,
                           expectedStart: String = null,
                           expectedImage: String = null,
                           expectedDescription: String = null,
                           expectedKeywords: String = null): Unit = {
    articleReport.append("URL:      ").append(TAB).append(article.finalUrl).append(NL)
    articleReport.append("TITLE:    ").append(TAB).append(article.title).append(NL)
    article.topImage.foreach { img =>
      articleReport.append("IMAGE:    ").append(TAB).append(img.imageSrc).append(NL)
      articleReport.append("IMGKIND:  ").append(TAB).append(img.imageExtractionType).append(NL)
    }
    articleReport
      .append("CONTENT:  ")
      .append(TAB)
      .append(article.cleanedArticleText.get.replace("\n", "    "))
      .append(NL)
    articleReport.append("METAKW:   ").append(TAB).append(article.metaKeywords).append(NL)
    articleReport.append("METADESC: ").append(TAB).append(article.metaDescription).append(NL)
    articleReport.append("DOMAIN:   ").append(TAB).append(article.domain).append(NL)
    articleReport.append("LINKHASH: ").append(TAB).append(article.linkHash).append(NL)
    articleReport.append("MOVIES:   ").append(TAB).append(article.movies).append(NL)
    articleReport.append("TAGS:     ").append(TAB).append(article.tags).append(NL)

    assertNotNull("Resulting article was NULL!", article)

    if (expectedTitle != null) {
      val title: String = article.title
      assertNotNull("Title was NULL!", title)
      assertEquals("Expected title was not returned!", expectedTitle, title)
    }
    if (expectedStart != null) {
      val articleText: String = article.cleanedArticleText.get
      assertNotNull("Resulting article text was NULL!", articleText)
      assertTrue("Article text was not as long as expected beginning!",
                 expectedStart.length <= articleText.length)
      val actual: String = articleText.substring(0, expectedStart.length)
      assertEquals("The beginning of the article text was not as expected!", expectedStart, actual)
    }
    if (expectedImage != null) {
      val image = article.topImage.get
      assertNotNull("Top image was NULL!", image)
      val src: String = image.imageSrc
      assertNotNull("Image src was NULL!", src)
      assertEquals("Image src was not as expected!", expectedImage, src)
    }
    if (expectedDescription != null) {
      val description: String = article.metaDescription
      assertNotNull("Meta Description was NULL!", description)
      assertEquals("Meta Description was not as expected!", expectedDescription, description)
    }
    if (expectedKeywords != null) {
      val keywords: String = article.metaDescription
      assertNotNull("Meta Keywords was NULL!", keywords)
      assertEquals("Meta Keywords was not as expected!", expectedKeywords, keywords)
    }
  }

  def printReport() {
    println(articleReport)
  }

  def getHtml(filename: String): String = {
    FileHelper.loadResourceFile(TestUtils.staticHtmlDir + filename, Gander.getClass)
  }
}
