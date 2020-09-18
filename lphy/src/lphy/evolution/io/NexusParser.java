

package lphy.evolution.io;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import jebl.evolution.taxa.Taxon;
import lphy.evolution.alignment.CharSetAlignment;
import lphy.evolution.alignment.SimpleAlignment;
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
 *
 * @author Walter Xie
 */
public class NexusParser {
    @Deprecated
    protected final int READ_AHEAD_LIMIT = 50000;
    @Deprecated
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
            //@Deprecated Marks the present position in the stream.
            reader.mark(READ_AHEAD_LIMIT);
            this.importer = new ExtNexusImporter(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Be careful. Resets the stream to the most recent mark.
     *
     * @see BufferedReader#reset()
     */
    @Deprecated
    protected void resetReader() throws IOException {
        this.reader.reset();
        this.importer = new ExtNexusImporter(reader);
    }


    /**
     * Parse Nexus to LPHY {@link SimpleAlignment}
     *
     * @param ignoreCharset If true, ignore charset in Nexus,
     *                      only return single {@link SimpleAlignment}.
     *                      If false, return {@link CharSetAlignment} when Nexus has charsets.
     * @return LPHY {@link SimpleAlignment} or {@link CharSetAlignment}.
     */
    public lphy.evolution.alignment.AbstractAlignment getLPhyAlignment(boolean ignoreCharset) {

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

        final SimpleAlignment lphyAlg = new
                SimpleAlignment(idMap, nchar, sequenceType);

        // fill in sequences for single partition
        for (final Taxon taxon : taxa) {
            Sequence sequence = jeblAlg.getSequence(taxon);
            for (int i = 0; i < sequence.getLength(); i++) {
                State state = sequence.getState(i);
                int stateNum = state.getIndex();
                lphyAlg.setState(taxon.getName(), i, stateNum);
            }

        }

        // add ages
        final String ageMode = "forward";
        final Map<String, Double> ageMap = importer.getAgeMap(ageMode);
        if (ageMap != null) {
            lphyAlg.setAgeMap(ageMap);
        }

        final Map<String, List<CharSetBlock>> charsetMap = importer.getCharsetMap();

        if (!ignoreCharset && charsetMap.size() > 0) { // charset is optional
//            System.out.println( Arrays.toString(charsetMap.entrySet().toArray()) );
//            CharSetAlignment charSetAlignment = new CharSetAlignment(charsetMap, partNames, lphyAlg);
//            System.out.println(charSetAlignment);
            // this imports all charsets
            return new CharSetAlignment(charsetMap, lphyAlg);
        }
        return lphyAlg; // sing partition
    }

    public static void main(final String[] args) {
        try {
            Path nexFile = Paths.get(args[0]);
            String fileName = nexFile.getFileName().toString();
            System.out.println("Loading " + fileName);
            final NexusParser parser = new NexusParser(nexFile);

//            List<Alignment> alignmentList = parser.importAlignments();
//                alignmentList.forEach(System.out::println);

            if (fileName.equals("Dengue4.nex")) {
                SimpleAlignment lphyAlg =
                        (SimpleAlignment) parser.getLPhyAlignment(true);

                System.out.println(lphyAlg.toJSON());

            } else if (fileName.equals("primate.nex")) {
                lphy.evolution.alignment.CharSetAlignment lphyAlg =
                        (CharSetAlignment) parser.getLPhyAlignment(false);
                System.out.println(lphyAlg.toJSON());
//            lphy.evolution.alignment.Alignment[] twoAlg = lphyAlg.getPartAlignments(new String[]{"noncoding", "coding"});
                System.out.println(lphyAlg.toJSON(new String[]{"noncoding", "coding"}));

            }




        } catch (Exception e) {
            e.printStackTrace();
        }
    } // main


} // class NexusParser