package lphy.base.evolution.tree;

import lphy.core.model.GenerativeDistribution;
import lphy.core.model.Value;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Centralized shared code for tree generative distributions conditioned on a root age and/or an
 * origin age. {@code rootAge} is the age of the most recent common ancestor of the sampled taxa;
 * {@code originAge} is the age of the start of the process (the stem above the root). At most one
 * of the two may be specified.
 * <p>
 * {@link TaxaConditionedTreeGenerator} extends this so that taxa-conditioned generators inherit the
 * same age-conditioning vocabulary; generators that condition only on age (e.g. a full tree with
 * extinct lineages) may extend this directly.
 */
public abstract class AgeConditionedTreeGenerator implements GenerativeDistribution<TimeTree> {

    public static final String rootAgeParamName = "rootAge";
    public static final String originAgeParamName = "originAge";

    /** the age of the most recent common ancestor of the sampled taxa; null if not conditioned on */
    protected Value<Number> rootAge;

    /** the age of the origin of the process; null if not conditioned on */
    protected Value<Number> originAge;

    /** Make sure to use this random generator in all child classes */
    protected RandomGenerator random;

    public AgeConditionedTreeGenerator(Value<Number> rootAge, Value<Number> originAge) {
        this.rootAge = rootAge;
        this.originAge = originAge;
        this.random = RandomUtils.getRandom();
    }

    /**
     * Validate the age-conditioning parameters: at most one of {@link #rootAge} and
     * {@link #originAge} may be specified.
     *
     * @param exactlyOneRequired if true, exactly one of rootAge and originAge must be specified.
     */
    protected void checkAgeParameters(boolean exactlyOneRequired) {
        if (rootAge != null && originAge != null)
            throw new IllegalArgumentException("Only one of " + rootAgeParamName + " and " +
                    originAgeParamName + " may be specified.");
        if (exactlyOneRequired && rootAge == null && originAge == null)
            throw new IllegalArgumentException("One of " + rootAgeParamName + " and " +
                    originAgeParamName + " must be specified.");
    }

    /** Add the non-null age parameters to the given params map. */
    protected void addAgeParams(Map<String, Value> map) {
        if (rootAge != null) map.put(rootAgeParamName, rootAge);
        if (originAge != null) map.put(originAgeParamName, originAge);
    }

    /**
     * Set an age parameter by name.
     *
     * @return true if {@code paramName} was an age parameter (and was set), false otherwise.
     */
    protected boolean setAgeParam(String paramName, Value value) {
        if (rootAgeParamName.equals(paramName)) {
            rootAge = value;
            return true;
        }
        if (originAgeParamName.equals(paramName)) {
            originAge = value;
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        addAgeParams(map);
        return map;
    }

    public String toString() {
        return getName();
    }
}
