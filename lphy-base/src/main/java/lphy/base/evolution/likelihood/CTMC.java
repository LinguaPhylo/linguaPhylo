package lphy.base.evolution.likelihood;

import lphy.core.model.GenerativeDistribution;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.parser.argument.ParameterInfo;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

//TODO in dev
public class CTMC implements GenerativeDistribution<Integer> {

    Value<Integer> X;
    Value<Double> t;
    Value<double[][]> Q;
    Random random;

    public static final String XParamName = "X";
    public static final String tParamName = "t";
    public static final String QParamName = "Q";


    public CTMC(@ParameterInfo(name = XParamName, description = "the starting state.") Value<Integer> X,
                @ParameterInfo(name = tParamName, description = "the time.") Value<Double> t,
                @ParameterInfo(name = QParamName, description = "the instantaneous rate matrix.") Value<double[][]> Q,
                Random random) {
        this.X = X;
        this.t = t;
        this.Q = Q;
        this.random = random;
    }


    @Override
    public RandomVariable<Integer> sample() {
        // TODO draw a random discrete state from this continuous-time Markov chain given a Q matrix and time.

        RealMatrix q = new Array2DRowRealMatrix(Q.value());
        q.scalarMultiply(t.value());

        EigenDecomposition decomposition = new EigenDecomposition(q);

        throw new RuntimeException("Not implemented!");
        //return new RandomVariable<>("Y",0,this);
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(tParamName, t);
            put(QParamName, Q);
            put(XParamName, X);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case tParamName:
                t = value;
                break;
            case QParamName:
                Q = value;
                break;
            case XParamName:
                X = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
    }
}
