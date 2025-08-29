package io.github.nafg.simpleform

import scala.annotation.unused

import magnolia1.{CaseClass, ProductDerivation}

extension [TC[_], A](p: CaseClass.Param[TC, A])
  inline def dereference(a: A): p.PType = p.deref(a)

object FormTypeDerive extends ProductDerivation[FormType] {
  def join[A](ctx: CaseClass[FormType, A]): FormType[A] = new ProductFormType[A](ctx)
}
