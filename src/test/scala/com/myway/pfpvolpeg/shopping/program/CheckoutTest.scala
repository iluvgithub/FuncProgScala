package com.myway.pfpvolpeg.shopping.program

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.{catsSyntaxEq => _}
import com.myway.pfpvolpeg.shopping.domain._
import munit.CatsEffectSuite
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.MockitoSugar.mock
import squants.market.Money

class CheckoutTest extends CatsEffectSuite {

  val paymentClient = mock[PaymentClient[IO]]
  val shoppingCart = mock[ShoppingCart[IO]]
  val orders = mock[Orders[IO]]
  val checkout = Checkout(paymentClient, shoppingCart, orders)
  val card = mock[Card]
  val userId = mock[UserId]

  val total: Money = mock[Money]

  test("process with error cart ") {
    // arrange
    when(shoppingCart.get(userId)).thenReturn(IO.raiseError(new RuntimeException("error abc")))
    // act
    val out: IO[OrderId] = checkout.process(userId, card)
    // assert

    out.attempt.map(_.fold(
      t => assertEquals("error abc", t.getMessage),
      _ => fail(""))
    )
  }

  test("process with empty cart ") {
    // arrange

    when(shoppingCart.get(userId)).thenReturn(IO(CartTotal(Nil, total)))
    // act
    val out: IO[OrderId] = checkout.process(userId, card)
    // assert
    out.attempt.map(_.fold(t => assertEquals(EmptyCartError, t), _ => fail("")))
  }

  test("process with non empty cart ") {
    // arrange
    val total = mock[Money]
    val item = mock[Item]
    val items: List[CartItem] = CartItem(item, Quantity(1)) :: Nil
    when(shoppingCart.get(userId)).thenReturn(IO(CartTotal(items, total)))
    when(shoppingCart.delete(userId)).thenReturn(IO(userId))
    val pid = mock[PaymentId]
    when(paymentClient.process(Payment(userId, any(), card))).thenReturn(IO(pid))
    val captor = ArgumentCaptor.forClass(classOf[UserId])
    val orderId = mock[OrderId]

    when(orders.create(userId, pid, NonEmptyList.fromList(items).get, total)).thenReturn(IO(orderId))
    // act
    val out: IO[OrderId] = checkout.process(userId, card)
    // assert
    out.attempt.map(_.fold(
      _ => fail(""),
      actual => {
        assertEquals(orderId, actual)
        verify(shoppingCart).delete(captor.capture())
        assertEquals(userId, captor.getValue)
      }
    )
    )

  }

}
