package model.cip

import generator.{CipAuditEventSchema, JsonValidatorMacro}
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

@CipAuditEventSchema(schemaFile =
  "/schemas/one-login/explicitAuditEvents/one-login-fraud-signals/detail/AccountConcernRaised.json"
)
case class AccountConcernRaised(oneLoginUserId:String, eventTimeframeStart: LocalDate, eventTimeframeEnd:LocalDate,
                                initiatingEntity:String)

object AccountConcernRaised:
  implicit val format: OFormat[AccountConcernRaised] = JsonValidatorMacro.generateValidatedJson
