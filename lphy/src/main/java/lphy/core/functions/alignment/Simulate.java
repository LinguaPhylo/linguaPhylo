package lphy.core.functions.alignment;

import lphy.core.GraphicalLPhyParser;
import lphy.core.LPhyParser;
import lphy.core.Sampler;
import lphy.evolution.alignment.Alignment;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.MapValue;
import lphy.parser.REPL;
import lphy.system.UserDir;
import lphy.util.LoggerUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class Simulate extends DeterministicFunction<Map<String, Object>> {

    private static final String lphyScriptParamName = "lphy";
    private static final String idParamName = "alignmentId";
    private static final String logAlgParamName = "logAlignments";
    private final File file;
    private final String[] algId;
    private boolean saveIntermediateAlg = true;

    private Map<String, Alignment> intermediateAlignments = new HashMap<>();

    public Simulate(@ParameterInfo(name = lphyScriptParamName, description = "the file path of the lphy script to simulate data.") Value<String> filePathVal,
                    @ParameterInfo(name = idParamName, description = "the array of selected alignment ID(s) if script produces intermediate alignment(s).")
                    Value<String[]> algIdVal){
//                    @ParameterInfo(name = logAlgParamName, description = "log intermediate alignments.", optional = true) Value<Boolean> logAlgVal) {

        if (filePathVal == null) throw new IllegalArgumentException("The lphy file path can't be null!");
        File filePath = Paths.get(filePathVal.value()).toFile();
        if (!filePath.exists())
            filePath = UserDir.getUserPath(filePathVal.value()).toFile();
        if (!filePath.exists())
            throw new IllegalArgumentException("Cannot locate the lphy file path : " + filePath);
        this.file = filePath;
        LoggerUtils.log.info("Simulate data from lphy script: " + filePath.getAbsolutePath());

        if (algIdVal == null) throw new IllegalArgumentException("The alignment ID can't be null!");
        this.algId = algIdVal.value();
//        if (logAlgVal != null) {
//            saveIntermediateAlg = logAlgVal.value();
//        }
    }

    @GeneratorInfo(name = "simulate", description = "The function to simulate data from a given lphy script.")
    public Value<Map<String, Object>> apply() {
        LPhyParser parser = new REPL();
        try {
            parser.source(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GraphicalLPhyParser gparser = new GraphicalLPhyParser(parser);
        Sampler sampler = new Sampler(gparser);

        List<RandomValueLogger> loggers = new ArrayList<>();
        sampler.sample(1, loggers);

        // have to match MapValue
        Map<String, Object> alignmentMap = new HashMap<>();
        for (String id : algId) {
            Value<?> algValue = parser.getValue(id, LPhyParser.Context.model);
            if (algValue != null && algValue.value() instanceof Alignment alig) {
                LoggerUtils.log.info("Simulate alignment (" + alig.getTaxa().ntaxa() +
                        " taxa, " + alig.nchar() + " sites, " + alig.getSequenceTypeStr() +
                        ") from " + file.getAbsolutePath());
                alignmentMap.put(id, alig);
            } else
                throw new IllegalArgumentException("Cannot locate the alignment " + algValue +
                        " from the lphy script " + file);
        }
        if (saveIntermediateAlg) {
            Map<String, Value<?>> model = parser.getModelDictionary();
            for (Map.Entry<String, Value<?>> entry : model.entrySet()) {
                if (!Arrays.asList(algId).contains(entry.getKey()) && entry.getValue().value() instanceof Alignment al) {
                    intermediateAlignments.put(entry.getKey(), al);
                }
//                    System.out.println(entry.getKey() + " => " + entry.getValue());
            }
        }
        return new MapValue(null, alignmentMap, this);
    }

    public Map<String, Alignment> getIntermediateAlignments() {
        return intermediateAlignments;
    }
}
