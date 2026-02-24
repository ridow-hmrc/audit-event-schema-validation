package generator

import com.networknt.schema.SchemaRegistry
import com.networknt.schema.regex.JoniRegularExpressionFactory
import play.api.libs.json.{JsBoolean, JsMacroImpl, JsNumber, JsObject, JsString, JsValue, Json, OFormat}
import tools.jackson.databind.{JsonNode, ObjectMapper}

import java.io.File
import java.net.URI
import java.time.LocalDate
import scala.quoted.*
import scala.deriving.Mirror
import scala.io.Source
import scala.jdk.CollectionConverters._
import scala.util.Random

object JsonValidatorMacro:

  val requiredAnnotation                          = "generator.CipAuditEventSchema"
  inline def generateValidatedJson[T]: OFormat[T] = ${ generateImpl[T] }

  def generateImpl[T: Type](using Quotes): Expr[OFormat[T]] =
    import quotes.reflect.*

    def buildJson(tpe: TypeRepr): JsValue =
      val sym = tpe.typeSymbol

      tpe.asType match
        case '[String] =>
          JsString(Random.alphanumeric.take(8).mkString)
        case '[Int] =>
          JsNumber(Random.nextInt(100))
        case '[Boolean] =>
          JsBoolean(Random.nextBoolean())
        case '[Double] =>
          JsNumber(Random.nextDouble())
        case '[LocalDate] =>
          JsString(LocalDate.now.toString)
        case '[Option[t]] =>
          buildJson(TypeRepr.of[t])
        case t if sym.isClassDef && sym.flags.is(Flags.Case) =>
          val fields = sym.caseFields
          val body   = fields
            .map { field =>
              val name     = field.name
              val fieldTpe = tpe.memberType(field)
              name -> buildJson(fieldTpe)
            }
          JsObject(body)
        case _ => report.errorAndAbort(s"Unsupported type: ${tpe.show}")

    val generatedJson = buildJson(TypeRepr.of[T])

    val schema = loadSchema[T]

    if !isValidJson(generatedJson.toString, schema) then report.errorAndAbort(s"AuditEvent does not match CIP Schema")

    JsMacroImpl.format[T]

  private def loadSchema[T: Type](using Quotes): URI = {
    import quotes.reflect.*
    val typeSymbol = TypeRepr.of[T].typeSymbol
    val annot      = typeSymbol.annotations.find(_.tpe.show == requiredAnnotation).getOrElse {
      report.errorAndAbort(s"${typeSymbol.name} must be annotated with @$requiredAnnotation")
    }

    val schemaFile: String = annot match {
      case Apply(Select(New(_), _), List(NamedArg("schemaFile", Literal(c)))) => c.value.toString
      case _ => report.errorAndAbort("Could not read schemaFile path from annotation.")
    }
    new File(schemaFile).toURI
  }

  private def isValidJson(json: String, schemaLocation: URI)(using Quotes): Boolean = {
    import com.networknt.schema.SchemaRegistryConfig
    import quotes.reflect.*
    val schemaRegistryConfig: SchemaRegistryConfig = SchemaRegistryConfig
      .builder()
      .regularExpressionFactory(JoniRegularExpressionFactory.getInstance())
      .build()

    val schemaRegistry     = SchemaRegistry.builder().schemaRegistryConfig(schemaRegistryConfig).build()
    val schemaStream       = Source.fromURI(schemaLocation).mkString
    val schema             = schemaRegistry.getSchema(schemaStream)
    val objMapper          = new ObjectMapper()
    val jsonnode: JsonNode = objMapper.readTree(json)
    val errors             = schema.validate(jsonnode).asScala

    errors.foreach(f => {
      report.info(s"Failed CIP validation: ${f.toString}")
    })
    errors.isEmpty
  }
