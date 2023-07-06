//package lphy.base.logger;
//
//import lphy.base.evolution.tree.TimeTree;
//import lphy.base.parser.nexus.NexusWriter;
//import lphy.core.logger.*;
//import lphy.core.model.Symbols;
//import lphy.core.model.Value;
//import lphy.core.simulator.SimulatorListener;
//import lphy.core.vectorization.VectorUtils;
//
//import java.io.*;
//import java.util.*;
//
///**
// * Store all trees in a Map {@link #treesByValueId},
// * where key is the tree id, and values are the list of  @link #TimeTree}.
// */
//public class TreeLoggerListener implements SimulatorListener, LoggerListener, LPhyFileWriter {
//
//    Map<String, List<TimeTree>> treesByValueId;
//
////    List<Value> treesAllReps = new ArrayList<>();
//
//    int numReplicates = 0;
//
//    ValueFormatter valueFormatter;
//    Mode mode;
//
//    File outputDir;
//
//    public TreeLoggerListener() {
//    }
//
//
//    @Override
//    public void setMode(Mode mode) {
//        this.mode = mode;
//    }
//
//    @Override
//    public Mode getMode() {
//        if (mode == null)
//            setMode(LoggerListener.DEFAULT_MODE);
//        return this.mode;
//    }
//
//    @Override
//    public void setValueFormatter(ValueFormatter valueFormatter) {
//        this.valueFormatter = valueFormatter;
//    }
//
//
//    @Override
//    public void start(int numReplicates) {
//        this.numReplicates = numReplicates;
//    }
//
//    @Override
//    public void replicate(int index, List<Value> values) {
//        if (index < 1)
//            setTreesFrom(values);
//
//
//    }
//
//    @Override
//    public void complete() {
//
//    }
//
//    private void setTreesFrom(List<Value> randomValues) {
//        treesByValueId = new TreeMap<>();
//
//        for (Value v : randomValues) {
//            if (v.value() instanceof TimeTree timeTree) {
//
//                List<TimeTree> trees = treesByValueId.computeIfAbsent(v.getId(), k -> new ArrayList<>());
//                trees.add(timeTree);
//
//            } else if (v.value() instanceof TimeTree[] timeTrees) {
//                // VectorizedRandomVariable
//                for (int i = 0; i < timeTrees.length; i++) {
//                    String id = v.getId() + VectorUtils.INDEX_SEPARATOR + i;
//                    List<TimeTree> trees = treesByValueId.computeIfAbsent(id, k -> new ArrayList<>());
//                    trees.add(timeTrees[i]);
//                }
//            }
//        }
//    }
//
//    /**
//     * Ignore str. When rep < 1, to write all in 1 file.
//     */
//    public void writeToFile(File outputDir, String prefix, String str, int rep) {
//        if (rep < 1) {
//            treesByValueId.forEach((key, treeList) -> {
//                File file = openFile(outputDir, prefix, "_" + Symbols.getCanonical(key), ".trees");
//                try {
//                    NexusWriter.write(null, treeList, new PrintStream(file));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//    }
//
//
////    @Override
////    public String getFormatterDescription() {
////        return getFormatterName() + " writes the trees generated from simulations into a file " +
////                "employed by a command-line application.";
////    }
//
////    public Map<String, List<TimeTree>> getTreesByValueId() {
////        return treesByValueId;
////    }
//
////    private String fileName;
////
////    private List<Value<TimeTree>> getTreeValues(List<Value<?>> variables) {
////        List<Value<TimeTree>> trees = new ArrayList<>();
////        for (Value v : variables) {
////            if (v.value() instanceof TimeTree) {
////                trees.add((Value<TimeTree>)v);
////            } else if (v.value() instanceof TimeTree[]) {
////                // VectorizedRandomVariable
////                TimeTree[] value = (TimeTree[]) v.value();
////                for (int i = 0; i < value.length; i++) {
////                    TimeTree t = value[i];
////                    trees.add(new Value<>(v.getId()+ VectorUtils.INDEX_SEPARATOR +i, t));
////                }
////            }
////        }
////        trees.sort(Comparator.comparing(Value::getId));
////        return trees;
////    }
//}
