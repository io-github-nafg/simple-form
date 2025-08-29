package io.github.nafg.simpleform

import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object FormTypeTests extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("FormType")(
      test("Simple") {
        case class Person(name: String, age: Int)
        val instance = FormTypeDerive.derived[Person]
        val person   = Person("Mike", 33)
        val encoded  = instance.encode(person)

        assertTrue(
          instance.decode(SimpleForm("name" -> "Mike", "age" -> "33")) == Right(person),
          instance.decode(encoded) == Right(person)
        )
      },
      test("Optional") {
        case class Person(name: String, age: Option[Int])
        val person1  = Person("Mike", Some(33))
        val person2  = Person("Mike", None)
        val instance = FormTypeDerive.derived[Person]
        val encoded1 = instance.encode(person1)
        val encoded2 = instance.encode(person2)

        assertTrue(
          instance.decode(SimpleForm("name" -> "Mike", "age" -> "33")) == Right(person1),
          instance.decode(SimpleForm("name" -> "Mike")) == Right(person2),
          instance.decode(encoded1) == Right(person1),
          instance.decode(encoded2) == Right(person2)
        )
      },
      test("Multiple") {
        case class Person(name: String, pets: List[String])
        val instance = FormTypeDerive.derived[Person]
        val person1  = Person("Mike", List("cat", "dog"))
        val person2  = Person("Mike", Nil)
        val encoded1 = instance.encode(person1)
        val encoded2 = instance.encode(person2)

        assertTrue(
          instance.decode(SimpleForm("name" -> "Mike", "pets" -> "cat", "pets" -> "dog")) == Right(person1),
          instance.decode(SimpleForm("name" -> "Mike")) == Right(person2),
          instance.decode(encoded1) == Right(person1),
          instance.decode(encoded2) == Right(person2)
        )
      }
    )

}
