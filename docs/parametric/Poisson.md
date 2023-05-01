Poisson distribution
====================
Poisson([Number](../types/Number.md) **lambda**, [Integer](../types/Integer.md) **offset**, [Integer](../types/Integer.md) **min**, [Integer](../types/Integer.md) **max**)
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------

The probability distribution of the number of events when the expected number of events is lambda, supported on the set { 0, 1, 2, 3, ... }.

### Parameters

- [Number](../types/Number.md) **lambda** - the expected number of events.
- [Integer](../types/Integer.md) **offset** - optional parameter to add a constant to the returned result. default is 0
- [Integer](../types/Integer.md) **min** - optional parameter to specify a condition that the number of events must be greater than or equal to this mininum
- [Integer](../types/Integer.md) **max** - optional parameter to specify a condition that the number of events must be less than or equal to this maximum

### Return type

[Integer](../types/Integer.md)


### Examples

- expression4.lphy
- simpleRandomLocalClock2.lphy



