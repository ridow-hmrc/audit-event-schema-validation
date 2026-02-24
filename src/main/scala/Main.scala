import generator.CipAuditEventSchema
import model.{SubscriptionAuditEvent, SubscriptionInformation}
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

@main def hello(): Unit = {
  println("Hello world!")
  val instance =
    SubscriptionAuditEvent("event", Option(false), LocalDate.now(), SubscriptionInformation("name", "email", 15))
  println(Json.toJson(instance))
}
