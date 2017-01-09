package gander

import org.junit.Test
import org.junit.Assert._

/**
 * Created by Jim Plush
 * User: jim
 * Date: 8/14/11
 */

class GanderTest {

  @Test
  def badlink() {
    implicit val config = new Configuration
    val url = "http://nolove888.com/2011/08/13/LINKNOTEXISTS"
    val goose = new Gander(config)
    val article = goose.extractContent(url)
    assertNull(article.topNode)
  }


}
