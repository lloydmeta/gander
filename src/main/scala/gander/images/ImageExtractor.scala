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

import org.jsoup.nodes.Document
import gander.utils.{CanLog, Logging}
import org.slf4j.Logger

/**
  * Created by Jim Plush
  * User: jim
  * Date: 8/18/11
  */
// represents a file stored on disk that we've downloaded
case class LocallyStoredImage(imgSrc: String,
                              localFileName: String,
                              linkhash: String,
                              bytes: Long,
                              fileExtension: String = "",
                              height: Int = 0,
                              width: Int = 0)

trait ImageExtractor extends CanLog {

  protected def doc: Document

  def getBestImage(): Option[Image]

  protected def logPrefix: String = ImageExtractor.loggingPrefix

  protected def critical(msg: String, refs: Any*): Unit = {
    ImageExtractor.critical(msg, refs: _*)
  }

  protected def critical(t: Throwable, msg: String, refs: Any*): Unit = {
    ImageExtractor.critical(t, msg, refs: _*)
  }

  protected def debug(msg: String, refs: Any*): Unit = {
    ImageExtractor.debug(msg, refs: _*)
  }

  protected def debug(t: Throwable, msg: String, refs: Any*): Unit = {
    ImageExtractor.debug(t, msg, refs: _*)
  }

  protected def info(msg: String, refs: Any*): Unit = {
    ImageExtractor.info(msg, refs: _*)
  }

  protected def info(t: Throwable, msg: String, refs: Any*): Unit = {
    ImageExtractor.info(t, msg, refs: _*)
  }

  protected def logger: Logger = ImageExtractor.logger

  protected def trace(msg: String, refs: Any*): Unit = {
    ImageExtractor.trace(msg, refs: _*)
  }

  protected def trace(t: Throwable, msg: String, refs: Any*): Unit = {
    ImageExtractor.trace(t, msg, refs: _*)
  }

  protected def warn(msg: String, refs: Any*): Unit = {
    ImageExtractor.warn(msg, refs: _*)
  }

  protected def warn(t: Throwable, msg: String, refs: Any*): Unit = {
    ImageExtractor.warn(t, msg, refs: _*)
  }
}

object ImageExtractor extends Logging {
  val loggingPrefix = "images: "
}
