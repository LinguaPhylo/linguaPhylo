package lphy.evolution.sitemodel;

import lphy.evolution.NChar;

public class SiteModel {

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

    public Double[][] getQ() {
        return Q;
    }

    public boolean hasSiteRates() {
        return siteRates != null;
    }

    public Double[] siteRates() {
        return siteRates;
    }

    public Integer stateCount() {
        return Q.length;
    }

    public double getProportionInvariable() {
        return proportionInvariable;
    }
}
