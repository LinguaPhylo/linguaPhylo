Alignment
---------

An alignment of discrete character states, where states are integers.

### Methods

- **ages**
  - gets the ages of these taxa as an array of doubles.
- **canonicalStateCount**
  - the number of canonical states excluding ambiguous states in the alignment.
- **canonicalStates**
  - the canonical states excluding ambiguous states.
- **dataType**
  - get the data type of this alignment.
- **length**
  - gets the number of taxa.
- **nchar**
  - The number of characters/sites.
- **nodeCount**
  - the total number of nodes (left + internal) in a binary tree with these taxa.
- **stateCount**
  - the number of possible states including ambiguous states in the alignment.
- **states**
  - the possible states including ambiguous states.
- **taxa**
  - the taxa of the alignment.
- **taxaNames**
  - The names of the taxa.
### Examples

- simpleSerialCoalescentNex.lphy
- twoPartitionCoalescentNex.lphy

