package model.cip

import format.EnumFormat
import generator.{CipAuditEventSchema, JsonValidatorMacro}
import play.api.libs.json.{Format, Json, OFormat}

import java.time.LocalDate

enum RationalCodeEnum:
  case RA01
  case RA02
  case RA03
  case RA04
  case RA05
  case RA06
  case RA07
  case RA99

object RationalCodeEnum:
  implicit val format: Format[RationalCodeEnum] = EnumFormat.format[RationalCodeEnum]

enum CredentialTypeEnum:
  case account
  case CR
  case IR
  case drivingPermit
  case password
  case `phone-sms`
  case passport
  case email
  case unknown
  case authenticator
  case `verifiable-credential`
  case pin
  case X509
  case `fido2-platform`
  case `fido2-roaming`
  case `fido-u2f`
  case `phone-voice`
  case app
  case NINO
  case FWP

object CredentialTypeEnum:
  implicit val format: Format[CredentialTypeEnum] = EnumFormat.format[CredentialTypeEnum]

enum ReasonAdminEnum:
  case `user changed password via account management`
  case `User aborted session`
  case `User aborted web session`
  case `Web to app handover failure detected`
  case `An application error occurred`
  case `Context no longer available after abort`
  case `The session was aborted while redirecting`
  case `User aborted face to face session`
  case impersonation
  case coercion
  case `account-takeover`
  case `synthetic-identity`
  case `agent-misuse`
  case `third-party-misuse`
  case unspecified
  case `not-set`
  case `service-misuse`
  case stolen
  case lost
  case `credential-compromise`
  case `no account found matching this email`
  case `email value not permitted`
  case `email value already in use`
  case phishing
  case `data-breach`

object ReasonAdminEnum:
  implicit val format: Format[ReasonAdminEnum] = EnumFormat.format[ReasonAdminEnum]

@CipAuditEventSchema(schemaFile =
  "/schemas/one-login/explicitAuditEvents/one-login-fraud-signals/detail/AccountConcernRaised.json"
)
case class AccountConcernRaised(
    oneLoginUserId: String,
    eventTimeframeStart: LocalDate,
    eventTimeframeEnd: LocalDate,
    initiatingEntity: String,
    rationalCode: RationalCodeEnum,
    credentialType: CredentialTypeEnum,
    reasonAdmin: ReasonAdminEnum
)

object AccountConcernRaised:
  implicit val format: OFormat[AccountConcernRaised] = JsonValidatorMacro.generateValidatedJson
