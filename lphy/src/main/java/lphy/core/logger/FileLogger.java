package lphy.core.logger;

import java.io.File;

public interface FileLogger extends RandomValueLogger {

    /**
     * Config the log file(s)
     * @param outputDir  the directory path where the log file is logged
     * @param fileStem   the file stem for log files
     */
    void init(File outputDir, String fileStem);

    /**
     * @param fileStem    file stem, e.g., the file stem of input file
     * @param postfix     indexing the output files, e.g., _1, _2, ...
     * @param extension   the log file extension, e.g., .trees, .log
     * @return         the full name of a log file
     */
    default String createFileName(String fileStem, String postfix, String extension) {
        return fileStem + postfix + extension;
    }

    /**
     * @param fileName  the full name of a log file
     * @return          {@link File} concatenate the outputDir in {@link #init(File, String)} and fileName
     */
    File getFile(String fileName);

}
