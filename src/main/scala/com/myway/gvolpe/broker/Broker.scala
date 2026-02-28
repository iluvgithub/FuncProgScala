package com.myway.gvolpe.broker

import fs2.Stream

trait Producer[F[_], A] {
  def send(a: A): F[Unit]
  def send(a: A, properties: Map[String, String]): F[Unit]
}

trait Acker[F[_], A] {
  def ack(id: Consumer.MsgId): F[Unit]
  def ack(ids: Set[Consumer.MsgId]): F[Unit]
  def nack(id: Consumer.MsgId): F[Unit]
}

trait Consumer[F[_], A] extends Acker[F, A] {
  def receiveM: Stream[F, Consumer.Msg[A]]
  def receiveM(id: Consumer.MsgId): Stream[F, Consumer.Msg[A]]
  def receive: Stream[F, A]
  def lastMsgId: F[Option[Consumer.MsgId]]
}

object Consumer {
  type MsgId      = String
  type Properties = Map[String, String]

  final case class Msg[A](
    id: MsgId,
    props: Properties,
    payload: A
  )
}
