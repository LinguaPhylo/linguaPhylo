package lphy.evolution.sitemodel;

import lphy.graphicalModel.MethodInfo;
import lphy.graphicalModel.MultiDimensional;

public class SiteModel implements MultiDimensional {

    // instantaneous rate matrix
    Double[][] Q;

    // rate per site before accounting for proportion invariable.
    Double[] siteRates;

    // the proportion of invariable sites
    // this should be used to mask a binomial random fraction of the site rates to zero.
    Double proportionInvariable;

    int nchar;

    public SiteModel(Double[][] Q, Double[] siteRates, Double proportionInvariable) {
        this.Q = Q;
        this.siteRates = siteRates;
        this.proportionInvariable = proportionInvariable;
    }

    @MethodInfo(description = "the Q matrix for this site model")
    public Double[][] getQ() {
        return Q;
    }

    public boolean hasSiteRates() {
        return siteRates != null;
    }

    @MethodInfo(description = "the raw site rates for this site model")
    public Double[] siteRates() {
        return siteRates;
    }

    public Integer stateCount() {
        return Q.length;
    }

    @MethodInfo(description = "the proportion of invariable sites")
    public Double getProportionInvariable() {
        return proportionInvariable;
    }

    @Override
    public int getDimension() {
        return 1 + Q.length * Q.length + siteRates.length;
    }
}
