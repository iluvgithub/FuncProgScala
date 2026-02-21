package com.myway.expression.problem

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ExprTest extends AnyFunSuite with Matchers {

  val four = Const(4)

  val twoPlusThree = Add(Const(2), Const(3))

  val twoPlusThreeNegated = Neg(twoPlusThree)

  test("eval ") {
    // arrange
    def eval[A: Expr: Eval](a: A): Int = implicitly[Eval[A]].eval(a)
    // act & assert
    eval(four) shouldBe 4
    eval(twoPlusThree) shouldBe 5
    eval(twoPlusThreeNegated) shouldBe -5
  }

  test("show ") {
    // arrange
    def show[A: Expr: Show](a: A): String = implicitly[Show[A]].show(a)
    //  act & assert
    show(four) shouldBe "4"
    show(twoPlusThree) shouldBe "(2+3)"
    show(twoPlusThreeNegated) shouldBe "-(2+3)"
  }

}
