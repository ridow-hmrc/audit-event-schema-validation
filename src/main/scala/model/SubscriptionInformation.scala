package model

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class SubscriptionInformation(name: String, email: String, age: Int)

object SubscriptionInformation:
  implicit val format: OFormat[SubscriptionInformation] = Json.format[SubscriptionInformation]
