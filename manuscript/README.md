# Validations

## XMLs and true value

In [RSV2sim.lphy](https://github.com/LinguaPhylo/linguaPhylo/blob/master/tutorials/RSV2sim.lphy), 
the command `sim ~ PhyloCTMC` instructs the alignment will be the simulated.

```
lphybeast.LPhyBEAST -r 110 -l 50000000
                    -o $USER_HOME$/WorkSpace/linguaPhylo/manuscript/xmls/al2.xml
                    $USER_HOME$/WorkSpace/linguaPhylo/tutorials/RSV2sim.lphy
```

where `$USER_HOME$` is your home directory assigned to the 
[path variable of IntelliJ](https://www.jetbrains.com/help/idea/absolute-path-variables.html)

BEAST XMLs and true values from 110 simulations will be created in the folder 
"~/WorkSpace/.../xmls/".


## Simulations

- [WeightedDirichlet(conc=[1.0,1.0,1.0]), weights=...](alpha1)

- [WeightedDirichlet(conc=[2.0,2.0,2.0]), weights=...](alpha2)

- [No WeightedDirichlet prior](noWeigDiriPrior)

- [2nd runs using same XMLs](sim3par) without WeightedDirichlet prior

- [No WeightedDirichlet prior with one partition](sim1partition)


## BEAST logs

BEAST logs and trees are in the same folder. 
R script will produce intermediate `*.tsv` files containing 
the statistic summary of all traces from each log.


## Summary and Figures

The final summary `*.tsv` for each parameters and true values are in the same folder,
but figures are in the sub-folder `figs`.


## R scripts

All R scripts are in the [scripts](scripts) folder.

CreateTraceStats.R => Summary.R => PlotValidations.R

