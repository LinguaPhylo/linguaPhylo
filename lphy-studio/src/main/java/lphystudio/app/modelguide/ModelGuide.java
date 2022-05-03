package lphystudio.app.modelguide;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.GeneratorCategory;
import lphy.parser.ParserUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Walter Xie
 */
public class ModelGuide {

//    public static final String[] geTy = new String[]{"ALL","Generative Distribution","Functions"};

    // Immutable list
    public List<Model> allModels;
    public List<Model> selectedModels = new ArrayList<>(); // for jTable

    private GeneratorCategory currCate = GeneratorCategory.ALL;
//    private String currGeneType = geTy[0];
    private Model currentModel;

    public ModelGuide() {
        List<Class<GenerativeDistribution>> generativeDistributions = ParserUtils.getGenerativeDistributions();
        generativeDistributions.sort(Comparator.comparing(Class::getSimpleName));

        List<Class<DeterministicFunction>> functions = ParserUtils.getDeterministicFunctions();
        functions.sort(Comparator.comparing(Class::getSimpleName));

        setAllModels(generativeDistributions, functions);
        addSelectedModels();
    }

    private void setAllModels(List<Class<GenerativeDistribution>> generativeDistributions,
                              List<Class<DeterministicFunction>> functions) {
        List<Model> all = new ArrayList<>();
        Model m;
        for (Class<GenerativeDistribution> distCls : generativeDistributions) {
            m = new Model(distCls);
            all.add(m);
        }
        for (Class<DeterministicFunction> fun : functions) {
            m = new Model(fun);
            all.add(m);
        }
        allModels = Collections.unmodifiableList(all);
    }

    private void addSelectedModels() {
        selectedModels.clear();
        if (currCate.equals(GeneratorCategory.ALL)) {
            selectedModels.addAll(allModels);
        } else {
            for (Model m : allModels) {
                if (currCate.equals(m.getCategory()))
                    selectedModels.add(m);
            }
        }
    }

    public boolean setSelectedModels(Object criteria) {
        boolean isSame = false;
        if (criteria instanceof GeneratorCategory cate) {
            isSame = currCate == cate;
            currCate = cate;
        }
//        else if (criteria instanceof String geneType) {
//            currGeneType = geneType;
//        }
        addSelectedModels();
        return isSame;
    }

    public List<Model> getAllModels() {
        return allModels;
    }

    public List<Model> getSelectedModels() {
        return Collections.unmodifiableList(selectedModels);
    }

    public Model getModel(int i) {
        return selectedModels.get(i);
    }

//    public Model getCurrentModel() {
//        return currentModel;
//    }

//    public void setCurrentModel(Model currentModel) {
//        this.currentModel = currentModel;
//    }
}
