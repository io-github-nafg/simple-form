package io.github.nafg.simpleform

import scala.annotation.unused
import scala.language.experimental.macros

import magnolia1.{CaseClass, Magnolia}

object FormTypeDerive {
  @unused private type Typeclass[A] = FormType[A]

  def join[A](ctx: CaseClass[FormType, A]): FormType[A] = new ProductFormType[A](ctx)

  def derived[A]: FormType[A] = macro Magnolia.gen[A]
}
