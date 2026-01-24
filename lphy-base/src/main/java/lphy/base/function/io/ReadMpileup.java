package lphy.base.function.io;

import lphy.base.evolution.Mpileup;
import lphy.base.evolution.PileupSite;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.Taxon;
import lphy.base.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.core.io.UserDir;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static lphy.base.evolution.datatype.Variant.getCanonicalState;

public class ReadMpileup extends DeterministicFunction<Mpileup> {
    public static final String taxaParamName = "names";

    public ReadMpileup(@ParameterInfo(name = ReaderConst.FILE, description = "the name of mpileup file including path, should have .mpileup or .pileup suffix") Value<String> mpileup,
                       @ParameterInfo(name = taxaParamName, description = "an array of objects representing taxa names") Value<Object[]> taxaNames) {
        if (mpileup == null) throw new IllegalArgumentException("The mpileup file name can't be null!");
        if (taxaNames == null) throw new IllegalArgumentException("The taxa names can't be null!");

        if (!(mpileup.value().endsWith(".mpileup") || mpileup.value().endsWith(".pileup"))) {
            throw new IllegalArgumentException("Invalid mpileup file: " + mpileup.value() +
                    "\nMust end with .mpileup or .pileup");
        }

        setParam(ReaderConst.FILE, mpileup);
        setParam(taxaParamName, taxaNames);
    }

    @GeneratorInfo(name = "readMpileup", description = "Read in a mpileup file and the taxa names that used to generate mpileup file.")
    @Override
    public Value<Mpileup> apply() {
        String mpileupFile = getMpileup().value();
        Value names = getTaxaNames();
        String[] taxaNames;
        //Taxa taxaNames = getTaxa().value();

        if (names.value().getClass().isArray()) {
            taxaNames = (String[]) names.value();

        } else throw new IllegalArgumentException(taxaParamName + " must be an array.");


        Mpileup mpileup = readMpileup(mpileupFile, taxaNames);

        return new Value<>(null, mpileup, this);
    }

    private Mpileup readMpileup(String mpileupFile, String[] taxaNames) {
        Mpileup mpileup;

        Path mpileupPath = Path.of(mpileupFile);

        try (BufferedReader mpileupReader = Files.newBufferedReader(mpileupPath, StandardCharsets.UTF_8)) {

            // Read all cell names into a list

            String line;

            //String[] chroms = new String[mpileupReader.];
            List<String> chroms = new ArrayList<>();
            List<Integer> positions = new ArrayList<>();
            List<Integer> refs = new ArrayList<>();
            List<Map<String, PileupSite.CellPileupData>> pileupData = new ArrayList<>();
            int index = 0;

            while ((line = mpileupReader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] parts = line.split("\t");
                if (parts.length < 3) {
                    LoggerUtils.log.warning("Skipping malformed line: " + line);
                    continue;
                }

                chroms.add(parts[0]);
                positions.add(Integer.parseInt(parts[1]));
                refs.add(getCanonicalState(parts[2]));

                // Each cell contributes 3 fields: readCount, reads, mapQ
                int expectedDataCols = 3 * taxaNames.length;
                if (parts.length < 3 + expectedDataCols) {
                    LoggerUtils.log.warning("Line has fewer columns than expected for all cells: " + line);
                    continue;
                }

                Map<String, PileupSite.CellPileupData> cellData = new LinkedHashMap<>();
                for (int i = 0; i < taxaNames.length; i++) {
                    int baseIndex = 3 + i * 3;
                    try {
                        int readCount = Integer.parseInt(parts[baseIndex]);
                        String reads = parts[baseIndex + 1];
                        String mapQ = parts[baseIndex + 2];
                        cellData.put(taxaNames[i],
                                new PileupSite.CellPileupData(readCount, reads, mapQ));
                    } catch (NumberFormatException e) {
                        LoggerUtils.log.warning("Bad readCount for cell " + taxaNames[i] + " at " + chroms.get(index) + ":" + positions.get(index));
                    }
                }
                pileupData.add(cellData);
                index++;
            }
            String[] chrom = chroms.toArray(new String[0]);
            int[] pos = positions.stream().mapToInt(Integer::intValue).toArray();
            int[] ref = refs.stream().mapToInt(Integer::intValue).toArray();
            mpileup = new Mpileup(chrom, pos, ref, pileupData);
            return mpileup;
        } catch (FileNotFoundException | NoSuchFileException e) {
            LoggerUtils.log.severe("File not found: " + e.getMessage() +
                    "\nCurrent working dir = " + UserDir.getUserDir());
        } catch (IOException e) {
            LoggerUtils.logStackTrace(e);
        }

        return null;
    }

    public Value<String> getMpileup() {
        return getParams().get(ReaderConst.FILE);
    }

    public Value<String> getTaxaNames() {
        return getParams().get(taxaParamName);
    }

}
