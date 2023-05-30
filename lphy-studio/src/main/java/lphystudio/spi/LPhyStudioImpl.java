package lphystudio.spi;

import jebl.evolution.sequences.SequenceType;
import lphy.core.graphicalmodel.components.Func;
import lphy.core.graphicalmodel.components.GenerativeDistribution;
import lphy.core.spi.LPhyExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Empty class to show studio ext in the Extension Manager.
 * @author Walter Xie
 */
public class LPhyStudioImpl implements LPhyExtension {

    /**
     * Required by ServiceLoader.
     */
    public LPhyStudioImpl() {
    }

    @Override
    public List<Class<? extends GenerativeDistribution>> getDistributions() {
        return new ArrayList<>();
    }

    @Override
    public List<Class<? extends Func>> getFunctions() {
        return new ArrayList<>();
    }

    @Override
    public Map<String, ? extends SequenceType> getSequenceTypes() {
        return new ConcurrentHashMap<>();
    }
}
