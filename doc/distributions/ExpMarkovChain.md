ExpMarkovChain distribution
===========================
ExpMarkovChain(Object **initialMean**, Object **firstValue**, Object **n**)
---------------------------------------------------------------------------

A chain of random variables. X[0] ~ Exp(mean=initialMean) or X[0] ~ LogNormal(meanlog, sdlog); X[i+1] ~ Exp(mean=X[i])

### Parameters

- Object **initialMean** - This is the mean of the exponential from which the first value of the chain is drawn.
- Object **firstValue** - This is the value of the 1st element of the chain (X[0]).
- Object **n** - the dimension of the return. Use either X[0] ~ Exp(mean=initialMean); or X[0] ~ LogNormal(meanlog, sdlog); Then X[i+1] ~ Exp(mean=X[i])

### Return type

- Object



