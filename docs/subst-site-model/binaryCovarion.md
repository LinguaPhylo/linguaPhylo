binaryCovarion function
=======================
binaryCovarion([Number](../types/Number.md) **alpha**, [Number](../types/Number.md) **s**, [Number[]](../types/Number[].md) **vfreq**, [Number[]](../types/Number[].md) **hfreq**, [Number](../types/Number.md) **meanRate**)
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

The rate matrix of the Covarion model for Binary data. It is equivalent to the BEAST mode.

### Parameters

- [Number](../types/Number.md) **alpha** - the rate of evolution in slow mode.
- [Number](../types/Number.md) **s** - the rate of flipping between slow and fast modes
- [Number[]](../types/Number[].md) **vfreq** - the frequencies of the visible states
- [Number[]](../types/Number[].md) **hfreq** - the frequencies of the hidden rates
- [Number](../types/Number.md) **meanRate** - (optional) the mean rate of the process. default = 1.0

### Return type

[Double[][]](../types/Double[][].md)


### Examples

- cpacific.lphy



