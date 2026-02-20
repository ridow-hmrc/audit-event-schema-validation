import generator.CipAuditEventSchema
import model.{SubscriptionAuditEvent, SubscriptionInformation}
import play.api.libs.json.{Json, OFormat}


@main def hello(): Unit = {
  println("Hello world!")
  val instance = SubscriptionAuditEvent("event", SubscriptionInformation("name", "email", 15))
  println(Json.toJson(instance))
}




