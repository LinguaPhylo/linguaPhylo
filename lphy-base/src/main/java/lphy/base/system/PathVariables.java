package lphy.base.system;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Replace path var to path
 * @author Walter Xie
 */
public class PathVariables {

    public static final String HOME = "$HOME$";

    public static Path convertPathVar(String pathRaw) {
        if (pathRaw.startsWith(HOME)) {
            pathRaw = pathRaw.replace(HOME, "");
            return Paths.get(System.getProperty("user.home"), pathRaw);
        }
        return Paths.get(pathRaw);
    }

}
