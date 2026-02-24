package generator

import com.github.erosb.jsonsKema.{JsonParser, Schema, SchemaDocumentLoadingException, SchemaLoader, Validator}
import play.api.libs.json.{JsMacroImpl, Json, OFormat}

import java.io.File
import java.time.LocalDate
import scala.quoted.*
import scala.deriving.Mirror
import scala.util.Random

object JsonValidatorMacro:

  val requiredAnnotation = "generator.CipAuditEventSchema"
  inline def generateValidatedJson[T]: OFormat[T] = ${ generateImpl[T] }

  def addQuotations(str:String) = s"\"$str\""
  
  def generateImpl[T: Type](using Quotes): Expr[OFormat[T]] =
    import quotes.reflect.*

    def buildJson(tpe: TypeRepr): String =
      val sym = tpe.typeSymbol

      tpe.asType match
        case '[String] =>
          addQuotations(Random.alphanumeric.take(8).mkString)
        case '[Int] =>
          Random.nextInt(100).toString
        case '[Boolean] =>
          Random.nextBoolean().toString
        case '[Double] =>
          Random.nextDouble().toString
        case '[LocalDate] =>
          addQuotations(LocalDate.now.toString)
        case t if sym.isClassDef && sym.flags.is(Flags.Case) =>
          val fields = sym.caseFields
          val body = fields.map { field =>
            val name = field.name
            val fieldTpe = tpe.memberType(field)
            s"\"$name\": ${buildJson(fieldTpe)}"
          }.mkString(", ")
          s"{ $body }"
        case _ => report.errorAndAbort(s"Unsupported type: ${tpe.show}")
        

    val generatedJson = buildJson(TypeRepr.of[T])

    val schema = loadSchema[T]
    
    if !isValidJson(generatedJson, schema) then
      report.errorAndAbort(s"AuditEvent does not match CIP Schema")

    JsMacroImpl.format[T]

  private def loadSchema[T:Type](using Quotes): Schema = {
    import quotes.reflect.*
    val typeSymbol = TypeRepr.of[T].typeSymbol
    val annot = typeSymbol.annotations.find(_.tpe.show == requiredAnnotation).getOrElse {
      report.errorAndAbort(s"${typeSymbol.name} must be annotated with @$requiredAnnotation")
    }

    val schemaFile: String = annot match {
      case Apply(Select(New(_), _), List(NamedArg("schemaFile", Literal(c)))) => c.value.toString
      case _ => report.errorAndAbort("Could not read schemaFile path from annotation.")
    }
    try {
      SchemaLoader.forURL(new File(schemaFile).toURI.toURL.toString).load() 
    } catch {
      case error: Throwable => report.errorAndAbort(s"Failed to load schema file: $schemaFile")
    }
  }

  private def isValidJson(json: String, schema: Schema)(using Quotes): Boolean = {
    import quotes.reflect.*
    val validator = Validator.forSchema(schema)
    
    val parsedJson = new JsonParser(json).parse()

    val validationResult = Option(validator.validate(parsedJson))
    validationResult.tapEach(f => {
      report.info(s"Failed CIP validation: ${f.toString}")
    })
    validationResult.isEmpty
  }