package lphy.base.function.io;

import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.Taxon;
import lphy.base.evolution.alignment.Alignment;
import lphy.base.evolution.alignment.MetaDataAlignment;
import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.base.function.alignment.MetaDataOptions;
import lphy.core.io.UserDir;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

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

    public ReadFasta(@ParameterInfo(name = ReaderConst.FILE, description = "the name of fasta file including path, which contains an alignment.") Value<String> filePath,
                     @ParameterInfo(name = ReaderConst.OPTIONS, description = "the map containing optional arguments and their values for reuse.",
                             optional=true) Value<Map<String, String>> options,
                     @ParameterInfo(name = ReaderConst.SEQUENCE_TYPE, description = "the sequence type for sequences in the fasta format, " +
                             "default to guess the type between Nucleotide and Amino Acid.",
                             narrativeName = "sequence type", optional = true) Value<SequenceType> sequenceType ) {


        if (filePath == null) throw new IllegalArgumentException("The file name can't be null!");
        setParam(ReaderConst.FILE, filePath);

        if (options != null) setParam(ReaderConst.OPTIONS, options);
        if (sequenceType != null) setParam(ReaderConst.SEQUENCE_TYPE, sequenceType);
    }


    @GeneratorInfo(name="readFasta", verbClause = "is read from", narrativeName = "fasta file",
            category = GeneratorCategory.TAXA_ALIGNMENT, examples = {"covidDPG.lphy"},
            description = "A function that parses an alignment from a fasta file.")
    public Value<Alignment> apply() {

        String filePath = ((Value<String>) getParams().get(ReaderConst.FILE)).value();

        Value<Map<String, String>> optionsVal = getParams().get(ReaderConst.OPTIONS);
        String ageDirectionStr = MetaDataOptions.getAgeDirectionStr(optionsVal);
        String ageRegxStr = MetaDataOptions.getAgeRegxStr(optionsVal);
        String spRegxStr = MetaDataOptions.getSpecieseRegex(optionsVal);

        //*** parsing ***//

        Path nexPath = UserDir.getUserPath(filePath);

        Reader reader = getReader(nexPath.toString());

        Value<SequenceType> sequenceTypeVal = getParams().get(ReaderConst.SEQUENCE_TYPE);
        SequenceType sequenceType = sequenceTypeVal != null ? sequenceTypeVal.value() : null;
        // if null, then guess the sequence type
        Alignment faData = getAlignment(reader, sequenceType, ageRegxStr, ageDirectionStr, spRegxStr);

        return new Value<>(null, faData, this);

    }

    /**
     * The utility method to import an alignment in a fasta format from reader.
     * If both ageRegxStr and spRegxStr are null, then create a {@link SimpleAlignment}.
     * @param reader           it can be created from either a file or string.
     * @param sequenceTypeToGuess   {@link SequenceType}. If null, then guess the sequence type between Nucleotide and Amino Acid.
     * @param ageRegxStr       Java regular expression to extract dates from taxa names.
     * @param ageDirectionStr  {@link MetaDataAlignment.AgeDirection}.
     * @param spRegxStr        Java regular expression to extract species from taxa names.
     * @return  {@link Alignment} imported from a fasta format.
     */
    public static Alignment getAlignment(Reader reader, SequenceType sequenceTypeToGuess,
                                          String ageRegxStr, String ageDirectionStr, String spRegxStr) {
        List<Sequence> sequenceList = new ArrayList<>();
        try {
            FastaImporter fastaImporter = new FastaImporter(reader, sequenceTypeToGuess);
            sequenceList = fastaImporter.importSequences();
        } catch (IOException | ImportException e) {
            LoggerUtils.logStackTrace(e);
        }
        if (sequenceList.size() < 1)
            throw new IllegalArgumentException("Fasta file has no sequence !");

        // actual sequence type after guessing
        SequenceType sequenceType = sequenceList.get(0).getSequenceType();

        Taxon[] taxons = new Taxon[sequenceList.size()];
        int siteCount = Objects.requireNonNull(sequenceList.get(0)).getLength();
        // create taxa
        for (int i = 0; i < sequenceList.size(); i++) {
            Sequence s = sequenceList.get(i);
            jebl.evolution.taxa.Taxon t = s.getTaxon();
            taxons[i] =new Taxon(t.getName());
        }

        Alignment faData;
        if ( !(ageRegxStr == null && spRegxStr == null) ) {
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

        int len = sequenceList.get(0).getLength();
        // fill in sequences
        for (int i = 0; i < sequenceList.size(); i++) {
            Sequence sequence = sequenceList.get(i);
            if (len != sequence.getLength())
                throw new IllegalArgumentException("Sequence " + i + " has different length ! Alignment is required.");
            for (int s = 0; s < sequence.getLength(); s++) {
                //*** convert char into int ***//
                State state = sequence.getState(s);
                int stateNum = state.getIndex();
                // the taxon index in List should be same to Taxon[] taxonArray in Alignment
                faData.setState(i, s, stateNum);
            }
        }
        return faData;
    }

    private Reader getReader(String fileName) {
        Reader reader = null;
        try {
            // Common Extensions: .fasta, .fas, .fa
            // Other Extensions (depending on sequence type):
            // .fna (nucleotide sequences), .ffn (nucleotide sequences),
            // .faa (amino acid sequences), .frn (RNA sequences), .mpfa (multiple protein FASTA)
            if (!(fileName.endsWith("fasta") || fileName.endsWith("fas") || fileName.endsWith("fa") ||
                    fileName.endsWith("fna") || fileName.endsWith("ffn") ||
                    fileName.endsWith("faa") || fileName.endsWith("frn") || fileName.endsWith("mpfa") ))
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
