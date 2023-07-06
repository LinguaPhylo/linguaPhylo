//package lphy.base.logger;
//
//import lphy.base.evolution.alignment.SimpleAlignment;
//import lphy.core.logger.*;
//import lphy.core.model.Value;
//import lphy.core.simulator.SimulatorListener;
//
//import java.io.*;
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * Logging alignments by different format
// */
//public class SimpleAlignmentLoggerListener implements SimulatorListener, LoggerListener, LPhyFileWriter {
//
////    List<Value> alignmentsPerRep; // Value<SimpleAlignment> or Value<SimpleAlignment[]>
//    int numReplicates = 0;
//
//    ValueFormatter valueFormatter;
////    Mode mode;
//
//    File outputDir;
//    String prefix = "";
//
//    public SimpleAlignmentLoggerListener() {//SPI
//    }
//
//
//    @Override
//    public void setMode(Mode mode) {
////        this.mode = mode;
//        throw new UnsupportedOperationException("TODO");
//    }
//
//    @Override
//    public Mode getMode() {
////        if (mode == null)
////            setMode(LoggerListener.DEFAULT_MODE);
//        return LoggerListener.DEFAULT_MODE;
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
////        setSimpleAlignmentsFrom(values);
//        List<Value> alignmentsPerRep = values.stream()
//                .filter(v -> v.value() != null)
//                .filter(v -> v.value() instanceof SimpleAlignment || v.value() instanceof SimpleAlignment[])
//                .collect(Collectors.toList());
//
//        for (Value algnValue : alignmentsPerRep) {
//            writeToFile(index, algnValue);
//        }
//    }
//
//    @Override
//    public void complete() {
//
//    }
//
////    private void setSimpleAlignmentsFrom(List<Value> randomValues) {
////        alignmentsPerRep = randomValues.stream()
////                .filter(v -> v.value() != null)
////                .filter(v -> v.value() instanceof SimpleAlignment || v.value() instanceof SimpleAlignment[])
////                .collect(Collectors.toList());
////    }
//
////    private void validate() {
////        if (alignmentsPerRep.size() != numReplicates)
////            throw new RuntimeException("There are " + alignmentsPerRep.size() +
////                    " states logged, which is not same as the number of replicates " + numReplicates + " !");
////    }
//
////    private void openFiles(List<RandomVariable<TimeTree>> treeVariables) throws IOException {
////        for (RandomVariable<TimeTree> tree : treeVariables) {
////            String fileName = name + "_" + tree.getId() + ".trees";
////            fileWriter.add(new FileWriter(fileName));
////        }
////    }
//
//
//    private void writeToFile(int rep, Value alignmentValue) {
//
//        if (rep < 0 || rep >= numReplicates)
//            throw new RuntimeException("The replication index " + rep + " (start from 0) " +
//                    "must be smaller than number of Replicates " + numReplicates);
//
//        String[] ids = valueFormatter.getValueID(alignmentValue);
//        String[] headers = valueFormatter.header(alignmentValue);
//        String[] bodies = valueFormatter.format(alignmentValue);
//        String[] footers = valueFormatter.footer(alignmentValue);
//
//        assert ids.length == bodies.length;
//
//        //TODO it seems not to require Mode
//        // if (getMode().equals(Mode.CELL)) {
//        //
//        // } else {
//
//        // 1 alignment per file
//        for (int i = 0; i < bodies.length; i++) {
//            // if maxId > 0, add postfix, e.g. _0.nexus
//            String postfix = "_" + ids[i] + (numReplicates > 0 ? "_" + rep : "");
//
//            //TODO Cannot set outputDir if using SPI, unless add IO interface in core
//            File file = openFile(outputDir, prefix, postfix, ".nexus");
//
//            try (PrintStream printStream = new PrintStream(file)) {
//                // in case 1 header for multiple bodies
//                String header = headers.length == 1 ? headers[0] : headers[i];
//                printStream.println(header);
//                printStream.println(bodies[i]);
//                String footer = footers.length == 1 ? footers[0] : footers[i];
//                printStream.println(footer);
//
//            } catch (FileNotFoundException e) {
//                LoggerUtils.log.severe("Cannot find file " + file.getAbsolutePath() + " !");
//                e.printStackTrace();
//            }
//        }
//
//
//    }
//
//    @Override
//    public void configOutput(File outputDir, String prefix) {
//        this.outputDir = outputDir;
//        this.prefix = prefix;
//    }
//
//    @Override
//    public File getOutputDir() {
//        if (outputDir == null)
//          return LPhyFileWriter.super.getOutputDir();
//        return outputDir;
//    }
//
//    //    @Override
////    public String getFormatterDescription() {
////        return getFormatterName() + " writes the alignment generated from each simulation into a file " +
////                "employed by a command-line application.";
////    }
//}
