package lphy.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class IOUtils {

    public static final String USER_DIR = "user.dir";

    /**
     * @see #getPath(Path)
     */
    public static Path getPath(String pathStr){
        Path path = Paths.get(pathStr);
        return getPath(path);
    }

    /**
     * @param path
     * @return  for the relative path, it will return a path
     *          concatenating user.dir before the relative path.
     *
     */
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
        if (pathStr != null) {
            System.setProperty(USER_DIR, pathStr);
            System.out.println("Set " + USER_DIR + " = " + pathStr);
        }
    }

    public static Path getUserDir() {
        String wd = System.getProperty(USER_DIR);
        if (wd != null)
            return Paths.get(wd);
        return Paths.get("");
    }


}
