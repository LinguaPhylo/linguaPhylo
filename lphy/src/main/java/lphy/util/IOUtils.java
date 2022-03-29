package lphy.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class IOUtils {

    public static final String USER_DIR = "user.dir";

    /**
     * @see #getUserPath(Path)
     */
    public static Path getUserPath(String pathStr){
        Path path = Paths.get(pathStr);
        return getUserPath(path);
    }

    /**
     * @param path
     * @return  If the given path is a relative path,
     *          it will return a path concatenating user.dir
     *          before the relative path.
     *          Otherwise, it returns the given path.
     */
    public static Path getUserPath(Path path){
        if (path.isAbsolute())
            return path;
        else {
            String wd = System.getProperty(USER_DIR);
            return Paths.get(wd, path.toString());
//            return path.toAbsolutePath();
        }
    }

    /**
     * @param wdStr set working directory (user.dir) to the given string
     */
    public static void setUserDir(String wdStr) {
        if (wdStr != null) {
            System.setProperty(USER_DIR, wdStr);
            System.out.println("Set " + USER_DIR + " = " + wdStr);
        }
    }

    /**
     * @return  user.dir, or empty string if it is null.
     */
    public static Path getUserDir() {
        String wd = System.getProperty(USER_DIR);
        if (wd != null)
            return Paths.get(wd);
        return Paths.get("");
    }

}
