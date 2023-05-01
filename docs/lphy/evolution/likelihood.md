# The phylogenetic continuous-time Markov process

This LPHY package describes the generative distribution responsible for generating sequence data by an
evolutionary process along a phylogenetic time tree. In its simplest form the PhyloCTMC generative
distribution takes an instantaneous transition matrix (Q), a number of sites (L) and a tree:

```
tree = newick(tree="((A:0.1,B:0.1):0.2,(C:0.15,D:0.15):0.15);");
D ~ PhyloCTMC(tree=tree, Q=jukesCantor(), L=100);
```

## Molecular clock

If the tree is not in units of substitutions per site, then it is possible to include a molecular clock rate
to scale the branches of the tree from time to substitutions per site. This example will give the same
result as the previous example, but the tree is now in units of time, so the mutation rate (mu) in units of
substitutions per site per unit time is given.

```
tree = newick(tree="((A:10,B:10):20,(C:15,D:15):15);");
D ~ PhyloCTMC(tree=tree, Q=jukesCantor(), L=100, mu=0.01);
```

## Nucleotide substitution models

There are a number of built in functions to construct standard nucleotide rate matrices. Some examples:

```
jc = jukesCantor();
hky = hky(kappa=2.0, freq=[0.2, 0.25, 0.3, 0.25]);
gtr = gtr(rates=[0.1, 0.25, 0.15, 0.15, 0.25, 0.1], freq=[0.2, 0.25, 0.3, 0.25]);
```

The Dirichlet prior is a natural prior for relative rates and frequencies, so a full model for GTR could
look like this:

```
π ~ Dirichlet(conc=[3.0,3.0,3.0,3.0]); // dirichlet prior on base frequencies
R ~ Dirichlet(conc=[1.0, 2.0, 1.0, 1.0, 2.0, 1.0]); // dirichlet prior on relative rates
Q = gtr(freq=π, rates=R); // construct the GTR instantaneous rate matrix
```

## Rate heterogeneity across sites

The PhyloCTMC generative distribution takes a parameter siteRates to describe rates across sites.
For inference, a common model is the discretized Gamma distribution of site rate heterogeneity (Yang, 1994).

To match this model for simulation in LPHY do the following:

```
tree = newick(tree="((A:0.1,B:0.1):0.2,(C:0.15,D:0.15):0.15);");
siteRates ~ G(shape=0.5, ncat=4, reps=100); // generate 100 site rates from a discretized Gamma
D ~ PhyloCTMC(tree=tree, Q=jukesCantor(), siteRates=siteRates);
```

## Relaxed clock models (rate heterogeneity across branches)

The PhyloCTMC generative distribution can also take an optional parameter branchRates to describe the
rates across branches:

```
ψ = newick(tree="((A:0.1,B:0.1):0.2,(C:0.15,D:0.15):0.15);");
branchRates = [0.5, 2.0, 1.0, 1.0, 2.0, 0.5];
D ~ PhyloCTMC(L=100, Q=jukesCantor(), tree=ψ, branchRates=branchRates);
```