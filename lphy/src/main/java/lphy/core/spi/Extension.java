package lphy.core.spi;

public interface Extension {

    default String getModuleName() {
        Module module = getClass().getModule();
        return module.getName();
    }

    default String getExtensionName() {
        return getModuleName() + "." + getClass().getSimpleName();
    }

}
