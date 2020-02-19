package james.parser;

import java.util.function.BinaryOperator;

import james.graphicalModel.GraphicalModelNode;
import james.graphicalModel.Value;
import james.graphicalModel.types.*;

/** applies an operator elementwise to a pari of Values **/
public interface ElementWise2Args<R,S> {
	
	Value apply(R a, S b, BinaryOperator o);
	
	static ElementWise2Args<DoubleValue, DoubleValue> elementWiseDD() {
		return (a,b,o) -> {
			Double va = (Double) a.value();
			Double vb = (Double) b.value();
			Double r = (Double) o.apply(va, vb);
			return new DoubleValue("", r);
		};
	}

	static ElementWise2Args<DoubleArray2DValue, DoubleArray2DValue> elementWiseD2D2() {
		return (a,b,o) -> {
			Double[][] va = (Double[][]) a.value();
			Double[][] vb = (Double[][]) b.value();
			Double[][] r = new Double[va.length][va[0].length];
			for (int i = 0; i < va.length; i++) {
				for (int j = 0; j < va.length; j++) {

					r[i][j] = (Double) o.apply(va[i][j], vb[i][j]);
				}
			}
			return new DoubleArray2DValue("", r);
		};
	}
	
	static ElementWise2Args<DoubleArrayValue, DoubleArrayValue> elementWiseDADA() {
		return (a,b,o) -> {
			Double[] va = (Double[]) a.value();
			Double[] vb = (Double[]) b.value();
			Double[] r = new Double[va.length];
			for (int i = 0; i < va.length; i++) {
				r[i] = (Double) o.apply(va[i], vb[i]);
			}
			return new DoubleArrayValue("", r);
		};
	}
	
	static ElementWise2Args<IntegerValue, IntegerArrayValue> elementWiseDDA() {
		return (a,b,o) -> {
			Integer va = (Integer) a.value();
			Integer[] vb = (Integer[]) b.value();
			Integer[] r = new Integer[vb.length];
			for (int i = 0; i < vb.length; i++) {
				r[i] = (Integer) o.apply(va, vb[i]);
			}
			return new IntegerArrayValue("", r);
		};
	}

	static ElementWise2Args<IntegerArrayValue, IntegerValue> elementWiseDAD() {
		return (a,b,o) -> {
			Integer[] va = (Integer[]) a.value();
			Integer vb = (Integer) b.value();
			Integer[] r = new Integer[va.length];
			for (int i = 0; i < va.length; i++) {
				r[i] = (Integer) o.apply(va[i], vb);
			}
			return new IntegerArrayValue("", r);
		};
	}
	
	static ElementWise2Args<IntegerValue, IntegerArray2DValue> elementWiseDD2() {
		return (a,b,o) -> {
			Integer va = (Integer) a.value();
			Integer[][] vb = (Integer[][]) b.value();
			Integer[][] r = new Integer[vb.length][vb[0].length];
			for (int i = 0; i < vb.length; i++) {
				for (int j = 0; j < vb.length; j++) {

					r[i][j] = (Integer) o.apply(va, vb[i][j]);
				}
			}
			return new IntegerArray2DValue("", r);
		};
	}	
	
	static ElementWise2Args<IntegerArray2DValue, IntegerValue> elementWiseD2D() {
		return (a,b,o) -> {
			Integer[][] va = (Integer[][]) a.value();
			Integer vb = (Integer) b.value();
			Integer[][] r = new Integer[va.length][va[0].length];
			for (int i = 0; i < va.length; i++) {
				for (int j = 0; j < va.length; j++) {

					r[i][j] = (Integer) o.apply(va[i][j], vb);
				}
			}
			return new IntegerArray2DValue("", r);
		};
	}	
	
	static ElementWise2Args<IntegerValue, IntegerValue> elementWiseII() {
		return (a,b,o) -> {
			Integer va = (Integer) a.value();
			Integer vb = (Integer) b.value();
			Integer r = (Integer) o.apply(va, vb);
			return new IntegerValue("", r);
		};
	}

	static ElementWise2Args<IntegerArray2DValue, IntegerArray2DValue> elementWiseI2I2() {
		return (a,b,o) -> {
			Integer[][] va = (Integer[][]) a.value();
			Integer[][] vb = (Integer[][]) b.value();
			Integer[][] r = new Integer[va.length][va[0].length];
			for (int i = 0; i < va.length; i++) {
				for (int j = 0; j < va.length; j++) {

					r[i][j] = (Integer) o.apply(va[i][j], vb[i][j]);
				}
			}
			return new IntegerArray2DValue("", r);
		};
	}
	
	static ElementWise2Args<IntegerArrayValue, IntegerArrayValue> elementWiseIAIA() {
		return (a,b,o) -> {
			Integer[] va = (Integer[]) a.value();
			Integer[] vb = (Integer[]) b.value();
			Integer[] r = new Integer[va.length];
			for (int i = 0; i < va.length; i++) {
				r[i] = (Integer) o.apply(va[i], vb[i]);
			}
			return new IntegerArrayValue("", r);
		};
	}

	
	static ElementWise2Args<IntegerValue, IntegerArrayValue> elementWiseIIA() {
		return (a,b,o) -> {
			Integer va = (Integer) a.value();
			Integer[] vb = (Integer[]) b.value();
			Integer[] r = new Integer[vb.length];
			for (int i = 0; i < vb.length; i++) {
				r[i] = (Integer) o.apply(va, vb[i]);
			}
			return new IntegerArrayValue("", r);
		};
	}

	static ElementWise2Args<IntegerArrayValue, IntegerValue> elementWiseIAI() {
		return (a,b,o) -> {
			Integer[] va = (Integer[]) a.value();
			Integer vb = (Integer) b.value();
			Integer[] r = new Integer[va.length];
			for (int i = 0; i < va.length; i++) {
				r[i] = (Integer) o.apply(va[i], vb);
			}
			return new IntegerArrayValue("", r);
		};
	}
	
	static ElementWise2Args<IntegerValue, IntegerArray2DValue> elementWiseII2() {
		return (a,b,o) -> {
			Integer va = (Integer) a.value();
			Integer[][] vb = (Integer[][]) b.value();
			Integer[][] r = new Integer[vb.length][vb[0].length];
			for (int i = 0; i < vb.length; i++) {
				for (int j = 0; j < vb.length; j++) {

					r[i][j] = (Integer) o.apply(va, vb[i][j]);
				}
			}
			return new IntegerArray2DValue("", r);
		};
	}	
	
	static ElementWise2Args<IntegerArray2DValue, IntegerValue> elementWiseI2I() {
		return (a,b,o) -> {
			Integer[][] va = (Integer[][]) a.value();
			Integer vb = (Integer) b.value();
			Integer[][] r = new Integer[va.length][va[0].length];
			for (int i = 0; i < va.length; i++) {
				for (int j = 0; j < va.length; j++) {

					r[i][j] = (Integer) o.apply(va[i][j], vb);
				}
			}
			return new IntegerArray2DValue("", r);
		};
	}	
	
	
	static ElementWise2Args<IntegerValue, DoubleValue> elementWiseID() {
		return (a,b,o) -> {
			Integer va = (Integer) a.value();
			Double vb = (Double) b.value();
			Double r = (Double) o.apply(va, vb);
			return new DoubleValue("", r);
		};
	}

	static ElementWise2Args<DoubleValue, IntegerValue> elementWiseDI() {
		return (a,b,o) -> {
			Double va = (Double) a.value();
			Integer vb = (Integer) b.value();
			Double r = (Double) o.apply(va, vb);
			return new DoubleValue("", r);
		};
	}

	static ElementWise2Args<IntegerValue, DoubleArrayValue> elementWiseIDA() {
		return (a,b,o) -> {
			Integer va = (Integer) a.value();
			Double[] vb = (Double[]) b.value();
			Double[] r = new Double[vb.length];
			for (int i = 0; i < vb.length; i++) {
				r[i] = (Double) o.apply(va, vb[i]);
			}
			return new DoubleArrayValue("", r);
		};
	}

	static ElementWise2Args<DoubleArrayValue, IntegerValue> elementWiseDAI() {
		return (a,b,o) -> {
			Double[] va = (Double[]) a.value();
			Integer vb = (Integer) b.value();
			Double[] r = new Double[va.length];
			for (int i = 0; i < va.length; i++) {
				r[i] = (Double) o.apply(va[i], vb);
			}
			return new DoubleArrayValue("", r);
		};
	}
	
	static ElementWise2Args<IntegerValue, DoubleArray2DValue> elementWiseID2() {
		return (a,b,o) -> {
			Integer va = (Integer) a.value();
			Double[][] vb = (Double[][]) b.value();
			Double[][] r = new Double[vb.length][vb[0].length];
			for (int i = 0; i < vb.length; i++) {
				for (int j = 0; j < vb.length; j++) {
					r[i][j] = (Double) o.apply(va, vb[i][j]);
				}
			}
			return new DoubleArray2DValue("", r);
		};
	}	
	
	static ElementWise2Args<DoubleArray2DValue, IntegerValue> elementWiseD2I() {
		return (a,b,o) -> {
			Double[][] va = (Double[][]) a.value();
			Integer vb = (Integer) b.value();
			Double[][] r = new Double[va.length][va[0].length];
			for (int i = 0; i < va.length; i++) {
				for (int j = 0; j < va.length; j++) {
					r[i][j] = (Double) o.apply(va[i][j], vb);
				}
			}
			return new DoubleArray2DValue("", r);
		};
	}	
	
	static ElementWise2Args<DoubleValue, IntegerArrayValue> elementWiseDIA() {
		return (a,b,o) -> {
			Double va = (Double) a.value();
			Integer[] vb = (Integer[]) b.value();
			Double[] r = new Double[vb.length];
			for (int i = 0; i < vb.length; i++) {
				r[i] = (Double) o.apply(va, vb[i]);
			}
			return new DoubleArrayValue("", r);
		};
	}

	static ElementWise2Args<IntegerArrayValue, DoubleValue> elementWiseIAD() {
		return (a,b,o) -> {
			Integer[] va = (Integer[]) a.value();
			Double vb = (Double) b.value();
			Double[] r = new Double[va.length];
			for (int i = 0; i < va.length; i++) {
				r[i] = (Double) o.apply(va[i], vb);
			}
			return new DoubleArrayValue("", r);
		};
	}
	
	static ElementWise2Args<DoubleValue, IntegerArray2DValue> elementWiseDI2() {
		return (a,b,o) -> {
			Double va = (Double) a.value();
			Integer[][] vb = (Integer[][]) b.value();
			Double[][] r = new Double[vb.length][vb[0].length];
			for (int i = 0; i < vb.length; i++) {
				for (int j = 0; j < vb.length; j++) {
					r[i][j] = (Double) o.apply(va, vb[i][j]);
				}
			}
			return new DoubleArray2DValue("", r);
		};
	}	
	
	static ElementWise2Args<IntegerArray2DValue, DoubleValue> elementWiseI2D() {
		return (a,b,o) -> {
			Integer[][] va = (Integer[][]) a.value();
			Double vb = (Double) b.value();
			Double[][] r = new Double[va.length][va[0].length];
			for (int i = 0; i < va.length; i++) {
				for (int j = 0; j < va.length; j++) {
					r[i][j] = (Double) o.apply(va[i][j], vb);
				}
			}
			return new DoubleArray2DValue("", r);
		};
	}	

	static ElementWise2Args<DoubleArrayValue, IntegerArrayValue> elementWiseDAIA() {
		return (a,b,o) -> {
			Double [] va = (Double[]) a.value();
			Integer[] vb = (Integer[]) b.value();
			Double[] r = new Double[vb.length];
			for (int i = 0; i < vb.length; i++) {
				r[i] = (Double) o.apply(va[i], vb[i]);
			}
			return new DoubleArrayValue("", r);
		};
	}

	static ElementWise2Args<IntegerArrayValue, DoubleArrayValue> elementWiseIADA() {
		return (a,b,o) -> {
			Integer[] va = (Integer[]) a.value();
			Double[] vb = (Double []) b.value();
			Double[] r = new Double[va.length];
			for (int i = 0; i < va.length; i++) {
				r[i] = (Double) o.apply(va[i], vb[i]);
			}
			return new DoubleArrayValue("", r);
		};
	}
	
	static ElementWise2Args<DoubleArray2DValue, IntegerArray2DValue> elementWiseD2I2() {
		return (a,b,o) -> {
			Double[][] va = (Double[][]) a.value();
			Integer[][] vb = (Integer[][]) b.value();
			Double[][] r = new Double[vb.length][vb[0].length];
			for (int i = 0; i < vb.length; i++) {
				for (int j = 0; j < vb.length; j++) {
					r[i][j] = (Double) o.apply(va[i][j], vb[i][j]);
				}
			}
			return new DoubleArray2DValue("", r);
		};
	}	
	
	static ElementWise2Args<IntegerArray2DValue, DoubleArray2DValue> elementWiseI2D2() {
		return (a,b,o) -> {
			Integer[][] va = (Integer[][]) a.value();
			Double[][] vb = (Double[][]) b.value();
			Double[][] r = new Double[va.length][va[0].length];
			for (int i = 0; i < va.length; i++) {
				for (int j = 0; j < va.length; j++) {
					r[i][j] = (Double) o.apply(va[i][j], vb[i][j]);
				}
			}
			return new DoubleArray2DValue("", r);
		};
	}

	static ElementWise2Args elementFactory(GraphicalModelNode[] values) {
		if (values.length != 2) {
			return null;
		}
		
		if (values[0].value() instanceof Double[][]) {
			if (values[1].value() instanceof Double[][]) {
				return elementWiseD2D2();
			} else if (values[1].value() instanceof Double[]) {
			} else if (values[1].value() instanceof Double) {
				return elementWiseD2D();
			} else if (values[1].value() instanceof Integer[][]) {
				return elementWiseD2I2();
			} else if (values[1].value() instanceof Integer[]) {
			} else if (values[1].value() instanceof Integer) {
				return elementWiseD2I();
			}
		} else if (values[0].value() instanceof Double[]) {
			if (values[1].value() instanceof Double[][]) {				
			} else if (values[1].value() instanceof Double[]) {
				return elementWiseDADA();
			} else if (values[1].value() instanceof Double) {
				return elementWiseDAD();
			} else if (values[1].value() instanceof Integer[][]) {
			} else if (values[1].value() instanceof Integer[]) {
				return elementWiseDAIA();
			} else if (values[1].value() instanceof Integer) {
				return elementWiseDAI();
			}
		} else if (values[0].value() instanceof Double) {
			if (values[1].value() instanceof Double[][]) {				
				return elementWiseDD2();
			} else if (values[1].value() instanceof Double[]) {
				return elementWiseDDA();
			} else if (values[1].value() instanceof Double) {
				return elementWiseDD();
			} else if (values[1].value() instanceof Integer[][]) {
				return elementWiseDI2();
			} else if (values[1].value() instanceof Integer[]) {
				return elementWiseDIA();
			} else if (values[1].value() instanceof Integer) {
				return elementWiseDI();
			}
		} else if (values[0].value() instanceof Integer[][]) {
			if (values[1].value() instanceof Double[][]) {				
				return elementWiseI2D2();
			} else if (values[1].value() instanceof Double[]) {
			} else if (values[1].value() instanceof Double) {
				return elementWiseI2D();
			} else if (values[1].value() instanceof Integer[][]) {
				return elementWiseI2I2();
			} else if (values[1].value() instanceof Integer[]) {
			} else if (values[1].value() instanceof Integer) {
				return elementWiseI2I();
			}
		} else if (values[0].value() instanceof Integer[]) {
			if (values[1].value() instanceof Double[][]) {				
			} else if (values[1].value() instanceof Double[]) {
				return elementWiseIADA();
			} else if (values[1].value() instanceof Double) {
				return elementWiseIAD();
			} else if (values[1].value() instanceof Integer[][]) {
			} else if (values[1].value() instanceof Integer[]) {
				return elementWiseIAIA();
			} else if (values[1].value() instanceof Integer) {
				return elementWiseIAI();
			}
		} else if (values[0].value() instanceof Integer) {
			if (values[1].value() instanceof Double[][]) {				
				return elementWiseID2();
			} else if (values[1].value() instanceof Double[]) {
				return elementWiseIDA();
			} else if (values[1].value() instanceof Double) {
				return elementWiseID();
			} else if (values[1].value() instanceof Integer[][]) {
				return elementWiseII2();
			} else if (values[1].value() instanceof Integer[]) {
				return elementWiseIIA();
			} else if (values[1].value() instanceof Integer) {
				return elementWiseII();
			}
		}	
		
		return null;
	}	
}
