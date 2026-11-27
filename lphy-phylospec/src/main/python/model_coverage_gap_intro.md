LPhy is designed to enable the specification of phylogenetic models using a concise and readable syntax, with a
reference implementation built on Java that supports data simulation and an extensibility mechanism for adding new
functionality and data types -- see the
[LPhy features overview](https://linguaphylo.github.io/features/) and [DEV_NOTE2](../../../../DEV_NOTE2.md).
PhyloSpec is not a runtime itself but a cross-engine *specification* that engines such as BEAST2, BEAST X, RevBayes,
and LPhy are each expected to implement -- see its pages on the
[specification format](https://phylospec.vercel.app/specification),
the [modeling language](https://phylospec.vercel.app/language), and the
[core component library](https://phylospec.vercel.app/components).

**Top difference: LPhy is built to simulate data, PhyloSpec is built to analyse observed data**

> **This gap matters more than any type or generator gap below.** LPhy simulates by default: a script without data
> generates the tree and the alignment itself. PhyloSpec scripts are written for inference on an observed alignment.
> The language can express a data-free model, but nothing in PhyloSpec simulates one, and writing one is clumsy.

- **LPhy simulates by default.** Every `~` statement is sampled, so a script without a `data` block simulates the
  whole model, from the parameters down to the tree and the alignment. Everything the simulation needs is a model
  argument: the taxa come from `n` or `taxa` on every tree prior (`Yule(lambda=..., n=10)`, `Coalescent(theta=...,
  taxa=taxa(names=1:10))`), and the alignment length comes from `L` on `PhyloCTMC` (`PhyloCTMC(tree=..., Q=...,
  L=200)`). Observing data is optional: adding a `data` block that clamps the alignment turns the same model into
  an inference model.
- **PhyloSpec can express a data-free model, but only awkwardly.** An unobserved `Alignment sim ~ PhyloCTMC(...)`
  type-checks, and so does a model with no data file at all, such as:

  ```
  Tree tree ~ Yule(birthRate=1.0, taxa=[taxon(name="a"), taxon(name="b"), taxon(name="c"), taxon(name="d")])
  QMatrix q = jc69()
  Vector<Rate> rates ~ DiscreteGammaInv(shape=0.5, numCategories=4, numSites=200)
  Alignment sim ~ PhyloCTMC(tree=tree, qMatrix=q, siteRates=rates)
  ```

  The dimensions that LPhy takes as plain arguments are harder to give:
  - **Taxa must be listed one by one.** Every tree prior (`Yule`, `BirthDeath`, `Coalescent`, `SkylineCoalescent`,
    `FossilizedBirthDeath`) requires `taxa`. `Taxa` is an alias of `Vector<Taxon>`, so without data the taxa must be
    written as a vector of `taxon(name=...)` calls. There is no count like LPhy's `n`, and a vector of plain
    strings is rejected. Otherwise the taxa come from `taxa(alignment)` or `taxa(tree)`.
  - **There is no alignment length argument.** `PhyloCTMC`'s output type is `Alignment<...; numSites=siteRates.num>`,
    so the number of sites can only be set through `siteRates`: a vector of known length, or a site-rate
    distribution that takes `numSites` (`DiscreteGammaInv`). That also brings in rate variation across sites, unless
    every rate is 1.0. LPhy has a plain `L`.
- **Nothing in PhyloSpec runs a simulation.** The two engine integrations (BEAST 3 and BEAST X) are inference
  engines. The language page frames the purpose as detecting invalid models "before inference", and its example
  reads `fromNexus` and clamps with `observed as`. Simulators appear only as a possible future tool. Every
  PhyloSpec test and example script that uses `PhyloCTMC` or `PhyloBM` also has an `observed as` clause, except
  one written in an outdated syntax that no longer type-checks.
- **What this means for `lphy-phylospec`.** A typical PhyloSpec script converts to an LPhy *inference* model. LPhy
  can then simulate from it in two ways:
  - reuse the shape of the observed data: keep its taxa and set `L` to its number of sites, which is what an LPhy
    script does with `L=D.nchar()`;
  - accept data-free scripts written as above, which convert directly.

  PhyloSpec would need two additions to make simulation scripts as simple as LPhy's: a `Taxa` generator that takes
  names or a count, and a length argument on `PhyloCTMC`. The affected rows below carry a note pointing back to
  this section.

Apart from simulation, LPhy and PhyloSpec line up against each other in three distinct ways:

**1. Modeling language**

**Same:** both express a model as a graph of statements built from two operators -- `=` for a deterministic assignment
or function call, and `~` for a stochastic, distribution-sampled assignment.

**Different:**
1. Typing -- LPhy has no type declarations at all; it is dynamically typed, inferring each value's type at runtime
   (see [Section 2](#2-types)). PhyloSpec requires every variable to be declared with an explicit, static type.
2. Vectorization -- PhyloSpec vectorizes with an explicit, for-loop-style indexed assignment
   (`Real expX[i] = exp(x[i]) for i in 1:num(x)`). LPhy has no loop construct at all; a distribution instead
   produces independent, identically distributed replicates through its own vector-shaped parameters.
3. Clamping data to an observation -- PhyloSpec clamps inline, in the same statement, with an explicit
   `observed as` clause. LPhy clamps implicitly: a script declares the same-named variable once with an observed
   value in its `data` block and again with a distribution in its `model` block, and the shared name alone is
   what triggers the clamp. In LPhy the clamp is optional (without it the variable is simulated); in PhyloSpec
   it is optional too, but its scripts are written to clamp the alignment (see the top difference above).
4. Inline expressions -- both allow a deterministic call to nest inside another expression's arguments (e.g.
   `log(100)` inside a distribution's `sd` argument). LPhy's stochastic operator `~` cannot: it may only appear as
   a top-level statement, never nested inline (there is no way to write the equivalent of
   `f(kappa ~ LogNormal(0,1))`). PhyloSpec instead treats distributions as first-class objects that can be passed
   as arguments and composed like any other value.

**2. Component library**

**Same:** both build their library out of **generators** -- named, typed producers of values -- split into the
same two kinds: a **distribution** (stochastic, sampled with `~`) and a **function** (deterministic, no
sampling) -- see [Section 3](#3-generators).

**Different:**
1. Building block paired with each generator -- LPhy pairs a `Generator` with a `Value`, a runtime wrapper class
   that carries the actual value together with its type. PhyloSpec pairs a `Generator` with a `Type`, a purely
   static declaration with no runtime wrapper at all -- see [Section 2](#2-types).
2. How distribution-vs-function is recorded -- LPhy's split is a Java generic type hierarchy:
   `GenerativeDistribution<T>` (`sample()` returns `RandomVariable<T>`) vs `DeterministicFunction<T>`
   (`apply()` returns `Value<T>`), where `T` is the produced value's own type (e.g. `Double`, `TimeTree`).
   PhyloSpec's own signature says so directly instead, as a type string: a distribution's produced type is
   written `Distribution<T>`, a function's is the plain `T` -- see [Section 3](#3-generators).
3. How the produced type `T` itself is recorded -- LPhy's `T` is a bare Java class name, read by reflecting
   on the generic parameter above (e.g. `Double`, `TimeTree`). PhyloSpec's `T` is a hand-written type
   expression that can be considerably richer, including nested generics and dimension constraints (e.g.
   `Yule` -> `Tree<;numTaxa=taxa.num>`). The two vocabularies for `T` are not directly comparable, so
   [Section 3](#3-generators)'s comparison only checks for the `Distribution<...>` wrapper itself, never `T`
   itself; matching `T` (e.g. LPhy's `Double` to PhyloSpec's `Real`/`PositiveReal`/...) is instead
   [Section 2](#2-types)'s job.
4. Method calls -- LPhy also calls some functions with dot syntax on a value (e.g. `tree.rootAge()`) instead of
   as a stand-alone call. It's still an ordinary deterministic function underneath, just invoked differently.
   PhyloSpec has no dot-call syntax at all, so these are matched by hand against the closest PhyloSpec function
   instead of compared directly -- see [Section 3](#3-generators).
5. Operators -- LPhy implements operators (`+`, `<`, `&&`, ...) as ordinary named `DeterministicFunction`s, so
   they're listed in its library like any other generator. PhyloSpec treats operators as language grammar,
   resolved by a separate type-checking pass, and never lists them as components at all -- see
   [Section 4](#4-math--logic).

**3. Extending the library**

**Same:** both provide a way to add or declare which components a given tool actually supports.

**Different:**
1. LPhy's extension is code-level: a new component is added by implementing a Java interface and registering it
   through the Java SPI (`ServiceLoader`) mechanism -- the same route extension modules use to plug into the core
   library without modifying it.
2. PhyloSpec's is declarative, since it has no runtime of its own: each engine is expected to implement the
   interfaces the Component Library Format describes, and separately declares what it doesn't support (or
   restricts) through a companion *engine integration format* document.
