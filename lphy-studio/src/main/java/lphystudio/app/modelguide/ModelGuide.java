package lphystudio.app.modelguide;

import lphy.core.model.BasicFunction;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.MethodInfo;
import lphy.core.parser.ParserLoader;
import lphy.core.parser.function.MethodCall;

import java.util.*;

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
//    private Model currentModel;

    public ModelGuide() {
        List<Class<GenerativeDistribution>> generativeDistributions = ParserLoader.getGenerativeDistributions();
        generativeDistributions.sort(Comparator.comparing(Class::getSimpleName));

        List<Class<DeterministicFunction>> functions = ParserLoader.getDeterministicFunctions();
        functions.sort(Comparator.comparing(BasicFunction::getName));

//        Set<Class<?>> types = Collections.unmodifiableSet();
        List<Class<?>> types = new ArrayList<>(ParserLoader.types);
        types.sort(Comparator.comparing(Class::getSimpleName));

        setAllModels(generativeDistributions, functions, types);
        addSelectedModels();
    }

    private void setAllModels(List<Class<GenerativeDistribution>> generativeDistributions,
                              List<Class<DeterministicFunction>> functions, List<Class<?>> types) {
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
        for (Class<?> type : types) {
            TreeMap<String, MethodInfo>  methodInfoTreeMap = MethodCall.getMethodCalls(type);
            // only add when it has MethodInfo
            if (methodInfoTreeMap.size() > 0) {
                m = new Model(type);
                all.add(m);
            }
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

    public List<Model> getModelsExcl(List<GeneratorCategory> exclCate) {
        List<Model> currModels = new ArrayList<>();
        for (Model model : selectedModels) {
            GeneratorCategory cate = model.getCategory();
            if (!exclCate.contains(cate)) currModels.add(model);
        }
        return currModels;
    }

    public List<Model> getSelectedModels() {
        return Collections.unmodifiableList(selectedModels);
    }

    public Model getModel(int i) {
        return selectedModels.get(i);
    }

    public GeneratorCategory getCurrentCategory() {
        return currCate;
    }
}
