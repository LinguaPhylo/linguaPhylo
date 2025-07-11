<a href="https://linguaphylo.github.io">LPhy</a> Language Reference (version 1.7.0)
===================================================================================
This an automatically generated language reference of the <a href="https://linguaphylo.github.io">LinguaPhylo</a> (LPhy) statistical phylogenetic modeling language.

Parametric distribution
-----------------------
- [Bernoulli](parametric/Bernoulli.md)
- [Beta](parametric/Beta.md)
- [Categorical](parametric/Categorical.md)
- [Cauchy](parametric/Cauchy.md)
- [Dirichlet](parametric/Dirichlet.md)
- [DirichletMultinomial](parametric/DirichletMultinomial.md)
- [DiscretizeGamma](parametric/DiscretizeGamma.md)
- [Exp](parametric/Exp.md)
- [ExpMarkovChain](parametric/ExpMarkovChain.md)
- [Gamma](parametric/Gamma.md)
- [Geometric](parametric/Geometric.md)
- [InverseGamma](parametric/InverseGamma.md)
- [LogNormal](parametric/LogNormal.md)
- [NegativeBinomial](parametric/NegativeBinomial.md)
- [Normal](parametric/Normal.md)
- [NormalGamma](parametric/NormalGamma.md)
- [Poisson](parametric/Poisson.md)
- [RandomBooleanArray](parametric/RandomBooleanArray.md)
- [RandomComposition](parametric/RandomComposition.md)
- [UCLN_Mean1](parametric/UCLN_Mean1.md)
- [Uniform](parametric/Uniform.md)
- [UniformDiscrete](parametric/UniformDiscrete.md)
- [Weibull](parametric/Weibull.md)
- [WeightedDirichlet](parametric/WeightedDirichlet.md)

Tree models
-----------
- [BirthDeathSampling](tree-model/BirthDeathSampling.md)
- [BirthDeathSerialSampling](tree-model/BirthDeathSerialSampling.md)
- [BirthDeath](tree-model/BirthDeath.md)
- [CalibratedYule](tree-model/CalibratedYule.md)
- [FossilBirthDeathTree](tree-model/FossilBirthDeathTree.md)
- [FullBirthDeath](tree-model/FullBirthDeath.md)
- [MultispeciesCoalescent](tree-model/MultispeciesCoalescent.md)
- [CoalescentPopFunc](tree-model/CoalescentPopFunc.md)
- [RhoSampleTree](tree-model/RhoSampleTree.md)
- [Coalescent](tree-model/Coalescent.md)
- [SimBDReverse](tree-model/SimBDReverse.md)
- [SimFBDAge](tree-model/SimFBDAge.md)
- [SimFossilsPoisson](tree-model/SimFossilsPoisson.md)
- [SkylineCoalescent](tree-model/SkylineCoalescent.md)
- [StructuredCoalescent](tree-model/StructuredCoalescent.md)
- [SubsampledTree](tree-model/SubsampledTree.md)
- [Yule](tree-model/Yule.md)

Other generative distributions
------------------------------
- [AutoCorrelatedLogRates](distributions/AutoCorrelatedLogRates.md)
- [ErrorModel](distributions/ErrorModel.md)
- [MissingSites](distributions/MissingSites.md)
- [PhyloBrownian](distributions/PhyloBrownian.md)
- [PhyloCTMC](distributions/PhyloCTMC.md)
- [PhyloMultivariateBrownian](distributions/PhyloMultivariateBrownian.md)
- [PhyloOU](distributions/PhyloOU.md)
- [SNPSampler](distributions/SNPSampler.md)
- [sample](distributions/sample.md)
- [SampleBranch](distributions/SampleBranch.md)
- [Sequence](distributions/Sequence.md)
- [bSiteRates](distributions/bSiteRates.md)

Sequence type
-------------
- [aminoAcids](sequence-type/aminoAcids.md)
- [binaryDataType](sequence-type/binaryDataType.md)
- [nucleotides](sequence-type/nucleotides.md)
- [standard](sequence-type/standard.md)

Taxa & alignment
----------------
- [copySites](taxa-alignment/copySites.md)
- [taxa](taxa-alignment/taxa.md)
- [distance](taxa-alignment/distance.md)
- [extantTaxa](taxa-alignment/extantTaxa.md)
- [extractTrait](taxa-alignment/extractTrait.md)
- [informativeSites](taxa-alignment/informativeSites.md)
- [invariableSites](taxa-alignment/invariableSites.md)
- [readDelim](taxa-alignment/readDelim.md)
- [readFasta](taxa-alignment/readFasta.md)
- [readNexus](taxa-alignment/readNexus.md)
- [rmTaxa](taxa-alignment/rmTaxa.md)
- [selectSites](taxa-alignment/selectSites.md)
- [species](taxa-alignment/species.md)
- [variableSites](taxa-alignment/variableSites.md)
- [fasta](taxa-alignment/fasta.md)

Substitution and site models
----------------------------
- [bModelSet](subst-site-model/bModelSet.md)
- [binaryCovarion](subst-site-model/binaryCovarion.md)
- [binaryRateMatrix](subst-site-model/binaryRateMatrix.md)
- [f81](subst-site-model/f81.md)
- [gtr](subst-site-model/gtr.md)
- [generalTimeReversible](subst-site-model/generalTimeReversible.md)
- [hky](subst-site-model/hky.md)
- [jukesCantor](subst-site-model/jukesCantor.md)
- [k80](subst-site-model/k80.md)
- [lewisMK](subst-site-model/lewisMK.md)
- [migrationMatrix](subst-site-model/migrationMatrix.md)
- [nucleotideModel](subst-site-model/nucleotideModel.md)
- [strToDouble](subst-site-model/strToDouble.md)
- [wag](subst-site-model/wag.md)
- [bSiteModel](subst-site-model/bSiteModel.md)

Tree functions
--------------
- [extantTree](tree-func/extantTree.md)
- [setInternalNodesID](tree-func/setInternalNodesID.md)
- [labelClade](tree-func/labelClade.md)
- [localBranchRates](tree-func/localBranchRates.md)
- [mrca](tree-func/mrca.md)
- [countMigrations](tree-func/countMigrations.md)
- [newick](tree-func/newick.md)
- [pruneTree](tree-func/pruneTree.md)
- [readTrees](tree-func/readTrees.md)
- [substituteClade](tree-func/substituteClade.md)

Other functions
---------------
- [arange](functions/arange.md)
- [argi](functions/argi.md)
- [AutoCorrelatedClock](functions/AutoCorrelatedClock.md)
- [concat2Str](functions/concat2Str.md)
- [concatArray](functions/concatArray.md)
- [Cons_Exp_ConsPopFunc](functions/Cons_Exp_ConsPopFunc.md)
- [constantPopFunc](functions/constantPopFunc.md)
- [setDifference](functions/setDifference.md)
- [elementsAt](functions/elementsAt.md)
- [ExpansionPopFunc](functions/ExpansionPopFunc.md)
- [exponentialPopFunc](functions/exponentialPopFunc.md)
- [get](functions/get.md)
- [gompertzPopFunc_f0](functions/gompertzPopFunc_f0.md)
- [gompertzPopFunc_t50](functions/gompertzPopFunc_t50.md)
- [ifelse](functions/ifelse.md)
- [intersect](functions/intersect.md)
- [length](functions/length.md)
- [localClock](functions/localClock.md)
- [logisticPopFunc](functions/logisticPopFunc.md)
- [map](functions/map.md)
- [nchar](functions/nchar.md)
- [ntaxa](functions/ntaxa.md)
- [parseInt](functions/parseInt.md)
- [rangeInt](functions/rangeInt.md)
- [rep](functions/rep.md)
- [repArray](functions/repArray.md)
- [stochasticVariableSelection](functions/stochasticVariableSelection.md)
- [select](functions/select.md)
- [simulate](functions/simulate.md)
- [slice](functions/slice.md)
- [sort](functions/sort.md)
- [split](functions/split.md)
- [sum](functions/sum.md)
- [hammingWeight](functions/hammingWeight.md)
- [sumCols](functions/sumCols.md)
- [sumRows](functions/sumRows.md)
- [setUnion](functions/setUnion.md)
- [unique](functions/unique.md)

Types
-----
- [Boolean[]](types/Boolean[].md)
- [Double[]](types/Double[].md)
- [Integer[]](types/Integer[].md)
- [Number[]](types/Number[].md)
- [Object[]](types/Object[].md)
- [String[]](types/String[].md)
- [PopulationFunction[]](types/PopulationFunction[].md)
- [Variant[]](types/Variant[].md)
- [TimeTree[]](types/TimeTree[].md)
- [Double[][]](types/Double[][].md)
- [Number[][]](types/Number[][].md)
- [String[][]](types/String[][].md)
- [Boolean](types/Boolean.md)
- [Double](types/Double.md)
- [Integer](types/Integer.md)
- [Number](types/Number.md)
- [Object](types/Object.md)
- [String](types/String.md)
- [SequenceType](types/SequenceType.md)
- [BModelSet](types/BModelSet.md)
- [NChar](types/NChar.md)
- [Taxa](types/Taxa.md)
- [Alignment](types/Alignment.md)
- [ContinuousCharacterData](types/ContinuousCharacterData.md)
- [FastaAlignment](types/FastaAlignment.md)
- [MetaDataAlignment](types/MetaDataAlignment.md)
- [SimpleAlignment](types/SimpleAlignment.md)
- [PopulationFunction](types/PopulationFunction.md)
- [SVSPopulation](types/SVSPopulation.md)
- [SiteModel](types/SiteModel.md)
- [TimeTree](types/TimeTree.md)
- [TimeTreeNode](types/TimeTreeNode.md)
- [Table](types/Table.md)

