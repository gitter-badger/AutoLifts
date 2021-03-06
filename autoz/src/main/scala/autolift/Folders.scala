package autolift

import scalaz.{Foldable, Monoid}

object Folders extends Folders

trait Folders extends FolderImplicits with FolderFunctions

trait FolderImplicits{
	implicit class FolderOps[F[_]: Foldable, A](fa: F[A]){
		def foldWith[B, C](f: B => C)(implicit lift: FoldWith[F[A], B => C]): lift.Out = lift(fa, f)

		def foldComplete(implicit fold: FoldComplete[F[A]]): fold.Out = fold(fa)

		def foldOver[M[_]](implicit fold: FoldOver[M, F[A]]): fold.Out = fold(fa)

		def foldAny[B](f: B => Boolean)(implicit fold: FoldAny[F[A], B => Boolean]): Boolean = fold(fa, f)

		def foldAll[B](f: B => Boolean)(implicit fold: FoldAll[F[A], B => Boolean]): Boolean = fold(fa, f)
	}
}

trait FolderFunctions{
	def foldWith[A, B : Monoid](f: A => B) = new FoldedWith(f)

	sealed class FoldedWith[A, B : Monoid](protected[autolift] val f: A => B){
		def andThen[C >: B, D : Monoid](fw: FoldedWith[C, D]) = new FoldedWith(f andThen fw.f)

		def compose[C, D <: A](fw: FoldedWith[C, D]) = fw andThen this

		def map[C : Monoid](g: B => C): FoldedWith[A, C] = new FoldedWith(f andThen g)

		def apply[That](that: That)(implicit fold: FoldWith[That, A => B]): fold.Out = fold(that, f)
	}

	def foldAny[A](f: A => Boolean) = new FoldedAny[A](f)

	sealed class FoldedAny[A](f: A => Boolean){
		def apply[That](that: That)(implicit fold: FoldAny[That, A => Boolean]): Boolean = fold(that, f)
	}

	def foldAll[A](f: A => Boolean) = new FoldedAny[A](f)

	sealed class FoldedAll[A](f: A => Boolean){
		def apply[That](that: That)(implicit fold: FoldAll[That, A => Boolean]): Boolean = fold(that, f)
	}
}

/**
 * Type class supporting folding over a nested stack of type constructors given a mapping from a type to something which
 * has a Monoid.
 *
 * @author Owein Reese
 *
 * @tparam Obj The types to be folded.
 * @tparam Function The function over which to map and then fold.
 */
trait FoldWith[Obj, Function] extends DFunction2[Obj, Function]

object FoldWith extends LowPriorityFoldWith{
	def apply[Obj, Function](implicit lift: FoldWith[Obj, Function]): Aux[Obj, Function, lift.Out] = lift

	implicit def base[F[_], A, C >: A, B](implicit fold: Foldable[F], ev: Monoid[B]): Aux[F[A], C => B, B] =
		new FoldWith[F[A], C => B]{
			type Out = B

			def apply(fa: F[A], f: C => B) = fold.foldMap(fa)(f)
		}
}

trait LowPriorityFoldWith{
	type Aux[Obj, Function, Out0] = FoldWith[Obj, Function]{ type Out = Out0 }

	implicit def recur[F[_], G, Function, Out0](implicit fold: Foldable[F], 
														 lift: FoldWith.Aux[G, Function, Out0], 
														 ev: Monoid[Out0]): Aux[F[G], Function, Out0] =
		new FoldWith[F[G], Function]{
			type Out = Out0

			def apply(fg: F[G], f: Function) = fold.foldMap(fg){ g: G => lift(g, f) }
		}
}

/**
 * Type class supporting folding over a stack of nested type constructors given that the inner most type has an instance of
 * Monoid.
 *
 * @author Owein Reese
 *
 * @tparam Obj The types to be folded.
 */
trait FoldComplete[Obj] extends DFunction1[Obj]

object FoldComplete extends LowPriorityFoldComplete{
	def apply[Obj](implicit lift: FoldComplete[Obj]): Aux[Obj, lift.Out] = lift

	implicit def base[F[_], A](implicit fold: Foldable[F], ev: Monoid[A]): Aux[F[A], A] =
		new FoldComplete[F[A]]{
			type Out = A

			def apply(fa: F[A]) = fold.fold(fa)
		}
}

trait LowPriorityFoldComplete{
	type Aux[Obj, Out0] = FoldComplete[Obj]{ type Out = Out0 }

	implicit def recur[F[_], G, Out0](implicit fold: Foldable[F], 
											   lift: Aux[G, Out0], 
											   ev: Monoid[Out0]): Aux[F[G], Out0] =
		new FoldComplete[F[G]]{
			type Out = Out0

			def apply(fg: F[G]) = fold.foldMap(fg){ g: G => lift(g) }
		}
}

/**
 * Type class supporting folding on a stack of type constructors up to and included a type `F` but nothing more.
 *
 * @author Owein Reese
 *
 * @tparam F The type constructor up to which folding should occur.
 * @tparam Obj the types to fold over.
 */
trait FoldOver[F[_], Obj] extends DFunction1[Obj]

object FoldOver extends LowPriorityFoldOver{
	def apply[F[_], Obj](implicit fold: FoldOver[F, Obj]): Aux[F, Obj, fold.Out] = fold

	implicit def base[F[_], A, Out0](implicit fold: FoldComplete.Aux[F[A], Out0]): Aux[F, F[A], Out0] =
		new FoldOver[F, F[A]]{
			type Out = Out0

			def apply(fa: F[A]) = fold(fa)
		}
}

trait LowPriorityFoldOver{
	type Aux[F[_], Obj, Out0] = FoldOver[F, Obj]{ type Out = Out0 }

	implicit def recur[F[_], G[_], H, Out0](implicit fold: Foldable[G], 
													 over: FoldOver.Aux[F, H, Out0], 
													 ev: Monoid[Out0]): Aux[F, G[H], Out0] =
		new FoldOver[F, G[H]]{
			type Out = Out0

			def apply(gh: G[H]) = fold.foldMap(gh){ h: H => over(h) }
		}
}

/**
 * Type class supporting checking if all of some type defined by `Function` evaluate to `true` within a nested stack of type
 * constructors.
 *
 * @author Owein Reese
 *
 * @tparam Obj The type constructors over which to evaluate `Function`
 * @tparam Function The boolean producing function which will be iterated over the first applicable type with type stack.
 */
trait FoldAll[Obj, Function] extends ((Obj, Function) => Boolean)

object FoldAll extends LowPriorityFoldAll{
	def apply[Obj, Function](implicit fold: FoldAll[Obj, Function]) = fold

	implicit def base[F[_], A, C >: A](implicit fold: Foldable[F]) =
		new FoldAll[F[A], C => Boolean]{
			def apply(fa: F[A], f: C => Boolean) = fold.all(fa)(f)
		}
}

trait LowPriorityFoldAll{
	implicit def recur[F[_], G, Function](implicit fold: Foldable[F], all: FoldAll[G, Function]) =
		new FoldAll[F[G], Function]{
			def apply(fg: F[G], f: Function) = fold.all(fg){ g: G => all(g, f) }
		}
}

/**
 * Type class supporting checking if at least one of a type defined by `Function` evaluate to `true` within a nested stack of 
 * type constructors.
 *
 * @author Owein Reese
 *
 * @tparam Obj The type constructors over which to evaluate `Function`
 * @tparam Function The boolean producing function which will be iterated over the first applicable type with type stack.
 */
trait FoldAny[Obj, Function] extends ((Obj, Function) => Boolean)

object FoldAny extends LowPriorityFoldAny{
	def apply[Obj, Function](implicit fold: FoldAny[Obj, Function]) = fold

	implicit def base[F[_], A, C >: A](implicit fold: Foldable[F]) =
		new FoldAny[F[A], C => Boolean]{
			def apply(fa: F[A], f: C => Boolean) = fold.any(fa)(f)
		}
}

trait LowPriorityFoldAny{
	implicit def recur[F[_], G, Function](implicit fold: Foldable[F], any: FoldAny[G, Function]) =
		new FoldAny[F[G], Function]{
			def apply(fg: F[G], f: Function) = fold.any(fg){ g: G => any(g, f) }
		}
}