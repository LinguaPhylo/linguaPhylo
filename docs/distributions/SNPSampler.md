SNPSampler distribution
=======================
SNPSampler([Alignment](../types/Alignment.md) **alignment**, [Number](../types/Number.md) **p**, [Number](../types/Number.md) **r**)
------------------------------------------------------------------------------------------------------------------------------------

Sample SNPs from a given nucleotide one sequence alignment by using a binomial distribution to choose mutation sites (with the number of trials equal to the number of sites). For each selected site, use the reference nucleotide as the reference allele and randomly choose a different nucleotide as the alternative allele.

### Parameters

- [Alignment](../types/Alignment.md) **alignment** - the one sequence alignment
- [Number](../types/Number.md) **p** - (optional) the probability of each site to be SNP, deafult to be 0.01
- [Number](../types/Number.md) **r** - (optional) the ratio of heterozygousSNPs and non-reference homozygous SNPs, default all SNPs are heterozygous

### Return type

[Variant[]](../types/Variant[].md)


### Examples

- 



