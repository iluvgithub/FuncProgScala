package com.myway.advanced.chap1

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class PrintableTest extends AnyFunSuite {

  case class Cat(name: String, age: Int, color: String)

  implicit val catPrintable: Printable[Cat] = new Printable[Cat] {
    import PrintableInstances._
    override def format(cat: Cat): String =
      s"${Printable.format(cat.name)} is a ${Printable.format(cat.age)} year-old ${Printable.format(cat.color)} cat."
  }
  test("simple print ") {
    // arrange
    val cat = Cat("Snow", 3, "White")
    // act
    val out = Printable.format(cat)
    // assert
    out shouldBe "Snow is a 3 year-old White cat."
  }

  test("Printable Syntax print ") {
    // arrange
    import PrintableSyntax._
    val cat = Cat("Garfield", 35, "ginger and black")
    // act
    val out = cat.format
    // assert
    out shouldBe "Garfield is a 35 year-old ginger and black cat."
    cat.print
  }

  test("Printable Syntax print cats") {
    // arrange
    import cats.Show
    import cats.syntax.show._
    implicit val dateShow: Show[Cat] =
      Show.show(c => s"${c.name.show} is a ${c.age.show} year-old ${c.color.show} cat.")
    val cat = Cat("Garfield", 42, "ginger and black")
    // act
    val out = cat.show
    // assert
    out shouldBe "Garfield is a 42 year-old ginger and black cat."
  }

  test("Eq Int") {
    // arrange
    import cats.Eq
    import cats.instances.int._
    // act
    val eqInt = Eq[Int]
    // assert
    eqInt.eqv(123, 123) shouldBe true
    123 === 123 shouldBe true
    import cats.syntax.eq._
    123 =!= 234 shouldBe true
    import cats.instances.option._
    Some(123) === Some(123) shouldBe true
    import cats.syntax.option._
    1.some =!= None shouldBe true
    1.some =!= 0.some.map(_ + 2) shouldBe true
    Option(123) =!= Option.empty[Int] shouldBe true
    Option(1) =!= Option.empty[Int] shouldBe true
  }

  test("Eq Int 2") {
    import cats.syntax.option._
    1.some === None shouldBe false
    import cats.syntax.eq._
    1.some =!= None shouldBe true
  }
}
