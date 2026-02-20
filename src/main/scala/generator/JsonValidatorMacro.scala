package generator

import com.github.erosb.jsonsKema.{JsonParser, Schema, SchemaLoader, Validator}
import play.api.libs.json.{JsMacroImpl, Json, OFormat}

import java.io.File
import scala.quoted.*
import scala.deriving.Mirror
import scala.util.Random

object JsonValidatorMacro:

  val requiredAnnotation = "generator.CipAuditEventSchema"
  // 1. The Entry Point
  inline def generateValidatedJson[T]: OFormat[T] = ${ generateImpl[T] }

  def generateImpl[T: Type](using Quotes): Expr[OFormat[T]] =
    import quotes.reflect.*

    // 2. Recursive helper to build the JSON string at compile-time
    def buildJson(tpe: TypeRepr): String =
      val sym = tpe.typeSymbol

      if tpe =:= TypeRepr.of[String] then
        s"\"${Random.alphanumeric.take(8).mkString}\""
      else if tpe =:= TypeRepr.of[Int] then
        Random.nextInt(100).toString
      else if sym.isClassDef && sym.flags.is(Flags.Case) then
        val fields = sym.caseFields
        val body = fields.map { field =>
          val name = field.name
          val fieldTpe = tpe.memberType(field)
          s"\"$name\": ${buildJson(fieldTpe)}"
        }.mkString(", ")
        s"{ $body }"
      else
        report.errorAndAbort(s"Unsupported type: ${tpe.show}")

    val generatedJson = buildJson(TypeRepr.of[T])

    val schema = loadSchema[T]
    // 3. YOUR VALIDATION LOGIC
    if !isValidJson(generatedJson, schema) then
      report.errorAndAbort(s"AuditEvent does not match CIP Schema")

    JsMacroImpl.format[T]

  def loadSchema[T:Type](using Quotes): Schema = {
    import quotes.reflect.*
    val typeSymbol = TypeRepr.of[T].typeSymbol
    val annot = typeSymbol.annotations.find(_.tpe.show == requiredAnnotation).getOrElse {
      report.errorAndAbort(s"${typeSymbol.name} must be annotated with @$requiredAnnotation")
    }

    val schemaFile: String = annot match {
      case Apply(Select(New(_), _), List(NamedArg("schemaFile", Literal(c)))) => c.value.toString
      case _ => report.errorAndAbort("Could not read schemaFile path from annotation.")
    }


     SchemaLoader.forURL(new File(schemaFile).toURI.toURL.toString).load()

  }
  
  def isValidJson(json: String, schema: Schema): Boolean = {
    val validator = Validator.forSchema(schema)

    val parsedJson = new JsonParser(json).parse()

    val validationResult = Option(validator.validate(parsedJson))

    validationResult.isEmpty
  }