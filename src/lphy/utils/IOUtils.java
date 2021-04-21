package lphy.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class IOUtils {

    public static final String USER_DIR = "user.dir";

    public static Path getPath(String pathStr){
        Path path = Paths.get(pathStr);
        return getPath(path);
    }

    public static Path getPath(Path path){
        if (path.isAbsolute())
            return path;
        else {
            String wd = System.getProperty(USER_DIR);
            return Paths.get(wd, path.toString());
//            return path.toAbsolutePath();
        }
    }


    public static void setUserDir(String pathStr) {
        System.setProperty(USER_DIR, pathStr);
        System.out.println("Set "+ USER_DIR +" = " + pathStr);
    }

}
