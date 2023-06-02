package lphy.base.functions.alignment;

import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.Taxon;
import lphy.base.evolution.alignment.Alignment;
import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.base.evolution.io.MetaDataAlignment;
import lphy.base.evolution.io.MetaDataOptions;
import lphy.base.system.UserDir;
import lphy.core.exception.LoggerUtils;
import lphy.core.model.components.*;

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


    @GeneratorInfo(name="readFasta", verbClause = "is read from", narrativeName = "fasta file",
            category = GeneratorCategory.TAXA_ALIGNMENT, examples = {"covidDPG.lphy"},
            description = "A function that parses an alignment from a fasta file.")
    public Value<Alignment> apply() {

        String fileName = ((Value<String>) getParams().get(fileParamName)).value();

        Value<Map<String, String>> optionsVal = getParams().get(optionsParamName);
        String ageDirectionStr = MetaDataOptions.getAgeDirectionStr(optionsVal);
        String ageRegxStr = MetaDataOptions.getAgeRegxStr(optionsVal);
        String spRegxStr = MetaDataOptions.getSpecieseRegex(optionsVal);

        //*** parsing ***//
        SequenceType sequenceType = SequenceType.NUCLEOTIDE;

        Path nexPath = UserDir.getUserPath(fileName);

        Reader reader = getReader(nexPath.toString());

        List<Sequence> sequenceList = new ArrayList<>();
        try {
            FastaImporter fastaImporter = new FastaImporter(reader, sequenceType);
            sequenceList = fastaImporter.importSequences();
        } catch (IOException | ImportException e) {
            LoggerUtils.logStackTrace(e);
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

        Alignment faData;
        if (optionsVal != null) {
            faData = new MetaDataAlignment(Taxa.createTaxa(taxons), siteCount, sequenceType);

            // set age to Taxon
            if (ageRegxStr != null)
                ((MetaDataAlignment) faData).setAgesParsedFromTaxaName(ageRegxStr, ageDirectionStr);
            // set species to Taxon
            if (spRegxStr != null)
                ((MetaDataAlignment) faData).setSpeciesParsedFromTaxaName(spRegxStr);
        } else {
            faData = new SimpleAlignment(Taxa.createTaxa(taxons), siteCount, sequenceType);
        }

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
                throw new IOException("Cannot find Fasta file ! " + nexFile +
                        ", user.dir = " + System.getProperty("user.dir"));

            reader = Files.newBufferedReader(nexFile); // StandardCharsets.UTF_8
//            reader.mark(READ_AHEAD_LIMIT); // to reset reader back to READ_AHEAD_LIMIT
        } catch (IOException e) {
            LoggerUtils.logStackTrace(e);
        }
        return reader;
    }

}
