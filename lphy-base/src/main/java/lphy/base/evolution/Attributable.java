package lphy.base.evolution;

import java.util.Map;

/**
 * TODO
 */
public interface Attributable {


    void setAttr(String name, Object value);


    Object getAttr(String name);


    void removeAttr(String name);


    Map<String, Object> getAttrMap();


    public static class Utils {


    }

}
