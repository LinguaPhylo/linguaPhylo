

package lphy.evolution.io;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import jebl.evolution.taxa.Taxon;
import lphy.evolution.alignment.CharSetAlignment;
import lphy.evolution.traits.CharSetBlock;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Apply {@link ExtNexusImporter} to parsing Nexus into LPhy objects.
 */
public class NexusParser {
    @Deprecated protected final int READ_AHEAD_LIMIT = 50000;
    @Deprecated BufferedReader reader;

    ExtNexusImporter importer;

    protected Path nexFile; // lock to 1 file now

    public NexusParser(Path nexFile) {
        this.nexFile = nexFile;

        try {
            if (!(nexFile.toString().endsWith("nex") || nexFile.toString().endsWith("nexus") ||
                    nexFile.toString().endsWith("nxs")))
                throw new IOException("Nexus file name's suffix is invalid ! " + nexFile);
            if (!nexFile.toFile().exists() || nexFile.toFile().isDirectory())
                throw new IOException("Cannot find Nexus file: " + nexFile);

            reader = Files.newBufferedReader(nexFile); // StandardCharsets.UTF_8
            //@Deprecated Marks the present position in the stream.
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
    @Deprecated
    protected void resetReader() throws IOException {
        this.reader.reset();
        this.importer = new ExtNexusImporter(reader);
    }


    public lphy.evolution.alignment.Alignment getLPhyAlignment(String[] partNames) {

        try {
            importer.importNexus();
//            resetReader();
//            importer.importCharsets();
        } catch (IOException | ImportException e) {
            e.printStackTrace();
        }
        final List<Alignment> alignmentList = importer.getAlignments();
        if (alignmentList.size() < 1)
            throw new IllegalArgumentException("Cannot find alignment !");
        if (alignmentList.size() > 1)
            throw new UnsupportedOperationException("Multiple alignments are not supported ! " + alignmentList.size());

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

        // fill in sequences for single partition
        for (final Taxon taxon : taxa) {
            Sequence sequence = jeblAlg.getSequence(taxon);
            for (int i = 0; i < sequence.getLength(); i++) {
                State state = sequence.getState(i);
                int stateNum = state.getIndex();
                lphyAlg.setState(taxon.getName(), i, stateNum, true);
            }

        }

        final Map<String, List<CharSetBlock>> charsetMap = importer.getCharsetMap();
        if (charsetMap.size() > 0) { // multi-partition
//            System.out.println( Arrays.toString(charsetMap.entrySet().toArray()) );
            CharSetAlignment charSetAlignment = new CharSetAlignment(charsetMap, partNames, lphyAlg);
            System.out.println(charSetAlignment);
            return charSetAlignment;
        }
        return lphyAlg; // sing partition
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