package lphy.core.logger;

import java.util.List;

/**
 * Implement this to make the value to be loggable into a file.
 */
public interface TextFileFormatted {

    List<String> getTextForFile();

    String getFileType();

}
