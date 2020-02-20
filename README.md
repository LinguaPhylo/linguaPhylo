# Graphical models for phylogenetic simulation and analysis

## Communicating and reproducing probabilistic models for phylogenetic analysis

A new paradigm for scientific computing and data science has begun to emerged in the last decade. A recent example is the publication of the first "computationally reproducible article" using eLife's Reproducible Document Stack which blends features of a traditional manuscript with live code, data and interactive figures.

Although standard tools for statistical phylogenetics provide a degree of reproducibility and reusability through popular open-source software and computer-readable data file formats, there is still much to do. The ability to construct and accurately communicate probabilistic models in phylogenetics is frustratingly underdeveloped. There is low interoperability between different inference packages (e.g. BEAST1, BEAST2, MrBayes, RevBayes), and the file formats that these software use have low readability for researchers.

In this project we aim to develop a model specification language to concisely and precisely define probabilistic phylogenetic models. The aim is to work towards a _lingua franca_ for probabilistic models of phylogenetic evolution along with tools to specify, graphically visualise and simulate data from models defined in such a language. 

A key step beyond this initial work will be to provide the ability for models specified in this way to be applied to data using standard inference tools such as MrBayes, RevBayes, BEAST1 and BEAST2.
