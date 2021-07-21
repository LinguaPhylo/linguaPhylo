# Validations

## XMLs and true value

BEAST XMLs and true values from simulations are in the [xmls](xmls) folder.

In RSV2.lphy, change `codon ~ PhyloCTMC` to `sim ~ PhyloCTMC`, 
so that the alignment will be the simulated data.

```
lphybeast.LPhyBEAST -r 100 -l 50000000
                    -o ~/WorkSpace/linguaPhylo/manuscript/xmls/RSV2.xml
                    ~/WorkSpace/linguaPhylo/tutorials/RSV2.lphy
```

## BEAST logs

BEAST logs and trees are in the [logs](logs) folder. 
`*.tsv` files contain the statistic summary of all traces from each log.


## Summary and Figures

The final summary `*.tsv` files and figures are in the [figs](figs) folder.


## R scripts

All R scripts are in the [scripts](scripts) folder.

CreateTraceStats.R => Summary.R => PlotValidations.R

