# Birth-death process generative distributions

This LPHY package describes a family of birth-death-sampling generative distributions that produce TimeTrees.

The simplest model in this package is the one parameter model Yule. The birth rate (lambda) parameter determines at 
what rate, per unit time, each lineage bifurcates to produce two daughter lineages. This describes a pure birth process,
so that the number of lineages grows monotonically forward in time. 

## Yule model

In its simplest form the Yule model has a stopping criterion based on the final number of leaves in the tree. 
The following code will generate a random Yule tree with 16 leaves:

```
ψ ~ Yule(lambda=1.0, n=16);
```

It is also possible to give explicit taxa labels to the generative distribution:

```
taxa = 1:16;
ψ ~ Yule(lambda=1.0, taxa=taxa);
```

or

```
taxa = ["A", "B", "C", "D"];
ψ ~ Yule(lambda=1.0, taxa=taxa);
```

### Calibrated Yule model

It is also possible the generate a Yule tree with a given rootAge:

```
ψ ~ Yule(lambda=1.0, n=16, rootAge=10);
```

This is useful if you want to produce a calibrated analysis where there is a separate prior on the root age of the tree:

```
rootAge ~ LogNormal(meanlog=2.0, sdlog=1.0);
ψ ~ Yule(lambda=1.0, n=16, rootAge=rootAge);
```

## Birth-death model

The birth-death tree process is a generalisation of the Yule model. A second rate of extinction (mu) is added alongside
the rate of speciation (lambda). Running forward in time it is possible for the number of lineages to both grow and 
shrink, and indeed for the tree process to go completely extinct. 

### Calibrated Birth-Death process

A calibrated birth-death process can be describe like so:

```
ψ ~ BirthDeath(lambda=2.0, mu=1.0, n=16, rootAge=10);
```

It is important to realise that the resulting tree will contain only the extant lineages. All extinct lineages will be
suppressed. As with the Yule model it is possible to directly specify the taxa names:

```
taxa = ["A", "B", "C", "D"];
ψ ~ BirthDeath(lambda=2.0, mu=1.0, taxa=taxa, rootAge=10);
```

### Full birth-death process

It is possible to generate a full birth-death tree that includes also extinct lineages using the following generative
distribution:

```
ψ ~ FullBirthDeath(lambda=2.0, mu=1.0, rootAge=10);
```

Note that unlike the BirthDeath generative distribution, this process will generate a tree with a random number of
leaf nodes, both in terms of extinct and extant lineages. The only conditioning is on the root age.

### Birth-death-sampling process

A birth-death-sampling process adds a third parameter (rho) the probability of sampling each extant lineage. Again, 
only extant lineages are leaves in the resulting tree and extinct lineages are suppressed:

```
T ~ BirthDeathSampling(lambda=2.0, mu=1.0, rho=0.5, rootAge=3);
```

An alternative parameterization allows specification of the diversification and turnover rates:

```
T ~ BirthDeathSampling(diversification=1.0, turnover=0.5, rho=0.5, rootAge=3);
```

Again it is important to note that the number of tips of the tree is a random variable, and is not conditioned on,
unlike for the BirthDeath generative distribution above.

### Birth-death-serial-sampling process

A birth-death-serial-sampling (Stadler and Yang, 2013) process adds a fourth parameter (psi), the rate of sampling each lineage per unit time. 
Now leaves of the tree can either be extinct (psi-sampled) or extant (rho-sampled). 

```             
ages = [0.0,1.0,2.0,3.0,4.0];
ψ ~ BirthDeathSerialSampling(lambda=1, mu=0.5, rho=0.1, psi=1, rootAge=5, ages=ages);
```

In this case the number of leaves in the tree, and their ages are conditioned on, so are not random variables.

## References

* Tanja Stadler, Ziheng Yang (2013) "Dating Phylogenies with Sequentially Sampled Tips", Systematic Biology, 62(5):674–688 https://doi.org/10.1093/sysbio/syt030