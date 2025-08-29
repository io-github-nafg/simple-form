package io.github.nafg.simpleform

import java.time.*
import java.time.temporal.ChronoUnit

import scala.util.Try

trait FormType[A] {
  def encode(value: A): SimpleForm
  def decode(form: SimpleForm, name: String = ""): Either[List[String], A]

  def run(simpleForm: SimpleForm): Either[(SimpleForm, List[String]), A] = decode(simpleForm).left.map(simpleForm -> _)
}
object FormType   {
  trait StringCodec[A] {
    def encode(value: A): String
    def decode(string: String): Either[List[String], A]

    def xmap[B](f: A => B)(g: B => A): StringCodec[B] =
      new StringCodec[B] {
        def encode(value: B)       = StringCodec.this.encode(g(value))
        def decode(string: String) = StringCodec.this.decode(string).map(f)
      }
  }
  object StringCodec   {
    implicit val boolean: StringCodec[Boolean]                =
      new StringCodec[Boolean] {
        def encode(value: Boolean) = value.toString
        def decode(string: String) = string match {
          case "true" | "yes" | "on"  => Right(true)
          case "false" | "no" | "off" => Right(false)
          case value                  => Left(List(s"$value is not a boolean"))
        }
      }
    implicit val int: StringCodec[Int]                        =
      new StringCodec[Int] {
        def encode(value: Int)     = value.toString
        def decode(string: String) = string.toIntOption.toRight(List(s"$string is not an integer"))
      }
    implicit val long: StringCodec[Long]                      =
      new StringCodec[Long] {
        def encode(value: Long)    = value.toString
        def decode(string: String) = string.toLongOption.toRight(List(s"$string is not an integer"))
      }
    implicit val bigDecimal: StringCodec[BigDecimal]          =
      new StringCodec[BigDecimal] {
        def encode(value: BigDecimal) = value.toString
        def decode(string: String)    =
          Try(BigDecimal(string)).toEither.left.map(_ => List(s"$string is not a BigDecimal"))
      }
    implicit val string: StringCodec[String]                  =
      new StringCodec[String] {
        def encode(value: String)                                = value
        def decode(string: String): Either[List[String], String] = Right(string)
      }
    implicit val localTime: StringCodec[LocalTime]            =
      new StringCodec[LocalTime] {
        def encode(value: LocalTime) = value.toString
        def decode(string: String)   =
          Try(LocalTime.parse(string)).toEither.left.map(_ => List(s"$string is not a LocalTime"))
      }
    implicit val localDate: StringCodec[LocalDate]            =
      new StringCodec[LocalDate] {
        def encode(value: LocalDate) = value.toString
        def decode(string: String)   =
          Try(LocalDate.parse(string)).toEither.left.map(_ => List(s"$string is not a LocalDate"))
      }
    implicit val localDateTime: StringCodec[LocalDateTime]    = new StringCodec[LocalDateTime] {
      def encode(value: LocalDateTime) = value.toString
      def decode(string: String)       =
        Try(LocalDateTime.parse(string)).toEither.left.map(_ => List(s"$string is not a LocalDateTime"))
    }
    implicit val instantAsLocalDateTime: StringCodec[Instant] =
      localDateTime
        .xmap[Instant](_.atZone(ZoneId.systemDefault()).toInstant) { instant =>
          instant
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime
            .truncatedTo(ChronoUnit.MINUTES)
        }
  }

  implicit val boolean: FormType[Boolean] = new FormType[Boolean] {
    def encode(value: Boolean)                 = if (value) SimpleForm("" -> "on") else SimpleForm()
    def decode(form: SimpleForm, name: String) =
      form.get(name) match {
        case Some("on")  => Right(true)
        case None        => Right(false)
        case Some(value) => Left(List(s"Got $value but expected 'on' or nothing"))
      }
  }

  implicit def fromStringDecoder[A](implicit codec: StringCodec[A]): FormType[A] =
    new FormType[A] {
      def encode(value: A)                       = SimpleForm("" -> codec.encode(value))
      def decode(form: SimpleForm, name: String) =
        form.get(name).toRight(List(s"$name not found")).flatMap(codec.decode)
    }

  implicit def option[A](implicit decoder: StringCodec[A]): FormType[Option[A]] = new FormType[Option[A]] {
    override def encode(value: Option[A]): SimpleForm                                    =
      value match {
        case Some(a) => SimpleForm("" -> decoder.encode(a))
        case None    => SimpleForm()
      }
    override def decode(form: SimpleForm, name: String): Either[List[String], Option[A]] =
      form.get(name) match {
        case Some(value) if value.nonEmpty => decoder.decode(value).map(Some(_))
        case _                             => Right(None)
      }
  }

  implicit def list[A](implicit codec: StringCodec[A]): FormType[List[A]] =
    new FormType[List[A]] {
      override def encode(value: List[A]): SimpleForm                                    =
        SimpleForm(value.map(a => "" -> codec.encode(a))*)
      override def decode(form: SimpleForm, name: String): Either[List[String], List[A]] =
        form.values.getOrElse(name, Nil).partitionMap(codec.decode) match {
          case (Nil, rights) => Right(rights.toList)
          case (lefts, _)    => Left(lefts.flatten.toList)
        }
    }

  implicit def optionList[A](implicit formType: FormType[List[A]]): FormType[Option[List[A]]] =
    new FormType[Option[List[A]]] {
      override def encode(value: Option[List[A]]): SimpleForm                                    =
        value.fold(SimpleForm())(formType.encode)
      override def decode(form: SimpleForm, name: String): Either[List[String], Option[List[A]]] =
        formType.decode(form, name).map(Some(_))
    }

  def apply[A](implicit formType: FormType[A]): FormType[A] = formType
}
