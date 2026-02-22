package com.myway.io.tagless

import cats.effect.IO
import munit.CatsEffectSuite
import org.mockito.MockitoSugar.mock

class ItemsTest extends CatsEffectSuite {

  test("add item") {
    // arrange
    val itemCounterIo: IO[ItemsCounter[IO]] = for {
      counter <- Counter.make[IO]
      items   <- Items.make[IO]
    } yield new ItemsCounter(counter, items)
    val item = mock[Item]
    // act
    val io = for {
      itemCounter <- itemCounterIo
      _           <- itemCounter.addItem(item)
      _           <- itemCounter.addItem(item)
      out         <- itemCounter.countItems
    } yield out
    // assert
    io.map(res => assertEquals(res, 2))
  }

}
