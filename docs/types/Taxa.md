Taxa
----

An interface that taxa-dimensioned objects can implement, such as Alignment and TimeTree.
It reserves three types of metadata internally: taxa names, ages of taxa, and species.

### Methods

- **ages**
  - gets the ages of these taxa as an array of doubles.
- **length**
  - gets the number of taxa.
- **nodeCount**
  - the total number of nodes (left + internal) in a binary tree with these taxa.
- **species**
  - gets the species of these taxa as an array of strings.
- **taxaNames**
  - The names of the taxa.
### Examples

- jcCoalescent.lphy
- twoPartitionCoalescent.lphy

