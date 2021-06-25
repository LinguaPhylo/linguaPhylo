localBranchRates function
=========================
localBranchRates(TimeTree **tree**, Boolean[] **indicators**, Double[] **rates**)
---------------------------------------------------------------------------------

A function that returns branch rates for the given tree, indicator mask and raw rates. Each branch takes on the rate of its node index if the indicator is true, or inherits the rate of its parent branch otherwise.

### Parameters

- TimeTree **tree** - the tree.
- Boolean[] **indicators** - a boolean indicator for each node except the root. True if there is a new rate on the branch above this node, false if the rate is inherited from the parent node.
- Double[] **rates** - A rate for each node in the tree (except root). Only those with a corresponding indicator are used.

### Return type

- Double[]



