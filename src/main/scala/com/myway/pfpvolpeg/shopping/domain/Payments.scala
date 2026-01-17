package com.myway.pfpvolpeg.shopping.domain

import squants.market.Money

trait PaymentClient[F[_]] {
  def process(payment: Payment): F[PaymentId]
}
case class Payment(
  id: UserId,
  total: Money,
  card: Card
)
final case class Card(
  name: String,
  number: String,     // 16-digit number, kept as String to preserve formatting
  expiration: String, // MMYY, e.g. "0821"
  cvv: String         // 3-digit CVV
)
