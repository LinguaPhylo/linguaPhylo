hky function
============
hky([Number](../types/Number.md) **kappa**, [Double[]](../types/Double[].md) **freq**, [Number](../types/Number.md) **meanRate**)
---------------------------------------------------------------------------------------------------------------------------------

The HKY instantaneous rate matrix. Takes a kappa and base frequencies (and optionally a total rate) and produces an HKY85 rate matrix.

### Parameters

- [Number](../types/Number.md) **kappa** - the kappa of the HKY process.
- [Double[]](../types/Double[].md) **freq** - the base frequencies.
- [Number](../types/Number.md) **meanRate** - the total rate of substitution per unit time. Default 1.0.

### Return type

[Double[][]](../types/Double[][].md)

### Reference

Hasegawa, M., Kishino, H. & Yano, T. (1985) Dating of the human-ape splitting by a molecular clock of mitochondrial DNA. J Mol Evol 22, 160–174[https://doi.org/10.1007/BF02101694](https://doi.org/10.1007/BF02101694)

