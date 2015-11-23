package autolift

import export._
import autolift.scalaz._

@reexports[ScalazLiftF, ScalazLiftAp, ScalazLiftB]
object Scalaz{
	implicit def mkM[Obj, Fn](implicit lift: ScalazLiftF[Obj, Fn]): ScalazLiftF.Aux[Obj, Fn, lift.Out] = lift
	implicit def mkAp[Obj, Fn](implicit lift: ScalazLiftAp[Obj, Fn]): ScalazLiftAp.Aux[Obj, Fn, lift.Out] = lift
	implicit def mkFM[Obj, Fn](implicit lift: ScalazLiftB[Obj, Fn]): ScalazLiftB.Aux[Obj, Fn, lift.Out] = lift
	//implicit def mkFl[M[_], Obj](implicit lift: ScalazLiftFlatten[M, Obj]): ScalazLiftFlatten.Aux[M, Obj, lift.Out] = lift
}