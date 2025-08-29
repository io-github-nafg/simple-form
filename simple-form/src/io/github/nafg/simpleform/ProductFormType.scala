package io.github.nafg.simpleform

import magnolia1.CaseClass

class ProductFormType[A](ctx: CaseClass[FormType, A]) extends FormType[A] {
  override def encode(value: A): SimpleForm =
    SimpleForm.combine(ctx.parameters.map { param =>
      param.label ->
        param.typeclass.encode(param.dereference(value))
    })

  override def decode(form: SimpleForm, name: String) = {
    val prefix = if (name.isEmpty) "" else name + "."
    ctx
      .constructEither { param =>
        val name = prefix + param.label
        param.typeclass.decode(form, name).left.map(_.map(msg => s"$name: $msg"))
      }
      .left
      .map(_.flatten)
  }
}
