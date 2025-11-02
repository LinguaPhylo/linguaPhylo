package lphy.base.function.io;

import lphy.base.evolution.Mpileup;
import lphy.base.evolution.PileupSite;
import lphy.core.io.UserDir;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static lphy.base.evolution.datatype.Variant.getCanonicalState;

public class ReadMpileup extends DeterministicFunction<List<Mpileup>> {
    public final String cellNameFileName = "cellNames";
    Value<String> cellNames;

    public ReadMpileup(@ParameterInfo(name = ReaderConst.FILE, description = "the name of mpileup file including path, should have .mpileup or .pileup suffix") Value<String> mpileup,
                       @ParameterInfo(name = cellNameFileName, description = "the name of cell name txt file including path, with .txt suffix. Containing the cell names of mpileup samples, one name per line.") Value<String> cellNames) {
        if (mpileup == null) throw new IllegalArgumentException("The mpileup file name can't be null!");
        if (cellNames == null) throw new IllegalArgumentException("The cell name file name can't be null!");

        if (!(mpileup.value().endsWith(".mpileup") || mpileup.value().endsWith(".pileup"))) {
            throw new IllegalArgumentException("Invalid mpileup file: " + mpileup.value() +
                    "\nMust end with .mpileup or .pileup");
        }
        if (!(cellNames.value().endsWith(".txt"))) {
            throw new IllegalArgumentException("Invalid cell name file: " + cellNames.value() +
                    "\nMust end with .txt");
        }

        setParam(ReaderConst.FILE, mpileup);
        setParam(cellNameFileName, cellNames);
    }

    @GeneratorInfo(name = "readMpileup", description = "Read in a mpileup file and the cell names order file that used to generate mpileup file.")
    @Override
    public Value<List<Mpileup>> apply() {
        String mpileupFile = getMpileup().value();
        String cellNameFile = getCellNames().value();

        List<Mpileup> mpileups = readMpileup(mpileupFile, cellNameFile);

        return new Value<>(null, mpileups, this);
    }

    private List<Mpileup> readMpileup(String mpileupFile, String cellNameFile) {
        List<Mpileup> mpileups = new ArrayList<>();

        Path mpileupPath = Path.of(mpileupFile);
        Path cellNamePath = Path.of(cellNameFile);

        try (BufferedReader mpileupReader = Files.newBufferedReader(mpileupPath, StandardCharsets.UTF_8);
             BufferedReader cellNameReader = Files.newBufferedReader(cellNamePath, StandardCharsets.UTF_8)) {

            // Read all cell names into a list
            List<String> cellNames = cellNameReader.lines()
                    .filter(line -> !line.isBlank())
                    .toList();

            String line;

            while ((line = mpileupReader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] parts = line.split("\t");
                if (parts.length < 3) {
                    LoggerUtils.log.warning("Skipping malformed line: " + line);
                    continue;
                }

                String chrom = parts[0];
                int pos = Integer.parseInt(parts[1]);
                String ref = parts[2];

                // Each cell contributes 3 fields: readCount, reads, mapQ
                int expectedDataCols = 3 * cellNames.size();
                if (parts.length < 3 + expectedDataCols) {
                    LoggerUtils.log.warning("Line has fewer columns than expected for all cells: " + line);
                    continue;
                }

                Map<String, PileupSite.CellPileupData> cellData = new LinkedHashMap<>();
                for (int i = 0; i < cellNames.size(); i++) {
                    int baseIndex = 3 + i * 3;
                    try {
                        int readCount = Integer.parseInt(parts[baseIndex]);
                        String reads = parts[baseIndex + 1];
                        String mapQ = parts[baseIndex + 2];
                        cellData.put(cellNames.get(i),
                                new PileupSite.CellPileupData(readCount, reads, mapQ));
                    } catch (NumberFormatException e) {
                        LoggerUtils.log.warning("Bad readCount for cell " + cellNames.get(i) + " at " + chrom + ":" + pos);
                    }
                }
                Mpileup mpileup = new Mpileup(chrom, pos, getCanonicalState(ref), cellData);
                mpileups.add(mpileup);
            }
        } catch (FileNotFoundException | NoSuchFileException e) {
            LoggerUtils.log.severe("File not found: " + e.getMessage() +
                    "\nCurrent working dir = " + UserDir.getUserDir());
        } catch (IOException e) {
            LoggerUtils.logStackTrace(e);
        }

        return mpileups;
    }

    public Value<String> getMpileup() {
        return getParams().get(ReaderConst.FILE);
    }

    public Value<String> getCellNames() {
        return getParams().get(cellNameFileName);
    }

}
