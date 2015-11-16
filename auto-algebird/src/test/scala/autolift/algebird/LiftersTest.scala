package autolift.algebird

import autolift._
import autolift.Lifters._
import org.scalatest._
import com.twitter.algebird.Functor
import Algebird._

case class Foo[A](a: A)
object Foo{
	implicit val fun = new Functor[Foo]{
		def map[T, U](m: Foo[T])(fn: T => U): Foo[U] = Foo(fn(m.a))
	}
}

class LiftersTest extends FlatSpec{
	def same[A](x: A, y: A) = assert(x == y)
	val intF = {x: Int => x+1}

	"liftMap on an Foo[Foo]" should "work" in{
		val in = Foo(Foo(1))
		val out = in liftMap intF

		same[Foo[Foo[Int]]](out, Foo(Foo(2)))
	}
	"liftMap on an Foo" should "work" in{
		val in = Foo(1)
		val out = in liftMap intF

		same[Foo[Int]](out, Foo(2))
	}
}