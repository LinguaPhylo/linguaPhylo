MetaDataAlignment
=================
Methods
-------

- **ages**
  - gets the ages of these taxa as an array of doubles.
- **charset**
  - return a partition alignment. If the string doesn't match charset's syntax, then check if the string matches a defined name in the nexus file. Otherwise it is an error. The string is referred to one partition at a call, but can be multiple blocks, such as d.charset("2-457\3 660-896\3").
- **dataType**
  - get the data type of this alignment.
- **getTaxaNames**
  - The names of the taxa.
- **length**
  - gets the number of taxa.
- **nchar**
  - The number of characters/sites.
- **nodeCount**
  - the total number of nodes (left + internal) in a binary tree with these taxa.
- **stateCount**
  - the number of possible states in the alignment.
- **taxa**
  - the taxa of the alignment.
- **taxaNames**
  - The names of the taxa.