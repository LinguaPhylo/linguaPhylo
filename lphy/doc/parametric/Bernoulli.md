Bernoulli distribution
======================
Bernoulli([Number](../types/Number.md) **p**)
---------------------------------------------

The coin toss distribution. With true (heads) having probability p.

### Parameters

- [Number](../types/Number.md) **p** - the probability of success.

### Return type

[Boolean](../types/Boolean.md)



Bernoulli([Double](../types/Double.md) **p**, [Integer](../types/Integer.md) **replicates**, [Integer](../types/Integer.md) **minSuccesses**)
---------------------------------------------------------------------------------------------------------------------------------------------

The Bernoulli process for n iid trials. The success (true) probability is p. Produces a boolean n-tuple.

### Parameters

- [Double](../types/Double.md) **p** - the probability of success.
- [Integer](../types/Integer.md) **replicates** - the number of bernoulli trials.
- [Integer](../types/Integer.md) **minSuccesses** - Optional condition: the minimum number of ones in the boolean array.

### Return type

[Boolean[]](../types/Boolean[].md)



