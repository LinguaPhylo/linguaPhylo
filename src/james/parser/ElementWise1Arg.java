package james.parser;

import java.util.function.Function;

import james.graphicalModel.GraphicalModelNode;
import james.graphicalModel.Value;
import james.graphicalModel.types.*;

/** applies an operator elementwise to a single Value **/
public interface ElementWise1Arg<R> {
	
	Value apply(R a, Function o);
	
	static ElementWise1Arg<DoubleValue> elementWiseD() {
		return (a,o) -> {
			Double va = (Double) a.value();
			Double r = (Double) o.apply(va);
			return new DoubleValue("", r);
		};
	}

	static ElementWise1Arg<DoubleArray2DValue> elementWiseD2() {
		return (a,o) -> {
			Double[][] va = (Double[][]) a.value();
			Double[][] r = new Double[va.length][va[0].length];
			for (int i = 0; i < va.length; i++) {
				for (int j = 0; j < va.length; j++) {
					r[i][j] = (Double) o.apply(va[i][j]);
				}
			}
			return new DoubleArray2DValue("", r);
		};
	}
	
	static ElementWise1Arg<DoubleArrayValue> elementWiseDA() {
		return (a,o) -> {
			Double[] va = (Double[]) a.value();
			Double[] r = new Double[va.length];
			for (int i = 0; i < va.length; i++) {
				r[i] = (Double) o.apply(va[i]);
			}
			return new DoubleArrayValue("", r);
		};
	}
	
	
	static ElementWise1Arg<IntegerValue> elementWiseI() {
		return (a,o) -> {
			Integer va = (Integer) a.value();
			Integer r = (Integer) o.apply(va);
			return new IntegerValue("", r);
		};
	}

	static ElementWise1Arg<IntegerArray2DValue> elementWiseI2() {
		return (a,o) -> {
			Integer[][] va = (Integer[][]) a.value();
			Integer[][] r = new Integer[va.length][va[0].length];
			for (int i = 0; i < va.length; i++) {
				for (int j = 0; j < va.length; j++) {
					r[i][j] = (Integer) o.apply(va[i][j]);
				}
			}
			return new IntegerArray2DValue("", r);
		};
	}
	
	static ElementWise1Arg<IntegerArrayValue> elementWiseIA() {
		return (a,o) -> {
			Integer[] va = (Integer[]) a.value();
			Integer[] r = new Integer[va.length];
			for (int i = 0; i < va.length; i++) {
				r[i] = (Integer) o.apply(va[i]);
			}
			return new IntegerArrayValue("", r);
		};
	}


	static ElementWise1Arg elementFactory(GraphicalModelNode[] values) {
		if (values.length != 1) {
			return null;
		}
		
		if (values[0].value() instanceof Double[][]) {
			return elementWiseD2();
		} else if (values[0].value() instanceof Double[]) {
			return elementWiseDA();
		} else if (values[0].value() instanceof Double) {
			return elementWiseD();
		} else if (values[0].value() instanceof Integer[][]) {
			return elementWiseI2();
		} else if (values[0].value() instanceof Integer[]) {
			return elementWiseIA();
		} else if (values[0].value() instanceof Integer) {
			return elementWiseI();
		}
		return null;
	}	
}
