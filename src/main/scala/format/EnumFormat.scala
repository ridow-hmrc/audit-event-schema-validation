package format

import play.api.libs.json.{Format, JsError, JsString, JsSuccess, OFormat}

import scala.compiletime.summonAll
import scala.deriving.Mirror

object EnumFormat {

  inline def format[E](using m: Mirror.SumOf[E]): Format[E] = {
    val enumInstances =
      summonAll[Tuple.Map[m.MirroredElemTypes, ValueOf]].productIterator.asInstanceOf[Iterator[ValueOf[E]]].map(_.value)

    val enumMap: Map[String, E] = enumInstances.map(o => o.asInstanceOf[Any].toString -> o).toMap

    Format[E](
      {
        case JsString(name) =>
          enumMap
            .get(name)
            .fold(JsError(s"Enum value '$name' is not in allowed list - ${enumMap.keys.mkString(", ")}"))(JsSuccess(_))
        case js =>
          JsError(s"Invalid Json: expected string, got: $js")
      },
      o => JsString(o.asInstanceOf[Any].toString)
    )
  }

}
