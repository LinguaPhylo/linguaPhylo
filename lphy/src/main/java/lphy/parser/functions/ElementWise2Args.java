package lphy.parser.functions;

import lphy.graphicalModel.GraphicalModelNode;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.*;

import java.util.function.BiFunction;

/** applies an operator elementwise to a pari of Values **/
public interface ElementWise2Args<R,S> {
	
	Value apply(R a, S b, BiFunction o);
	
//	static ElementWise2Args<Value<Double>, Value<Double>> elementWiseDD() {
//		return (a,b,o) -> {
//			Double va = a.value();
//			Double vb = b.value();
//			Object r = o.apply(va, vb);
//			if (r instanceof Boolean rB)
//				// the logical operators
//				return new BooleanValue("", rB);
//			// default to Double
//			return new DoubleValue("", (Double) r);
////			else
////				throw new UnsupportedOperationException("The " + r.getClass().getName() + " type of result " + r + " is not handled !");
//		};
//	}

	static ElementWise2Args<Value<Double[][]>, Value<Double[][]>> elementWiseD2D2() {
		return (a,b,o) -> {
			Double[][] va = a.value();
			Double[][] vb = b.value();
			if (o.apply(va[0][0], vb[0][0]) instanceof Boolean) {
				Boolean[][] r = new Boolean[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {
						r[i][j] = (Boolean) o.apply(va[i][j], vb[i][j]);
					}
				}
				return new BooleanArray2DValue("", r);
			} else {
                // default to Double
				Double[][] r = new Double[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {
						r[i][j] = (Double) o.apply(va[i][j], vb[i][j]);
					}
				}
				return new DoubleArray2DValue("", r);
			}
		};
	}
	
//	static ElementWise2Args<Value<Double[]>, Value<Double[]>> elementWiseDADA() {
//		return (a,b,o) -> {
//			Double[] va = a.value();
//			Double[] vb = b.value();
//			if (o.apply(va[0], vb[0]) instanceof Boolean) {
//				Boolean[] r = new Boolean[va.length];
//				for (int i = 0; i < va.length; i++) {
//					r[i] = (Boolean) o.apply(va[i], vb[i]);
//				}
//				return new BooleanArrayValue("", r);
//			} else {
//				// default to Double
//				Double[] r = new Double[va.length];
//				for (int i = 0; i < va.length; i++) {
//					r[i] = (Double) o.apply(va[i], vb[i]);
//				}
//				return new DoubleArrayValue("", r);
//			}
//		};
//	}
//
//	static ElementWise2Args<Value<Double>, Value<Double[]>> elementWiseDDA() {
//		return (a,b,o) -> {
//			Double va = a.value();
//			Double[] vb = b.value();
//			if (o.apply(va, vb[0]) instanceof Boolean) {
//				Boolean[] r = new Boolean[vb.length];
//				for (int i = 0; i < vb.length; i++) {
//					r[i] = (Boolean) o.apply(va, vb[i]);
//				}
//				return new BooleanArrayValue("", r);
//			} else {
//				Double[] r = new Double[vb.length];
//				for (int i = 0; i < vb.length; i++) {
//					r[i] = (Double) o.apply(va, vb[i]);
//				}
//				return new DoubleArrayValue("", r);
//			}
//		};
//	}

//	static ElementWise2Args<Value<Double[]>, Value<Double>> elementWiseDAD() {
//		return (a,b,o) -> {
//			Double[] va = a.value();
//			Double vb = b.value();
//			if (o.apply(va[0], vb) instanceof Boolean) {
//				Boolean[] r = new Boolean[va.length];
//				for (int i = 0; i < va.length; i++) {
//					r[i] = (Boolean) o.apply(va[i], vb);
//				}
//				return new BooleanArrayValue("", r);
//			} else {
//				Double[] r = new Double[va.length];
//				for (int i = 0; i < va.length; i++) {
//					r[i] = (Double) o.apply(va[i], vb);
//				}
//				return new DoubleArrayValue("", r);
//			}
//		};
//	}
	
	static ElementWise2Args<Value<Double>, Value<Double[][]>> elementWiseDD2() {
		return (a,b,o) -> {
			Double va = a.value();
			Double[][] vb = b.value();
			if (o.apply(va, vb[0][0]) instanceof Boolean) {
				Boolean[][] r = new Boolean[vb.length][vb[0].length];
				for (int i = 0; i < vb.length; i++) {
					for (int j = 0; j < vb[i].length; j++) {

						r[i][j] = (Boolean) o.apply(va, vb[i][j]);
					}
				}
				return new BooleanArray2DValue("", r);
			} else {
				Double[][] r = new Double[vb.length][vb[0].length];
				for (int i = 0; i < vb.length; i++) {
					for (int j = 0; j < vb[i].length; j++) {

						r[i][j] = (Double) o.apply(va, vb[i][j]);
					}
				}
				return new DoubleArray2DValue("", r);
			}
		};
	}	
	
	static ElementWise2Args<Value<Double[][]>, Value<Double>> elementWiseD2D() {
		return (a,b,o) -> {
			Double[][] va = a.value();
			Double vb = b.value();
			if (o.apply(va[0][0], vb) instanceof Boolean) {
				Boolean[][] r = new Boolean[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {

						r[i][j] = (Boolean) o.apply(va[i][j], vb);
					}
				}
				return new BooleanArray2DValue("", r);
			} else {
				Double[][] r = new Double[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {

						r[i][j] = (Double) o.apply(va[i][j], vb);
					}
				}
				return new DoubleArray2DValue("", r);
			}
		};
	}	
	
//	static ElementWise2Args<Value<Integer>, Value<Integer>> elementWiseII() {
//		return (a,b,o) -> {
//			Integer va = a.value();
//			Integer vb = b.value();
//
//			Object result = o.apply(va, vb);
//			if (result instanceof Boolean rB)
//				// the logical operators
//				return new BooleanValue("", rB);
//			// default to Integer
//			Integer r = null;
//			if (result instanceof Integer) {
//				r = (Integer)result;
//			} else if (result instanceof Number) {
//				r = ((Number)result).intValue();
//			}
//			return new IntegerValue(null, r);
//		};
//	}

	static ElementWise2Args<Value<Integer[][]>, Value<Integer[][]>> elementWiseI2I2() {
		return (a,b,o) -> {
			Integer[][] va = a.value();
			Integer[][] vb = b.value();
			if (o.apply(va[0][0], vb[0][0]) instanceof Boolean) {
				Boolean[][] r = new Boolean[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {

						r[i][j] = (Boolean) o.apply(va[i][j], vb[i][j]);
					}
				}
				return new BooleanArray2DValue("", r);
			} else {
				Integer[][] r = new Integer[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {

						r[i][j] = (Integer) o.apply(va[i][j], vb[i][j]);
					}
				}
				return new IntegerArray2DValue("", r);
			}
		};
	}
	
//	static ElementWise2Args<Value<Integer[]>, Value<Integer[]>> elementWiseIAIA() {
//		return (a,b,o) -> {
//			Integer[] va = a.value();
//			Integer[] vb = b.value();
//			if (o.apply(va[0], vb[0]) instanceof Boolean) {
//				Boolean[] r = new Boolean[va.length];
//				for (int i = 0; i < va.length; i++) {
//					r[i] = (Boolean) o.apply(va[i], vb[i]);
//				}
//				return new BooleanArrayValue("", r);
//			} else {
//				Integer[] r = new Integer[va.length];
//				for (int i = 0; i < va.length; i++) {
//					r[i] = (Integer) o.apply(va[i], vb[i]);
//				}
//				return new IntegerArrayValue("", r);
//			}
//		};
//	}

	
//	static ElementWise2Args<Value<Integer>, Value<Integer[]>> elementWiseIIA() {
//		return (a,b,o) -> {
//			Integer va = a.value();
//			Integer[] vb = b.value();
//			if (o.apply(va, vb[0]) instanceof Boolean) {
//				Boolean[] r = new Boolean[vb.length];
//				for (int i = 0; i < vb.length; i++) {
//					r[i] = (Boolean) o.apply(va, vb[i]);
//				}
//				return new BooleanArrayValue("", r);
//			} else {
//				Integer[] r = new Integer[vb.length];
//				for (int i = 0; i < vb.length; i++) {
//					r[i] = (Integer) o.apply(va, vb[i]);
//				}
//				return new IntegerArrayValue("", r);
//			}
//		};
//	}
//
//	static ElementWise2Args<Value<Integer[]>, Value<Integer>> elementWiseIAI() {
//		return (a,b,o) -> {
//			Integer[] va = a.value();
//			Integer vb = b.value();
//			if (o.apply(va[0], vb) instanceof Boolean) {
//				Boolean[] r = new Boolean[va.length];
//				for (int i = 0; i < va.length; i++) {
//					r[i] = (Boolean)o.apply(va[i], vb);
//				}
//				return new BooleanArrayValue("", r);
//			} else {
//				Integer[] r = new Integer[va.length];
//				for (int i = 0; i < va.length; i++) {
//					r[i] = ((Double)o.apply(va[i], vb)).intValue();
//				}
//				return new IntegerArrayValue("", r);
//			}
//		};
//	}
	
	static ElementWise2Args<Value<Integer>, Value<Integer[][]>> elementWiseII2() {
		return (a,b,o) -> {
			Integer va =  a.value();
			Integer[][] vb = b.value();
			if (o.apply(va, vb[0][0]) instanceof Boolean) {
				Boolean[][] r = new Boolean[vb.length][vb[0].length];
				for (int i = 0; i < vb.length; i++) {
					for (int j = 0; j < vb[i].length; j++) {

						r[i][j] = (Boolean) o.apply(va, vb[i][j]);
					}
				}
				return new BooleanArray2DValue("", r);
			} else {
				Integer[][] r = new Integer[vb.length][vb[0].length];
				for (int i = 0; i < vb.length; i++) {
					for (int j = 0; j < vb[i].length; j++) {

						r[i][j] = (Integer) o.apply(va, vb[i][j]);
					}
				}
				return new IntegerArray2DValue("", r);
			}
		};
	}	
	
	static ElementWise2Args<Value<Integer[][]>, Value<Integer>> elementWiseI2I() {
		return (a,b,o) -> {
			Integer[][] va = a.value();
			Integer vb = b.value();
			if (o.apply(va[0][0], vb) instanceof Boolean) {
				Boolean[][] r = new Boolean[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {

						r[i][j] = (Boolean) o.apply(va[i][j], vb);
					}
				}
				return new BooleanArray2DValue("", r);
			} else {
				Integer[][] r = new Integer[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {

						r[i][j] = (Integer) o.apply(va[i][j], vb);
					}
				}
				return new IntegerArray2DValue("", r);
			}
		};
	}	
	
	
//	static ElementWise2Args<Value<Integer>, Value<Double>> elementWiseID() {
//		return (a,b,o) -> {
//			Integer va = a.value();
//			Double vb = b.value();
//			Object r = o.apply(va, vb);
//			if (r instanceof Boolean rB)
//				// the logical operators
//				return new BooleanValue("", rB);
//			// default to Double
//			return new DoubleValue("", (Double) r);
//		};
//	}

//	static ElementWise2Args<Value<Double>, Value<Integer>> elementWiseDI() {
//		return (a,b,o) -> {
//			Double va = a.value();
//			Integer vb = b.value();
//			Object r = o.apply(va, vb);
//			if (r instanceof Boolean rB)
//				// the logical operators
//				return new BooleanValue("", rB);
//			// default to Double
//			return new DoubleValue("", (Double) r);
//		};
//	}

//	static ElementWise2Args<Value<Integer>, Value<Double[]>> elementWiseIDA() {
//		return (a,b,o) -> {
//			Integer va = a.value();
//			Double[] vb = b.value();
//			if (o.apply(va, vb[0]) instanceof Boolean) {
//				Boolean[] r = new Boolean[vb.length];
//				for (int i = 0; i < vb.length; i++) {
//					r[i] = (Boolean) o.apply(va, vb[i]);
//				}
//				return new BooleanArrayValue("", r);
//			} else {
//				Double[] r = new Double[vb.length];
//				for (int i = 0; i < vb.length; i++) {
//					r[i] = (Double) o.apply(va, vb[i]);
//				}
//				return new DoubleArrayValue("", r);
//			}
//		};
//	}
//
//	static ElementWise2Args<Value<Double[]>, Value<Integer>> elementWiseDAI() {
//		return (a,b,o) -> {
//			Double[] va = a.value();
//			Integer vb = b.value();
//			if (o.apply(va[0], vb) instanceof Boolean) {
//				Boolean[] r = new Boolean[va.length];
//				for (int i = 0; i < va.length; i++) {
//					r[i] = (Boolean) o.apply(va[i], vb);
//				}
//				return new BooleanArrayValue("", r);
//			} else {
//				Double[] r = new Double[va.length];
//				for (int i = 0; i < va.length; i++) {
//					r[i] = (Double) o.apply(va[i], vb);
//				}
//				return new DoubleArrayValue("", r);
//			}
//		};
//	}
	
	static ElementWise2Args<Value<Integer>, Value<Double[][]>> elementWiseID2() {
		return (a,b,o) -> {
			Integer va = a.value();
			Double[][] vb = b.value();
			if (o.apply(va, vb[0][0]) instanceof Boolean) {
				Boolean[][] r = new Boolean[vb.length][vb[0].length];
				for (int i = 0; i < vb.length; i++) {
					for (int j = 0; j < vb[i].length; j++) {
						r[i][j] = (Boolean) o.apply(va, vb[i][j]);
					}
				}
				return new BooleanArray2DValue("", r);
			} else {
				Double[][] r = new Double[vb.length][vb[0].length];
				for (int i = 0; i < vb.length; i++) {
					for (int j = 0; j < vb[i].length; j++) {
						r[i][j] = (Double) o.apply(va, vb[i][j]);
					}
				}
				return new DoubleArray2DValue("", r);
			}
		};
	}	
	
	static ElementWise2Args<Value<Double[][]>, Value<Integer>> elementWiseD2I() {
		return (a,b,o) -> {
			Double[][] va = a.value();
			Integer vb = b.value();
			if (o.apply(va[0][0], vb) instanceof Boolean) {
				Boolean[][] r = new Boolean[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {
						r[i][j] = (Boolean) o.apply(va[i][j], vb);
					}
				}
				return new BooleanArray2DValue("", r);
			} else {
				Double[][] r = new Double[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {
						r[i][j] = (Double) o.apply(va[i][j], vb);
					}
				}
				return new DoubleArray2DValue("", r);
			}
		};
	}	
	
//	static ElementWise2Args<Value<Double>, Value<Integer[]>> elementWiseDIA() {
//		return (a,b,o) -> {
//			Double va = a.value();
//			Integer[] vb = b.value();
//			if (o.apply(va, vb[0]) instanceof Boolean) {
//				Boolean[] r = new Boolean[vb.length];
//				for (int i = 0; i < vb.length; i++) {
//					r[i] = (Boolean) o.apply(va, vb[i]);
//				}
//				return new BooleanArrayValue("", r);
//			} else {
//				Double[] r = new Double[vb.length];
//				for (int i = 0; i < vb.length; i++) {
//					r[i] = (Double) o.apply(va, vb[i]);
//				}
//				return new DoubleArrayValue("", r);
//			}
//		};
//	}
//
//	static ElementWise2Args<Value<Integer[]>, Value<Double>> elementWiseIAD() {
//		return (a,b,o) -> {
//			Integer[] va = a.value();
//			Double vb = b.value();
//			if (o.apply(va[0], vb) instanceof Boolean) {
//				Boolean[] r = new Boolean[va.length];
//				for (int i = 0; i < va.length; i++) {
//					r[i] = (Boolean) o.apply(va[i], vb);
//				}
//				return new BooleanArrayValue("", r);
//			} else {
//				Double[] r = new Double[va.length];
//				for (int i = 0; i < va.length; i++) {
//					r[i] = (Double) o.apply(va[i], vb);
//				}
//				return new DoubleArrayValue("", r);
//			}
//		};
//	}
	
	static ElementWise2Args<Value<Double>, Value<Integer[][]>> elementWiseDI2() {
		return (a,b,o) -> {
			Double va = a.value();
			Integer[][] vb = b.value();
			if (o.apply(va, vb[0][0]) instanceof Boolean) {
				Boolean[][] r = new Boolean[vb.length][vb[0].length];
				for (int i = 0; i < vb.length; i++) {
					for (int j = 0; j < vb[i].length; j++) {
						r[i][j] = (Boolean) o.apply(va, vb[i][j]);
					}
				}
				return new BooleanArray2DValue("", r);
			} else {
				Double[][] r = new Double[vb.length][vb[0].length];
				for (int i = 0; i < vb.length; i++) {
					for (int j = 0; j < vb[i].length; j++) {
						r[i][j] = (Double) o.apply(va, vb[i][j]);
					}
				}
				return new DoubleArray2DValue("", r);
			}
		};
	}	
	
	static ElementWise2Args<Value<Integer[][]>, Value<Double>> elementWiseI2D() {
		return (a,b,o) -> {
			Integer[][] va = a.value();
			Double vb = b.value();
			if (o.apply(va[0][0], vb) instanceof Boolean) {
				Boolean[][] r = new Boolean[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {
						r[i][j] = (Boolean) o.apply(va[i][j], vb);
					}
				}
				return new BooleanArray2DValue("", r);
			} else {
				Double[][] r = new Double[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {
						r[i][j] = (Double) o.apply(va[i][j], vb);
					}
				}
				return new DoubleArray2DValue("", r);
			}
		};
	}	

//	static ElementWise2Args<Value<Double[]>, Value<Integer[]>> elementWiseDAIA() {
//		return (a,b,o) -> {
//			Double [] va = a.value();
//			Integer[] vb = b.value();
//			if (o.apply(va[0], vb[0]) instanceof Boolean) {
//				Boolean[] r = new Boolean[vb.length];
//				for (int i = 0; i < vb.length; i++) {
//					r[i] = (Boolean) o.apply(va[i], vb[i]);
//				}
//				return new BooleanArrayValue("", r);
//			} else {
//				Double[] r = new Double[vb.length];
//				for (int i = 0; i < vb.length; i++) {
//					r[i] = (Double) o.apply(va[i], vb[i]);
//				}
//				return new DoubleArrayValue("", r);
//			}
//		};
//	}
//
//	static ElementWise2Args<Value<Integer[]>, Value<Double[]>> elementWiseIADA() {
//		return (a,b,o) -> {
//			Integer[] va = a.value();
//			Double[] vb = b.value();
//			if (o.apply(va[0], vb[0]) instanceof Boolean) {
//				Boolean[] r = new Boolean[va.length];
//				for (int i = 0; i < va.length; i++) {
//					r[i] = (Boolean) o.apply(va[i], vb[i]);
//				}
//				return new BooleanArrayValue("", r);
//
//			} else {
//				Double[] r = new Double[va.length];
//				for (int i = 0; i < va.length; i++) {
//					r[i] = (Double) o.apply(va[i], vb[i]);
//				}
//				return new DoubleArrayValue("", r);
//			}
//		};
//	}
//
	static ElementWise2Args<Value<Double[][]>, Value<Integer[][]>> elementWiseD2I2() {
		return (a,b,o) -> {
			Double[][] va = a.value();
			Integer[][] vb = b.value();
			if (o.apply(va[0][0], vb[0][0]) instanceof Boolean) {
				Boolean[][] r = new Boolean[vb.length][vb[0].length];
				for (int i = 0; i < vb.length; i++) {
					for (int j = 0; j < vb[i].length; j++) {
						r[i][j] = (Boolean) o.apply(va[i][j], vb[i][j]);
					}
				}
				return new BooleanArray2DValue("", r);
			} else {
				Double[][] r = new Double[vb.length][vb[0].length];
				for (int i = 0; i < vb.length; i++) {
					for (int j = 0; j < vb[i].length; j++) {
						r[i][j] = (Double) o.apply(va[i][j], vb[i][j]);
					}
				}
				return new DoubleArray2DValue("", r);
			}
		};
	}	
	
	static ElementWise2Args<Value<Integer[][]>, Value<Double[][]>> elementWiseI2D2() {
		return (a,b,o) -> {
			Integer[][] va = a.value();
			Double[][] vb = b.value();
			if (o.apply(va[0][0], vb[0][0]) instanceof Boolean) {
				Boolean[][] r = new Boolean[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {
						r[i][j] = (Boolean) o.apply(va[i][j], vb[i][j]);
					}
				}
				return new BooleanArray2DValue("", r);
			} else {
				Double[][] r = new Double[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {
						r[i][j] = (Double) o.apply(va[i][j], vb[i][j]);
					}
				}
				return new DoubleArray2DValue("", r);
			}
		};
	}

	//*** TODO generalise all above methods into methods below ? ***//

	/**
	 * a vs b
	 * @return  BooleanValue, DoubleValue, IntegerValue.
	 *          if result is Number, both inputs have to be integer to return IntegerValue,
	 *          else return DoubleValue.
	 */
	static ElementWise2Args<Value<Object>, Value<Object>> elementWiseOO() {
		return (a,b,o) -> {
			Object result = o.apply(a.value(), b.value());
			if (result instanceof Boolean rB)
				// the logical operators
				return new BooleanValue(null, rB);
			else if (result instanceof Integer rI) {
				return new IntegerValue(null, rI);
			} else if (result instanceof Number) {
				if ( a.value() instanceof Integer && b.value() instanceof Integer ) {
					// if result is Number, both inputs have to be integer to return IntegerValue
					Integer r = ((Number)result).intValue();
					return new IntegerValue(null, r);
				} else {
					// default to double
					Double r = ((Number)result).doubleValue();
					return new DoubleValue(null, r);
				}
			} else {
				throw new UnsupportedOperationException("Unsupported result type " +
						result.getClass().getName());
			}
		};
	}

	/**
	 * a vs b[]
	 * @return BooleanArrayValue, DoubleArrayValue, IntegerArrayValue
	 */
	static ElementWise2Args<Value<Object>, Value<Object[]>> elementWiseOO1() {
		return (a,b,o) -> {
			Object va = a.value();
			Object[] vb = b.value();
			// check types
			Object result = o.apply(va, vb[0]);
			if (result instanceof Boolean) {
				Boolean[] r = new Boolean[vb.length];
				for (int i = 0; i < vb.length; i++) {
					r[i] = (Boolean) o.apply(va, vb[i]);
				}
				return new BooleanArrayValue("", r);
			} else if (result instanceof Integer) {
				Integer[] r = new Integer[vb.length];
				for (int i = 0; i < vb.length; i++) {
					r[i] = (Integer) o.apply(va, vb[i]);
				}
				return new IntegerArrayValue("", r);
			} else if (result instanceof Number) {
				if ( va instanceof Integer && vb[0] instanceof Integer ) {
					// if result is Number, both inputs have to be integer to return IntegerValue
					Integer[] r = new Integer[vb.length];
					for (int i = 0; i < vb.length; i++) {
						r[i] = ((Number) o.apply(va, vb[i])).intValue();
					}
					return new IntegerArrayValue(null, r);
				} else {
					// default to double
					Double[] r = new Double[vb.length];
					for (int i = 0; i < vb.length; i++) {
						r[i] = (Double) o.apply(va, vb[i]);
					}
					return new DoubleArrayValue("", r);
				}
			} else {
					throw new UnsupportedOperationException("Unsupported result type " +
							result.getClass().getName());
			}
		};
	}

	/**
	 * a[] vs b
	 * @return BooleanArrayValue, DoubleArrayValue, IntegerArrayValue
	 */
	static ElementWise2Args<Value<Object[]>, Value<Object>> elementWiseO1O() {
		return (a,b,o) -> {
			Object[] va = a.value();
			Object vb = b.value();
			// check types
			Object result = o.apply(va[0], vb);
			if (result instanceof Boolean) {
				Boolean[] r = new Boolean[va.length];
				for (int i = 0; i < va.length; i++) {
					r[i] = (Boolean) o.apply(va[i], vb);
				}
				return new BooleanArrayValue("", r);
			} else if (result instanceof Integer) {
				Integer[] r = new Integer[va.length];
				for (int i = 0; i < va.length; i++) {
					r[i] = (Integer) o.apply(va[i], vb);
				}
				return new IntegerArrayValue("", r);
			} else if (result instanceof Number) {
				if ( va[0] instanceof Integer && vb instanceof Integer ) {
					// if result is Number, both inputs have to be integer to return IntegerValue
					Integer[] r = new Integer[va.length];
					for (int i = 0; i < va.length; i++) {
						r[i] = ((Number) o.apply(va[i], vb)).intValue();
					}
					return new IntegerArrayValue(null, r);
				} else {
					// default to double
					Double[] r = new Double[va.length];
					for (int i = 0; i < va.length; i++) {
						r[i] = (Double) o.apply(va[i], vb);
					}
					return new DoubleArrayValue("", r);
				}
			} else {
				throw new UnsupportedOperationException("Unsupported result type " +
						result.getClass().getName());
			}
		};
	}

	/**
	 * a[] vs b[]. must be same length
	 * @return BooleanArrayValue, DoubleArrayValue, IntegerArrayValue
	 */
	static ElementWise2Args<Value<Object[]>, Value<Object[]>> elementWiseO1O1() {
		return (a,b,o) -> {
			Object[] va = a.value();
			Object[] vb = b.value();
			if (va.length != vb.length)
				throw new IllegalArgumentException("The pair-wise operation in vectors requires " +
						"both vectors have the same length ! " + va.length + " != " + vb.length);
			// check types
			Object result = o.apply(va[0], vb[0]);
			if (result instanceof Boolean) {
				Boolean[] r = new Boolean[va.length];
				for (int i = 0; i < va.length; i++) {
					r[i] = (Boolean) o.apply(va[i], vb[i]);
				}
				return new BooleanArrayValue("", r);
			} else if (result instanceof Integer) {
				Integer[] r = new Integer[va.length];
				for (int i = 0; i < va.length; i++) {
					r[i] = (Integer) o.apply(va[i], vb[i]);
				}
				return new IntegerArrayValue("", r);
			} else if (result instanceof Number) {
				if ( va[0] instanceof Integer && vb[0] instanceof Integer ) {
					// if result is Number, both inputs have to be integer to return IntegerValue
					Integer[] r = new Integer[va.length];
					for (int i = 0; i < va.length; i++) {
						r[i] = ((Number) o.apply(va[i], vb[i])).intValue();
					}
					return new IntegerArrayValue(null, r);
				} else {
					// default to double
					Double[] r = new Double[va.length];
					for (int i = 0; i < va.length; i++) {
						r[i] = (Double) o.apply(va[i], vb[i]);
					}
					return new DoubleArrayValue("", r);
				}
			} else {
				throw new UnsupportedOperationException("Unsupported result type " +
						result.getClass().getName());
			}
		};
	}


	static ElementWise2Args elementFactory(GraphicalModelNode[] values) {
		if (values.length != 2) {
			return null;
		}
		Object valA = values[0].value();
		Object valB = values[1].value();

		if (valA instanceof Double[][]) {
			if (valB instanceof Double[][]) {
				return elementWiseD2D2();
			} else if (valB instanceof Double[]) {
			} else if (valB instanceof Double) {
				return elementWiseD2D();
			} else if (valB instanceof Integer[][]) {
				return elementWiseD2I2();
			} else if (valB instanceof Integer[]) {
			} else if (valB instanceof Integer) {
				return elementWiseD2I();
			}
		} else if (valA instanceof Double[]) {
			if (valB instanceof Double[][]) {
//			} else if (values[1].value() instanceof Double[]) {
//				return elementWiseDADA();
//			} else if (values[1].value() instanceof Double) {
//				return elementWiseDAD();
			} else if (valB instanceof Integer[][]) {
//			} else if (values[1].value() instanceof Integer[]) {
//				return elementWiseDAIA();
//			} else if (values[1].value() instanceof Integer) {
//				return elementWiseDAI();
			}
		} else if (valA instanceof Double) {
			if (valB instanceof Double[][]) {
				return elementWiseDD2();
//			} else if (values[1].value() instanceof Double[]) {
//				return elementWiseDDA();
//			} else if (values[1].value() instanceof Double) {
//				return elementWiseDD();
			} else if (valB instanceof Integer[][]) {
				return elementWiseDI2();
//			} else if (values[1].value() instanceof Integer[]) {
//				return elementWiseDIA();
//			} else if (values[1].value() instanceof Integer) {
//				return elementWiseDI();
			}
		} else if (valA instanceof Integer[][]) {
			if (valB instanceof Double[][]) {
				return elementWiseI2D2();
			} else if (valB instanceof Double[]) {
			} else if (valB instanceof Double) {
				return elementWiseI2D();
			} else if (valB instanceof Integer[][]) {
				return elementWiseI2I2();
			} else if (valB instanceof Integer[]) {
			} else if (valB instanceof Integer) {
				return elementWiseI2I();
			}
		} else if (valA instanceof Integer[]) {
			if (valB instanceof Double[][]) {
//			} else if (values[1].value() instanceof Double[]) {
//				return elementWiseIADA();
//			} else if (values[1].value() instanceof Double) {
//				return elementWiseIAD();
			} else if (valB instanceof Integer[][]) {
//			} else if (values[1].value() instanceof Integer[]) {
//				return elementWiseIAIA();
//			} else if (values[1].value() instanceof Integer) {
//				return elementWiseIAI();
			}
		} else if (valA instanceof Integer) {
			if (valB instanceof Double[][]) {
				return elementWiseID2();
//			} else if (values[1].value() instanceof Double[]) {
//				return elementWiseIDA();
//			} else if (values[1].value() instanceof Double) {
//				return elementWiseID();
			} else if (valB instanceof Integer[][]) {
				return elementWiseII2();
//			} else if (values[1].value() instanceof Integer[]) {
//				return elementWiseIIA();
//			} else if (values[1].value() instanceof Integer) {
//				return elementWiseII();
			}
		}

		// TODO more general
		if (valA.getClass().isArray()) {
			Class<?> compTypeA = valA.getClass().getComponentType();
			// valA is 2D array
			if (compTypeA.isArray()) {
				//TODO Handle a[][]
			} else {
				// valA is 1D array.
				if (valB.getClass().isArray()) {
					Class<?> compTypeB = valB.getClass().getComponentType();
					// valB is 2D array
					if (compTypeB.isArray()) {
						//TODO Handle a[] vs b[][]
					} else {
						// valB is 1D array. Handle a[] vs b[]
						return elementWiseO1O1();
					}
				} else // Handle a[] vs b
					return elementWiseO1O();
			}
		} else {
			// valA is not an array.
			if (valB.getClass().isArray()) {
				Class<?> compTypeB = valB.getClass().getComponentType();
				// valB is 2D array
				if (compTypeB.isArray()) {
					//TODO Handle a vs b[][]
				} else {
					// valB is 1D array. Handle a vs b[]
					return elementWiseOO1();
				}
			} else // a vs b
			    return elementWiseOO();
		}
		return null;
	}

}
