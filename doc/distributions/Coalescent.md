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

Kingman JFC. The Coalescent. Stochastic Processes and their Applications 13, 235-248 (1982)[https://doi.org/10.1016/0304-4149(82)90011-4](https://doi.org/10.1016/0304-4149(82)90011-4)

