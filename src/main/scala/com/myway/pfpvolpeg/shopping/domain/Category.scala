package com.myway.pfpvolpeg.shopping.domain

import java.util.UUID

final case class CategoryId(value: UUID)
final case class CategoryName(name: String)

final case class Category(categoryId: CategoryId,
                          categoryName: CategoryName)
