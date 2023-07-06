package lphy.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.prefs.Preferences;

public class OutputSystem {
    public static final String OUTPUT_FILE_NAME = "output.txt";
    public static PrintStream out;
    private static final Preferences preferences = Preferences.userNodeForPackage(OutputSystem.class);
    private static final String OUTPUT_DIRECTORY_KEY = "lphy_output_dir";
    // private static final String TO_CONSOLE_KEY = "print_to_console";
    static {
        try {
            setOut(OUTPUT_FILE_NAME);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setOutputDirectory(String directory) {
        preferences.put(OUTPUT_DIRECTORY_KEY, directory);
    }

    public static File getOutputDirectory() {
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
    public static void setOut(String outputFileName) throws FileNotFoundException {
        File outputDirectory = getOutputDirectory();
//        boolean useSystemOut = getUseSystemOut();

        if (outputFileName != null && outputDirectory.exists()) {
            // Create a file in the directory
            File outputFile = new File(outputDirectory, outputFileName);
// FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            out = new PrintStream(outputFile);
//            setUseSystemOut(false);
        } else {
            out = System.out; // Return default System.out if output directory is not set
//            setUseSystemOut(true);
        }
    }

}

