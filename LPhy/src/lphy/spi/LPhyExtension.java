package lphy.spi;

import jebl.evolution.sequences.SequenceType;
import lphy.graphicalModel.Func;
import lphy.graphicalModel.GenerativeDistribution;

import java.util.List;
import java.util.Map;

/**
 * The service interface defined for SPI in META-INF/services.
 * Implement this interface to register classes of LPhy or its extensions,
 * which should include {@link GenerativeDistribution}, {@link Func},
 * and {@link SequenceType}.
 *
 * @author Walter Xie
 */
public interface LPhyExtension {

    List<Class<? extends GenerativeDistribution>> getDistributions();

    List<Class<? extends Func>> getFunctions();

    Map<String, ? extends SequenceType> getSequenceTypes();

}
