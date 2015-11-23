package autolift.algebird

import autolift._
import autolift.Lifters._
import org.scalatest._
import com.twitter.algebird._
import Algebird._

case class Foo[A](a: A)
object Foo{
	implicit val fun = new Functor[Foo]{
		def map[T, U](m: Foo[T])(fn: T => U): Foo[U] = Foo(fn(m.a))
	}

	implicit val bind = new Monad[Foo]{
		def apply[T](t: T) = Foo(t)
		def flatMap[T, U](foo: Foo[T])(fn: T => Foo[U]) = fn(foo.a)
	}
}

trait BaseSpec extends FlatSpec{
	def same[A](x: A, y: A) = assert(x == y)
}

class LiftMapTest extends BaseSpec{
	val intF = {x: Int => x+1 }
	val anyF = {x: Any => "1" }

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
	"liftMap on a Foo" should "work with functions" in{
		val in = Foo(1)
		val out = in liftMap anyF

		same[Foo[String]](out, Foo("1"))
	}
}

class LiftFlatMapTest extends BaseSpec{
	val intF = {x: Int => Foo(x) }
	val anyF = {x: Any => Foo("1") }

	"liftMap on an Foo[Foo]" should "work" in{
		val in = Foo(Foo(1))
		val out = in liftFlatMap intF

		same[Foo[Foo[Int]]](out, Foo(Foo(2)))
	}
	"liftMap on an Foo" should "work" in{
		val in = Foo(1)
		val out = in liftFlatMap intF

		same[Foo[Int]](out, Foo(2))
	}
	"liftMap on a Foo" should "work with functions" in{
		val in = Foo(1)
		val out = in liftFlatMap anyF

		same[Foo[String]](out, Foo("1"))
	}
}