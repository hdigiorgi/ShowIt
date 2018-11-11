package controllers

import scala.util.{Failure, Success, Try}
import scala.reflect.runtime.universe.{TypeTag, typeOf}

abstract class UrlFormDecoder(map: Map[String, Seq[String]]) {
  protected def S(key: String): Try[String] = getFromMapN(key,_.self)
  protected def F(key: String): Try[Float] = getFromMapN(key,_.toFloat)

  protected def getFromMap(key: String): Try[Seq[String]] = {
    map.get(key) match {
      case None => Failure(new RuntimeException(f"key '$key' not found in $map"))
      case Some(x) => Success(x)
    }
  }

  protected def getFromMapN[A: TypeTag](key: String, cast: String => A, pos: Integer = 0): Try[A] = {
    getFromMap(key).flatMap{seq =>
      if(pos >= seq.length) Failure(new IndexOutOfBoundsException(f"Cant read item at position $pos for key '$key' in $seq")) else {
        val elementString = seq(pos)
        Try(cast(elementString)) match {
          case Failure(castThrowable) =>
            val typeName = typeOf[A].typeSymbol.fullName
            Failure(new RuntimeException(f"can't cast the value '$elementString' to $typeName at position $pos for key '$key' in $seq with $castThrowable"))
          case Success(value) => Success(value)
        }
      }
    }
  }
}
