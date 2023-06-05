package lphy.base.bmodeltest;

import lphy.core.model.annotation.MethodInfo;
import lphy.core.model.annotation.TypeInfo;
import lphy.core.model.components.GeneratorCategory;

import java.util.*;

@TypeInfo(description = "The selected model set for bModelTest.",
        examples = {"simpleBModelTest.lphy"})
public class BModelSet {

    public enum ModelSet {allreversible, transitionTransversionSplit, namedSimple, namedExtended};

    ModelSet modelSet;

    int [][] models;

    // number of groups within a model
    int [] groupCount;
    // number of items in group
    int [][] subgroupCount;

    // integer number representing the model
    // e.g for model M191 = {1,2,3,4,5,3}, modelID = 123453
    int [] modelID;

    List<Integer> [] mergedModels;
    List<Integer> [] splitModels;

    public BModelSet(ModelSet modelSet) {

        this.modelSet = modelSet;
        initModels();
    }

    private void initModels() {

        MODELS = generateAllModels();

        switch (modelSet) {
            case allreversible:
                models = MODELS;
                break;
            case transitionTransversionSplit:
                models = MODELS;
                calcSubGroupCount(models);
                calcDag();
                setChildren(JC69, HKY);
                models = filterModels();
                break;
            case namedSimple:
                models = MODELS;
                calcSubGroupCount(models);
                calcDag();
                setChildren(JC69, HKY);
                setChildren(HKY, TN93);
                setChildren(TN93, GTR);
                models = filterModels();
                mergedModels = new List[models.length];
                splitModels = new List[models.length];
                for (int i = 0; i < 3; i++) {
                    mergedModels[i+1] = new ArrayList<>();
                    splitModels[i] = new ArrayList<>();
                    mergedModels[i+1].add(i);
                    splitModels[i].add(i + 1);
                }
                splitModels[3] = new ArrayList<>();
                mergedModels[0] = new ArrayList<>();
                break;
            case namedExtended:
                models = MODELS;
                calcSubGroupCount(models);
                calcDag();
                setChildren(JC69, HKY);
                setChildren(HKY, TN93);
                splitModels[HKY].add(K81);
                setChildren(TN93, TIM);
                setChildren(K81, NEW2);
                setChildren(TIM, NEW);
                splitModels[TIM].add(NEW);
                setChildren(TVM, GTR);
                setChildren(NEW, GTR);
                models = filterModels();
                break;
        }
    }

    protected int[][] generateAllModels() {
        return generateAllReversibleModels(new int[6]);
    }

    int[][] generateAllReversibleModels(int [] model) {
        List<int []> modelset = new ArrayList<>();
        while (model != null) {
            int [] nextmodel = model.clone();
            boolean [] done = new boolean[6];
            int groupcount = 0;
            int max = 0;
            for (int d : nextmodel) {
                max = Math.max(d + 1, max);
                if (!done[d]) {
                    done[d] = true;
                    groupcount++;
                }
            }
            if (max == groupcount) {
                normaliseModel(nextmodel);
                modelset.add(nextmodel);
            }

            model = nextModel(model);
        }

        Collections.sort(modelset , new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                for (int i = 0; i < o1.length; i++) {
                    if (o1[i] != o2[i]) {
                        if (o1[i] > o2[i]) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }
                return 0;
            }
        });
        for (int i = modelset.size() - 1; i > 0; i--) {
            int [] model1 = modelset.get(i);
            int [] model2 = modelset.get(i - 1);
            boolean equals = true;
            for (int k = 0; k < model1.length; k++) {
                if (model1[k] != model2[k]) {
                    equals = false;
                    break;
                }
            }
            if (equals) {
                modelset.remove(i);
            }
        }


        return modelset.toArray(new int[][]{});
    }

    private int [] nextModel(int[] model) {
        int [] nextmodel = model.clone();
        int pos = 5;
        while (pos >= 0 && nextmodel[pos] == pos) {
            nextmodel[pos] = 0;
            pos--;
        }
        if (pos == -1) {
            return null;
        }
        nextmodel[pos]++;
        return nextmodel;
    }

    public static void normaliseModel(int[] newmodel) {
        // sort the group numbers
        int [] shouldbe = new int[newmodel.length];
        Arrays.fill(shouldbe, -1);
        int k = 0;
        for (int i = 0; i < newmodel.length; i++) {
            if (shouldbe[newmodel[i]] == -1) {
                shouldbe[newmodel[i]] = k++;
            }
        }
        for (int i = 0; i < newmodel.length; i++) {
            newmodel[i] = shouldbe[newmodel[i]];
        }
    }

    private int[][] filterModels() {
        Set<Integer> filtered = new HashSet<>();
        filtered.add(0);
        Set<Integer> candidates = new HashSet<>();
        candidates.add(0);
        boolean [] done = new boolean[models.length];
        while (candidates.size() > 0) {
            int i = (Integer) candidates.toArray()[0];
            if (!done[i]) {
                for (int child : splitModels[i]) {
                    candidates.add(child);
                    filtered.add(child);
                }
                done[i] = true;
            }
            candidates.remove(i);
        }

        int [][] filteredModels = new int[filtered.size()][];
        List<Integer> f = new ArrayList<>();
        f.addAll(filtered);
        Collections.sort(f);
        int k = 0;
        for (int i : f) {
            filteredModels[k++] = models[i];
        }

        splitModels = null;
        mergedModels = null;
        return filteredModels;
    }

    private void calcDag() {
        mergedModels = new List[models.length];
        splitModels = new List[models.length];
        for (int i = 0; i < models.length; i++) {
            mergedModels[i] = new ArrayList<Integer>();
            splitModels[i] = new ArrayList<Integer>();
        }
        for (int i = 0; i < models.length; i++) {
            for (int j = 0; j < models.length; j++) {
                if (isSplit(j, i)) {
                    mergedModels[i].add(j);
                    boolean todo = isSplit(i, j);
                    splitModels[j].add(i);
                }
            }
        }
    }


    /** return whether is model1 nested in model2, eg, HKY is nested in JC69 **/
    public boolean isSplit(int m1, int m2) {
        if (getGroupCount(m1) + 1 != (getGroupCount(m2))) {
            return false;
        }
        int [] model1 = models[m1];
        int [] model2 = models[m2];
        int [] map = new int[model1.length];
        Arrays.fill(map, -1);
        int [] revmap = new int[model1.length];
        Arrays.fill(revmap, -1);
        int k1 = -1, k2 = -1;
        for (int i = 0; i < model1.length; i++) {
            if (map[model1[i]] != model2[i]) {
                if (map[model1[i]] < 0 && revmap[model2[i]] < 0) {
                    map[model1[i]] = model2[i];
                    revmap[model2[i]] = model1[i];
                } else if (k2 < 0) {
                    k1 = model1[i];
                    k2 = model2[i];
                } else if (model1[i] != k1 || model2[i] != k2) {
                    return false;
                }
            }
        }
        return k2 >= 0;
    }

    private void calcSubGroupCount(int[][] models) {
        groupCount = new int [models.length];
        subgroupCount = new int [models.length][6];
        for (int i = 0; i < models.length; i++) {
            for (int j = 0; j < 6; j++) {
                int m = models[i][j];
                groupCount[i] = Math.max(groupCount[i], m + 1);
                subgroupCount[i][m]++;
            }
        }

        modelID = new int [models.length];
        for (int i = 0; i < models.length; i++) {
            modelID[i] = calcModelID(models[i]);
        }
    }

    private int calcModelID(int[] model) {
        int modelID = 0;
        int k = 1;
        for (int j = model.length - 1; j >= 0; j--) {
            modelID += model[j] * k;
            k = k * 10;
        }
        return modelID;
    }

    private void setChildren(int parent, int child) {
        splitModels[parent].clear();
        splitModels[parent].add(child);
    }

    @MethodInfo(narrativeName="number of models", verbClause = "in",
            category = GeneratorCategory.MODEL_AVE_SEL, examples = {"simpleBModelTest.lphy","simpleBModelTest2.lphy"},
            description = "the number of models in this model set.")
    public Integer size() {
        return models.length;
    }

    @MethodInfo(narrativeName="the name of this model set", verbClause = "in", description = "the number of models in this model set.")
    public String getName() {
        return modelSet.name();
    }


    @MethodInfo(narrativeName="name of the model", description = "the name of the given model indicator.")
    public String getModelName(Integer modelIndicator) {
        int[] model = getModel(modelIndicator);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < model.length; i++) {
            builder.append(model[i]);
        }
        return builder.toString();
    }


    public int[] getModel(int i) {
        return models[i];
    }
    public int getGroupCount(int i) {
        return groupCount[i];
    }
    public int[] getSubGroupCount(int i) {
        return subgroupCount[i];
    }
    public int getMaxGroupCount() {
        return 6;
    }
    public int getModelNumber(int[] model) {
        int _modelID = calcModelID(model);
        int i = Arrays.binarySearch(modelID, _modelID);
        return i;
    }

    public static int [][] MODELS = new int[][]{
            {0,0,0,0,0,0},	 // Jukes Cantor	 F81	/* M1 */
            {0,0,0,0,0,1},			/* M7 */
            {0,0,0,0,1,0},			/* M6 */
            {0,0,0,0,1,1},			/* M22 */
            {0,0,0,0,1,2},			/* M47 */
            {0,0,0,1,0,0},			/* M5 */
            {0,0,0,1,0,1},			/* M21 */
            {0,0,0,1,0,2},			/* M46 */
            {0,0,0,1,1,0},			/* M20 */
            {0,0,0,1,1,1},			/* M23 */
            {0,0,0,1,1,2},			/* M107 */
            {0,0,0,1,2,0},			/* M45 */
            {0,0,0,1,2,1},			/* M106 */
            {0,0,0,1,2,2},			/* M105 */
            {0,0,0,1,2,3},			/* M142 */
            {0,0,1,0,0,0},			/* M4 */
            {0,0,1,0,0,1},			/* M19 */
            {0,0,1,0,0,2},			/* M44 */
            {0,0,1,0,1,0},			/* M18 */
            {0,0,1,0,1,1},			/* M24 */
            {0,0,1,0,1,2},			/* M104 */
            {0,0,1,0,2,0},			/* M43 */
            {0,0,1,0,2,1},			/* M103 */
            {0,0,1,0,2,2},			/* M98 */
            {0,0,1,0,2,3},			/* M141 */
            {0,0,1,1,0,0},			/* M17 */
            {0,0,1,1,0,1},			/* M25 */
            {0,0,1,1,0,2},			/* M102 */
            {0,0,1,1,1,0},			/* M26 */
            {0,0,1,1,1,1},			/* M8 */
            {0,0,1,1,1,2},			/* M65 */
            {0,0,1,1,2,0},			/* M101 */
            {0,0,1,1,2,1},			/* M64 */
            {0,0,1,1,2,2},			/* M108 */
            {0,0,1,1,2,3},			/* M175 */
            {0,0,1,2,0,0},			/* M42 */
            {0,0,1,2,0,1},			/* M100 */
            {0,0,1,2,0,2},			/* M97 */
            {0,0,1,2,0,3},			/* M140 */
            {0,0,1,2,1,0},			/* M99 */
            {0,0,1,2,1,1},			/* M63 */
            {0,0,1,2,1,2},			/* M109 */
            {0,0,1,2,1,3},			/* M174 */
            {0,0,1,2,2,0},			/* M96 */
            {0,0,1,2,2,1},			/* M110 */
            {0,0,1,2,2,2},			/* M62 */
            {0,0,1,2,2,3},			/* M172 */
            {0,0,1,2,3,0},			/* M139 */
            {0,0,1,2,3,1},			/* M173 */
            {0,0,1,2,3,2},			/* M171 */
            {0,0,1,2,3,3},			/* M170 */
            {0,0,1,2,3,4},			/* M202 */
            {0,1,0,0,0,0},			/* M3 */
            {0,1,0,0,0,1},			/* M16 */
            {0,1,0,0,0,2},			/* M41 */
            {0,1,0,0,1,0},	  // HKY	 F84	/* M15 */
            {0,1,0,0,1,1},			/* M27 */
            {0,1,0,0,1,2},			/* M95 */
            {0,1,0,0,2,0},	 // TN93		/* M40 */
            {0,1,0,0,2,1},			/* M94 */
            {0,1,0,0,2,2},			/* M83 */
            {0,1,0,0,2,3},			/* M138 */
            {0,1,0,1,0,0},			/* M14 */
            {0,1,0,1,0,1},			/* M28 */
            {0,1,0,1,0,2},			/* M93 */
            {0,1,0,1,1,0},			/* M29 */
            {0,1,0,1,1,1},			/* M9 */
            {0,1,0,1,1,2},			/* M71 */
            {0,1,0,1,2,0},			/* M92 */
            {0,1,0,1,2,1},			/* M70 */
            {0,1,0,1,2,2},			/* M111 */
            {0,1,0,1,2,3},			/* M184 */
            {0,1,0,2,0,0},			/* M39 */
            {0,1,0,2,0,1},			/* M91 */
            {0,1,0,2,0,2},			/* M82 */
            {0,1,0,2,0,3},			/* M137 */
            {0,1,0,2,1,0},			/* M90 */
            {0,1,0,2,1,1},			/* M69 */
            {0,1,0,2,1,2},			/* M112 */
            {0,1,0,2,1,3},			/* M183 */
            {0,1,0,2,2,0},			/* M81 */
            {0,1,0,2,2,1},			/* M113 */
            {0,1,0,2,2,2},			/* M58 */
            {0,1,0,2,2,3},			/* M163 */
            {0,1,0,2,3,0},			/* M136 */
            {0,1,0,2,3,1},			/* M182 */
            {0,1,0,2,3,2},			/* M162 */
            {0,1,0,2,3,3},			/* M161 */
            {0,1,0,2,3,4},			/* M201 */
            {0,1,1,0,0,0},			/* M13 */
            {0,1,1,0,0,1},			/* M30 */
            {0,1,1,0,0,2},			/* M89 */
            {0,1,1,0,1,0},			/* M31 */
            {0,1,1,0,1,1},			/* M10 */
            {0,1,1,0,1,2},			/* M75 */
            {0,1,1,0,2,0},			/* M88 */
            {0,1,1,0,2,1},			/* M74 */
            {0,1,1,0,2,2},			/* M114 */
            {0,1,1,0,2,3},			/* M187 */
            {0,1,1,1,0,0},			/* M32 */
            {0,1,1,1,0,1},			/* M11 */
            {0,1,1,1,0,2},			/* M77 */
            {0,1,1,1,1,0},			/* M12 */
            {0,1,1,1,1,1},			/* M2 */
            {0,1,1,1,1,2},			/* M37 */
            {0,1,1,1,2,0},			/* M76 */
            {0,1,1,1,2,1},			/* M36 */
            {0,1,1,1,2,2},			/* M57 */
            {0,1,1,1,2,3},			/* M132 */
            {0,1,1,2,0,0},			/* M87 */
            {0,1,1,2,0,1},			/* M72 */
            {0,1,1,2,0,2},			/* M115 */
            {0,1,1,2,0,3},			/* M186 */
            {0,1,1,2,1,0},			/* M73 */
            {0,1,1,2,1,1},			/* M35 */
            {0,1,1,2,1,2},			/* M56 */
            {0,1,1,2,1,3},			/* M131 */
            {0,1,1,2,2,0},			/* M116 */
            {0,1,1,2,2,1},			/* M55 */
            {0,1,1,2,2,2},			/* M48 */
            {0,1,1,2,2,3},			/* M151 */
            {0,1,1,2,3,0},			/* M185 */
            {0,1,1,2,3,1},			/* M130 */
            {0,1,1,2,3,2},			/* M150 */
            {0,1,1,2,3,3},			/* M149 */
            {0,1,1,2,3,4},			/* M197 */
            {0,1,2,0,0,0},			/* M38 */
            {0,1,2,0,0,1},			/* M86 */
            {0,1,2,0,0,2},			/* M80 */
            {0,1,2,0,0,3},			/* M135 */
            {0,1,2,0,1,0},			/* M85 */
            {0,1,2,0,1,1},			/* M66 */
            {0,1,2,0,1,2},			/* M117 */
            {0,1,2,0,1,3},			/* M179 */
            {0,1,2,0,2,0},			/* M79 */
            {0,1,2,0,2,1},			/* M118 */
            {0,1,2,0,2,2},			/* M59 */
            {0,1,2,0,2,3},			/* M167 */
            {0,1,2,0,3,0},			/* M134 */
            {0,1,2,0,3,1},			/* M178 */
            {0,1,2,0,3,2},			/* M166 */
            {0,1,2,0,3,3},			/* M158 */
            {0,1,2,0,3,4},			/* M200 */
            {0,1,2,1,0,0},			/* M84 */
            {0,1,2,1,0,1},			/* M67 */
            {0,1,2,1,0,2},			/* M119 */
            {0,1,2,1,0,3},			/* M181 */
            {0,1,2,1,1,0},			/* M68 */
            {0,1,2,1,1,1},			/* M34 */
            {0,1,2,1,1,2},			/* M54 */
            {0,1,2,1,1,3},			/* M129 */
            {0,1,2,1,2,0},			/* M120 */
            {0,1,2,1,2,1},			/* M53 */
            {0,1,2,1,2,2},			/* M49 */
            {0,1,2,1,2,3},			/* M155 */
            {0,1,2,1,3,0},			/* M180 */
            {0,1,2,1,3,1},			/* M128 */
            {0,1,2,1,3,2},			/* M154 */
            {0,1,2,1,3,3},			/* M146 */
            {0,1,2,1,3,4},			/* M196 */
            {0,1,2,2,0,0},			/* M78 */
            {0,1,2,2,0,1},			/* M121 */
            {0,1,2,2,0,2},			/* M60 */
            {0,1,2,2,0,3},			/* M169 */
            {0,1,2,2,1,0},	 // Kimura 81		/* M122 */
            {0,1,2,2,1,1},			/* M52 */
            {0,1,2,2,1,2},			/* M50 */
            {0,1,2,2,1,3},			/* M157 NEW2 */
            {0,1,2,2,2,0},			/* M61 */
            {0,1,2,2,2,1},			/* M51 */
            {0,1,2,2,2,2},			/* M33 */
            {0,1,2,2,2,3},			/* M126 */
            {0,1,2,2,3,0},	 // Posada 03		/* M168 */
            {0,1,2,2,3,1},			/* M156 */
            {0,1,2,2,3,2},			/* M125 */
            {0,1,2,2,3,3},			/* M143 */
            {0,1,2,2,3,4},			/* M193 NEW */
            {0,1,2,3,0,0},			/* M133 */
            {0,1,2,3,0,1},			/* M176 */
            {0,1,2,3,0,2},			/* M164 */
            {0,1,2,3,0,3},			/* M159 */
            {0,1,2,3,0,4},			/* M199 */
            {0,1,2,3,1,0},			/* M177 */
            {0,1,2,3,1,1},			/* M127 */
            {0,1,2,3,1,2},			/* M152 */
            {0,1,2,3,1,3},			/* M147 */
            {0,1,2,3,1,4},	 // Posada 03		/* M195 */
            {0,1,2,3,2,0},			/* M165 */
            {0,1,2,3,2,1},			/* M153 */
            {0,1,2,3,2,2},			/* M124 */
            {0,1,2,3,2,3},			/* M144 */
            {0,1,2,3,2,4},			/* M192 TVM */
            {0,1,2,3,3,0},			/* M160 */
            {0,1,2,3,3,1},			/* M148 */
            {0,1,2,3,3,2},			/* M145 */
            {0,1,2,3,3,3},			/* M123 */
            {0,1,2,3,3,4},			/* M190 */
            {0,1,2,3,4,0},			/* M198 */
            {0,1,2,3,4,1},			/* M194 */
            {0,1,2,3,4,2},			/* M191 */
            {0,1,2,3,4,3},			/* M189 */
            {0,1,2,3,4,4},			/* M188 */
            {0,1,2,3,4,5}	   // GTR		/* M203 */
    };

    final static int JC69 = 0;
    final static int HKY = 55;
    final static int TN93 = 58;
    final static int TIM = 172;
    final static int NEW = 176;
    final static int NEW2 = 167;
    final static int GTR = 202;
    final static int TVM = 190;
    final static int XTRA= 176;
    final static int K81 = 164;

}
