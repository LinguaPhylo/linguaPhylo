Coalescent distribution
=======================
Coalescent(Number **theta**, Integer **n**, Taxa **taxa**, Double[] **ages**)
-----------------------------------------------------------------------------

The Kingman coalescent with serially sampled data. (Rodrigo and Felsenstein, 1999)

### Parameters

- Number **theta** - effective population size, possibly scaled to mutations or calendar units.
- Integer **n** - number of taxa.
- Taxa **taxa** - Taxa object, (e.g. Taxa or TimeTree or Object[])
- Double[] **ages** - an array of leaf node ages.

### Return type

- TimeTree

### Reference

Rodrigo AG, Felsenstein J. (1999). Coalescent Approaches to HIV Population Genetics, The Evolution of HIV, Chapter 8, edited by Crandall K., Johns Hopkins Univ. Press, Baltimore.

