Bernoulli distribution
======================
Bernoulli(Number **p**)
-----------------------

The coin toss distribution. With true (heads) having probability p.

### Parameters

- Number **p** - the probability of success.

### Return type

- Boolean



Bernoulli(Double **p**, Integer **replicates**, Integer **minSuccesses**)
-------------------------------------------------------------------------

The Bernoulli process for n iid trials. The success (true) probability is p. Produces a boolean n-tuple.

### Parameters

- Double **p** - the probability of success.
- Integer **replicates** - the number of bernoulli trials.
- Integer **minSuccesses** - Optional condition: the minimum number of ones in the boolean array.

### Return type

- Boolean[]



