BirthDeath distribution
=======================
BirthDeath(Number **lambda**, Number **mu**, Integer **n**, Object **taxa**, Number **rootAge**)
------------------------------------------------------------------------------------------------

A tree of only extant species, which is conceptually embedded<br>in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>Conditioned on root age and on number of taxa.

### Parameters

- Number **lambda** - per-lineage birth rate.
- Number **mu** - per-lineage death rate.
- Integer **n** - the number of taxa. optional.
- Object **taxa** - a string array of taxa id or a taxa object (e.g. dataframe, alignment or tree), optional.
- Number **rootAge** - the age of the root.

### Return type

- TimeTree

### Reference

Joseph Heled, Alexei J. Drummond, Calibrated Birth–Death Phylogenetic Time-Tree Priors for Bayesian Inference, Systematic Biology, Volume 64, Issue 3, May 2015.[https://doi.org/10.1093/sysbio/syu089](https://doi.org/10.1093/sysbio/syu089)

BirthDeath(Number **diversification**, Number **turnover**, Number **rootAge**)
-------------------------------------------------------------------------------

The Birth-death-sampling tree distribution over tip-labelled time trees.<br>Conditioned on root age.

### Parameters

- Number **diversification** - diversification rate.
- Number **turnover** - turnover.
- Number **rootAge** - the number of taxa.

### Return type

- TimeTree



