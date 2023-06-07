package lphy.parser;

import lphy.core.distributions.VectorizedDistribution;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.VectorUtils;
import lphy.graphicalModel.VectorizedRandomVariable;

import java.util.ArrayList;
import java.util.List;

public class DataClampingUtils {


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

}
