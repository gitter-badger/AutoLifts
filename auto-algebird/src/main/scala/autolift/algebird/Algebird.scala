package autolift

import export._
import autolift.algebird._

@reexports[AlgeLiftF, AlgeLiftB, AlgeLiftFlatten]
object Algebird{
	implicit def mkF[Obj, Function](implicit lift: AlgeLiftF[Obj, Function]): AlgeLiftF.Aux[Obj, Function, lift.Out] = lift
	implicit def mkFM[Obj, Function](implicit lift: AlgeLiftB[Obj, Function]): AlgeLiftB.Aux[Obj, Function, lift.Out] = lift
	//implicit def mkFl[M[_], Obj](implicit lift: AlgeLiftFlatten[M, Obj]): AlgeLiftFlatten.Aux[M, Obj, lift.Out] = lift
}