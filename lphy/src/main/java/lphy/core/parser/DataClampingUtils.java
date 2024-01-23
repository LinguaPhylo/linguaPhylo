package lphy.core.parser;

import lphy.core.model.RandomVariable;
import lphy.core.vectorization.IID;
import lphy.core.vectorization.VectorUtils;
import lphy.core.vectorization.VectorizedDistribution;
import lphy.core.vectorization.VectorizedRandomVariable;

import java.util.ArrayList;
import java.util.List;

public class DataClampingUtils {

    public static boolean isDataClamping(Var var, LPhyParserDictionary parser) {
        return var.getId() != null && parser.getDataDictionary().containsKey(var.getId());
    }

    /**
     * @param id            id of VectorizedRandomVariable
     * @param vectDist      the generator
     * @param dataArray     data to clamp
     * @return    {@link VectorizedRandomVariable} for the data clamping case,
     *            when the generator is {@link VectorizedDistribution}.
     */
    public static VectorizedRandomVariable getDataClampedVectorizedRandomVariable(
            String id, VectorizedDistribution<?> vectDist, Object[] dataArray) {

        List<RandomVariable> componentVariables = new ArrayList<>();
        // loop through the data dataArray to clamp
        Object[] objects = dataArray;
        for (int i = 0; i < objects.length; i++) {
            Object element = objects[i];
            String elemId = id + VectorUtils.INDEX_SEPARATOR + i;
            // clamp data to component variables
            RandomVariable compVar = new RandomVariable(elemId, element, vectDist.getBaseDistribution(i));
            compVar.setClamped(true);
            componentVariables.add(compVar);
        }
        // require to wrap with VectorizedRandomVariable
        VectorizedRandomVariable variable = new VectorizedRandomVariable(id, componentVariables, vectDist);
        variable.setClamped(true);
        return variable;
    }

    public static VectorizedRandomVariable getDataClampedVectorizedRandomVariable(
            String id, IID<?> iid, Object[] dataArray) {

        List<RandomVariable> componentVariables = new ArrayList<>();
        // loop through the data dataArray to clamp
        Object[] objects = dataArray;
        for (int i = 0; i < objects.length; i++) {
            Object element = objects[i];
            String elemId = id + VectorUtils.INDEX_SEPARATOR + i;
            // clamp data to component variables
            RandomVariable compVar = new RandomVariable(elemId, element, iid.getBaseDistribution());
            compVar.setClamped(true);
            componentVariables.add(compVar);
        }
        // require to wrap with VectorizedRandomVariable
        VectorizedRandomVariable variable = new VectorizedRandomVariable(id, componentVariables, iid);
        variable.setClamped(true);
        return variable;
    }
}
