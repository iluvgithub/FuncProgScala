package com.myway.pfpvolpeg.shopping.domain

import java.util.UUID

trait Users[F[_]] {
  def find(
            username: UserName
          ): F[Option[UserWithPassword]]

  def create(
              username: UserName,
              password: EncryptedPassword
            ): F[UserId]
}

final case class UserId(value: UUID)
case class UserName(value: String)

case class Password(value: String)

case class EncryptedPassword(value: String)
case class User(id: UserId, name: UserName)
case class UserWithPassword(
                             id: UserId,
                             name: UserName,
                             password: EncryptedPassword
                           )

final case class AdminUser(
                            uuid: UUID,
                            username: String
                          )