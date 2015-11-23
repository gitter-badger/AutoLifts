package autolift.algebird

import autolift._
import com.twitter.algebird._
import export._

trait AlgeLiftF[Obj, Fn] extends LiftF[Obj, Fn]

@exports(Subclass)
object AlgeLiftF extends LowPriorityAlgeLiftF{
	def apply[Obj, Fn](implicit lift: AlgeLiftF[Obj, Fn]): Aux[Obj, Fn, lift.Out] = lift

	@export(Subclass)
	implicit def base[F[_], A, C >: A, B](implicit functor: Functor[F]): Aux[F[A], C => B, F[B]] =
		new AlgeLiftF[F[A], C => B]{
			type Out = F[B]

			def apply(fa: F[A], f: C => B) = functor.map(fa)(f)
		}
}

trait LowPriorityAlgeLiftF{
	type Aux[Obj, Fn, Out0] = AlgeLiftF[Obj, Fn]{ type Out = Out0 }

	@export(Subclass)
	implicit def recur[F[_], G, Fn](implicit functor: Functor[F], lift: LiftF[G, Fn]): Aux[F[G], Fn, F[lift.Out]] =
		new AlgeLiftF[F[G], Fn]{
			type Out = F[lift.Out]

			def apply(fg: F[G], f: Fn) = functor.map(fg){ g: G => lift(g, f) }
		}
}

//TODO: applicative using joinWith ...

trait AlgeLiftB[Obj, Fn] extends LiftB[Obj, Fn]

@exports(Subclass)
object AlgeLiftB extends LowPriorityAlgeLiftB {
	def apply[Obj, Fn](implicit lift: AlgeLiftB[Obj, Fn]): Aux[Obj, Fn, lift.Out] = lift

	@export(Subclass)
	implicit def base[M[_], A, C >: A, B](implicit fm: Monad[M]): Aux[M[A], C => M[B], M[B]] =
		new AlgeLiftB[M[A], C => M[B]]{
			type Out = M[B]

			def apply(fa: M[A], f: C => M[B]) = fm.flatMap(fa)(f)
		}
}

trait LowPriorityAlgeLiftB{
	type Aux[Obj, Fn, Out0] = LiftB[Obj, Fn]{ type Out = Out0 }

	@export(Subclass)
	implicit def recur[F[_], G, Fn](implicit functor: Functor[F], lift: LiftB[G, Fn]): Aux[F[G], Fn, F[lift.Out]] =
		new AlgeLiftB[F[G], Fn]{
			type Out = F[lift.Out]

			def apply(fg: F[G], f: Fn) = functor.map(fg){ g: G => lift(g, f) }
		}
}

trait AlgeLiftFlatten[M[_], Obj] extends LiftFlatten[M, Obj]

//export-hook bug, uncomment when Miles works his typical wizardry.
/*@exports(Subclass)
object AlgeLiftFlatten extends LowPriorityAlgeLiftFlatten{
	def apply[M[_], Obj](implicit lift: AlgeLiftFlatten[M, Obj]): Aux[M, Obj, lift.Out] = lift

	@export(Subclass)
	implicit def base[M[_], A](implicit fm: Monad[M]): Aux[M, M[M[A]], M[A]] =
		new AlgeLiftFlatten[M, M[M[A]]]{
			type Out = M[A]

			def apply(mma: M[M[A]]) = fm.flatMap(mma){ ma: M[A] => ma }
		}
}

trait LowPriorityAlgeLiftFlatten{
	type Aux[M[_], Obj, Out0] = AlgeLiftFlatten[M, Obj]{ type Out = Out0 }

	@export(Subclass)
	implicit def recur[M[_], F[_], G](implicit functor: Functor[F], lift: LiftFlatten[M, G]): Aux[M, F[G], F[lift.Out]] =
		new AlgeLiftFlatten[M, F[G]]{
			type Out = F[lift.Out]

			def apply(fg: F[G]) = functor.map(fg){ g: G => lift(g) }
		}
}*/