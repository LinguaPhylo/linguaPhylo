package lphy.core.io;

import lphy.core.logger.LoggerUtils;
import lphy.core.model.Symbols;
import lphy.core.simulator.RandomUtils;

import java.io.File;
import java.io.IOException;

public class FileConfig {

    public final File lphyInputFile; // the input lphy script file, which can be null

    // filePrefix is lphy script file base name as default,
    // used for output file prefix
    private final String filePrefix;
    public final int numReplicates;

    public final Long seed; // if null, then random

    public FileConfig(int numReplicates, File lphyInputFile, Long seed) throws IOException {
        this.lphyInputFile = lphyInputFile;
        this.numReplicates = numReplicates;
        this.seed = seed;
        this.filePrefix = getLPhyFilePrefix(lphyInputFile);
    }

    public FileConfig(int numReplicates, File lphyInputFile) throws IOException {
        this(numReplicates, lphyInputFile, null);
    }


//    public FileConfig(int numReplicates, String filePrefix, Long seed) {
//        this.filePrefix = filePrefix;
//        this.numReplicates = numReplicates;
//        this.seed = seed;
//        this.lphyFile = null;
//    }

    public static String getOutFileName(String valueId, int index, int numReplicates,
                                 String filePrefix, String fileExtension) {
        String postfix = (numReplicates > 1 ? "_r" + index : "") + "_" + valueId;
        return OutputSystem.getOutputFileName(filePrefix, postfix, fileExtension);
    }

    public static String getOutFileName(String valueId, String filePrefix, String fileExtension) {
        // convert greek symbols to English
        String postfix = "_" + Symbols.getCanonical(valueId);
        return OutputSystem.getOutputFileName(filePrefix, postfix, fileExtension);
    }

    public static String getOutFileName(String filePrefix, String fileExtension) {
        return OutputSystem.getOutputFileName(filePrefix, "", fileExtension);
    }

    public String getFilePrefix() {
        return filePrefix;
    }

    public int getNumReplicates() {
        return numReplicates;
    }

    //    public String getOutFileName(String valueId, int index, String fileExtension) {
//        return getOutFileName(valueId, index, numReplicates, filePrefix, fileExtension);
//    }
//
//    public String getOutFileName(String valueId, String fileExtension) {
//        // convert greek symbols to English
//        String postfix = "_" + Symbols.getCanonical(valueId);
//        return OutputSystem.getOutputFileName(filePrefix, postfix, fileExtension);
//    }
//
//    public String getOutFileName(String fileExtension) {
//        return OutputSystem.getOutputFileName(filePrefix, "", fileExtension);
//    }

    private static final String LPHY_EXTETION = ".lphy";

    private static String getLPhyFilePrefix(File lphyFile) throws IOException {
        if (lphyFile == null || !lphyFile.exists())
            throw new IOException("Cannot find LPhy script file ! " +
                    (lphyFile != null ? lphyFile.getAbsolutePath() : null));

        String fileName = lphyFile.getName();

        if (!fileName.endsWith(LPHY_EXTETION))
            throw new IOException("Invalid LPhy file name: the extension has to be '" + LPHY_EXTETION + "' !");
        return fileName.substring(0, fileName.indexOf(LPHY_EXTETION));
    }

    public static class Utils {

        public static FileConfig createSimulationFileConfig(File lphyFile, File outDir, int numReplicates,
                                                             Long seed ) throws IOException {
            // if user.dir is not the parent folder of lphyFile, then set to it
            if (! UserDir.getUserDir().toAbsolutePath().equals(lphyFile.getParentFile())) {
                UserDir.setUserDir(lphyFile.getParentFile().getAbsolutePath());
            }

            if (seed != null)
                RandomUtils.setSeed(seed);

            if (outDir != null)
                OutputSystem.setOutputDirectory(outDir.getAbsolutePath());

            LoggerUtils.log.info("Simulate data from LPhy script: " + lphyFile.getAbsolutePath() +
                    (seed != null ? " using seed " + seed : "") +
                    ".\nOutput files to " + OutputSystem.getOutputDirectory().getAbsolutePath());

            return new FileConfig( numReplicates, lphyFile, seed );
        }

    }
}
