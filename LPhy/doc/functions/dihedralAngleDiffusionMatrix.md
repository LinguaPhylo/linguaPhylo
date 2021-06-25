dihedralAngleDiffusionMatrix function
=====================================
dihedralAngleDiffusionMatrix(Integer **length**, Double **phiVariance**, Double **psiVariance**, Double **covariance**)
-----------------------------------------------------------------------------------------------------------------------

This function constructs a variance covariance matrix for the neutral angular diffusion model.

### Parameters

- Integer **length** - the length of the peptide backbone to model the angular diffusion of.
- Double **phiVariance** - the variance of the phi angles.
- Double **psiVariance** - the variance of the psi angles.
- Double **covariance** - the covariance between phi and psi angles.

### Return type

- Double[][]



