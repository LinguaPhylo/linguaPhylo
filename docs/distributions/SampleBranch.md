SampleBranch distribution
=========================
SampleBranch([TimeTree](../types/TimeTree.md) **tree**, [Number](../types/Number.md) **age**)
---------------------------------------------------------------------------------------------

Randomly sample a branch among the branches at a given age in the given tree, represented by the node attached to this branch. The function is deterministic when there is only one branch at the given age. The branch is represented by the node under it.

### Parameters

- [TimeTree](../types/TimeTree.md) **tree** - the full tree to sample branch from.
- [Number](../types/Number.md) **age** - the age that branch would sample at.

### Return type

[TimeTreeNode](../types/TimeTreeNode.md)


### Examples

- substituteClade.lphy



