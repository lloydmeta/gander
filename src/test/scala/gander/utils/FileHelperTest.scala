package gander.utils

import org.junit.Test
import org.junit.Assert._
import gander.text.StopWords

/**
* Created by Jim Plush
* User: jim
* Date: 8/16/11
*/

class FileHelperTest {

  @Test
  def loadFileContents() {
    println("loading test")
    val txt = FileHelper.loadResourceFile("stopwords-en.txt", StopWords.getClass)
    assertTrue(txt.startsWith("a's"))
  }

}