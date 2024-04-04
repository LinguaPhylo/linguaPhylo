package lphy.core.logger;

import java.util.List;

/**
 * Implement this to make the value to be loggable into a file.
 * This can be used lphy extension developers to create their own format to log.
 * It supposes to replace {@link ValueFormatter} in future.
 */
public interface TextFileFormatted {

    List<String> getTextForFile();

    String getFileType();

}
