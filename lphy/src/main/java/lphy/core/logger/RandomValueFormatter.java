package lphy.core.logger;

import lphy.core.model.Value;

import java.util.List;

/**
 * The logger formatter interface has no side effect.
 */
@Deprecated
public interface RandomValueFormatter {

    void setSelectedItems(List<Value<?>> randomValues);

    List<?> getSelectedItems();

    /**
     * Called once for all replicates, e.g., build the header.
     */
    String getHeaderFromValues();

    /**
     * Build the string of contents should be logged per replicate.
     *
     * @param rowIndex the index of a row, e.g., a replicate of simulations.
     */
    String getRowFromValues(int rowIndex);

    /**
     * Called once, all replicates have been logged.
     */
    String getFooterFromValues();

    String getFormatterDescription();

    default String getModuleName() {
        Module module = getClass().getModule();
        return module.getName();
    }

    default String getFormatterName() {
        return getModuleName() + "." + getClass().getSimpleName();
    }

}