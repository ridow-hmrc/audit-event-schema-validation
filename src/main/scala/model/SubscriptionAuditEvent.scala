package model

import generator.*
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

@CipAuditEventSchema(
  schemaFile = "src/main/resources/subscription-audit-event-schema.json"
)
case class SubscriptionAuditEvent(
    eventType: String,
    eventCancelled: Option[Boolean],
    eventDate: LocalDate,
    subscriptionInformation: SubscriptionInformation
)

object SubscriptionAuditEvent {
  implicit val format: OFormat[SubscriptionAuditEvent] =
    JsonValidatorMacro.generateValidatedJson

}
