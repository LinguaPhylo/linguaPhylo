package james.parser;

import java.util.function.BinaryOperator;

import james.graphicalModel.GraphicalModelNode;
import james.graphicalModel.Value;
import james.graphicalModel.types.*;

/** applies an operator elementwise to a Value **/
public interface ElementWise<R,S> {
	
	Value apply(R a, S b, BinaryOperator o);
	
	static ElementWise<DoubleValue, DoubleValue> elementWiseDD() {
		return (a,b,o) -> {
			Double va = (Double) a.value();
			Double vb = (Double) b.value();
			Double r = (Double) o.apply(va, vb);
			return new DoubleValue("", r);
		};
	}

	static ElementWise<DoubleArray2DValue, DoubleArray2DValue> elementWiseD2D2() {
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
	
	static ElementWise<DoubleArrayValue, DoubleArrayValue> elementWiseDADA() {
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
	
	static ElementWise<IntegerValue, IntegerArrayValue> elementWiseDDA() {
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

	static ElementWise<IntegerArrayValue, IntegerValue> elementWiseDAD() {
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
	
	static ElementWise<IntegerValue, IntegerArray2DValue> elementWiseDD2() {
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
	
	static ElementWise<IntegerArray2DValue, IntegerValue> elementWiseD2D() {
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
	
	static ElementWise<IntegerValue, IntegerValue> elementWiseII() {
		return (a,b,o) -> {
			Integer va = (Integer) a.value();
			Integer vb = (Integer) b.value();
			Integer r = (Integer) o.apply(va, vb);
			return new IntegerValue("", r);
		};
	}

	static ElementWise<IntegerArray2DValue, IntegerArray2DValue> elementWiseI2I2() {
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
	
	static ElementWise<IntegerArrayValue, IntegerArrayValue> elementWiseIAIA() {
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

	
	static ElementWise<IntegerValue, IntegerArrayValue> elementWiseIIA() {
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

	static ElementWise<IntegerArrayValue, IntegerValue> elementWiseIAI() {
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
	
	static ElementWise<IntegerValue, IntegerArray2DValue> elementWiseII2() {
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
	
	static ElementWise<IntegerArray2DValue, IntegerValue> elementWiseI2I() {
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
	
	
	static ElementWise<IntegerValue, DoubleValue> elementWiseID() {
		return (a,b,o) -> {
			Integer va = (Integer) a.value();
			Double vb = (Double) b.value();
			Double r = (Double) o.apply(va, vb);
			return new DoubleValue("", r);
		};
	}

	static ElementWise<DoubleValue, IntegerValue> elementWiseDI() {
		return (a,b,o) -> {
			Double va = (Double) a.value();
			Integer vb = (Integer) b.value();
			Double r = (Double) o.apply(va, vb);
			return new DoubleValue("", r);
		};
	}

	static ElementWise<IntegerValue, DoubleArrayValue> elementWiseIDA() {
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

	static ElementWise<DoubleArrayValue, IntegerValue> elementWiseDAI() {
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
	
	static ElementWise<IntegerValue, DoubleArray2DValue> elementWiseID2() {
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
	
	static ElementWise<DoubleArray2DValue, IntegerValue> elementWiseD2I() {
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
	
	static ElementWise<DoubleValue, IntegerArrayValue> elementWiseDIA() {
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

	static ElementWise<IntegerArrayValue, DoubleValue> elementWiseIAD() {
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
	
	static ElementWise<DoubleValue, IntegerArray2DValue> elementWiseDI2() {
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
	
	static ElementWise<IntegerArray2DValue, DoubleValue> elementWiseI2D() {
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

	static ElementWise<DoubleArrayValue, IntegerArrayValue> elementWiseDAIA() {
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

	static ElementWise<IntegerArrayValue, DoubleArrayValue> elementWiseIADA() {
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
	
	static ElementWise<DoubleArray2DValue, IntegerArray2DValue> elementWiseD2I2() {
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
	
	static ElementWise<IntegerArray2DValue, DoubleArray2DValue> elementWiseI2D2() {
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

	static ElementWise elementFactory(GraphicalModelNode[] values) {
		if (values.length != 2) {
			return null;
		}
		
		if (values[0] instanceof DoubleArray2DValue) {
			if (values[1] instanceof DoubleArray2DValue) {
				return elementWiseD2D2();
			} else if (values[1] instanceof DoubleArrayValue) {
			} else if (values[1] instanceof DoubleValue) {
				return elementWiseD2D();
			} else if (values[1] instanceof IntegerArray2DValue) {
				return elementWiseD2I2();
			} else if (values[1] instanceof IntegerArrayValue) {
			} else if (values[1] instanceof IntegerValue) {
				return elementWiseD2I();
			}
		} else if (values[0] instanceof DoubleArrayValue) {
			if (values[1] instanceof DoubleArray2DValue) {				
			} else if (values[1] instanceof DoubleArrayValue) {
				return elementWiseDADA();
			} else if (values[1] instanceof DoubleValue) {
				return elementWiseDAD();
			} else if (values[1] instanceof IntegerArray2DValue) {
			} else if (values[1] instanceof IntegerArrayValue) {
				return elementWiseDAIA();
			} else if (values[1] instanceof IntegerValue) {
				return elementWiseDAI();
			}
		} else if (values[0] instanceof DoubleValue) {
			if (values[1] instanceof DoubleArray2DValue) {				
				return elementWiseDD2();
			} else if (values[1] instanceof DoubleArrayValue) {
				return elementWiseDDA();
			} else if (values[1] instanceof DoubleValue) {
				return elementWiseDD();
			} else if (values[1] instanceof IntegerArray2DValue) {
				return elementWiseDI2();
			} else if (values[1] instanceof IntegerArrayValue) {
				return elementWiseDIA();
			} else if (values[1] instanceof IntegerValue) {
				return elementWiseDI();
			}
		} else if (values[0] instanceof IntegerArray2DValue) {
			if (values[1] instanceof DoubleArray2DValue) {				
				return elementWiseI2D2();
			} else if (values[1] instanceof DoubleArrayValue) {
			} else if (values[1] instanceof DoubleValue) {
				return elementWiseI2D();
			} else if (values[1] instanceof IntegerArray2DValue) {
				return elementWiseI2I2();
			} else if (values[1] instanceof IntegerArrayValue) {
			} else if (values[1] instanceof IntegerValue) {
				return elementWiseI2I();
			}
		} else if (values[0] instanceof IntegerArrayValue) {
			if (values[1] instanceof DoubleArray2DValue) {				
			} else if (values[1] instanceof DoubleArrayValue) {
				return elementWiseIADA();
			} else if (values[1] instanceof DoubleValue) {
				return elementWiseIAD();
			} else if (values[1] instanceof IntegerArray2DValue) {
			} else if (values[1] instanceof IntegerArrayValue) {
				return elementWiseIAIA();
			} else if (values[1] instanceof IntegerValue) {
				return elementWiseIAI();
			}
		} else if (values[0] instanceof IntegerValue) {
			if (values[1] instanceof DoubleArray2DValue) {				
				return elementWiseID2();
			} else if (values[1] instanceof DoubleArrayValue) {
				return elementWiseIDA();
			} else if (values[1] instanceof DoubleValue) {
				return elementWiseID();
			} else if (values[1] instanceof IntegerArray2DValue) {
				return elementWiseII2();
			} else if (values[1] instanceof IntegerArrayValue) {
				return elementWiseIIA();
			} else if (values[1] instanceof IntegerValue) {
				return elementWiseII();
			}
		}
		
		return null;
	}	
}
