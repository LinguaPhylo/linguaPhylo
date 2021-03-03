package lphy.core.functions;

import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import lphy.evolution.Taxa;
import lphy.evolution.Taxon;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.io.MetaDataAlignment;
import lphy.evolution.io.MetaDataOptions;
import lphy.evolution.sequences.SequenceTypeFactory;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.StringValue;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * D = readFasta(file="h3n2_2deme.fna");
 * @see MetaDataAlignment
 */
public class ReadFasta extends DeterministicFunction<Alignment> {

    private final String fileParamName = "file";
    private final String optionsParamName = "options";

    public ReadFasta(@ParameterInfo(name = fileParamName, description = "the name of fasta file.") Value<String> fileName,
                     @ParameterInfo(name = optionsParamName, description = "the map containing optional arguments and their values for reuse.",
                             optional=true) Value<Map<String, String>> options ) {


        if (fileName == null) throw new IllegalArgumentException("The file name can't be null!");
        setParam(fileParamName, fileName);

        if (options != null) setParam(optionsParamName, options);
    }


    @GeneratorInfo(name="readFasta",description = "A function that parses an alignment from a Nexus file.")
    public Value<Alignment> apply() {

        String fileName = ((Value<String>) getParams().get(fileParamName)).value();

        Value<Map<String, String>> optionsVal = getParams().get(optionsParamName);
        String ageDirectionStr = MetaDataOptions.getAgeDirectionStr(optionsVal);
        String ageRegxStr = MetaDataOptions.getAgeRegxStr(optionsVal);

        //*** parsing ***//
        SequenceTypeFactory sequenceTypeFactory = new SequenceTypeFactory();
        SequenceType sequenceType = sequenceTypeFactory.getDataType("nucleotide");

        Reader reader = getReader(fileName);

        List<Sequence> sequenceList = new ArrayList<>();
        try {
            FastaImporter fastaImporter = new FastaImporter(reader, sequenceType);
            sequenceList = fastaImporter.importSequences();
        } catch (IOException | ImportException e) {
            e.printStackTrace();
        }
        if (sequenceList.size() < 1)
            throw new IllegalArgumentException("Fasta file has no sequence !");

        Taxon[] taxons = new Taxon[sequenceList.size()];
        int siteCount = Objects.requireNonNull(sequenceList.get(0)).getLength();
        // create taxa
        for (int i = 0; i < sequenceList.size(); i++) {
            Sequence s = sequenceList.get(i);
            jebl.evolution.taxa.Taxon t = s.getTaxon();
            taxons[i] =new Taxon(t.getName());
        }

        MetaDataAlignment faData = new MetaDataAlignment(Taxa.createTaxa(taxons), siteCount, sequenceType);

        // fill in sequences
        for (int i = 0; i < sequenceList.size(); i++) {
            Sequence sequence = sequenceList.get(i);
            for (int s = 0; s < sequence.getLength(); s++) {
                //*** convert char into int ***//
                State state = sequence.getState(s);
                int stateNum = state.getIndex();
                // the taxon index in List should be same to Taxon[] taxonArray in Alignment
                faData.setState(i, s, stateNum);
            }
        }

        // dates
        if (ageRegxStr != null)
            faData.setAgesFromTaxaName(ageRegxStr, ageDirectionStr);

        return new Value<>(null, faData, this);

    }

    private Reader getReader(String fileName) {
        Reader reader = null;
        try {
            if (!(fileName.endsWith("fasta") || fileName.endsWith("fna") || fileName.endsWith("ffn") ||
                    fileName.endsWith("faa") || fileName.endsWith("frn")))
                throw new IOException("Fasta file name's suffix is invalid ! " + fileName);

            final Path nexFile = Paths.get(fileName);

            if (!nexFile.toFile().exists() || nexFile.toFile().isDirectory())
                throw new IOException("Cannot find Fasta file ! " + nexFile);

            reader = Files.newBufferedReader(nexFile); // StandardCharsets.UTF_8
//            reader.mark(READ_AHEAD_LIMIT); // to reset reader back to READ_AHEAD_LIMIT
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reader;
    }

}
