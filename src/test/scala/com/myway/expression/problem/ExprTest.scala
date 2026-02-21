package com.myway.expression.problem

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ExprTest extends AnyFunSuite with Matchers {

  val four: Const = Const(4)

  val twoPlusThree: Add[Const, Const] = Add(Const(2), Const(3))

  val twoPlusThreeNegated: Neg[Add[Const, Const]] = Neg(twoPlusThree)

  val twoTimeThree: Mult[Const, Const] = Mult(Const(2), Const(3))

  val twoTimeThreeNegated: Neg[Mult[Const, Const]] = Neg(twoTimeThree)

  test("eval ") {
    // arrange
    def eval[A: Expr: Eval](a: A): Int = implicitly[Eval[A]].eval(a)
    // act & assert
    eval(four) shouldBe 4
    eval(twoPlusThree) shouldBe 5
    eval(twoPlusThreeNegated) shouldBe -5
    eval(twoTimeThree) shouldBe 6
    eval(twoTimeThreeNegated) shouldBe -6
  }

  test("show ") {
    // arrange
    def show[A: Expr: Show](a: A): String = implicitly[Show[A]].show(a)
    //  act & assert
    show(four) shouldBe "4"
    show(twoPlusThree) shouldBe "(2+3)"
    show(twoPlusThreeNegated) shouldBe "-(2+3)"
    show(twoTimeThree) shouldBe "2*3"
    show(twoTimeThreeNegated) shouldBe "-2*3"
  }

}
