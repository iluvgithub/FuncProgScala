package com.myway.advanced.chap1

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper;

class PrintableTest extends AnyFunSuite {

  final case class Cat(name: String, age: Int, color: String)
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
      Show.show(
       c=> s"${c.name.show} is a ${c.age.show} year-old ${c.color.show} cat."
      )
    val cat = Cat("Garfield", 42, "ginger and black")
    // act
    val out = cat.show
    // assert
    out shouldBe "Garfield is a 42 year-old ginger and black cat."
  }
}
