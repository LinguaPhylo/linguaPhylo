# Weights Comparison

Compare how the weights affect the simulation results 
with or without the `WeightedDirichlet` prior.  
So we can understand whether adding Dirichlet density will be the key 
to avoid the data saturation.

## Simulations

Use [RSV2sim3.lphy](https://github.com/LinguaPhylo/linguaPhylo/blob/master/tutorials/RSV2sim3.lphy)
to simulate 110 XMLs for `WeightedDirichlet(conc=[1.0,1.0], weights=[200,400])`
and `WeightedDirichlet(conc=[2.0,2.0], weights=[200,400])`.
Then remove the WeightedDirichlet prior from XML, 
and create another two sets of 110 XMLs.


## 2 * 2 comparisons





