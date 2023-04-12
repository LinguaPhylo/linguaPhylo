LPhy Language Reference (version 1.4.1)
=======================================
This an automatically generated language reference of the LinguaPhylo (LPhy) statistical phylogenetic modeling language.

Parametric distribution
-----------------------
- [Bernoulli](parametric/Bernoulli.md)
- [Beta](parametric/Beta.md)
- [Cauchy](parametric/Cauchy.md)
- [Dirichlet](parametric/Dirichlet.md)
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
- [Uniform](parametric/Uniform.md)
- [UniformDiscrete](parametric/UniformDiscrete.md)
- [Weibull](parametric/Weibull.md)
- [WeightedDirichlet](parametric/WeightedDirichlet.md)

Tree models
-----------
- [BirthDeathSampling](tree-model/BirthDeathSampling.md)
- [BirthDeathSerialSampling](tree-model/BirthDeathSerialSampling.md)
- [BirthDeath](tree-model/BirthDeath.md)
- [FossilBirthDeathTree](tree-model/FossilBirthDeathTree.md)
- [FullBirthDeath](tree-model/FullBirthDeath.md)
- [MultispeciesCoalescent](tree-model/MultispeciesCoalescent.md)
- [RhoSampleTree](tree-model/RhoSampleTree.md)
- [Coalescent](tree-model/Coalescent.md)
- [SimBDReverse](tree-model/SimBDReverse.md)
- [SimFBDAge](tree-model/SimFBDAge.md)
- [SimFossilsPoisson](tree-model/SimFossilsPoisson.md)
- [SkylineCoalescent](tree-model/SkylineCoalescent.md)
- [StructuredCoalescent](tree-model/StructuredCoalescent.md)
- [Yule](tree-model/Yule.md)

Other generative distributions
------------------------------
- [Categorical](distributions/Categorical.md)
- [ErrorModel](distributions/ErrorModel.md)
- [MissingSites](distributions/MissingSites.md)
- [PhyloBrownian](distributions/PhyloBrownian.md)
- [PhyloCTMC](distributions/PhyloCTMC.md)
- [PhyloMultivariateBrownian](distributions/PhyloMultivariateBrownian.md)
- [PhyloOU](distributions/PhyloOU.md)
- [sample](distributions/sample.md)
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
- [extantTaxa](taxa-alignment/extantTaxa.md)
- [extractTrait](taxa-alignment/extractTrait.md)
- [invariableSites](taxa-alignment/invariableSites.md)
- [readFasta](taxa-alignment/readFasta.md)
- [readNexus](taxa-alignment/readNexus.md)
- [selectSites](taxa-alignment/selectSites.md)
- [species](taxa-alignment/species.md)
- [variableSites](taxa-alignment/variableSites.md)

Substitution and site models
----------------------------
- [bModelSet](subst-site-model/bModelSet.md)
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
- [wag](subst-site-model/wag.md)
- [bSiteModel](subst-site-model/bSiteModel.md)

Tree functions
--------------
- [extantTree](tree-func/extantTree.md)
- [localBranchRates](tree-func/localBranchRates.md)
- [countMigrations](tree-func/countMigrations.md)
- [newick](tree-func/newick.md)
- [pruneTree](tree-func/pruneTree.md)

Other functions
---------------
- [arange](functions/arange.md)
- [argi](functions/argi.md)
- [concat2Str](functions/concat2Str.md)
- [concatArray](functions/concatArray.md)
- [get](functions/get.md)
- [intersect](functions/intersect.md)
- [length](functions/length.md)
- [nchar](functions/nchar.md)
- [ntaxa](functions/ntaxa.md)
- [parseInt](functions/parseInt.md)
- [rangeInt](functions/rangeInt.md)
- [rep](functions/rep.md)
- [repArray](functions/repArray.md)
- [select](functions/select.md)
- [simulate](functions/simulate.md)
- [slice](functions/slice.md)
- [sort](functions/sort.md)
- [split](functions/split.md)
- [sum](functions/sum.md)
- [hammingWeight](functions/hammingWeight.md)
- [unique](functions/unique.md)

Types
-----
- [Boolean[]](types/Boolean[].md)
- [Double[]](types/Double[].md)
- [Integer[]](types/Integer[].md)
- [Number[]](types/Number[].md)
- [Object[]](types/Object[].md)
- [Double[][]](types/Double[][].md)
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
- [SimpleAlignment](types/SimpleAlignment.md)
- [MetaDataAlignment](types/MetaDataAlignment.md)
- [SiteModel](types/SiteModel.md)
- [TimeTree](types/TimeTree.md)

Built-in
--------
- [binary operators functions](built-in-binary-operators.md)
- [math functions](built-in-math.md)
- [trigonometric functions](built-in-trigonometry.md)

