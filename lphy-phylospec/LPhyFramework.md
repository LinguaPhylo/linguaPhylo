# LPhy Framework

## 1. Overview

LPhy has no separate "compile" step. Parsing **is** model-building: as the parser
walks the script, it directly constructs the Java objects that *are* the
probabilistic model — there is no intermediate AST that gets translated later.

Two packages matter:

| Package | Role |
|---|---|
| `parser` (+ `parser.antlr`) | Turns script text into objects, wires them together |
| `model` + `parser.graphicalmodel` | Defines what those objects are (`Value`, `Generator`, `GraphicalModel`) |

This document walks through that in three steps:

- **[§2 — From script to graph](#2-from-script-to-graph-parsing-pipeline).**
  The ANTLR-generated parser tree is walked by a single visitor
  (`LPhyListenerImpl`) that resolves each `name(...)` call to a Java class by
  reflection, and wires `Value`/`Generator` objects together as it goes — no
  separate AST-to-model translation pass exists.
- **[§3 — The graph after parsing](#3-the-graph-after-parsing).** The
  resulting `GraphicalModel` is just two id→`Value` dictionaries
  (`data`/`model` blocks). The PGM (probabilistic graphical model) itself isn't stored as a list — it's
  recovered on demand by following `getInputs()`/`getOutputs()` references
  outward from named "sink" values, as shown in a worked example.
- **[§4 — Vectorization](#4-vectorization-iid-and-vectorizeddistribution).**
  `IID` (explicit `replicates=n`) and `VectorizedDistribution`/
  `VectorizedFunction` (implicit, from array arguments) let one `~`/`=` line
  produce an array of values. Both still appear as a single `Generator` node
  in the graph — their per-element sub-generators are deliberately hidden
  from `getInputs()`/`getOutputs()`, while the produced value
  (`VectorizedRandomVariable`/`CompoundVectorValue`) keeps per-element nodes
  reachable via `CompoundVector.getComponentValue()`.

---

## 2. From Script to Graph (parsing pipeline)

```
 .lphy text
     │  LPhyParserAction.parse()          (parser/LPhyParserAction.java)
     ▼
 ANTLR parse tree                          (parser/antlr/LPhy.g4)
     │  visitor.visit(tree)
     ▼
 LPhyListenerImpl.LPhyASTVisitor           (parser/LPhyListenerImpl.java)
     │  (walks the tree, building objects as it goes)
     ▼
 live object graph, stored in
 the data/model dictionaries              (parser/graphicalmodel/GraphicalModel.java)
```

While walking the tree, the visitor:

- **Tracks context.** Entering `data { }` / `model { }` flips a `Context.data` /
  `Context.model` flag that decides which dictionary an assignment lands in
  (`visitDatablock` / `visitModelblock`).
- **Turns literals into `Value`s.** `"abc"`, `1`, `1.5`, `true` → `StringValue`,
  `IntegerValue`, `DoubleValue`, `BooleanValue`.
- **Resolves calls by reflection.** For `Name(arg=...)`, the parser looks up all
  registered Java classes named `Name` (discovered via an SPI registry,
  `core/spi/LoaderManager`) and tries each constructor until the argument
  names/types match (`ParserUtils.getMatchingFunctions` /
  `getMatchingGenerativeDistributions`).
- **Wires edges as it constructs.** `Generator.setInput(name, value)` both sets
  the parameter on the generator *and* calls `value.addOutput(generator)` — this
  single call creates both directions of the edge.
- **Distinguishes `=` from `~`.** `x = f(...)` is a deterministic relation
  (`visitDeterm_relation`); `x ~ Dist(...)` samples a `RandomVariable`
  (`visitStoch_relation`).
- **Handles data clamping.** If `x` exists in both blocks, the model-block
  `RandomVariable` is replaced by one holding the *data* block's value, flagged
  `setObserved(true)`.

---

## 3. The Graph After Parsing

`GraphicalModel` doesn't store one flat node list. It stores two dictionaries:

```
dataDictionary  : id -> Value      (everything declared in data { })
modelDictionary : id -> Value      (everything declared in model { })
```

The actual **graph** is recovered on demand by following object references.
Every node implements `GraphicalModelNode<T>` (`getInputs()`, `value()`), and
there are exactly two kinds of node:

- **`Value<T>`** — a slot holding data. Knows its `function` (the `Generator`
  that produced it — its one "input"), and its `outputs` (every `Generator`
  that consumes it as an argument).
  - **`RandomVariable<T> extends Value<T>`** — same idea, but produced by a
    `GenerativeDistribution` instead of a `DeterministicFunction`.
- **`Generator<T>`** — either a `DeterministicFunction` (`=`) or a
  `GenerativeDistribution` (`~`). Not itself a `Value`, but still a node:
  `getInputs()` returns its parameter `Value`s.

So the PGM alternates `Value ↔ Generator ↔ Value ↔ …`:

```
        ┌───────────────┐        ┌───────────────┐
        │ Value: alpha=2│──────▶ │  GenDist       │──────▶ RandomVariable: x
        └───────────────┘        │  LogNormal     │
        ┌───────────────┐        │  (alpha, sigma)│
        │ Value: sigma=1│──────▶ │                │
        └───────────────┘        └───────────────┘
```

**Finding the graph:** a "sink" is a named value with no outputs
(`getDataModelSinks()`). `GraphicalModelUtils.getAllValuesFromSinks` walks
backward from every sink through `getInputs()`, recursively, to rebuild the
full set of reachable `Value`s — this is how the studio GUI draws the model,
and how `computeLogPosterior()` finds every `RandomVariable` to sum
`logDensity` over (using the clamped data value if observed, the sampled value
otherwise).

### 3.1 Worked example: sink-traversal

```
model {
  theta ~ LogNormal(meanlog=0.0, sdlog=1.0);
  psi   ~ Coalescent(theta=theta, n=10);
  D     ~ PhyloCTMC(tree=psi, mu=1.0, Q=jc69());
}
```

`theta` and `psi` each have an **output** (something downstream consumes them),
so neither is a sink. `D` has no output — it's the only sink. Traversal starts
there and follows `getInputs()` backward, node by node:

```
 getDataModelSinks() ─▶  D  (RandomVariable, no outputs ⇒ SINK)
                         │  D.getInputs() = [PhyloCTMC]
                         ▼
                    PhyloCTMC (GenerativeDistribution)
                         │  .getInputs() = [psi, mu=1.0, Q]
             ┌───────────┼──────────────────┐
             ▼           ▼                  ▼
   psi (RandomVariable) mu=1.0 (Value,   Q (Value, from jc69()
             │           no function       DeterministicFunction,
             │           ⇒ leaf/constant)  no inputs ⇒ leaf)
             │  psi.getInputs() = [Coalescent]
             ▼
        Coalescent (GenerativeDistribution)
             │  .getInputs() = [theta, n=10]
        ┌────┴────────┐
        ▼             ▼
 theta (RandomVariable)  n=10 (Value, leaf/constant)
        │  theta.getInputs() = [LogNormal]
        ▼
   LogNormal (GenerativeDistribution)
        │  .getInputs() = [meanlog=0.0, sdlog=1.0]
        ▼
   meanlog, sdlog  (Values, leaves/constants)
```

`GraphicalModelUtils.getAllValuesFromSinks` recurses exactly this path but only
records the `Value` nodes it passes through (skipping `Generator` nodes),
giving `{D, psi, mu, Q, theta, n, meanlog, sdlog}` — every random variable and
constant that `D` actually depends on. `getAllVariablesFromSinks()` filters
that same walk down to just the `RandomVariable`s (`D`, `psi`, `theta`), which
is exactly the set `computeLogPosterior()` sums `logDensity` over.

---

## 4. Vectorization: `IID` and `VectorizedDistribution`

LPhy lets a single `~` relation produce an **array** of random values, e.g.

```
n = 10;
lambda ~ Exponential(mean=1.0, replicates=n);   // IID: n independent draws, same distribution
x ~ Normal(mean=lambda, sd=1.0);                // Vectorized: n draws, different mean each
```

Both look, from the outside, like a normal `x ~ Dist(...)` line producing one
`RandomVariable`. Vectorization is entirely about *what the generator does
internally* — the surrounding graph structure doesn't change.

### 4.1 Two different mechanisms, two different intents

| | `IID` | `VectorizedDistribution` / `VectorizedFunction` |
|---|---|---|
| Triggered by | an explicit `replicates=n` argument | passing an **array** where the constructor expects a scalar |
| Meaning | *n* independent draws from **the same** distribution | *n* draws from **n distributions with different parameters**, one per array element |
| Where it's decided | `IID.match(...)` | `VectorMatchUtils.vectorMatch(...)` |
| Internally holds | one shared `baseDistribution` | a `List` of *n* per-component `Generator`s, each built by slicing the i'th element out of every array argument |

Both live in `core/vectorization/`.

### 4.2 How the parser picks one

`ParserUtils.constructGenerator(...)` tries, **in this order**, for every
matching constructor:

1. **Direct match** — argument types match the constructor exactly → build the
   plain `Generator` as normal.
2. **`IID.match`** — a valid `replicates` argument is present → wrap in `IID`.
3. **`VectorMatchUtils.vectorMatch`** — an argument is an array where a scalar
   was expected → wrap in `VectorizedDistribution` (or `VectorizedFunction` for
   `=` relations).

Only one of these three shapes is ever built for a given `~`/`=` line.

### 4.3 How they sit in the graph: still just one node

`IID` and `VectorizedDistribution` both **implement `GenerativeDistribution`**
(and `VectorizedFunction` extends `DeterministicFunction`). That's the key
design point: to everything outside `core/vectorization`, they are
indistinguishable from an ordinary generator — one `Generator` node in the PGM.

Their `setParam` implementations deliberately do **not** call `setInput` on
the hidden per-component generators:

```java
// IID.setParam(...)
if (!paramName.equals(REPLICATES_PARAM_NAME)) {
    // not setInput because the base distributions are hidden from the graphical model
    baseDistribution.setParam(paramName, value);
}
```

This means the *n* internal component distributions/functions never appear as
separate nodes, never show up in `getInputs()`/`getOutputs()`, and are
invisible to `GraphicalModelUtils` traversal. The graph stays exactly as big as
the script says — one node per `~`/`=` line, regardless of vector length.

### 4.4 The produced value carries its own hidden sub-structure

Even though the *generator* hides its internals, the *value* it produces does
not throw that information away — it just keeps it off to the side, outside
the `getInputs()`/`getOutputs()` edges:

- `IID.sample()` / `VectorizedDistribution.sample()` return a
  **`VectorizedRandomVariable<T>`** (`extends RandomVariable<T[]>`), which
  privately keeps a `List<RandomVariable<T>>` of the *n* per-element
  `RandomVariable`s (e.g. auto-named `x_0`, `x_1`, …).
- `VectorizedFunction.apply()` returns the deterministic equivalent,
  **`CompoundVectorValue<T>`**, holding a `List<Value<T>>`.
- Both implement `CompoundVector<T>`, exposing `getComponentValue(i)` — a way
  to reach the *i*-th component `Value` node directly, without re-deriving it.

This is what lets `x[3]` be resolved cleanly, lets each component be flagged
`isObserved` independently under data clamping, and lets narrative/UI code
describe "for i in 0..n-1, x[i] ~ Normal(...)" instead of one opaque array.

### 4.5 Why `CompoundVector` matters when wiring nested vectors

When a `VectorizedDistribution`/`VectorizedFunction` slices an array argument
to build its *i*-th component, it prefers the *existing* per-component node
over manufacturing a new one:

```java
if (value instanceof CompoundVector) {
    componentValue = ((CompoundVector) value).getComponentValue(i);   // reuse the real node
} else {
    componentValue = new SliceValue<>(i, value);                      // wrap a plain array value
}
```

This matters when one vectorized generator feeds another (e.g. an `IID` array
of rates feeding a `VectorizedDistribution` of branch lengths): without it,
you'd get an anonymous `SliceValue` disconnected from the upstream
`RandomVariable`'s real identity; with it, the component-level edge is exact.

### 4.6 Data clamping still works, element-by-element

If a vectorized `x` is also declared in `data { }`, `ObservationUtils`
rebuilds a clamped `VectorizedRandomVariable` where **each element** is its own
observed `RandomVariable`, tied to the correct per-component base distribution
(`ObservationUtils.setObservationsToVectorizedRandomVariable`, one overload for
`IID`, one for `VectorizedDistribution`). So clamping composes with
vectorization the same way it does for scalars — just applied component-wise.

---

## 5. Quick Reference

| Concept | File |
|---|---|
| Parse entry point | `parser/LPhyParserAction.java` |
| Tree → objects | `parser/LPhyListenerImpl.java` |
| Generator resolution (incl. IID/vector match order) | `parser/ParserUtils.java` |
| `GraphicalModel` (data/model dictionaries, sinks) | `parser/graphicalmodel/GraphicalModel.java` |
| PGM traversal from sinks | `parser/graphicalmodel/GraphicalModelUtils.java` |
| `Value` / `RandomVariable` / `Generator` | `core/model/Value.java`, `RandomVariable.java`, `Generator.java` |
| `IID` (explicit replicates) | `core/vectorization/IID.java` |
| `VectorizedDistribution` / `VectorizedFunction` (implicit, array args) | `core/vectorization/VectorizedDistribution.java`, `VectorizedFunction.java` |
| Vector-match decision logic | `core/vectorization/VectorMatchUtils.java`, `VectorUtils.java` |
| Produced vector values | `core/vectorization/VectorizedRandomVariable.java`, `CompoundVectorValue.java` |
| Data clamping for vectors | `parser/ObservationUtils.java` |
