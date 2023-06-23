package lphy.base.simulator;

import lphy.core.logger.LoggerUtils;
import lphy.core.simulator.Sampler;
import picocli.CommandLine;
import picocli.CommandLine.PicocliException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static lphy.core.simulator.SimulateUtils.simulateLPhyScriptOutToFile;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

/**
 * Command line program for running a script to create simulated data.
 * @author Walter Xie
 */
@CommandLine.Command(name = "slphy", footer = "Copyright(c) 2023",
        description = "The command line program for running a LPhy script to create simulated data.",
        version = { "SLPhy " + SLPhy.VERSION,
                "Local JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                "OS: ${os.name} ${os.version} ${os.arch}"})
public class SLPhy implements Callable<Integer> {

    public static final String VERSION = "0.0.1";

    @CommandLine.Parameters(paramLabel = "LPhy_scripts",
            description = "The file path of the LPhy model specification (e.g., /My/Path/my-model.lphy), " +
                    "where the file name must have the extension '.lphy'.")
    Path infile;

    @CommandLine.Option(names = {"-V", "--version"}, versionHelp = true, description = "display version info")
    boolean versionInfoRequested;
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Option(names = {"-r", "--replicates"}, defaultValue = "1", showDefaultValue = ALWAYS,
            description = "the number of simulations to run given one LPhy script, " +
            "usually to create data for well-calibrated study.") int reps = 1;
    @CommandLine.Option(names = {"-seed", "--seed"}, description = "the seed.") Long seed;

//    @CommandLine.Option(names = {"-lf", "--logFile"}, defaultValue = "true", showDefaultValue = ALWAYS,
//            description = "Whether to log random values to file")
//    boolean writeVarsToFile;
//    @CommandLine.Option(names = {"-tf", "--treeFiles"},  defaultValue = "true", showDefaultValue = ALWAYS,
//            description = "Whether to log simulated trees to file")
//    boolean writeTreesToFile;
//    @CommandLine.Option(names = {"-af", "--alignmentFiles"}, defaultValue = "true", showDefaultValue = ALWAYS,
//            description = "Whether to log simulated alignments to file")
//    boolean writeAlignmentsToFile;

    public SLPhy() {
    }

    @Override
    public Integer call() throws PicocliException {

        long start = System.currentTimeMillis();

        File outDir = null; // so it will be assigned to input file dir as default

        try {
            Sampler sampler = simulateLPhyScriptOutToFile(infile.toFile(), reps, seed, outDir);
        } catch (IOException e) {
            throw new PicocliException(e.getMessage(), e);
        }

        long end = System.currentTimeMillis();
        LoggerUtils.log.info("Write all files to " + outDir);
        LoggerUtils.log.info("Sampled " + infile + " " + reps + (reps>1?" times":" time") +
                ", taking " + (end - start) + " ms.");
        return 0;
    }

    public static void main(String[] args) {

        // must set -Dpicocli.disable.closures=true using picocli:4.7.0
        // otherwise java.lang.NoClassDefFoundError: groovy.lang.Closure
        int exitCode = new CommandLine(new SLPhy()).execute(args);

        if (exitCode != 0)
            LoggerUtils.log.severe("SLPhy does not exit normally !");
        System.exit(exitCode);

    }

}
