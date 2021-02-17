# LinguaPhylo: Communicating and reproducing probabilistic models for phylogenetic analysis

[![Build Status](https://github.com/LinguaPhylo/linguaPhylo/workflows/Lphy%20tests/badge.svg)](https://github.com/LinguaPhylo/linguaPhylo/actions?query=workflow%3A%22Lphy+tests%22)

A new paradigm for scientific computing and data science has begun to emerged in the last decade. A recent example is the publication of the first "computationally reproducible article" using eLife's Reproducible Document Stack which blends features of a traditional manuscript with live code, data and interactive figures.

Although standard tools for statistical phylogenetics provide a degree of reproducibility and reusability through popular open-source software and computer-readable data file formats, there is still much to do. The ability to construct and accurately communicate probabilistic models in phylogenetics is frustratingly underdeveloped. There is low interoperability between different inference packages (e.g. BEAST1, BEAST2, MrBayes, RevBayes), and the file formats that these software use have low readability for researchers.

This repository contains two related projects: LinguaPhylo (LPhy for short) and LPhyBEAST.

## LinguaPhylo (LPhy for short - pronounced el-fee)

In this project we aim to develop a model specification language to concisely and precisely define probabilistic phylogenetic models. The aim is to work towards a _lingua franca_ for probabilistic models of phylogenetic evolution. This language should be readable by both humans and computers. Here is a full example:

[//]: # (generate this html from LPhy Studio)
<p>
    <div class=default>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
        model {
      </span>
    </div>
    <div class=default>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
        &nbsp;&nbsp; 
      </span>
      <span style="color: #00c400; font-size: 12pt; font-family: monospace,monospace">
        λ
      </span>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
         ~ 
      </span>
      <span style="color: #0000ff; font-size: 12pt; font-family: monospace,monospace">
        <b>LogNormal</b>
      </span>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
        (
      </span>
      <span style="color: #808080; font-size: 12pt; font-family: monospace,monospace">
        meanlog=
      </span>
      <span style="color: #ff00ff; font-size: 12pt; font-family: monospace,monospace">
        3.0
      </span>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
        , 
      </span>
      <span style="color: #808080; font-size: 12pt; font-family: monospace,monospace">
        sdlog=
      </span>
      <span style="color: #ff00ff; font-size: 12pt; font-family: monospace,monospace">
        1.0
      </span>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
        );
      </span>
    </div>
    <div class=default>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
        &nbsp;&nbsp; 
      </span>
      <span style="color: #00c400; font-size: 12pt; font-family: monospace,monospace">
        ψ
      </span>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
         ~ 
      </span>
      <span style="color: #0000ff; font-size: 12pt; font-family: monospace,monospace">
        <b>Yule</b>
      </span>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
        (
      </span>
      <span style="color: #808080; font-size: 12pt; font-family: monospace,monospace">
        lambda=
      </span>
      <span style="color: #00c400; font-size: 12pt; font-family: monospace,monospace">
        λ
      </span>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
        , 
      </span>
      <span style="color: #808080; font-size: 12pt; font-family: monospace,monospace">
        n=
      </span>
      <span style="color: #ff00ff; font-size: 12pt; font-family: monospace,monospace">
        16
      </span>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
        );
      </span>
    </div>
    <div class=default>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
        &nbsp;&nbsp; 
      </span>
      <span style="color: #00c400; font-size: 12pt; font-family: monospace,monospace">
        D
      </span>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
         ~ 
      </span>
      <span style="color: #0000ff; font-size: 12pt; font-family: monospace,monospace">
        <b>PhyloCTMC</b>
      </span>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
        (
      </span>
      <span style="color: #808080; font-size: 12pt; font-family: monospace,monospace">
        L=
      </span>
      <span style="color: #ff00ff; font-size: 12pt; font-family: monospace,monospace">
        200
      </span>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
        , 
      </span>
      <span style="color: #808080; font-size: 12pt; font-family: monospace,monospace">
        Q=
      </span>
      <span style="color: #c400c4; font-size: 12pt; font-family: monospace,monospace">
        jukesCantor
      </span>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
        (), 
      </span>
      <span style="color: #808080; font-size: 12pt; font-family: monospace,monospace">
        tree=
      </span>
      <span style="color: #00c400; font-size: 12pt; font-family: monospace,monospace">
        ψ
      </span>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
        );
      </span>
    </div>
    <div class=default>
      <span style="color: #000000; font-size: 12pt; font-family: monospace,monospace">
        }
      </span>
    </div>
</p>

Each of the lines in this  model block expresses how a random variable (to the left of the tilde) is generated from a generative distribution.

The first line creates a random variable, λ, that is log-normally distributed. The second line creates a tree, ψ, with 16 taxa from the Yule process with a lineage birth rate equal to λ. The third line produces a multiple sequence alignment with a length of 200, by simulating a Jukes Cantor model of sequence evolution down the branchs of the tree ψ. As you can see, each of the random variables depends on the last, so this is a hierarchical model that ultimately defines a probability distribution of sequence alignments of size 16 x 200.

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
