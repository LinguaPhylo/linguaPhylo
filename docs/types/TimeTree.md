TimeTree
--------

A phylogenetic tree scaled by time, including the contemporary taxa as a special case.

### Methods

- **branchCount**
  - the total number of branches in the tree (returns nodeCount() - 1)
- **directAncestorCount**
  - the total number of nodes in the tree that are direct ancestors (i.e. have a single parent and a single child, or have one child that is a zero-branch-length leaf).
- **extantCount**
  - the total number of extant leaves in the tree (leaf nodes with age 0.0).
- **getBranchRates**
  - get the array of all branch rates in the tree.
- **getLabeledNode**
  - get the node by given label.
- **getOldestInternalNode**
  - get the oldest internal node under the given maximum age.
- **getTaxaNames**
  - get all taxa names
- **hasOrigin**
  - returns true if this tree has an origin node (defined as a root node with a single child.
- **leafCount**
  - the total number of leaf nodes in the tree (leaf nodes with any age, but excluding zero-branch-length leaf nodes, which are logically direct ancestors).
- **nodeCount**
  - the total number of nodes in the tree (both leaf nodes and internal nodes).
- **rootAge**
  - the age of the root of the tree.
- **taxa**
  - the taxa of the tree.
- **treeLength**
  - the total length of the tree
### Examples

- jcCoalescent.lphy
- simpleSerialCoalescentNex.lphy

