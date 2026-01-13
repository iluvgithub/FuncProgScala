package com.myway.pfpvolpeg

trait Counter[F[_]] {
  def incr: F[Unit]
  def get: F[Int]
}