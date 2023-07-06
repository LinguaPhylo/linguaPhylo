package lphy.core.io;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Methods to set and get "user.dir".
 * This is mainly used to locate a file given the relative path.
 */
public class UserDir {

    public static final String USER_DIR = "user.dir";

    public static final String ALIGNMENT_DIR = "alignment.dir";

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

    /**
     * set alignment output directory to given string
     * @param dir directory path
     */
    public static void setAlignmentDir(String dir) {
        if (dir != null) {
            System.setProperty(ALIGNMENT_DIR, dir);
            System.out.println("Set " + ALIGNMENT_DIR + " = " + dir);
        }
    }

    /**
     * @return alignment output path (defaults to working directory user.dir)
     */
    public static Path getAlignmentDir() {
        String dir = System.getProperty(ALIGNMENT_DIR);
        if (dir != null) {
            return Paths.get(dir);
        } else {
            return getUserDir();
        }
    }

}
