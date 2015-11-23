package autolift.scalaz

import scalaz._
import autolift._
import export._

trait ScalazLiftF[Obj, Fn] extends LiftF[Obj, Fn]

@exports(Subclass)
object ScalazLiftF extends LowPriorityScalazLiftF {
	def apply[Obj, Fn](implicit lift: ScalazLiftF[Obj, Fn]): Aux[Obj, Fn, lift.Out] = lift

	@export(Subclass)
	implicit def base[F[_], A, C >: A, B](implicit functor: Functor[F]): Aux[F[A], C => B, F[B]] =
		new ScalazLiftF[F[A], C => B]{
			type Out = F[B]

			def apply(fa: F[A], f: C => B) = functor.map(fa)(f)
		}
}

trait LowPriorityScalazLiftF{
	type Aux[Obj, Fn, Out0] = ScalazLiftF[Obj, Fn]{ type Out = Out0 }

	@export(Subclass)
	implicit def recur[F[_], G, Fn](implicit functor: Functor[F], lift: LiftF[G, Fn]): Aux[F[G], Fn, F[lift.Out]] =
		new ScalazLiftF[F[G], Fn]{
			type Out = F[lift.Out]

			def apply(fg: F[G], f: Fn) = functor.map(fg){ g: G => lift(g, f) }
		}
}

trait ScalazLiftAp[Obj, Fn] extends LiftAp[Obj, Fn]

@exports(Subclass)
object ScalazLiftAp extends LowPriorityScalazLiftAp {
	def apply[Obj, Fn](implicit lift: ScalazLiftAp[Obj, Fn]): Aux[Obj, Fn, lift.Out] = lift

	@export(Subclass)
	implicit def base[F[_], A, B](implicit ap: Apply[F]): Aux[F[A], F[A => B], F[B]] =
		new ScalazLiftAp[F[A], F[A => B]]{
			type Out = F[B]

			def apply(fa: F[A], f: F[A => B]) = ap.ap(fa)(f)
		}
}

trait LowPriorityScalazLiftAp{
	type Aux[Obj, Fn, Out0] = ScalazLiftAp[Obj, Fn]{ type Out = Out0 }

	@export(Subclass)
	implicit def recur[F[_], G, Fn](implicit functor: Functor[F], lift: LiftAp[G, Fn]): Aux[F[G], Fn, F[lift.Out]] =
		new ScalazLiftAp[F[G], Fn]{
			type Out = F[lift.Out]

			def apply(fg: F[G], f: Fn) = functor.map(fg){ g: G => lift(g, f) }
		}
}

trait ScalazLiftB[Obj, Fn] extends LiftB[Obj, Fn]

@exports(Subclass)
object ScalazLiftB extends LowPriorityScalazLiftB {
	def apply[Obj, Fn](implicit lift: ScalazLiftB[Obj, Fn]): Aux[Obj, Fn, lift.Out] = lift

	@export(Subclass)
	implicit def base[M[_], A, C >: A, B](implicit bind: Bind[M]): Aux[M[A], C => M[B], M[B]] =
		new ScalazLiftB[M[A], C => M[B]]{
			type Out = M[B]

			def apply(fa: M[A], f: C => M[B]) = bind.bind(fa)(f)
		}
}

trait LowPriorityScalazLiftB{
	type Aux[Obj, Fn, Out0] = ScalazLiftB[Obj, Fn]{ type Out = Out0 }

	@export(Subclass)
	implicit def recur[F[_], G, Fn](implicit functor: Functor[F], lift: LiftB[G, Fn]): Aux[F[G], Fn, F[lift.Out]] =
		new ScalazLiftB[F[G], Fn]{
			type Out = F[lift.Out]

			def apply(fg: F[G], f: Fn) = functor.map(fg){ g: G => lift(g, f) }
		}
}