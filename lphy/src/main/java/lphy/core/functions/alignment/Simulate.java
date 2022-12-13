package lphy.core.functions.alignment;

import lphy.core.GraphicalLPhyParser;
import lphy.core.LPhyParser;
import lphy.core.Sampler;
import lphy.evolution.alignment.Alignment;
import lphy.graphicalModel.*;
import lphy.parser.REPL;
import lphy.util.LoggerUtils;
import lphy.util.RandomUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lphy.util.RandomUtils.SEED_PARA_NAME;

public class Simulate extends DeterministicFunction<Alignment> {

    private static final String lphyScriptParamName = "lphy";
    private static final String idParamName = "alignmentId";
    private static final String logAlgParamName = "logAlignments";
    private final File file;
    private final String algId;
    private boolean saveIntermediateAlg = true;

    private Map<String, Alignment> intermediateAlignments = new HashMap<>();

    public Simulate(@ParameterInfo(name = lphyScriptParamName, description = "the file path of the lphy script to simulate data.") Value<String> filePathVal,
                    @ParameterInfo(name = idParamName, description = "the selected alignment ID if script produces intermediate alignment(s).") Value<String> algIdVal,
                    @ParameterInfo(name = SEED_PARA_NAME, description = "the seed.", optional = true) Value<Integer> seedVal){
//                    @ParameterInfo(name = logAlgParamName, description = "log intermediate alignments.", optional = true) Value<Boolean> logAlgVal) {

        if (filePathVal == null) throw new IllegalArgumentException("The lphy file path can't be null!");
        File filePath = Paths.get(filePathVal.value()).toFile();
        if (!filePath.exists())
            throw new IllegalArgumentException("Cannot locate the lphy file path : " + filePath);
        this.file = filePath;

        if (algIdVal == null) throw new IllegalArgumentException("The alignment ID can't be null!");
        this.algId = algIdVal.value();
        if (seedVal != null) {
            RandomUtils.setSeed(seedVal.value());
        }
//        if (logAlgVal != null) {
//            saveIntermediateAlg = logAlgVal.value();
//        }
    }

    @GeneratorInfo(name = "simulate", description = "The function to simulate data from a given lphy script.")
    public Value<Alignment> apply() {
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

//TODO multi-alignments not working
        //
        Value<?> algValue = parser.getValue(algId, LPhyParser.Context.model);
        if (algValue != null && algValue.value() instanceof Alignment alig) {
            LoggerUtils.log.info("Simulate alignment (" + alig.getTaxa().ntaxa() +
                    " taxa, " + alig.nchar() + " sites, " + alig.getSequenceTypeStr() +
                    ") from " + file.getAbsolutePath());

            if (saveIntermediateAlg) {
                Map<String, Value<?>> model = parser.getModelDictionary();
                for (Map.Entry<String, Value<?>> entry : model.entrySet()) {
                    if (!entry.getKey().equals(algId) && entry.getValue().value() instanceof Alignment al) {
                        intermediateAlignments.put(entry.getKey(), al);
                    }
//                    System.out.println(entry.getKey() + " => " + entry.getValue());
                }
            }
            return new Value<>(algId, alig, this);
        }

        throw new IllegalArgumentException("Cannot locate the alignment " + algValue +
                " from the lphy script " + file);
    }

    public Map<String, Alignment> getIntermediateAlignments() {
        return intermediateAlignments;
    }
}
