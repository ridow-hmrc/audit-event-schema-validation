package model

import generator.*
import play.api.libs.json.{Json, OFormat}

@CipAuditEventSchema(
  schemaFile = "src/main/resources/subscription-audit-event-schema.json"
)
case class SubscriptionAuditEvent(
    eventType: String,
    subscriptionInformation: SubscriptionInformation
)

object SubscriptionAuditEvent {
  implicit val format: OFormat[SubscriptionAuditEvent] =
    JsonValidatorMacro.generateValidatedJson

}
