package lphy.core.simulator;

import lphy.core.logger.FileLogger;
import lphy.core.logger.LoggerUtils;
import lphy.core.logger.RandomValueLogger;
import lphy.core.parser.LPhyMetaParser;
import lphy.core.parser.REPL;
import lphy.core.spi.LoaderManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SimulateUtils {

    public static Sampler simulateLPhyScript(File lphyFile, int numReplicates,
                                             List<? extends RandomValueLogger> loggers) throws IOException {

        //TODO filter loggers ?

        //*** Parse LPhy file ***//
        LPhyMetaParser parser = new REPL();
        parser.source(lphyFile);

        // Sampler requires GraphicalLPhyParser
        Sampler sampler = new Sampler(parser);
        // init and logging in Sampler
        sampler.sample(numReplicates, loggers);

        return sampler;
    }

    public static Sampler simulateLPhyScriptOutToFile(File lphyFile, int numReplicates, Long seed,
                                             File outDir) throws IOException {
        if (lphyFile == null || !lphyFile.exists())
            throw new IOException("Cannot find LPhy script file ! " +
                    (lphyFile != null ? lphyFile.getAbsolutePath() : null));
        String fileName = lphyFile.getName();
        if (!fileName.endsWith(".lphy"))
            throw new IOException("Invalid LPhy file: the file name extension has to be '.lphy'");
        String fileStem = fileName.substring(0, fileName.indexOf(".lphy"));

        if (seed != null)
            RandomUtils.setSeed(seed);

        if (outDir == null)
            outDir = lphyFile.getParentFile();
        if (!outDir.exists())
            throw new IOException("The output directory does not exist : " + outDir);

        LoggerUtils.log.info("Simulate data from lphy script: " + lphyFile.getAbsolutePath() +
                (seed != null ? " using seed " + seed : "") +
                ".\nOutput files to " + outDir.getAbsolutePath());

        List<FileLogger> loggers = LoaderManager.getFileLoggers();
        // config output files
        for (FileLogger fileLogger : loggers) {
            fileLogger.init(outDir, fileStem);
        }

        return simulateLPhyScript(lphyFile, numReplicates, loggers);
    }


//    public static Sampler simulateLPhyScript(File lphyFile, int numReplicates,
//                                             List<? extends FileLogger> loggers, File outDir, String fileStem) throws IOException {
//
//        for (FileLogger fileLogger : loggers) {
//            fileLogger.init(outDir, fileStem);
//        }
//
//        return simulateLPhyScript(lphyFile, numReplicates, loggers);
//    }

}
