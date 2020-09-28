package lphy.core;

import lphy.graphicalModel.*;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

public class CTMC implements GenerativeDistribution<Integer> {

    Value<Integer> X;
    Value<Double> t;
    Value<double[][]> Q;
    Random random;

    String XParamName;
    String tParamName;
    String QParamName;


    public CTMC(@ParameterInfo(name = "X", description = "the starting state.") Value<Integer> X,
            @ParameterInfo(name = "t", description = "the time.") Value<Double> t,
                     @ParameterInfo(name = "Q", description = "the instantaneous rate matrix.") Value<double[][]> Q,
                     Random random) {
        this.X = X;
        this.t = t;
        this.Q = Q;
        this.random = random;

        XParamName = getParamName(0);
        tParamName = getParamName(1);
        QParamName = getParamName(2);
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
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(tParamName, t);
        map.put(QParamName, Q);
        map.put(XParamName, X);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(tParamName)) t = value;
        else if (paramName.equals(QParamName)) Q = value;
        else if (paramName.equals(XParamName)) X = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }
}
