package gander.opengraph

import org.joda.time.DateTime

/**
  * Created by Lloyd on 1/9/17.
  *
  * Copyright 2017
  */
final case class OpenGraphData(title: Option[String],
                               siteName: Option[String],
                               url: Option[String],
                               description: Option[String],
                               image: Option[String],
                               ogType: Option[String],
                               locale: Option[String],
                               author: Option[String],
                               publisher: Option[String],
                               publishedTime: Option[DateTime],
                               modifiedTime: Option[DateTime],
                               tags: Set[String],
                               section: Option[String])
