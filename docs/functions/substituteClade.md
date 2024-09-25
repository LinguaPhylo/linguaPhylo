substituteClade function
========================
substituteClade([TimeTree](../types/TimeTree.md) **baseTree**, [TimeTree](../types/TimeTree.md) **cladeTree**, [TimeTreeNode](../types/TimeTreeNode.md) **node**, [Double](../types/Double.md) **time**, [String](../types/String.md) **nodeLabel**)
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Substitute a clade in a tree with a given node and time, as well as the label of the clade root node. The original child clade would be replaced by the give tree.

### Parameters

- [TimeTree](../types/TimeTree.md) **baseTree** - the tree that we are going to add another tree onto.
- [TimeTree](../types/TimeTree.md) **cladeTree** - the tree that we are going to add it on the base tree
- [TimeTreeNode](../types/TimeTreeNode.md) **node** - the node with the branch that the branch tree would be add on to.
- [Double](../types/Double.md) **time** - (optional) the mutation happen time that the branch tree would be add onto the base tree
- [String](../types/String.md) **nodeLabel** - the name of added branch node.

### Return type

[TimeTree](../types/TimeTree.md)


### Examples

- substituteClade.lphy



