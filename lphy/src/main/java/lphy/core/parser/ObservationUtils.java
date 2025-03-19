package lphy.core.parser;

import lphy.core.model.RandomVariable;
import lphy.core.parser.graphicalmodel.GraphicalModel;
import lphy.core.vectorization.IID;
import lphy.core.vectorization.VectorUtils;
import lphy.core.vectorization.VectorizedDistribution;
import lphy.core.vectorization.VectorizedRandomVariable;

import java.util.ArrayList;
import java.util.List;

public class ObservationUtils {

    // true if this id is contained in both the data block
    // and the model block and the model id is a random variable.
    public static boolean isObserved(String id, GraphicalModel graphicalModel) {
        return (id != null && graphicalModel.getDataDictionary().containsKey(id) &&
                graphicalModel.getModelDictionary().containsKey(id) &&
                graphicalModel.getModelDictionary().get(id) instanceof RandomVariable);
    }

    /**
     * @param varInModel   the {@link Var} created by the generator in model block
     * @param parser       used to pull out data dict
     * @return     true if it is data clamping, given a variable id and data dict.
     *             This is currently used in parser.
     */
    public static boolean isObserved(Var varInModel, LPhyParserDictionary parser) {
        return varInModel.id != null && parser.getDataDictionary().containsKey(varInModel.id);
    }

    /**
     * @param dataToClamp data to clamp
     * @param id          id of VectorizedRandomVariable
     * @param generator    the generator
     * @return {@link VectorizedRandomVariable} for the data clamping case,
     * when the generator is {@link VectorizedDistribution}.
     */
    public static VectorizedRandomVariable setObservationsToVectorizedRandomVariable(
            Object[] dataToClamp, String id, VectorizedDistribution<?> generator) {

        List<RandomVariable> componentVariables = new ArrayList<>();
        // loop through the data dataArray to clamp
        Object[] objects = dataToClamp;
        for (int i = 0; i < objects.length; i++) {
            Object element = objects[i];
            String elemId = id + VectorUtils.INDEX_SEPARATOR + i;
            // clamp data to component variables
            RandomVariable compVar = new RandomVariable(elemId, element, generator.getBaseDistribution(i));
            compVar.setObserved(true);
            componentVariables.add(compVar);
        }
        // require to wrap with VectorizedRandomVariable
        VectorizedRandomVariable variable = new VectorizedRandomVariable(id, componentVariables, generator);
        variable.setObserved(true);
        return variable;
    }

    /**
     * @param observations normally mean data in Bayesian
     * @param id          id of VectorizedRandomVariable
     * @param generator         the generator
     * @return {@link VectorizedRandomVariable} for the data clamping case,
     * when the generator is {@link IID}.
     */
    public static VectorizedRandomVariable setObservationsToVectorizedRandomVariable(
            Object[] observations, String id, IID<?> generator) {

        List<RandomVariable> componentVariables = new ArrayList<>();
        // loop through the data dataArray to clamp
        Object[] objects = observations;
        for (int i = 0; i < objects.length; i++) {
            Object element = objects[i];
            String elemId = id + VectorUtils.INDEX_SEPARATOR + i;
            // clamp data to component variables
            RandomVariable compVar = new RandomVariable(elemId, element, generator.getBaseDistribution());
            compVar.setObserved(true);
            componentVariables.add(compVar);
        }
        // require to wrap with VectorizedRandomVariable
        VectorizedRandomVariable variable = new VectorizedRandomVariable(id, componentVariables, generator);
        variable.setObserved(true);
        return variable;
    }
}
