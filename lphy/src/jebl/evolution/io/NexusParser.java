

package jebl.evolution.io;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.BasicAlignment;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import jebl.evolution.taxa.Taxon;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Apply {@link ExtNexusImporter} to parsing Nexus into LPhy objects.
 */
public class NexusParser {
    protected final int READ_AHEAD_LIMIT = 50000;
    BufferedReader reader;
    ExtNexusImporter importer;

    protected Path nexFile; // lock to 1 file now

    public NexusParser(Path nexFile) {
        this.nexFile = nexFile;

        try {
            if (!(nexFile.toString().endsWith("nex") || nexFile.toString().endsWith("nexus") ||
                    nexFile.toString().endsWith("nxs")))
                throw new IOException("Nexus file name's suffix is invalid ! " + nexFile);
            if (!nexFile.toFile().exists() || nexFile.toFile().isDirectory())
                throw new IOException("Cannot find Nexus file ! " + nexFile);

            reader = Files.newBufferedReader(nexFile); // StandardCharsets.UTF_8
            // Marks the present position in the stream.
            reader.mark(READ_AHEAD_LIMIT);
            this.importer = new ExtNexusImporter(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Be careful. Resets the stream to the most recent mark.
     * @see BufferedReader#reset()
     */
    protected void resetReader() throws IOException {
        this.reader.reset();
        this.importer = new ExtNexusImporter(reader);
    }

    public lphy.evolution.alignment.Alignment getLPhyAlignment(String[] partNames) {
        List<Alignment> alignmentList = null;
        try {
            alignmentList = this.importAlignments();
//            resetReader();
//            importer.importCharsets();
        } catch (IOException | ImportException e) {
            e.printStackTrace();
        }

        if (alignmentList == null)
            throw new IllegalArgumentException("JEBL alignment list cannot be null !");
        if (alignmentList.size() != 1)
            throw new UnsupportedOperationException("multiple alignments are not supported ! " + alignmentList.size());

        Alignment jeblAlg = alignmentList.get(0);
        int nchar = jeblAlg.getSiteCount();
        List<Taxon> taxa = jeblAlg.getTaxa();
        int ntax = taxa.size();
        Map<String, Integer> idMap = new TreeMap<>();
        for (int i = 0; i < ntax; i++)
            idMap.put(taxa.get(i).getName(), i);

        SequenceType sequenceType = jeblAlg.getSequenceType();
        System.out.println("Create " + sequenceType + " alignment, ntax = " + ntax + ", nchar = " + nchar);

        final lphy.evolution.alignment.Alignment lphyAlg = new
                lphy.evolution.alignment.Alignment(ntax, nchar, idMap, sequenceType);

        // fill in sequences
        for (final Taxon taxon : taxa) {
            Sequence sequence = jeblAlg.getSequence(taxon);
            for (int i = 0; i < sequence.getLength(); i++) {
                State state = sequence.getState(i);
                int stateNum = state.getIndex();
                lphyAlg.setState(taxon.getName(), i, stateNum, true);
            }

        }

// TODO
//        CharSetAlignment(final Map<String, List<CharSetBlock>> charsetMap, String[] partNames,
//        final lphy.evolution.alignment.Alignment parentAlignment)

        return lphyAlg;
    }

    // **************************************************************
    // Extesion of NexusImporter
    // **************************************************************

    public List<jebl.evolution.alignments.Alignment> importAlignments() throws IOException, ImportException {
        boolean done = false;

        List<Taxon> taxonList = null;
        List<jebl.evolution.alignments.Alignment> alignments = new ArrayList<>();

        while (!done) {
            try {

                ExtNexusImporter.ExtNexusBlock block = importer.findNextBlockExt();

                if (block == ExtNexusImporter.ExtNexusBlock.TAXA) {
                    //TODO new datatype
                    taxonList = importer.parseTaxaBlock();

                } else if (block == ExtNexusImporter.ExtNexusBlock.CHARACTERS) {

                    if (taxonList == null) {
                        throw new NexusImporter.MissingBlockException("TAXA block is missing");
                    }

                    List<Sequence> sequences = importer.parseCharactersBlock(taxonList);
                    alignments.add(new BasicAlignment(sequences));

                } else if (block == ExtNexusImporter.ExtNexusBlock.DATA) {

                    // A data block doesn't need a taxon block before it
                    // but if one exists then it will use it.
                    List<Sequence> sequences = importer.parseDataBlock(taxonList);
                    alignments.add(new BasicAlignment(sequences));

                } else if (block == ExtNexusImporter.ExtNexusBlock.ASSUMPTIONS) {



                } else if (block == ExtNexusImporter.ExtNexusBlock.CALIBRATION) {
                    //TODO should be in another method
                    System.err.println("Warning: parsing CALIBRATION not implemented !");

                } else {
                    //TODO new block
                }

            } catch (EOFException ex) {
                done = true;
            }
        }

        if (alignments.size() == 0) {
            throw new NexusImporter.MissingBlockException("DATA or CHARACTERS block is missing");
        }

        return alignments;
//        return importer.importAlignments();
    }


    public static void main(final String[] args) {
        try {
            Path nexFile = Paths.get(args[0]); // primate-mtDNA.nex, Dengue4.nex
            final NexusParser parser = new NexusParser(nexFile);

//            List<Alignment> alignmentList = parser.importAlignments();
//                alignmentList.forEach(System.out::println);

            lphy.evolution.alignment.Alignment lphyAlg =
                    parser.getLPhyAlignment(new String[]{"noncoding", "coding"});
            System.out.println(lphyAlg.toJSON());

        } catch (Exception e) {
            e.printStackTrace();
        }
    } // main


} // class NexusParser