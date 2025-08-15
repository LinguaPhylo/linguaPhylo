package lphy.core.parser.function;

import lphy.core.model.GraphicalModelNode;
import lphy.core.model.Value;
import lphy.core.model.datatype.*;
import org.phylospec.types.Bool;
import org.phylospec.types.Int;

import java.util.function.BiFunction;

/** applies an operator elementwise to a pari of Values **/
public interface ElementWise2Args<R,S> {
	
	Value apply(R a, S b, BiFunction o);


	/**
	 * a vs b
	 * @return  BooleanValue, DoubleValue, IntegerValue.
	 *          If result is Number, both inputs have to be integer to return IntegerValue,
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
//			} else if (result instanceof Number) {
//				if ( a.value() instanceof Int && b.value() instanceof Int ) {
//					// if result is Number, both inputs have to be integer to return IntegerValue
//					Int r = ((Number)result).intValue();
//					return new IntegerValue(null, r);
//				} else {
//					// default to double
//					Double r = ((Number)result).doubleValue();
//					return new DoubleValue(null, r);
//				}
			} else {
				throw new UnsupportedOperationException("Unsupported result type " +
						result.getClass().getName());
			}
		};
	}

	/**
	 * a vs b[]
	 * @return BooleanArrayValue, DoubleArrayValue, IntegerArrayValue
	 *         If result is Number, both inputs have to be integer to return IntegerArrayValue,
	 *         else return DoubleArrayValue.
	 */
	static ElementWise2Args<Value<Object>, Value<Object[]>> elementWiseOO1() {
		return (a,b,o) -> {
			Object va = a.value();
			Object[] vb = b.value();
			// check types
			Object result = o.apply(va, vb[0]);
			if (result instanceof Bool) {
				Bool[] r = new Bool[vb.length];
				for (int i = 0; i < vb.length; i++) {
					r[i] = (Bool) o.apply(va, vb[i]);
				}
				return new BooleanArrayValue("", r);
			} else if (result instanceof Int) {
				Int[] r = new Int[vb.length];
				for (int i = 0; i < vb.length; i++) {
					r[i] = (Int) o.apply(va, vb[i]);
				}
				return new IntegerArrayValue("", r);
//			} else if (result instanceof Number) {
//				if ( va instanceof Integer && vb[0] instanceof Integer ) {
//					// if result is Number, both inputs have to be integer to return IntegerValue
//					Integer[] r = new Integer[vb.length];
//					for (int i = 0; i < vb.length; i++) {
//						r[i] = ((Number) o.apply(va, vb[i])).intValue();
//					}
//					return new IntegerArrayValue(null, r);
//				} else {
//					// default to double
//					Double[] r = new Double[vb.length];
//					for (int i = 0; i < vb.length; i++) {
//						r[i] = (Double) o.apply(va, vb[i]);
//					}
//					return new DoubleArrayValue("", r);
//				}
			} else {
					throw new UnsupportedOperationException("Unsupported result type " +
							result.getClass().getName());
			}
		};
	}

	/**
	 * a[] vs b
	 * @return BooleanArrayValue, DoubleArrayValue, IntegerArrayValue.
	 *         If result is Number, both inputs have to be integer to return IntegerArrayValue,
	 *         else return DoubleArrayValue.
	 */
	static ElementWise2Args<Value<Object[]>, Value<Object>> elementWiseO1O() {
		return (a,b,o) -> {
			Object[] va = a.value();
			Object vb = b.value();
			// check types
			Object result = o.apply(va[0], vb);
			if (result instanceof Bool) {
				Bool[] r = new Bool[va.length];
				for (int i = 0; i < va.length; i++) {
					r[i] = (Bool) o.apply(va[i], vb);
				}
				return new BooleanArrayValue("", r);
			} else if (result instanceof Int) {
				Int[] r = new Int[va.length];
				for (int i = 0; i < va.length; i++) {
					r[i] = (Int) o.apply(va[i], vb);
				}
				return new IntegerArrayValue("", r);
//			} else if (result instanceof Number) {
//				if ( va[0] instanceof Integer && vb instanceof Integer ) {
//					// if result is Number, both inputs have to be integer to return IntegerValue
//					Integer[] r = new Integer[va.length];
//					for (int i = 0; i < va.length; i++) {
//						r[i] = ((Number) o.apply(va[i], vb)).intValue();
//					}
//					return new IntegerArrayValue(null, r);
//				} else {
//					// default to double
//					Double[] r = new Double[va.length];
//					for (int i = 0; i < va.length; i++) {
//						r[i] = (Double) o.apply(va[i], vb);
//					}
//					return new DoubleArrayValue("", r);
//				}
			} else {
				throw new UnsupportedOperationException("Unsupported result type " +
						result.getClass().getName());
			}
		};
	}

	/**
	 * a[] vs b[]. must be same length
	 * @return BooleanArrayValue, DoubleArrayValue, IntegerArrayValue.
	 *         If result is Number, both inputs have to be integer to return IntegerArrayValue,
	 *         else return DoubleArrayValue.
	 */
	static ElementWise2Args<Value<Object[]>, Value<Object[]>> elementWiseO1O1() {
		return (a,b,o) -> {
			Object[] va = a.value();
			Object[] vb = b.value();
			if (va.length != vb.length)
				throw new IllegalArgumentException("The element-wise operation between 1d vectors requires " +
						"both vectors have the same length ! " + va.length + " != " + vb.length);
			// check types
			Object result = o.apply(va[0], vb[0]);
			if (result instanceof Bool) {
				Bool[] r = new Bool[va.length];
				for (int i = 0; i < va.length; i++) {
					r[i] = (Bool) o.apply(va[i], vb[i]);
				}
				return new BooleanArrayValue("", r);
			} else if (result instanceof Int) {
				Int[] r = new Int[va.length];
				for (int i = 0; i < va.length; i++) {
					r[i] = (Int) o.apply(va[i], vb[i]);
				}
				return new IntegerArrayValue("", r);
//			} else if (result instanceof Number) {
//				if ( va[0] instanceof Integer && vb[0] instanceof Integer ) {
//					// if result is Number, both inputs have to be integer to return IntegerValue
//					Integer[] r = new Integer[va.length];
//					for (int i = 0; i < va.length; i++) {
//						r[i] = ((Number) o.apply(va[i], vb[i])).intValue();
//					}
//					return new IntegerArrayValue(null, r);
//				} else {
//					// default to double
//					Double[] r = new Double[va.length];
//					for (int i = 0; i < va.length; i++) {
//						r[i] = (Double) o.apply(va[i], vb[i]);
//					}
//					return new DoubleArrayValue("", r);
//				}
			} else {
				throw new UnsupportedOperationException("Unsupported result type " +
						result.getClass().getName());
			}
		};
	}

	/**
	 * a vs b[][].
	 * @return BooleanArray2DValue, DoubleArray2DValue, IntegerArray2DValue.
	 *         If result is Number, both inputs have to be integer to return IntegerArray2DValue,
	 *         else return DoubleArray2DValue.
	 */
	static ElementWise2Args<Value<Object>, Value<Object[][]>> elementWiseOO2() {
		return (a,b,o) -> {
			Object va = a.value();
			Object[][] vb = b.value();
			Object result = o.apply(va, vb[0][0]);
			if (result instanceof Bool) {
				Bool[][] r = new Bool[vb.length][vb[0].length];
				for (int i = 0; i < vb.length; i++) {
					for (int j = 0; j < vb[i].length; j++) {
						r[i][j] = (Bool) o.apply(va, vb[i][j]);
					}
				}
				return new BooleanArray2DValue("", r);
			} else if (result instanceof Int) {
				Int[][] r = new Int[vb.length][vb[0].length];
				for (int i = 0; i < vb.length; i++) {
					for (int j = 0; j < vb[i].length; j++) {
						r[i][j] = (Int) o.apply(va, vb[i][j]);
					}
				}
				return new IntegerArray2DValue("", r);
//			} else if (result instanceof Number) {
//				if ( va instanceof Integer && vb[0][0] instanceof Integer ) {
//					// if result is Number, both inputs have to be integer to return IntegerValue
//					Integer[][] r = new Integer[vb.length][vb[0].length];
//					for (int i = 0; i < vb.length; i++) {
//						for (int j = 0; j < vb[i].length; j++) {
//							r[i][j] = ((Number) o.apply(va, vb[i][j])).intValue();
//						}
//					}
//					return new IntegerArray2DValue(null, r);
//				} else {
//					// default to double
//					Double[][] r = new Double[vb.length][vb[0].length];
//					for (int i = 0; i < vb.length; i++) {
//						for (int j = 0; j < vb[i].length; j++) {
//							r[i][j] = (Double) o.apply(va, vb[i][j]);
//						}
//					}
//					return new DoubleArray2DValue("", r);
//				}
			} else {
				throw new UnsupportedOperationException("Unsupported result type " +
						result.getClass().getName());
			}
		};
	}

	/**
	 * a[][] vs b.
	 * @return BooleanArray2DValue, DoubleArray2DValue, IntegerArray2DValue.
	 *         If result is Number, both inputs have to be integer to return IntegerArray2DValue,
	 *         else return DoubleArray2DValue.
	 */
	static ElementWise2Args<Value<Object[][]>, Value<Object>> elementWiseO2O() {
		return (a,b,o) -> {
			Object[][] va = a.value();
			Object vb = b.value();
			Object result = o.apply(va[0][0], vb);
			if (result instanceof Bool) {
				Bool[][] r = new Bool[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {
						r[i][j] = (Bool) o.apply(va[i][j], vb);
					}
				}
				return new BooleanArray2DValue("", r);
			} else if (result instanceof Int) {
				Int[][] r = new Int[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {
						r[i][j] = (Int) o.apply(va[i][j], vb);
					}
				}
				return new IntegerArray2DValue("", r);
//			} else if (result instanceof Number) {
//				if ( va[0][0] instanceof Integer && vb instanceof Integer ) {
//					// if result is Number, both inputs have to be integer to return IntegerValue
//					Integer[][] r = new Integer[va.length][va[0].length];
//					for (int i = 0; i < va.length; i++) {
//						for (int j = 0; j < va[i].length; j++) {
//							r[i][j] = ((Number) o.apply(va[i][j], vb)).intValue();
//						}
//					}
//					return new IntegerArray2DValue(null, r);
//				} else {
//					// default to double
//					Double[][] r = new Double[va.length][va[0].length];
//					for (int i = 0; i < va.length; i++) {
//						for (int j = 0; j < va[i].length; j++) {
//							r[i][j] = (Double) o.apply(va[i][j], vb);
//						}
//					}
//					return new DoubleArray2DValue("", r);
//				}
			} else {
				throw new UnsupportedOperationException("Unsupported result type " +
						result.getClass().getName());
			}
		};
	}

	/**
	 * a[] vs b[][].
	 * when performing element-wise addition between a 1D matrix (vector) and a 2D matrix,
	 * the vector should be added row-wise to each row of the matrix.
	 * This means each element in the vector is added to the corresponding element in each row of the matrix.
	 * @return BooleanArray2DValue, DoubleArray2DValue, IntegerArray2DValue.
	 *         If result is Number, both inputs have to be integer to return IntegerArray2DValue,
	 *         else return DoubleArray2DValue.
	 */
	static ElementWise2Args<Value<Object[]>, Value<Object[][]>> elementWiseO1O2() {
		return (a,b,o) -> {
			Object[] va = a.value();
			Object[][] vb = b.value();
			if (va.length != vb[0].length)
				throw new IllegalArgumentException("The element-wise operation " +
						"between 1D vector and 2D requires the length of 1D vector must be same as " +
						"the number of columns of 2D ! " + va.length + " != " + vb.length);
			// check types
			Object result = o.apply(va[0], vb[0][0]);
			if (result instanceof Bool) {
				Bool[][] r = new Bool[vb.length][vb[0].length];
				for (int i = 0; i < vb.length; i++) {
					for (int j = 0; j < vb[i].length; j++) {
						r[i][j] = (Bool) o.apply(va[j], vb[i][j]);
					}
				}
				return new BooleanArray2DValue("", r);
			} else if (result instanceof Int) {
				Int[][] r = new Int[vb.length][vb[0].length];
				for (int i = 0; i < vb.length; i++) {
					for (int j = 0; j < vb[i].length; j++) {
						r[i][j] = (Int) o.apply(va[j], vb[i][j]);
					}
				}
				return new IntegerArray2DValue("", r);
//			} else if (result instanceof Number) {
//				if ( va[0] instanceof Integer && vb[0][0] instanceof Integer ) {
//					// if result is Number, both inputs have to be integer to return IntegerValue
//					Integer[][] r = new Integer[vb.length][vb[0].length];
//					for (int i = 0; i < vb.length; i++) {
//						for (int j = 0; j < vb[i].length; j++) {
//							r[i][j] = ((Number) o.apply(va[j], vb[i][j])).intValue();
//						}
//					}
//					return new IntegerArray2DValue(null, r);
//				} else {
//					// default to double
//					Double[][] r = new Double[vb.length][vb[0].length];
//					for (int i = 0; i < vb.length; i++) {
//						for (int j = 0; j < vb[i].length; j++) {
//							r[i][j] = (Double) o.apply(va[j], vb[i][j]);
//						}
//					}
//					return new DoubleArray2DValue("", r);
//				}
			} else {
				throw new UnsupportedOperationException("Unsupported result type " +
						result.getClass().getName());
			}
		};
	}

	/**
	 * a[][] vs b[].
	 * when performing element-wise addition between a 1D matrix (vector) and a 2D matrix,
	 * the vector should be added row-wise to each row of the matrix.
	 * This means each element in the vector is added to the corresponding element in each row of the matrix.
	 * @return BooleanArray2DValue, DoubleArray2DValue, IntegerArray2DValue.
	 *         If result is Number, both inputs have to be integer to return IntegerArray2DValue,
	 *         else return DoubleArray2DValue.
	 */
	static ElementWise2Args<Value<Object[][]>, Value<Object[]>> elementWiseO2O1() {
		return (a,b,o) -> {
			Object[][] va = a.value();
			Object[] vb = b.value();
			if (va[0].length != vb.length)
				throw new IllegalArgumentException("The element-wise operation " +
						"between 1D vector and 2D requires the length of 1D vector must be same as " +
						"the number of columns of 2D ! " + va.length + " != " + vb.length);
			// check types
			Object result = o.apply(va[0][0], vb[0]);
			if (result instanceof Bool) {
				Bool[][] r = new Bool[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {
						r[i][j] = (Bool) o.apply(va[i][j], vb[j]);
					}
				}
				return new BooleanArray2DValue("", r);
			} else if (result instanceof Int) {
				Int[][] r = new Int[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {
						r[i][j] = (Int) o.apply(va[i][j], vb[j]);
					}
				}
				return new IntegerArray2DValue("", r);
//			} else if (result instanceof Number) {
//				if ( va[0][0] instanceof Integer && vb[0] instanceof Integer ) {
//					// if result is Number, both inputs have to be integer to return IntegerValue
//					Integer[][] r = new Integer[va.length][va[0].length];
//					for (int i = 0; i < va.length; i++) {
//						for (int j = 0; j < va[i].length; j++) {
//							r[i][j] = ((Number) o.apply(va[i][j], vb[j])).intValue();
//						}
//					}
//					return new IntegerArray2DValue(null, r);
//				} else {
//					// default to double
//					Double[][] r = new Double[va.length][va[0].length];
//					for (int i = 0; i < va.length; i++) {
//						for (int j = 0; j < va[i].length; j++) {
//							r[i][j] = (Double) o.apply(va[i][j], vb[j]);
//						}
//					}
//					return new DoubleArray2DValue("", r);
//				}
			} else {
				throw new UnsupportedOperationException("Unsupported result type " +
						result.getClass().getName());
			}
		};
	}

	/**
	 * a[][] vs b[][].
	 * @return BooleanArray2DValue, DoubleArray2DValue, IntegerArray2DValue.
	 *         If result is Number, both inputs have to be integer to return IntegerArray2DValue,
	 *         else return DoubleArray2DValue.
	 */
	static ElementWise2Args<Value<Object[][]>, Value<Object[][]>> elementWiseO2O2() {
		return (a,b,o) -> {
			Object[][] va = a.value();
			Object[][] vb = b.value();
			if (va.length != vb.length || va[0].length != vb[0].length)
				throw new IllegalArgumentException("The element-wise operation " +
						"between 2D vectors requires their dimensions must be same ! " +
						va.length + " != " + vb.length + " or " + va[0].length + " != " + vb[0].length);
			// check types
			Object result = o.apply(va[0][0], vb[0][0]);
			if (result instanceof Bool) {
				Bool[][] r = new Bool[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {
						r[i][j] = (Bool) o.apply(va[i][j], vb[i][j]);
					}
				}
				return new BooleanArray2DValue("", r);
			} else if (result instanceof Int) {
				Int[][] r = new Int[va.length][va[0].length];
				for (int i = 0; i < va.length; i++) {
					for (int j = 0; j < va[i].length; j++) {
						r[i][j] = (Int) o.apply(va[i][j], vb[i][j]);
					}
				}
				return new IntegerArray2DValue("", r);
//			} else if (result instanceof Number) {
//				if ( va[0][0] instanceof Integer && vb[0][0] instanceof Integer ) {
//					// if result is Number, both inputs have to be integer to return IntegerValue
//					Integer[][] r = new Integer[va.length][va[0].length];
//					for (int i = 0; i < va.length; i++) {
//						for (int j = 0; j < va[i].length; j++) {
//							r[i][j] = ((Number) o.apply(va[i][j], vb[i][j])).intValue();
//						}
//					}
//					return new IntegerArray2DValue(null, r);
//				} else {
//					// default to double
//					Double[][] r = new Double[va.length][va[0].length];
//					for (int i = 0; i < va.length; i++) {
//						for (int j = 0; j < va[i].length; j++) {
//							r[i][j] = (Double) o.apply(va[i][j], vb[i][j]);
//						}
//					}
//					return new DoubleArray2DValue("", r);
//				}
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

		if (valA.getClass().isArray()) {
			if ( ((Object[]) valA).length < 1)
				return elementWiseOO(); // empty array

			Class<?> compTypeA = valA.getClass().getComponentType();
			// valA is 2D array
			if (compTypeA.isArray()) {
				// valA is 2D array.
				if (valB.getClass().isArray()) {
					Class<?> compTypeB = valB.getClass().getComponentType();
					// valB is 2D array
					if (compTypeB.isArray()) {
						//Handle a[][] vs b[][]
						return elementWiseO2O2();
					} else {
						// Handle a[][] vs b[]
						return elementWiseO2O1();
					}
				} else // Handle a[][] vs b
					return elementWiseO2O();
			} else {
				// valA is 1D array.
				if (valB.getClass().isArray()) {
					Class<?> compTypeB = valB.getClass().getComponentType();
					// valB is 2D array
					if (compTypeB.isArray()) {
						// Handle a[] vs b[][]
						return elementWiseO1O2();
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
				if ( ((Object[]) valB).length < 1)
					return elementWiseOO(); // empty array

				Class<?> compTypeB = valB.getClass().getComponentType();
				// valB is 2D array
				if (compTypeB.isArray()) {
					// Handle a vs b[][]
					return elementWiseOO2();
				} else {
					// valB is 1D array. Handle a vs b[]
					return elementWiseOO1();
				}
			} else // a vs b
			    return elementWiseOO();
		}
//		return null;
	}

}
