# Validations

## Setup simulations

First, we need to create a LPhy script to configure our simulations for the model validation.
We are using [RSV2sim.lphy](https://github.com/LinguaPhylo/linguaPhylo/blob/master/tutorials/RSV2sim.lphy), 
where the LPhy command `sim ~ PhyloCTMC` instructs the alignment will be the simulated.

Use LPhyBEAST to run the following command in the terminal to create XMLs, 
true values and true trees.

```
$LPhyBEAST$ -r 110 -l 50000000
            -o $USER_HOME$/WorkSpace/linguaPhylo/manuscript/xmls/al2.xml
            $USER_HOME$/WorkSpace/linguaPhylo/tutorials/RSV2sim.lphy
```

where `$USER_HOME$` is your home directory assigned to the 
[path variable of IntelliJ](https://www.jetbrains.com/help/idea/absolute-path-variables.html)

BEAST XMLs and true values from 110 simulations will be created in the folder 
"~/WorkSpace/.../xmls/".


## BEAST logs

Run XMLs using BEAST 2, and put all BEAST logs and trees in the same folder 
for the pipeline. 
All required logs (we do not BEAST tree logs) are available from 
[LPhy website](https://github.com/LinguaPhylo/linguaPhylo.github.io/tree/master/covgtest).
The full version of backups is also available in the Dropbox.

## R pipeline

The results are summarised by
[5-Step Pipeline](https://github.com/walterxie/TraceR/blob/master/examples/Pipeline.md),
which will produce intermediate `*.tsv` files containing the statistic summaries 
in each step.
The R script [PlotValidations.R](PlotValidations.R) is the code to plot figures.

## Summary and Figure

The final summary `*.tsv` for each parameters and true values are in the same folder,
but figures are in the sub-folder `figs`.


## Simulations

The results have been moved to Dropbox.

- WeightedDirichlet(conc=[1.0,1.0,1.0]), weights=...) (alpha1)

- WeightedDirichlet(conc=[2.0,2.0,2.0]), weights=...) (alpha2)

- No WeightedDirichlet prior (noWeigDiriPrior)

- 2nd runs using same XMLs (sim3par) without WeightedDirichlet prior

- No WeightedDirichlet prior with one partition (sim1partition)
