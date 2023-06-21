package lphy.core.logger;

import java.io.File;

public interface FileLogger extends RandomValueLogger {

    void setDir(File dir);

    void setFileStem(String fileStem);

    default String createFileName(String fileStem, String postfix, String extension) {
        return fileStem + postfix + extension;
    }

    File getFile(String fileName);

}
