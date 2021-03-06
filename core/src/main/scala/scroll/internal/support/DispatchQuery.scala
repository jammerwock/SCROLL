package scroll.internal.support

import scroll.internal.support.DispatchQuery._

/**
  * Companion object for [[scroll.internal.support.DispatchQuery]] providing
  * some static dispatch functions and a fluent dispatch query creation API.
  */
object DispatchQuery {

  /**
    * Function always returning true
    */
  val anything: Any => Boolean = _ => true
  /**
    * Function always returning false
    */
  val nothing: Any => Boolean = _ => false

  def From(f: Any => Boolean) = new {
    def To(t: Any => Boolean) = new {
      def Through(th: Any => Boolean) = new {
        def Bypassing(b: Any => Boolean): DispatchQuery =
          new DispatchQuery(new From(f), new To(t), new Through(th), new Bypassing(b))
      }
    }
  }

  def Bypassing(b: Any => Boolean): DispatchQuery =
    new DispatchQuery(new From(anything, empty = true), new To(anything, empty = true), new Through(anything, empty = true), new Bypassing(b))

  def empty: DispatchQuery = new DispatchQuery(new From(anything), new To(anything), new Through(anything), new Bypassing(nothing), empty = true)

  /**
    * Dispatch filter selecting the sub-path from the starting edge until the end
    * of the path given as Seq, w.r.t. the evaluation of the selection function.
    *
    * @param sel   the selection function to evaluate on each element of the path
    * @param empty if set to true, the path will be returned unmodified
    */
  private class From(val sel: Any => Boolean, empty: Boolean = false) extends (Seq[Any] => Seq[Any]) {
    override def apply(edges: Seq[Any]): Seq[Any] = empty match {
      case true => edges
      case false => edges.slice(edges.indexWhere(sel), edges.size)
    }
  }

  /**
    * Dispatch filter selecting the sub-path from the last edge until the end
    * of the path given as Seq, w.r.t. the evaluation of the selection function.
    *
    * @param sel   the selection function to evaluate on each element of the path
    * @param empty if set to true, the path will be returned unmodified
    */
  private class To(val sel: Any => Boolean, empty: Boolean = false) extends (Seq[Any] => Seq[Any]) {
    override def apply(edges: Seq[Any]): Seq[Any] = empty match {
      case true => edges
      case false =>
        edges.lastIndexWhere(sel) match {
          case -1 => edges
          case _ => edges.slice(0, edges.lastIndexWhere(sel) + 1)
        }
    }
  }

  /**
    * Dispatch filter to specify which edges to keep on the path given as Seq,
    * w.r.t. the evaluation of the selection function.
    *
    * @param sel   the selection function to evaluate on each element of the path
    * @param empty if set to true, the path will be returned unmodified
    */
  private class Through(sel: Any => Boolean, empty: Boolean = false) extends (Seq[Any] => Seq[Any]) {
    override def apply(edges: Seq[Any]): Seq[Any] = empty match {
      case true => edges
      case false => edges.filter(sel)
    }
  }

  /**
    * Dispatch filter to specify which edges to skip on the path given as Seq,
    * w.r.t. the evaluation of the selection function.
    *
    * @param sel   the selection function to evaluate on each element of the path
    * @param empty if set to true, the path will be returned unmodified
    */
  private class Bypassing(sel: Any => Boolean, empty: Boolean = false) extends (Seq[Any] => Seq[Any]) {
    override def apply(edges: Seq[Any]): Seq[Any] = empty match {
      case true => edges
      case false => edges.filterNot(sel)
    }
  }

}

/**
  * Composed dispatch query, i.e. applying the composition of all dispatch queries the given set of edges
  * through the function ''reorder''.
  * All provided queries must be side-effect free!
  *
  * @param from      query selecting the starting element for the role dispatch query
  * @param to        query selecting the end element for the role dispatch query
  * @param through   query specifying intermediate elements for the role dispatch query
  * @param bypassing query specifying all elements to be left out for the role dispatch query
  */
class DispatchQuery(
                     from: From,
                     to: To,
                     through: Through,
                     bypassing: Bypassing,
                     private val empty: Boolean = false) {
  def isEmpty: Boolean = empty

  def reorder(anys: Seq[Any]): Seq[Any] = isEmpty match {
    case true => anys.distinct.reverse
    case false => from.andThen(to).andThen(through).andThen(bypassing)(anys.distinct).reverse
  }
}