package com.myway.pfpvolpeg.shopping.domain

import java.util.UUID

trait Brands[F[_]] {
  def findAll: F[List[Brand]]

  def create(name: BrandName): F[BrandId]
}

final case class BrandId(value: UUID)

final case class BrandName(name: String)
final case class Brand(brandId: BrandId, brandName: BrandName)
