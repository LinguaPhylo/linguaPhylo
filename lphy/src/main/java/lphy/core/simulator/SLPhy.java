package lphy.core.simulator;

import lphy.core.io.FileConfig;
import lphy.core.io.OutputSystem;
import lphy.core.logger.LoggerUtils;
import picocli.CommandLine;
import picocli.CommandLine.PicocliException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

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

    public static final String VERSION = "0.1.0";

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
            "usually to create data for well-calibrated study.") int numReps = 1;
    @CommandLine.Option(names = {"-seed", "--seed"}, description = "the seed.") Long seed;

    @CommandLine.Option(names = {"-D", "--data"}, split = ";",
            description = "Replace the constant value in the lphy script, multiple constants must be quoted " +
                    "and split by ';', but no ';' at the last: e.g. -D \"n=12;L=100\" or -D n=20")
    String[] lphyConst = null;

//    enum SPI { loggers } //TODO  functions, gendists
//    // arity = "0" not working
//    @CommandLine.Option(names = {"-ls", "--list"},
//            description = "List the services (e.g., ${COMPLETION-CANDIDATES}) that have been loaded by SPI")
//    SPI list;

    public SLPhy() {
    }

    NamedRandomValueSimulator simulator;

    @Override
    public Integer call() throws PicocliException {
        // must use absolute path, otherwise parent could be null for relative path
        File outDir = infile.toAbsolutePath().getParent().toFile();
        try {
            // If outDir = null, use preference, otherwise assign to the input file directory by default.
            FileConfig.Utils.validate(infile.toFile(), outDir);

            simulator = new NamedRandomValueSimulator();
            // must provide File lphyFile, int numReplicates, Long seed
            simulator.simulate(infile.toFile(), numReps, lphyConst, seed);
            // TODO save Map<Integer, List<Value>> simResMap ?
        } catch (IOException e) {
            throw new PicocliException(e.getMessage(), e);
        }

        System.out.println("Write all files to " + (outDir !=null ? outDir : OutputSystem.getOutputDirectory()));
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
