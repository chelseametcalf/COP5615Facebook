import spray.routing.PathMatchers._

class Identifier(id : String) {
  def this(id : Long) = this(id.toString)
  def asInt = id

  override def toString = id

  override def equals(that : Any) = that match {
    case that: Identifier => this.hashCode == that.hashCode
    case _ => false
  }
  override def hashCode = id.hashCode
}

// A custom Facebook ID type to allow for more complex IDs later
object FBID extends NumberMatcher[Int](Int.MaxValue, 10) {
  def fromChar(c: Char) = fromDecimalChar(c)
}