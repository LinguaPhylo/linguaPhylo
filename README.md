# LinguaPhylo: Communicating and reproducing probabilistic models for phylogenetic analysis

[![Build Status](https://github.com/LinguaPhylo/linguaPhylo/workflows/Lphy%20tests/badge.svg)](https://github.com/LinguaPhylo/linguaPhylo/actions?query=workflow%3A%22Lphy+tests%22)

A new paradigm for scientific computing and data science has begun to emerged in the last decade. A recent example is the publication of the first "computationally reproducible article" using eLife's Reproducible Document Stack which blends features of a traditional manuscript with live code, data and interactive figures.

Although standard tools for statistical phylogenetics provide a degree of reproducibility and reusability through popular open-source software and computer-readable data file formats, there is still much to do. The ability to construct and accurately communicate probabilistic models in phylogenetics is frustratingly underdeveloped. There is low interoperability between different inference packages (e.g. BEAST1, BEAST2, MrBayes, RevBayes), and the file formats that these software use have low readability for researchers.

This tool contains two related projects: [LinguaPhylo](https://github.com/LinguaPhylo/linguaPhylo) (LPhy for short) and [LPhyBEAST](https://github.com/LinguaPhylo/LPhyBeast).

## LinguaPhylo (LPhy for short - pronounced el-fee)

In this project we aim to develop a model specification language to concisely and precisely define probabilistic phylogenetic models. The aim is to work towards a _lingua franca_ for probabilistic models of phylogenetic evolution. This language should be readable by both humans and computers. Here is a full example:

<a href="./jc-yule.png"><img src="jc-yule.png" width="450" ></a>

Each line in this model block expresses how a random variable (left of the tilde) is generated from a generative distribution.

The first line creates a random variable, λ, that is log-normally distributed. The second line creates a tree, ψ, with 16 taxa from the Yule process with a lineage birth rate equal to λ. The third line produces a multiple sequence alignment with a length of 200, by simulating a Jukes Cantor model of sequence evolution down the branchs of the tree ψ. Each random variable depends on the previous, so this is a hierarchical model that ultimately defines a probability distribution of sequence alignments of size 16 x 200.

### Language features

The LPhy language features are described at https://linguaphylo.github.io/features/. 

### ANTLR parse tree

The parse tree to show how the above lphy script to be parsed by [ANTLR grammar](lphy/src/main/java/lphy/parser/Simulator.g4):

<a href="./parseTree.png"><img src="parseTree.png" width="600" ></a>

### Tree generative distributions

More details on the available tree generative distributions can be found here: 

* [Birth-death generative distributions](lphy/doc/lphy/evolution/birthdeath.md)
* [Coalescent generative distributions](lphy/doc/lphy/evolution/coalescent.md)

### Models of evolutionary rates and sequence evolution

You can read more details about the PhyloCTMC generative distribution and how to specify substitution models, 
site rates and branch rates here:

* [PhyloCTMC generative distribution](lphy/doc/lphy/evolution/likelihood.md)

### LinguaPhylo Studio

Along with the language definition, we also provide software to specify and visualise models as well as simulate data from models defined in LPhy. 

This software will also provide the ability for models specified in the LPhy language to be applied to data using standard inference tools such as MrBayes, RevBayes, BEAST1 and BEAST2. This will require software that can convert an LPhy specification into an input file that these inference engines understand. The first such software converter is LPhyBEAST described below.

## LPhyBEAST (pronounced el-fee-beast)

LPhyBEAST is a command-line program that takes an LPhy model specification, and a data block and produces a BEAST 2 XML input file.
It therefore enables LPHY as an alternative way to succinctly express and communicate BEAST2 analyses.

The source can be found here: [https://github.com/LinguaPhylo/LPhyBeast](https://github.com/LinguaPhylo/LPhyBeast)

## Homepage and tutorials

[https://linguaphylo.github.io/](https://linguaphylo.github.io/)

## Developer note

- [LPhy developer note](DEV_NOTE.md)

- [LPhyBEAST developer note](https://github.com/LinguaPhylo/LPhyBeast/blob/master/DEV_NOTE.md)

## License 

This software is licensed under the [GNU Lesser General Public License v3.0](https://github.com/LinguaPhylo/linguaPhylo/blob/master/LICENSE)

The toolbar icon art is licensed under the [Oracle Software Icon License](https://github.com/LinguaPhylo/linguaPhylo/blob/master/lphy-studio/src/main/resources/LICENSE.txt)

Also see https://www.oracle.com/a/tech/docs/software-icon-license-943-2012.html
