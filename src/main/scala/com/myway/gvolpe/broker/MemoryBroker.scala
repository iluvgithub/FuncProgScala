package com.myway.gvolpe.broker
import cats.effect.kernel.Resource
import cats.effect.std.Queue
import cats.{Applicative, Functor}
import com.myway.gvolpe.broker.Consumer.MsgId
import fs2.Stream

object MemoryBroker {

  def localProducer[F[_]: Applicative, A](queue: Queue[F, Option[A]]): Resource[F, Producer[F, A]] =
    Resource.make[F, Producer[F, A]](
      Applicative[F].pure(
        new Producer[F, A] {
          def send(a: A): F[Unit] = queue.offer(Some(a))

          def send(a: A, properties: Map[String, String]): F[Unit] = send(a)
        }
      )
    )(_ => queue.offer(None))



  def localConsumer[F[_]: Functor, A](queue: Queue[F, Option[A]]): Consumer[F, A] =
    new Consumer[F, A] {
      override def receiveM: fs2.Stream[F, Consumer.Msg[A]] = ???

      override def receiveM(id: MsgId): fs2.Stream[F, Consumer.Msg[A]] = ???

      override def receive: fs2.Stream[F, A] = Stream.fromQueueNoneTerminated(queue)

      override def lastMsgId: F[Option[MsgId]] = ???

      override def ack(id: MsgId): F[Unit] = ???

      override def ack(ids: Set[MsgId]): F[Unit] = ???

      override def nack(id: MsgId): F[Unit] = ???
    }

}
