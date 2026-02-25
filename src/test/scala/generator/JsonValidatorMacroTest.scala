package generator

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.*
import org.scalatest.{Inside, Inspectors}

import scala.language.postfixOps

class JsonValidatorMacroTest extends AnyFunSuite with Inside with Inspectors with Matchers:

  test("missing annotation") {
    """import generator.JsonValidatorMacro
        |import play.api.libs.json.{Json, OFormat}
        |
        |case class Subscription(name: String, age:Int, address: Address)
        |
        |case class Address(street: String, postcode: String)
        |
        |object Address:
        |  implicit val format: OFormat[Address] = Json.format[Address]
        |
        |object Subscription:
        |  implicit val format: OFormat[Subscription] = JsonValidatorMacro.generateValidatedJson[Subscription]""".stripMargin shouldNot compile
  }

  test("Schema file not found") {
    """
      |
      |import generator.{CipAuditEventSchema, JsonValidatorMacro}
      |import play.api.libs.json.{Json, OFormat}
      |
      |
      |@CipAuditEventSchema(schemaFile = "/subscription-schema-not-found.json")
      |case class Subscription(name: String, age:Int, address: Address)
      |
      |case class Address(street: String, postcode: String)
      |
      |object Address:
      |  implicit val format: OFormat[Address] = Json.format[Address]
      |
      |object Subscription:
      |  implicit val format: OFormat[Subscription] = JsonValidatorMacro.generateValidatedJson[Subscription]""".stripMargin shouldNot compile
  }

  test("Missing field") {
    """
      |
      |import generator.{CipAuditEventSchema, JsonValidatorMacro}
      |import play.api.libs.json.{Json, OFormat}
      |
      |src/test/resources/subscription-schema.json")
      |case class Subscription(name: String, age:Int, address: Address)
      |
      |case class Address(street: String)
      |
      |object Address:
      |  implicit val format: OFormat[Address] = Json.format[Address]
      |
      |object Subscription:
      |  implicit val format: OFormat[Subscription] = JsonValidatorMacro.generateValidatedJson[Subscription]""".stripMargin shouldNot compile
  }

  test("no compilation errors") {
    """
      |
      |import generator.{CipAuditEventSchema, JsonValidatorMacro}
      |import play.api.libs.json.{Json, OFormat}
      |
      |
      |@CipAuditEventSchema(schemaFile = "/subscription-schema.json")
      |case class Subscription(name: String, age:Int, list: List[String], set: Set[String], address: Address)
      |
      |case class Address(street: String, postcode: String)
      |
      |object Address:
      |  implicit val format: OFormat[Address] = Json.format[Address]
      |
      |object Subscription:
      |  implicit val format: OFormat[Subscription] = JsonValidatorMacro.generateValidatedJson[Subscription]""".stripMargin should compile
  }

  test("additional property") {
    """
      |
      |import generator.{CipAuditEventSchema, JsonValidatorMacro}
      |import play.api.libs.json.{Json, OFormat}
      |
      |
      |@CipAuditEventSchema(schemaFile = "/subscription-schema.json")
      |case class Subscription(name: String, age:Int,list: List[String], set: Set[String], address: Address)
      |
      |case class Address(street: String, postcode: String, country: Option[String])
      |
      |object Address:
      |  implicit val format: OFormat[Address] = Json.format[Address]
      |
      |object Subscription:
      |  implicit val format: OFormat[Subscription] = JsonValidatorMacro.generateValidatedJson[Subscription]""".stripMargin shouldNot compile
  }
