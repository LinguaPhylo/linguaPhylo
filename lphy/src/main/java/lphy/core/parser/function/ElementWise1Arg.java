package lphy.core.parser.function;

import lphy.core.model.GraphicalModelNode;
import lphy.core.model.Value;
import lphy.core.model.datatype.*;
import org.phylospec.types.Int;
import org.phylospec.types.Real;

import java.util.function.Function;

/** applies an operator elementwise to a single Value **/
public interface ElementWise1Arg<R> {
	
	Value apply(R a, Function o);

	static ElementWise1Arg<Value<Real>> elementWiseD() {
		return (a,o) -> {
			Real va = a.value();
			Real r = (Real) o.apply(va);
			return new RealValue("", r);
		};
	}

	static ElementWise1Arg<Value<Real[][]>> elementWiseD2() {
		return (a,o) -> {
			Real[][] va = (Real[][]) a.value();
			Real[][] r = new Real[va.length][va[0].length];
			for (int i = 0; i < va.length; i++) {
				for (int j = 0; j < va.length; j++) {
					r[i][j] = (Real) o.apply(va[i][j]);
				}
			}
			return new RealArray2DValue("", r);
		};
	}
	
	static ElementWise1Arg<Value<Real[]>> elementWiseDA() {
		return (a,o) -> {
			Real[] va = (Real[]) a.value();
			Real[] r = new Real[va.length];
			for (int i = 0; i < va.length; i++) {
				r[i] = (Real) o.apply(va[i]);
			}
			return new RealArrayValue("", r);
		};
	}
	
	
	static ElementWise1Arg<Value<Int>> elementWiseI() {
		return (a,o) -> {
			Int va = a.value();
			return new Value(null, o.apply(va));
		};
	}

	static ElementWise1Arg<Value<Int[][]>> elementWiseI2() {
		return (a,o) -> {
			Int[][] va = (Int[][]) a.value();
			Int[][] r = new Int[va.length][va[0].length];
			for (int i = 0; i < va.length; i++) {
				for (int j = 0; j < va.length; j++) {
					r[i][j] = (Int) o.apply(va[i][j]);
				}
			}
			return new IntegerArray2DValue("", r);
		};
	}
	
	static ElementWise1Arg<Value<Int[]>> elementWiseIA() {
		return (a,o) -> {
			Int[] va = (Int[]) a.value();
			Int[] r = new Int[va.length];
			for (int i = 0; i < va.length; i++) {
				r[i] = (Int) o.apply(va[i]);
			}
			return new IntegerArrayValue("", r);
		};
	}


	static ElementWise1Arg elementFactory(GraphicalModelNode[] values) {
		if (values.length != 1) {
			return null;
		}
		
		if (values[0].value() instanceof Real[][]) {
			return elementWiseD2();
		} else if (values[0].value() instanceof Real[]) {
			return elementWiseDA();
		} else if (values[0].value() instanceof Real) {
			return elementWiseD();
		} else if (values[0].value() instanceof Int[][]) {
			return elementWiseI2();
		} else if (values[0].value() instanceof Int[]) {
			return elementWiseIA();
		} else if (values[0].value() instanceof Int) {
			return elementWiseI();
		}
		return null;
	}	
}
