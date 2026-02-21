package com.myway.expression.problem

case class Const(c: Int)
case class Add[A, B](l: A, r: B)
trait Expr[A]

trait Eval[A] {
  def eval(a: A)(implicit ev: Expr[A]): Int
}

trait Show[A] {
  def show(a: A)(implicit ev: Expr[A]): String
}
object Const {
  implicit val expr: Expr[Const] = new Expr[Const] {}

  implicit val eval: Eval[Const] = new Eval[Const] {
    def eval(a: Const)(implicit ev: Expr[Const]): Int = a.c
  }

  implicit val show: Show[Const] = new Show[Const] {
    def show(a: Const)(implicit ev: Expr[Const]): String = a.c.toString
  }
}

object Add {
  implicit def expr[A, B](implicit
    leftExpr: Expr[A],
    rightExpr: Expr[B]
  ): Expr[Add[A, B]] = new Expr[Add[A, B]] {}

  implicit def eval[A, B](implicit
    leftExpr: Expr[A],
    rightExpr: Expr[B],
    leftEval: Eval[A],
    rightEval: Eval[B]
  ): Eval[Add[A, B]] = new Eval[Add[A, B]] {
    def eval(a: Add[A, B])(implicit ev: Expr[Add[A, B]]): Int =
      leftEval.eval(a.l) + rightEval.eval(a.r)
  }

  implicit def show[A, B](implicit
    leftExpr: Expr[A],
    rightExpr: Expr[B],
    leftShow: Show[A],
    rightShow: Show[B]
  ): Show[Add[A, B]] = new Show[Add[A, B]] {
    def show(a: Add[A, B])(implicit ev: Expr[Add[A, B]]): String =
      "(" + leftShow.show(a.l) + "+" + rightShow.show(a.r) + ")"
  }
}

// ────────────────────────────────────────────────
// Neg
case class Neg[A](a: A)

object Neg {
  implicit def expr[A](implicit
    subExpr: Expr[A]
  ): Expr[Neg[A]] = new Expr[Neg[A]] {}

  implicit def eval[A](implicit
    subExpr: Expr[A],
    subEval: Eval[A]
  ): Eval[Neg[A]] = new Eval[Neg[A]] {
    def eval(a: Neg[A])(implicit ev: Expr[Neg[A]]): Int =
      -subEval.eval(a.a)
  }

  implicit def show[A](implicit
    subExpr: Expr[A],
    subShow: Show[A]
  ): Show[Neg[A]] = new Show[Neg[A]] {
    def show(a: Neg[A])(implicit ev: Expr[Neg[A]]): String =
      "-" + subShow.show(a.a)
  }
}
