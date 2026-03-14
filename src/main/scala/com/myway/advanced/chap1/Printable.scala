package com.myway.advanced.chap1

trait Printable[A] {
  def format(a: A): String
}

object Printable {

  def format[A](a: A)(implicit printable: Printable[A]): String = printable.format(a)

  def print[A](a: A)(implicit printable: Printable[A]): Unit = println(format(a))
}

object PrintableInstances {

  implicit val stringPrintable: Printable[String] = new Printable[String] {
    override def format(a: String): String = a
  }

  implicit val intPrintable: Printable[Int] = new Printable[Int] {
    override def format(a: Int): String = s"$a"
  }

}

object PrintableSyntax {
  implicit class PrintOps[A](a: A) {
    def format(implicit printable: Printable[A]): String = Printable.format(a)
    def print(implicit printable: Printable[A]): Unit    = Printable.print(a)
  }
}
