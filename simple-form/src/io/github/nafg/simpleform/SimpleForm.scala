package io.github.nafg.simpleform

case class SimpleForm(values: Map[String, Seq[String]] = Map.empty) extends AnyVal {
  def get(name: String) = values.get(name).flatMap(_.lastOption)

  def prefixed(prefix: String) = SimpleForm(values.map {
    case ("", v) => prefix           -> v
    case (k, v)  => prefix + "." + k -> v
  })

  def update(name: String, value: String) = SimpleForm(values.updated(name, Seq(value)))
}

object SimpleForm {
  def apply(values: (String, String)*): SimpleForm   = SimpleForm(values.groupMap(_._1)(_._2))
  def combine(forms: Iterable[(String, SimpleForm)]) =
    SimpleForm(forms.flatMap { case (k, v) => v.prefixed(k).values }.toMap)
}
