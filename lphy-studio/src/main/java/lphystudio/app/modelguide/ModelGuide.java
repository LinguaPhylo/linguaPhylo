package lphystudio.app.modelguide;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GenerativeDistribution;
import lphy.parser.ParserUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Walter Xie
 */
public class ModelGuide {

    // Immutable list
    public List<Model> allModels;
    public List<Model> selectedModels; // for jTable
    private Model currentModel;

    public ModelGuide() {
        List<Class<GenerativeDistribution>> generativeDistributions = ParserUtils.getGenerativeDistributions();
        generativeDistributions.sort(Comparator.comparing(Class::getSimpleName));

        List<Class<DeterministicFunction>> functions = ParserUtils.getDeterministicFunctions();
        functions.sort(Comparator.comparing(Class::getSimpleName));

        setAllModels(generativeDistributions, functions);
    }

    private void setAllModels(List<Class<GenerativeDistribution>> generativeDistributions,
                                     List<Class<DeterministicFunction>> functions) {
        List<Model> all = new ArrayList<>();
        Model m;
        for(Class<GenerativeDistribution> distCls : generativeDistributions) {
            m = new Model(distCls);
            all.add(m);
        }
        for(Class<DeterministicFunction> fun : functions) {
            m = new Model(fun);
            all.add(m);
        }
        allModels = Collections.unmodifiableList(all);
    }

    public List<Model> getAllModels() {
        return allModels;
    }

    public List<Model> getSelectedModels() {


return new ArrayList<>();
    }

    public Model getModel(int i) {
        return allModels.get(i);
    }

    public Model getCurrentModel() {
        return currentModel;
    }

    public void setCurrentModel(Model currentModel) {
        this.currentModel = currentModel;
    }
}
