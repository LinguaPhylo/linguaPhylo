hky function
============
hky(Number **kappa**, Double[] **freq**, Number **meanRate**)
-------------------------------------------------------------

The HKY instantaneous rate matrix. Takes a kappa and base frequencies (and optionally a total rate) and produces an HKY85 rate matrix.

### Parameters

- Number **kappa** - the kappa of the HKY process.
- Double[] **freq** - the base frequencies.
- Number **meanRate** - the total rate of substitution per unit time. Default 1.0.

### Return type

- Double[][]

### Reference

Hasegawa, M., Kishino, H. & Yano, T. (1985) Dating of the human-ape splitting by a molecular clock of mitochondrial DNA. J Mol Evol 22, 160–174[https://doi.org/10.1007/BF02101694](https://doi.org/10.1007/BF02101694)

