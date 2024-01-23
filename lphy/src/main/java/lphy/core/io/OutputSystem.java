package lphy.core.io;

import lphy.core.logger.LoggerUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

public class OutputSystem {
    public static final String OUTPUT_FILE_NAME = "output.txt";
    public static PrintStream out;
    private static final Preferences preferences = Preferences.userNodeForPackage(OutputSystem.class);
    private static final String OUTPUT_DIRECTORY_KEY = "lphy_output_dir";
    // private static final String TO_CONSOLE_KEY = "print_to_console";
//    static {
//        setOut(OUTPUT_FILE_NAME); // TODO why? It seems unnecessary.
//    }

    public static void setOutputDirectory(String directory) {
        preferences.put(OUTPUT_DIRECTORY_KEY, directory);
    }

    public static File getOrCreateOutputDirectory() {
        // default user.dir
        String outputDirectory = preferences.get(OUTPUT_DIRECTORY_KEY,
                UserDir.getUserDir().toAbsolutePath().toString());
        File directory = new File(outputDirectory);
        if (!directory.exists())
            // Create the output directory if it doesn't exist
            directory.mkdirs();

        return directory;
    }

//    public static void setUseSystemOut(boolean useSystemOut) {
//        preferences.putBoolean(TO_CONSOLE_KEY, useSystemOut);
//    }
//
//    public static boolean getUseSystemOut() {
//        return preferences.getBoolean(TO_CONSOLE_KEY, false);
//    }

    /**
     * @param filePrefix a base name of input lphy file
     * @param filePostfix indexing the output files, e.g., _1, _2, ...
     * @param fileExtension the log file extension, e.g., .trees, .log
     * @return an output file name by concatenating prefix, postfix, extension.
     */
    public static String getOutputFileName(String filePrefix, String filePostfix, String fileExtension) {
        return filePrefix + filePostfix + fileExtension;
    }

    /**
     * If outputFileName is not null, then set {@link OutputSystem#out} to PrintStream of
     * the output directory and output file name, otherwise to {@link System#out}.
     */
    public static void setOut(String outputFileName) {
//        boolean useSystemOut = getUseSystemOut();
        // if outputFileName == null to System.out
        if (outputFileName != null) {
            File outputFile = getOutputFile(outputFileName, true);
            // FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            try {
                out = new PrintStream(outputFile);
            } catch (FileNotFoundException e) {
                LoggerUtils.log.severe("Cannot denote the output file : " + outputFileName +
                        "\ninto the resolved path : " + outputFile.getAbsolutePath());
                throw new RuntimeException(e);
            }
            // setUseSystemOut(false);
        } else {
            out = System.out; // Return default System.out if output directory is not set
            // setUseSystemOut(true);
        }

    }

    // consider outputFileName could be the absolute path, or relative, or only file name.
    // also check if the preferred output dir
    public static File getOutputFile(String outputFileName, boolean usePreferredOutDir) {
        File outDir = getOrCreateOutputDirectory();
        Path child = Paths.get(outputFileName);

        if (child.isAbsolute()) {
            // ignore outDir if outputFileName is absolute path
            return child.toFile();

        } else if (usePreferredOutDir && outDir.isDirectory()) {
            // Check if outDir is directory
            // construct the file by concatenating outDir with outputFileName
            return new File(outDir, outputFileName);
        }

        // default to simply construct the file from outputFileName
        return new File(outputFileName);
    }

}

