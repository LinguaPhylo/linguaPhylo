package lphy.core.spi;

import java.util.List;

public interface LPhyLoader {

    void loadExtension(String extClsName);

    void registerExtensions(String extClsName);

    List<? extends Extension> getExtensions();
}
