LPhy is designed to enable the specification of phylogenetic models using a concise and readable syntax, with a
reference implementation built on Java that supports data simulation and an extensibility mechanism for adding new
functionality and data types -- see the
[LPhy features overview](https://linguaphylo.github.io/features/) and [DEV_NOTE2](../../../../DEV_NOTE2.md).
PhyloSpec is not a runtime itself but a cross-engine *specification* that engines such as BEAST2, BEAST X, RevBayes,
and LPhy are each expected to implement -- see its pages on the
[specification format](https://phylospec.vercel.app/specification),
the [modeling language](https://phylospec.vercel.app/language), and the
[core component library](https://phylospec.vercel.app/components). Given that difference in nature, they line up
against each other in three distinct ways:

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
   what triggers the clamp.
4. Inline expressions -- both allow a deterministic call to nest inside another expression's arguments (e.g.
   `log(100)` inside a distribution's `sd` argument). LPhy's stochastic operator `~` cannot: it may only appear as
   a top-level statement, never nested inline (there is no way to write the equivalent of
   `f(kappa ~ LogNormal(0,1))`). PhyloSpec instead treats distributions as first-class objects that can be passed
   as arguments and composed like any other value.

**2. Component library**

**Same:** both build their library out of **generators** -- named, typed producers of values, covering both
distributions and functions.

**Different:**
1. Building block paired with each generator -- LPhy pairs a `Generator` with a `Value`, a runtime wrapper class
   that carries the actual value together with its type. PhyloSpec pairs a `Generator` with a `Type`, a purely
   static declaration with no runtime wrapper at all -- see [Section 2](#2-types).
2. Generator kinds -- LPhy's `Generator` covers three kinds: `GenerativeDistribution`, `DeterministicFunction`,
   and `Method call` (dot-syntax invoked on an existing object). PhyloSpec's component library, the **Core
   Component Library**, only has the first two as first-class objects, with no method-call syntax at all, so
   LPhy's method calls are matched against hand-picked PhyloSpec-equivalent functions rather than compared
   directly -- see [Section 3](#3-generators).
3. Operators -- LPhy implements operators (`+`, `<`, `&&`, ...) as ordinary named `DeterministicFunction`s, so
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
