# lphy-phylospec: class design v2

This document specifies how a PhyloSpec script becomes a live, sample-able, loggable LPhy model.
`phylospec-core`'s tiling framework is reused unmodified ([Design principles](#design-principles)):
each PhyloSpec AST node is mapped to a real LPhy object by a small taxonomy of tiles (§1–§5),
accumulated into `PhyloSpecLPhyDictionary` (§6) — an `LPhyParserDictionary` extending LPhy's own
`REPL` — and driven end to end by `PhyloSpecToLPhyRunner` (§8) via `LPhyCoreTileLibrary` (§7).
Every LPhy-specific decision — type resolution, vectorization, overload matching — is delegated to
LPhy's existing runtime (`ParserUtils`, `Sampler`) rather than reimplemented. On top of
`ParserUtils`, tiles add only a type filter on its matches (§1.6) and the output wiring that real
`.lphy` parsing also performs (§1.4). Generator mismatches between the two libraries are fixed in
LPhy itself, not in tiling code (Decision 3).

## Project structure

The class diagram (`class-diagram2.svg`, reproduced at the end of this document) groups every class
into these regions:

- **`phylospec-core`** — the tiling framework: `Tile`, `GeneratorTile`, `AstNodeTile`,
  `TemplateTile`, `TileInput`, `TileLibrary`, `EvaluateTiles`. Reused without modification.
- **`lphy-phylospec`** — the module this document specifies: the tile classes that map PhyloSpec
  AST shapes onto LPhy objects (§1–§5), the `PhyloSpecLPhyDictionary` accumulator (§6), the
  `LPhyCoreTileLibrary` that registers every tile (§7), and the `PhyloSpecToLPhyRunner`
  orchestrator (§8).
- **`lphy-core`** — LPhy's existing runtime: `GraphicalModel`, `Value`, `RandomVariable`,
  `Generator`, `ParserUtils`, `Sampler`. Reused without modification, apart from the operator
  factory methods that Decision 3 extends (§5).
- **`lphy-base`** — LPhy's generators. Not reused unmodified: Decision 3 moves every generator
  mismatch here, as annotations, new constructors or new generators (§3.2, §7.1).

## Design principles

Three decisions shape everything that follows.

**Decision 1 — delegate type, overload, and vectorization resolution to `ParserUtils`.** LPhy
already provides a complete, tested resolver from "generator name + argument values" to a
constructed object (`ParserUtils.getMatchingGenerativeDistributions`/`getMatchingFunctions` →
`constructGenerator`), which tries a direct constructor match, then `IID.match` (explicit
`replicates=n`), then `VectorMatchUtils.vectorMatch` (implicit vectorization), in that order
(`LPhyFramework.md` §4). Each of these is a runtime, argument-shape-driven decision, not a
static-type one. Rather than have `phylospec-core`'s `TypeToken` machinery encode PhyloSpec's
numeric refinement lattice (`Real ⊃ NonNegativeReal ⊃ PositiveReal, ...`) or the `Vector<T>`/
`Double[]` translation, every tile declares its inputs and its own produced type with one of exactly
two deliberately coarse, raw (unparameterized) Java shapes, and defers every finer-grained decision
to `ParserUtils` at `applyTile()` time, exactly as happens for hand-written `.lphy` scripts:

- `lphy.core.model.Value` — for every value-typed PhyloSpec expression (a literal, a variable, a
  function call, a sampled random variable).
- `lphy.core.model.GenerativeDistribution` — for every `Distribution<T>`-typed PhyloSpec expression,
  i.e. an unsampled distribution call. `GenerativeDistribution` is not a subtype of `Value`, so it
  must be a distinct shape: a `Value`-typed input would reject it at the tiling-level type check.

§3 gives the justification for why this is sound rather than merely convenient.

**Decision 2 — model PhyloSpec statements (`~`, `=`, `observed as`) as their own generator-agnostic
tiles.** A PhyloSpec script is a sequence of statements, not only a set of generator calls. LPhy
distinguishes a `~` draw (produces a `RandomVariable` via `.sample()`) from an `=` assignment
(produces a plain `Value` via `.apply()`) from data clamping (a value that is both drawn and
observed) — `LPhyFramework.md` §2. This design gives each of these its own generator-agnostic tile,
detailed in §2.

**Decision 3 — fix generator mismatches in `lphy-base`, not in tiles.** Hand-written tiles are kept
to a minimum. When a PhyloSpec generator differs from its LPhy counterpart, the difference is resolved
on the LPhy side (`lphy-base`, or `lphy-core` for operators, §5), so that the generic reflective
tiles (§1) cover it with no per-generator tiling code. Each mismatch takes the first step of this
ladder that works:

1. **Annotation.** For a pure rename of a generator or parameter, add
   `@GeneratorInfo(phylospec = ...)` / `@ParameterInfo(phylospec = ...)` to the existing LPhy class.
   Examples: `Yule(lambda)` → `birthRate`, `jukesCantor` → `jc69`.
2. **New LPhy constructor, same generator name, PhyloSpec's parameters.** Use this when PhyloSpec
   parameterizes the same function or distribution differently, e.g. `Exponential(rate)` vs. LPhy
   `Exp(mean)`. Give the LPhy generator a second parameterization that matches PhyloSpec's argument
   for argument, then annotate it as in step 1. Any conversion (`mean = 1/rate`) becomes ordinary
   Java inside the class. §3.2 gives the three ways to add the parameterization to an LPhy class.
3. **New LPhy generator.** Use this when the PhyloSpec generator has a different return type or no
   LPhy counterpart at all (e.g. `mrca → Age`, `gy94`). Add a new LPhy generator class annotated with
   the PhyloSpec name, or declare the generator unsupported (§10).

A tile is hand-written only when the mismatch is in the *shape of the PhyloSpec syntax* rather than
in a generator. Examples are a call nested inside a call (`IID`, §4), an operator (§5), a statement
form (§2), or an LPhy method call (§3.3). Such a tile is generic over every generator, never
specific to one.

This makes the tiled model the same model an LPhy user would write by hand. The PhyloSpec parameter
`Value` stays a direct parameter of the LPhy generator, so resampling, `computeLogPosterior()` and
`.lphy` export (§6.1) follow it with no helper nodes. The exported script reads `Exp(rate=r)`, not
`Exp(mean=1/r)`. LPhy users also gain each new parameterization.

## Workflow

```
.phylospec source
  │  Lexer / Parser, AST transforms, VariableResolver / TypeResolver / StochasticityResolver
  ▼                                                                    (phylospec-core, unchanged)
type-checked, stochasticity-annotated AST
  │  EvaluateTiles<PhyloSpecLPhyDictionary>.getBestTiling(...)
  ▼                                                                    (phylospec-core, unchanged)
one Tile per statement, drawn from LPhyCoreTileLibrary
  │  applyBestTiling(new PhyloSpecLPhyDictionary(name))
  ▼                                                                          (lphy-phylospec — §1–§7)
PhyloSpecLPhyDictionary
  │  Sampler / SimulatorListener, CanonicalCodeBuilder
  ▼                                                                       (lphy-core, unchanged)
simulated values, logged output, and/or exported .lphy text
```

The PhyloSpec AST is parsed and type-checked entirely by `phylospec-core`; nothing in
`lphy-phylospec` touches that stage. Tiling then converts the AST into LPhy objects in a single
pass. Applying a tile constructs the real LPhy `Value` or `Generator` for the AST node it covers,
through `ParserUtils` (Decision 1). A statement-level tile also registers the named result in
`PhyloSpecLPhyDictionary`'s data or model dictionary (§2).

There is no separate graph-assembly step. As with a hand-written `.lphy` script, the graph is never
stored as an explicit structure: each `Value` references the `Generator` that produced it, and each
`Generator` holds its parameter `Value`s (`LPhyFramework.md` §3). The graph exists as soon as a
tile constructs an object, and is recovered by traversal when locating sinks, computing the log
posterior, or printing a script (§6.1). So once `applyBestTiling` returns, `PhyloSpecLPhyDictionary`
already *is* the runnable model.

## 1. Generator-call tiles

**In short: generator calls are tiled automatically, by name.** No tile is written per generator.
LPhy's `@GeneratorInfo` and `@ParameterInfo` annotations say which PhyloSpec names each LPhy class
and parameter answer to:

1. **Generator name.** Each LPhy class is grouped under its LPhy name, `@GeneratorInfo.name()`.
   Only when that differs from PhyloSpec's name does the class set `@GeneratorInfo.phylospec()`,
   which then replaces it. One tile per group claims every PhyloSpec call with that name.
2. **Argument names.** The tile's inputs are named by `@ParameterInfo.name()`, replaced by
   `@ParameterInfo.phylospec()` only where that is set for a differing name (§1.1). The tile keeps
   a reverse map back to the LPhy names.
3. **Construction.** The tile renames the call's arguments to LPhy names and lets `ParserUtils`
   pick the matching constructor, as for a hand-written `.lphy` call (§1.2–§1.6).

Names must match exactly; there is no fuzzy matching. A rename therefore needs only an annotation
(Decision 3, step 1), though none are set in `lphy-base` yet (§11). Differences that names cannot
express need a new LPhy constructor (§3.2) or, for syntax shapes, their own tile (§3.3–§5).

Generator calls are covered by two small, near-identical `GeneratorTile<T, PhyloSpecLPhyDictionary>`
subclasses in `tiling/`, `ReflectiveDistributionTile` and `ReflectiveFunctionTile`, split along
LPhy's own kind distinction. There is one *instance* per PhyloSpec generator name, not one
hand-written Java class per generator. Each instance groups every LPhy class whose `@GeneratorInfo`
maps to that PhyloSpec name. LPhy itself already registers several classes under one generator name,
e.g. `BirthDeathTree` and `BirthDeathTreeDT` are both `"BirthDeath"`
(`LoaderManager.getAllGenerativeDistributionClasses(name)` returns a `Set<Class<?>>`). Occasionally
two different LPhy names map to one PhyloSpec name, e.g. `Coalescent` and `CoalescentPopFunc` would
both be annotated `phylospec = "Coalescent"`. The instance then holds both LPhy names (§1.6).

| | `ReflectiveDistributionTile` | `ReflectiveFunctionTile` |
|---|---|---|
| Backs | `GenerativeDistribution` classes | `BasicFunction`/`DeterministicFunction` classes |
| `T` | `GenerativeDistribution<?>` — the **unsampled** distribution object, matching PhyloSpec's own `Distribution<T>` call-expression type | `Value<?>` — the fully realized value, since `=`-shaped calls (`jc69(...)`) are ordinary value expressions usable anywhere, including nested inside another call's arguments, with no statement wrapper needed |
| `getPhyloSpecGeneratorName()` | the LPhy name (`@GeneratorInfo.name()`, via `GeneratorUtils.getGeneratorName`), unless `@GeneratorInfo.phylospec()` is set for a differing PhyloSpec name. Both fields are already present on `GeneratorInfo`/`ParameterInfo` in `lphy-core` | same |
| `getTileInputs()` | overridden (§1.1) | overridden (§1.1) |
| `applyTile(state, ...)` | §1.6 steps 1–2 via `ParserUtils.getMatchingGenerativeDistributions`; returns the `GenerativeDistribution` without sampling it | §1.6 steps 1–2 via `ParserUtils.getMatchingFunctions`; returns `generator.generate()` — a `Value<T>` — directly |

Splitting by kind, rather than using one branchy tile, mirrors LPhy's own clean split and keeps
each `T` precise enough that `getTypeToken()` needs no per-instance override (§3).

### 1.1 Building tile inputs from LPhy constructors

`Tile.getTileInputs()` normally works by field reflection: it walks
`this.getClass().getDeclaredFields()` for `TileInput`-typed fields and, for each, calls the
package-private `TileInput.resolveTypeFromField(Field)` to pull `T` out of the field's own generic
signature (`TileInput.java:36`). A reflective tile has no such fields — its parameter list is only
known once the wrapped LPhy generator class is reflected — so `getTileInputs()` is overridden to
build `GeneratorTileInput`s from `GeneratorUtils.getParameterInfo(constructor)` instead.

`resolveTypeFromField` is package-private to `org.phylospec.tiling.tiles`. `Tile`'s own
(unoverridden) call to it works regardless of who subclasses `Tile`, because the calling code lives
in that package — but an override living in `lphy.phylospec.tiling` cannot call it directly:
protected and package-private access does not extend to a subclass in a different package for a
package-private member. A dynamically-built `GeneratorTileInput` therefore needs its `TypeToken`
set another way. `TileInput.getTypeToken()` is a normal `public` method
(`return tile != null ? tile.getTypeToken() : this.typeToken;`), and public methods are overridable
across packages:

```java
class ReflectiveTileInput extends GeneratorTile.GeneratorTileInput<Value<?>, PhyloSpecLPhyDictionary> {
    ReflectiveTileInput(String phylospecArgName, boolean required) {
        super(phylospecArgName, required); // required = false, always — see §1.2
    }
    @Override public TypeToken<?> getTypeToken() {
        return getTile() != null ? getTile().getTypeToken() : TypeToken.of(Value.class);
    }
}
```

One `ReflectiveTileInput` is declared per distinct parameter name across all public constructors of
every grouped class. The name used is the parameter's LPhy `@ParameterInfo.name()`, unless
`@ParameterInfo.phylospec()` is set because PhyloSpec's name differs. A name appearing in only some
constructors is still declared once.
The tile also records a reverse map, PhyloSpec argument name → LPhy parameter name, which
`buildLPhyCall` (§1.6) uses. It keeps one such map per LPhy generator name in the group, because the
same PhyloSpec argument may legitimately map to different LPhy parameters in different LPhy
generators. For example, `populationSize` is `theta` on `Coalescent` but `popFunc` on
`CoalescentPopFunc`.

Within one LPhy generator name, the map must be a function. A PhyloSpec name must map to the same
LPhy name in every constructor and class registered under that LPhy name, since `ParserUtils`
resolves all of them from one argument map. `LPhyCoreTileLibrary` checks this when it builds the
tile and fails loudly on a conflict (§7, item 3). Different constructors may use *different*
PhyloSpec names; §1.2 explains how this tells overloads apart.

The same holds one level up, for the generator name. `ParserUtils` looks generators up by LPhy name
and returns every class registered under it: `"Coalescent"` returns both `Coalescent` and
`SerialCoalescent`, and `"BirthDeath"` returns four classes. So every class under one LPhy name must
resolve to the same PhyloSpec generator name. If only `SerialCoalescent` set `phylospec = "X"`, the
tile for `X` would query `ParserUtils("Coalescent")` and could construct `Coalescent` instead.
`LPhyCoreTileLibrary` checks this too, and fails loudly on a conflict (§7, item 3).

Three details of how the names are read:

- **Where `@GeneratorInfo` sits.** It annotates a class's generating method (`sample()` or
  `apply()`), not the class (`@Target(METHOD)`). `GeneratorUtils.getGeneratorInfo(Class)` finds it
  by scanning the methods. A class without one is named by its simple class name
  (`GeneratorUtils.getGeneratorName`).
- **Aliases are ignored.** `@GeneratorInfo.aliases()` lists deprecated LPhy names (e.g.
  `BirthDeathSampling`). `LPhyCoreImpl` registers them so that old `.lphy` scripts still resolve,
  but tiles use only `name()` or `phylospec()`, so no PhyloSpec name is matched to a deprecated
  LPhy name.
- **One naming rule, shared with the exporter.** `ComponentLibraryExporter` already applies the
  same rule when it writes `phylospec-lphy-component-library.json` (generators at
  `ComponentLibraryExporter.java:347`, parameters at `:404`). Both should call one shared helper,
  e.g. `PhyloSpecNames.of(Class)` and `PhyloSpecNames.of(ParameterInfo)`, so the coverage report and
  the tiles cannot disagree on a name.

`ReflectiveTileInput` always expects `Value`, because every parameter of an LPhy generator
constructor is a `Value<?>`. An input that expects a *distribution* is never reflective, since no
LPhy generator constructor takes a `GenerativeDistribution` parameter. Examples are `IID`'s `base`
(§4) and, if they are ever supported, `Truncated`, `Offset`, `RelaxedClock` or `Mixture` (§10).
Such inputs are declared only on hand-written combinator tiles, as a `GeneratorTileInput` whose
`getTypeToken()` returns `TypeToken.of(GenerativeDistribution.class)` (Decision 1). Declaring them as
`Value` would make `TileInput.getCompatibleInputTiles` reject every `ReflectiveDistributionTile` at
that argument (`FailedTilingAttempt.RejectedBoundary`), so no combinator would ever tile.

### 1.2 Covering all constructor overloads with one tile

A generator such as `Yule` has multiple constructors (with and without `rootAge` —
`ComponentLibraryExporter` already emits one `Generator` entry per constructor for this reason).
`GeneratorTile.tryToTile()` needs one fixed `List<TileInput>` to build `expectedInputParameters`
and call `call.resolveArgumentNames(...)`, so the declared set must be the union of every
overload's parameter names, each marked optional. This does not weaken correctness: PhyloSpec's own
`TypeResolver.visitCall` (`parser-and-json.md` §2) has already verified, before tiling runs, that
the call matches some overload in the component libraries given to its `ComponentResolver`, and
`ParserUtils.getGeneratorByArguments` — which iterates every constructor, calls `match(...)`, and
throws `RuntimeException("Required argument ... not found!")` for a genuinely incomplete call —
still enforces per-overload required-ness, exactly as it does for hand-written `.lphy` parsing. The
one construct this does not fit is `MapFunction(ArgumentValue... )`'s varargs-name-as-argument
pattern, which has no fixed parameter name to declare; this is a known, documented limitation, not
a silent mishandling — PhyloSpec has no dynamic-map-literal construct to map onto it in any case.

**One tile per PhyloSpec name covers all of its overloads.** Under Decision 3 every PhyloSpec
overload corresponds to an LPhy constructor in the group, so no other tile ever claims the same
PhyloSpec generator name. Overloads are told apart in two ways:

- **By argument name.** Different constructors may use different PhyloSpec names, and `ParserUtils`
  matches by LPhy parameter name. For example, `Exponential(rate=r)` maps to `{rate: r}`, and only
  the `rate` parameterization of `Exp` accepts it (§3.2).
- **By argument type,** for overloads that share names. This is §1.6's type filter.

`GeneratorTile.tryToTile` rejects any argument name the tile does not declare
(`ArgumentResolutionError.UnknownName` → `FailedTilingAttempt.Rejected`). So a PhyloSpec overload
whose argument names have no LPhy constructor yet fails at tiling time, naming the argument, rather
than at apply time.

### 1.3 Disambiguating multiple matches

`getMatchingGenerativeDistributions`/`getMatchingFunctions` return a `List<Generator>` — more than
one entry when more than one constructor overload matches the given arguments. This is rare after
§1.6's type filter, since argument names and types usually identify one constructor. Real `.lphy` parsing does not treat
this as fatal: `LPhyListenerImpl` (`LPhyListenerImpl.java:566-580`, `930-945`) logs a warning
("Found N matches for ... Picking first one!") and takes `matches.get(0)`. `applyTile` follows the
same policy, so a tiled model and a hand-written script resolve an identical ambiguous call to the
same generator.

### 1.4 Wiring outputs after construction

`ParserUtils.constructGenerator` only calls `constructor.newInstance(initargs)`; it does not wire
outputs. Real `.lphy` parsing performs one further step after picking a match, which
`ParserUtils` itself does not perform: `LPhyListenerImpl` loops over every resolved argument and
calls `generator.setInput(argName, value)`, with the comment *"must be done so that Values all know
their outputs."* `Generator.setInput` both re-sets the parameter (redundant with what the
constructor already did) and calls `value.addOutput(generator)` — the only place
`Value.getOutputs()` is ever populated.

Every `applyTile` in this document that constructs a generator via `ParserUtils` —
`ReflectiveDistributionTile`/`ReflectiveFunctionTile` (§1) and `IIDTile` (§4) — must perform this
same wiring loop over its resolved argument map before returning. §6.1 analyzes the practical
consequence of omitting this step.

### 1.5 Prerequisite: turning literals into LPhy constants

PhyloSpec literal expressions (a bare `0.0`, `10`, `"HKY"`, `true` appearing as a generator
argument) require their own tile. `LPhyFramework.md` §2 records that real `.lphy` parsing turns
exactly these into anonymous LPhy constants (`DoubleValue`, `IntegerValue`, `StringValue`,
`BooleanValue`) as its first step, before any generator resolution happens. Every
`ReflectiveDistributionTile` input in `LogNormal(logMean=0.0, logSd=1.0)` needs one of these already
tiled at the literal AST node before `tryToTile`'s per-input type check (§3) runs; without it,
`TileInput.getCompatibleInputTiles` finds zero candidates at that node
(`FailedTilingAttempt.RejectedCascade`) and nothing in the script tiles at all. This is a
precondition for the rest of this design, not an optional addition.

`LiteralTile extends AstNodeTile<Value<?>, Expr.Literal, PhyloSpecLPhyDictionary>` matches any
literal node and constructs the corresponding anonymous (`id = null`) LPhy constant `Value` from
its own literal type, mirroring `LPhyListenerImpl`'s literal handling. It is registered in
`LPhyCoreTileLibrary` alongside the structural tiles (§7), since, like them, it has nothing to do
with any specific generator name.

### 1.6 Resolving a PhyloSpec call to an LPhy generator

A reflective tile builds its LPhy object in two steps:

1. Resolve the tile's inputs and rename each PhyloSpec argument to its LPhy parameter name, using
   the per-LPhy-name reverse maps from §1.1. The result is one argument map per LPhy generator name
   in the group. This step is a pure rename. Decision 3 means no tile ever transforms, packs or
   reorders arguments.
2. Query `ParserUtils` once per LPhy generator name with that name's argument map, then concatenate
   the matches. Drop any match whose constructor parameter type does not accept the supplied value,
   using `argument.type.isAssignableFrom(value.value().getClass())` — the check
   `IID.match` and `VectorMatchUtils` already apply. Then pick the match (§1.3) and wire outputs
   (§1.4).

The type filter is needed because `ParserUtils.match` compares argument *names* only. Without it,
`Coalescent(populationSize=popFunc)` would match both `Coalescent(theta=popFunc)` and
`CoalescentPopFunc(popFunc=popFunc)` by name, and §1.3's pick-first rule could build `Coalescent`
with a `PopulationFunction` as `theta`. With the filter, a PhyloSpec overload that differs only by
type is resolved automatically, as long as the LPhy parameters declare their types precisely
(`Value<Number>` vs. `Value<PopulationFunction>`). The filter applies only to direct constructor
matches. A match that is an `IID`, `VectorizedDistribution` or `VectorizedFunction`
(`lphy.core.vectorization`) is kept as is. Those wrappers exist precisely because an argument is an
array where the parameter is a scalar, and `IID.match`/`VectorMatchUtils` have already type-checked
them against the array's component type.

Step 1 is exposed as its own method, so `IIDTile` (§4) can reuse a wrapped tile's step 1 and then
perform step 2 itself, with `replicates` added:

```java
// one entry per LPhy generator name in the group: LPhy name -> renamed argument map
record LPhyCall(Map<String, Map<String, Value>> argumentsByLPhyName) {}

interface LPhyCallBuilder {
    // resolves and renames this tile's own inputs; constructs nothing via ParserUtils
    LPhyCall buildLPhyCall(PhyloSpecLPhyDictionary state);
}
```

`ReflectiveDistributionTile` and `ReflectiveFunctionTile` implement it. Each one's `applyTile` is
`buildLPhyCall(state)` followed by step 2.

## 2. Statement-level tiles (`tiling/structural/`)

These match PhyloSpec statement shapes, independent of which generator appears in them — the
counterpart of `phylospec-beast3`'s `DrawTile`/`AssignmentTile`/`ObservedAsTile` (`tiling.md` §1).

### 2.1 Draws (`~`): `DrawTile`

`DrawTile extends AstNodeTile<Value<?>, Stmt.Draw, PhyloSpecLPhyDictionary>` matches any `~`
statement. One input: the right-hand-side distribution expression, declared with
expected type `TypeToken.of(GenerativeDistribution.class)` (raw — the same permissiveness as §1).
`applyTile` applies the input to obtain the `GenerativeDistribution`, calls `.generate()` to sample
a `RandomVariable<T>`, sets its id to the variable name, and registers it via
`state.put(id, value, Context.model)`. Its `getTypeToken()` is raw `Value`, like every other
value-producing tile (Decision 1). `tiling.md`'s `DrawTile` example recovers a concrete type via
`TypeToken.firstConcreteTypeArg(...)`, but there is nothing concrete to recover here, because the
input is a raw `GenerativeDistribution`. It is given `TilePriority.LOW` so that `ObservedAsTile`
wins on a `~` statement that is also observed.

### 2.2 Assignments (`=`): `AssignmentTile`

`AssignmentTile extends AstNodeTile<Value<?>, Stmt.Assignment, PhyloSpecLPhyDictionary>` matches
any `=` statement. One input: the right-hand-side value expression, already a `Value<?>`
from whichever tile covers it (`ReflectiveFunctionTile`, `OperatorTile`, `MathFunctionTile`, `MethodCallTile`,
`LiteralTile`, or a variable reference). `applyTile` sets the value's id to the variable name and
registers it via `state.put(id, value, Context.model)`. Like `DrawTile`, it is given
`TilePriority.LOW`, as a generic fallback that any more specific multi-statement template tile
should beat.

### 2.3 Observed data (`observed as`): `ObservedAsTile`

`ObservedAsTile extends TemplateTile<RandomVariable<?>, PhyloSpecLPhyDictionary>` matches the
template `"Any x ~ $distribution observed as $observation"` — the PhyloSpec-level idiom
`phylospec-beast3`'s own `ObservedAsTile` matches (the template describes PhyloSpec syntax, not
BEAST3; each engine writes its own tile against it). `applyTile` applies `$distribution` to obtain
the `GenerativeDistribution`, applies `$observation` to obtain the observed `Value`, and builds the
clamped `RandomVariable` the same way `ObservationUtils`'s scalar/vector overloads already do
(`new RandomVariable(id, observedValue.value(), distribution); rv.setObserved(true)`). It then
registers this random variable in both directions — `state.put(id, rv, Context.model)` and
`state.put(id, observedValue, Context.data)` — which is what makes `GraphicalModel.isObserved(id)`
and `computeLogPosterior()` work unmodified (`LPhyFramework.md` §3), since both read the data/model
dictionary pair directly. Because the template spans both the `~` statement and the `observed as`
statement, `EvaluateTiles`'s "variable reference jumps to its definition" mechanism (`tiling.md`
§3) marks the `~` statement as `consumedStatements`, so `DrawTile` never independently fires
for it — no special case is required beyond `DrawTile`'s `LOW` priority.

Indexed statements, `observed between` and string templates have no statement tile yet; §10
lists them with the fix for each.

## 3. Why coarse type checks are enough

`TypeToken.isAssignableFrom` (`TypeToken.java:124`) special-cases a raw `Class` target: if the
expected type is unparameterized (`Value.class`, not `Value<Double>`), it accepts any `Value<X>` —
the raw-vs-parameterized branch in `isAssignable` falls through to
`targetClass.isAssignableFrom(sourceClass-or-erased-raw-type)`. Declaring every input and output
with one of Decision 1's two raw shapes makes the tiling-level type check a no-op among LPhy
shapes: any LPhy value satisfies any value slot, and any LPhy distribution satisfies any
distribution slot.

This is safe specifically because of where LPhy sits relative to `phylospec-beast3`:

- BEAST3 needs `TypeToken` precision because one PhyloSpec type can map to several different
  concrete Java shapes it must choose between (`RealScalarParam` vs. a derived statistic vs. a raw
  double, `BoundDistribution<Tree, YuleModel>` vs. others) — `TypeToken` performs real
  disambiguation there (`tiling.md` §1–§2).
- LPhy has exactly one Java shape family for everything a generator can produce or consume:
  `Value<?>` (and `RandomVariable<?> extends Value<?>`). There is nothing to disambiguate at the
  tiling layer — the decision of whether and how a call actually constructs is a single call into
  `ParserUtils`, which already resolves it correctly, including the numeric-refinement and
  `Vector<T>`/`Double[]` translation `README.md`'s "Type-name mapping" section anticipated as
  substantial work, because that resolution never depended on PhyloSpec vocabulary in the first
  place — it is the same resolution ordinary `.lphy` text goes through.
- Both engines rely on the same precondition: the AST is already well-typed at the PhyloSpec level
  before tiling runs (`parser-and-json.md` §2, `tiling.md` §4). A well-typed script cannot hand a
  `Tree`-shaped argument to a `Real`-shaped parameter slot, so the tiling layer does not need to
  catch that itself.

**Accepted trade-off:** this forfeits some of `EvaluateTiles`' precise failure-cascade diagnostics
(`tiling.md` §3). A `RejectedBoundary` at a specific argument fires only for a value-vs-distribution
mismatch, which `TypeResolver` already excludes. A genuine mismatch (for example, no overload of a
generator matches once vectorization is tried too, or §1.6's type filter leaves no match) instead
surfaces later, as a `RuntimeException` from `ParserUtils` or `applyTile`, wrapped by `Tile.apply()`'s existing
catch-all into a `TileApplicationError` attached to the correct root AST node
(`Tile.java:156-171`) — less deep than BEAST3's cascade-DAG walk, but still attributed to a node
rather than a bare top-level failure.

### 3.1 Validation against the LPhy/PhyloSpec coverage report

`model_coverage_gap.md` (the generated LPhy/PhyloSpec comparison, current as of the PhyloSpec core
component library `1.4.0` / LPhy component library `0.1.0`, exported from LPhy `1.8.1-SNAPSHOT`)
provides concrete validation of the argument above. Its "In both" tables show
the coarse-`TypeToken` design handling the large majority of real cases without any translation
code, and narrow down exactly what remains:

- **Numeric refinement lattice.** Every one of PhyloSpec's `Real`/`NonNegativeReal`/`PositiveReal`/
  `Probability`/`Rate`/`Age` maps to LPhy's single `Double`; `Integer`/`NonNegativeInteger`/
  `PositiveInteger`/`Count` all map to LPhy's single `Integer` (the report's §2 "In both" types
  table). Every argument in every "In both" generator row bears this out — for example,
  `Yule(birthRate: Rate)` vs. LPhy `Yule(lambda: Number)`. No tile needs to know this lattice exists.
- **`Vector<T>`/`Matrix<T>` collapse.** PhyloSpec's one generic `Vector<T>` corresponds to seven
  distinct LPhy array classes (`Double[]`, `Integer[]`, `Boolean[]`, `Number[]`, `Object[]`,
  `Variant[]`, `TimeTree[]`); `Matrix<T>`/`SquareMatrix`/`QMatrix`/`StochasticMatrix` all collapse
  to LPhy's plain `Double[][]`. Raw `Value.class` swallows all of these identically, so no
  translation table is needed on the LPhy side.
- **PhyloSpec's dependent-type annotations carry no Java shape.** Signatures such as
  `Tree<;numTaxa=taxa.num>`, `Simplex<;num=concentration.num>`, and
  `PositiveInteger<;value=alignment.numSites>` constrain a value (a computed relationship between
  arguments), not a Java class; nothing in this design's tiles needs to model them. `TypeResolver`
  has already checked these constraints before tiling runs.
- **Pure renames account for most of the "different but no tile needed" cases** — for example
  `Coalescent(theta→populationSize)` or `jukesCantor→jc69` — fixable with
  `@GeneratorInfo.phylospec()`/`@ParameterInfo.phylospec()` annotations alone, not a tile
  (Decision 3, step 1). The full list is in §7.1.

What remains after removing all of the above is a small, enumerable set of parameterization
differences. Under Decision 3 these are fixed in `lphy-base` rather than in tiles (§3.2). Among
generator-call mismatches, the only kind that still needs a tile is the method-call shape (§3.3).

### 3.2 Fixing parameterization differences in `lphy-base`

Some PhyloSpec generators express the same quantity differently from their LPhy counterpart:

| PhyloSpec | LPhy today | Difference | Way (below) | `lphy-base` change |
|---|---|---|---|---|
| `Exponential(rate)` | `Exp(mean)` | `mean = 1/rate` | B | `Exp(mean \| rate)`, mutually exclusive |
| `Gamma(shape, rate)` | `Gamma(shape, scale)` | `scale = 1/rate` | B | `Gamma(shape, scale \| rate)`, mutually exclusive |
| `LogNormal(mean, logSd)` (2nd overload) | `LogNormal(meanlog, sdlog, offset)` | `mean` is the real-space mean: `meanlog = log(mean) − logSd²/2` | A | new constructor `LogNormal(mean, sdlog)` (`sdlog` annotated `logSd`); or declare unsupported |
| `gtr(rateAC, ..., rateGT, baseFrequencies)` (1st overload) | `gtr(rates: Double[], freq, meanRate)` | six named scalars instead of one array | A | new constructor `gtr(rateAC, ..., rateGT, freq, meanRate)` |

None of these needs a tile. Each LPhy generator gains PhyloSpec's parameterization, annotated with
the PhyloSpec names, and the reflective tile (§1) covers it like any rename. There are three ways
to add the parameterization, chosen by one Java constraint: **two constructors of the same class
cannot have the same erasure.** `Exp(Value<Number> mean)` and `Exp(Value<Number> rate)` would not
compile.

| Way | When | Cases |
|---|---|---|
| **A. New constructor** in the existing class | the new parameter list has a different erasure: a different arity or different parameter types | `LogNormal(mean, sdlog)` (2 params vs. 3); `gtr(rateAC, ..., rateGT, freq, meanRate)` (8 params vs. 3) |
| **B. Mutually exclusive optional parameters** in the existing constructor; the constructor checks that exactly one is given | same arity and types, so A would clash | `Exp(mean \| rate)`, `Gamma(shape, scale \| rate)` |
| **C. Sibling class registered under the same `@GeneratorInfo` name** | the parameterization is large enough that B would make one class unreadable | the LPhy `BirthDeath` precedent (`BirthDeathTree` / `BirthDeathTreeDT`) |

`Exp.java` already contains B in commented-out form (`//this.rate = rate;` and "Only one of mean and
rate can be specified."). A sketch:

```java
public Exp(@ParameterInfo(name = meanParamName, optional = true, ...) Value<Number> mean,
           @ParameterInfo(name = rateParamName, phylospec = "rate", optional = true, ...) Value<Number> rate) {
    if ((mean == null) == (rate == null))
        throw new IllegalArgumentException("Exactly one of mean and rate must be specified.");
    this.mean = mean;
    this.rate = rate;
    constructDistribution(random);
}

public double getMean() { return mean != null ? ValueUtils.doubleValue(mean) : 1.0 / ValueUtils.doubleValue(rate); }

@Override public Map<String, Value> getParams() {   // only the parameterization actually given
    return mean != null ? Map.of(meanParamName, mean) : Map.of(rateParamName, rate);
}
// setParam(...) accepts both names; sample()/density() read getMean()
```

The class must keep four things consistent, whichever way is used. These are what make the
tiling-built model behave exactly like a hand-written one:

1. **`getParams()` returns only the parameters actually given.** `CanonicalCodeBuilder` (§6.1) and
   `Generator.codeString()` render from it, so an exported script reads `Exp(rate=r)`.
2. **`setParam` accepts every parameter name.** Resampling and §1.4's output wiring call it with
   the name the generator was built with.
3. **Conversions are evaluated lazily** from the stored `Value`s, as in `getMean()` above, not
   copied once in the constructor. Then resampling an upstream random variable is picked up.
4. **Mutually exclusive parameters are validated in the constructor.** `ParserUtils` treats every
   optional parameter independently, so it accepts a call with both or with neither.

§7.1 lists these together with every other `lphy-base` change.

### 3.3 PhyloSpec functions that are LPhy method calls

PhyloSpec has no dot-call syntax, but some ordinary PhyloSpec function calls are the same as an LPhy
method call, with the receiver passed as a normal argument. This is a difference in the *shape of
the call*, not in a generator: the LPhy side is an instance method, not a generator class, so
Decision 3 has no generator to annotate or extend. It is therefore covered by one generic,
data-driven tile, `MethodCallTile`. The cases are `curated_equivalences.json`'s
`methodCallEquivalents`, built for `compare_component_libraries.py` (`LPhyVsPhylospecDesign.md`):

| PhyloSpec | LPhy method | `MethodCallTile`? |
|---|---|---|
| `rootAge(tree)` | `tree.rootAge()` | yes |
| `numBranches(tree)` | `tree.branchCount()` | yes |
| `age(node: String, tree)`, `age(taxon)` | `TimeTreeNode.getAge()` | no. The receiver is a node name, or a `Taxon` (no such LPhy type), not a `TimeTreeNode`. Moved to Decision 3, step 3 |
| `num(vector)` | `Taxa.length()` | no. Only a partial match, since PhyloSpec's `num` is generic. Instead annotate LPhy's generic `length(Object)` function with `phylospec = "num"` (step 1) |

Entries marked "no" are removed from `methodCallEquivalents` for tiling purposes.

`LPhyCoreTileLibrary` constructs one `MethodCallTile` instance per entry. A PhyloSpec name must
not be claimed by both a `MethodCallTile` and a reflective tile; §7 item 5's collision check also
covers this. `MethodCall`
(`lphy.core.parser.function.MethodCall`) is itself a `DeterministicFunction`, constructed directly
rather than resolved through `ParserUtils`. Its constructor
(`MethodCall(String methodName, Value<?> value, Value<?>[] arguments)`) performs the same reflective
method lookup used for real `.lphy` `receiver.method(args)` syntax. It throws a checked
`NoSuchMethodException` if no `@MethodInfo`-annotated method matches:

```java
class MethodCallTile extends GeneratorTile<Value<?>, PhyloSpecLPhyDictionary> {
    // built from one curated_equivalences.json methodCallEquivalents entry:
    // phylospecName, the LPhy method name, and which PhyloSpec argument is the receiver
    applyTile(...) {
        Value<?> receiver = /* resolved receiver-argument input */;
        Value<?>[] args   = /* remaining resolved inputs, in order */;
        try {
            return new MethodCall(lphyMethodName, receiver, args).apply();
        } catch (NoSuchMethodException e) {
            throw new WrappedTileApplicationError(this.getRootNode(), "No matching @MethodInfo "
                    + "method '" + lphyMethodName + "' on " + receiver.value().getClass(), e);
        }
    }
}
```

An entry qualifies only when the PhyloSpec arguments are exactly the receiver plus the method's own
arguments, in order. `methodCallEquivalents` must gain a `receiver` field naming that argument. Any
entry needing more than that is moved to Decision 3, step 3: a new LPhy generator annotated with the
PhyloSpec name. That includes argument adaptation, chaining, or a different return type — for
example `age(node: String, tree)`, which would need `tree.getLabeledNode(node).getAge()`, or
`mrca(clade, tree) → Age` versus LPhy's `mrca(tree, taxa) → TimeTreeNode`. No hand-written chaining
tile is added. `numSites`, `numTaxa` and `species` are *function* pairs, not method calls; §7.1
lists their fixes.

## 4. Mapping `IID` to LPhy `replicates`

PhyloSpec's `IID(baseDistribution, n)` wraps one call inside another; LPhy's equivalent instead
adds `replicates=n` as one more argument to the base distribution's own call (`LPhyFramework.md`
§4.1) — an AST-shape difference, not a naming or algebra difference, so no
`ReflectiveDistributionTile` can cover it (its inputs are keyed by its own generator's parameter
names and never look inside a nested call). Decision 3 cannot remove this mismatch on the LPhy
side either. LPhy's language has no way to pass a distribution as an argument value, so no LPhy
generator `IID(base, n)` can exist. This is the kind of syntax-shape mismatch that Decision 3 leaves
to a generic tile.

This is resolved with a single hand-written tile, not one per wrapped distribution:

```java
class IIDTile extends GeneratorTile<GenerativeDistribution<?>, PhyloSpecLPhyDictionary> {
    // getTypeToken() -> TypeToken.of(GenerativeDistribution.class)   (§1.1, Decision 1)
    GeneratorTileInput<GenerativeDistribution<?>, PhyloSpecLPhyDictionary> base = ...;
    GeneratorTileInput<Value<?>, PhyloSpecLPhyDictionary> num = ...;

    applyTile(state, ...) {
        if (!(base.getTile() instanceof LPhyCallBuilder inner))
            throw new WrappedTileApplicationError(getRootNode(), "IID base has no LPhy equivalent");
        LPhyCall call = inner.buildLPhyCall(state);              // §1.6, step 1 of the inner tile
        Value<?> n = num.apply(state, ...);
        // add replicates to every per-LPhy-name argument map
        call.argumentsByLPhyName().values().forEach(args -> args.put(IID.REPLICATES_PARAM_NAME, n));
        // §1.6 step 2: ParserUtils.getMatchingGenerativeDistributions per LPhy name, type filter,
        // pick the match (§1.3), wire outputs (§1.4); return the unsampled distribution
    }
}
```

`IIDTile` matches an `Expr.Call` named `IID`. Its `base` input is an ordinary tiled input, typed
`GenerativeDistribution` (§1.1), so whichever distribution tile the tiler picks for the inner call
is reused. Normally that is a `ReflectiveDistributionTile`; the exceptions are rejected (second
consequence below). `IIDTile` does not `apply` that inner tile, since that would construct the
distribution once without `replicates`. Instead it asks the inner tile for its LPhy call
(`buildLPhyCall`), adds `replicates`, and constructs the vectorized distribution itself.

Two consequences:

- **The inner call's renames are kept.** `IID(Exponential(rate=r), 10)` becomes
  `Exp(rate=r, replicates=10)` (with `Exp`'s `rate` parameterization, §3.2), and
  `IID(Yule(birthRate=b, ...), n)` becomes `Yule(lambda=b, ..., replicates=n)`. Re-resolving the
  inner `Expr.Call`'s raw PhyloSpec arguments directly against `ParserUtils` would pass `birthRate`
  to `Yule` and fail to match. Because §1.6's step 1 is a pure rename, `IID.match` sees exactly the
  argument map a hand-written `.lphy` call with `replicates` would produce.
- **A base distribution that is not built from an LPhy call is rejected with a clear error.** Its
  tile does not implement `LPhyCallBuilder`; examples include a nested `IID`, or a future combinator
  such as `Truncated`. LPhy's `IID` (`lphy.core.vectorization.IID`) is constructed from a
  `Constructor` plus `initArgs` and cannot wrap an already-built distribution object, so there is no
  LPhy equivalent to fall back on.

## 5. Operator and expression calls

LPhy's roughly 30 unary math functions and 17 binary operators (`sqrt`, `+`, `<=`, ...) are not
classes; they are `public static Function`/`BiFunction` factory methods on `ExpressionNode1Arg`/
`ExpressionNode2Args`, dispatched today by a hardcoded `switch` in `LPhyListenerImpl`
(`LPhyVsPhylospecDesign.md`, Rule 1(4)). `ComponentLibraryExporter` already reflects these out
generically (`buildExpressionOperatorGenerators`), using one hand-maintained name map,
`EXPRESSION_OPERATOR_SCRIPT_NAMES`, for the symbol-bound half. Tiling reuses that same map in
reverse, with one generic tile per PhyloSpec AST node kind rather than 48 tiles:

- **`OperatorTile`** — an `AstNodeTile` over `Expr.Binary` and `Expr.Unary`. PhyloSpec operators
  are grammar, not calls (`model_coverage_gap.md`'s §4), so a name-keyed `GeneratorTile` would never
  match them. It maps the operator symbol through the reversed map to the factory method.
- **`MathFunctionTile`** — a `GeneratorTile` over `Expr.Call`, one instance per named math function
  (`sqrt`, `exp`, `log`, ...). It is generated from the same reflection, so it is not hand-written
  per function.

Both build `new ExpressionNode1Arg(exprText, factory(), arg)` or its two-argument equivalent,
mirroring what `LPhyListenerImpl` does by hand today. `Expr.Grouping` (parentheses) needs no tile:
`EvaluateTiles.visitGrouping` already passes straight through to the inner expression.

Decision 3 applies to operators too. When a PhyloSpec operator does more than LPhy's, the fix goes
into `lphy-core`'s factory method, not into `OperatorTile`. PhyloSpec's `+` also concatenates
strings, while `ExpressionNode2Args.plus()` is numeric-only (`BiFunction<Number, Number, Number>`).
Generalizing `plus()` to concatenate when either operand is a `String` keeps `OperatorTile`
generic, and also gives `.lphy` scripts string `+`. The same applies to PhyloSpec's two-argument
`log(x, base)` against LPhy's one-argument `log`. LPhy's surplus operators (`%`, `**`, `&`, `&&`,
`|`, `||`) need no handling, since nothing in PhyloSpec can emit them.

## 6. The model dictionary: reusing LPhy's `REPL`

`PhyloSpecLPhyDictionary` is the `LPhyParserDictionary` that tiling fills. `LPhyParserDictionary`
is an interface, but `lphy-core` already ships a concrete, tested implementation of it,
`lphy.core.parser.REPL`. `REPL` provides the data/model dictionaries
(`SortedMap<String, Value<?>>`), the data/model value sets, `getName()`/`setName(String)`,
`getGeneratorClasses()`, `getLines()` and `clear()`. So `PhyloSpecLPhyDictionary` extends `REPL`
instead of reimplementing the interface, and this is the entire class:

```java
public class PhyloSpecLPhyDictionary extends REPL {
    public PhyloSpecLPhyDictionary(String name) {
        super();
        setName(name);
    }
}
```

Extending `REPL` gives the rest of LPhy's runtime without any change to `lphy-core`:

- **Sampling and logging.** `Sampler` (`lphy.core.simulator.Sampler`) takes an
  `LPhyParserDictionary`, so `new Sampler(dictionary)` works as is. `sampler.sample(seed)` and
  `sampler.sampleAll(numReplicates, loggers, seed)` draw values and hand them to the existing
  `SimulatorListener`s (`FileLoggerListener`, `ValueFileLoggerListener`,
  `RandomNumberLoggerListener`). No new logging code is needed.
- **Resampling without a text round-trip.** `Sampler.sample()` branches on a process-global flag,
  `LPhyParserDictionary.Utils.isSampleValuesUsingParser()`. When it is `true` (the default),
  `Sampler` re-serializes the model with `CanonicalCodeBuilder` and re-parses it with `parse(String)`
  on every sample. That works on a tiled dictionary: `REPL.parse(String)` is deliberately not
  overridden, and §6.1 shows the serialized text is valid `.lphy`. But it is wasted work, so the
  runner sets the flag to `false` (§8, step 4). `Sampler` then takes the `resampleFromDictionary`
  path, resampling the object graph in place through each `Value`'s `getGenerator()`. Because the
  flag is shared by the whole JVM, the runner restores its previous value afterwards, in case it
  shares the JVM with `lphy-studio` or another embedder. Keeping the real `parse(String)` means
  that forgetting the flag makes sampling slower, not broken.
- **Model queries.** `getAllValuesFromSinks`, `getDataModelSinks` and `computeLogPosterior`, all
  default methods on `GraphicalModel`, work unchanged, because the statement tiles (§2) fill the
  same two dictionaries, in the same way, as real `.lphy` parsing does.

### 6.1 Exporting a tiled dictionary as `.lphy` text

`CanonicalCodeBuilder.getCode(LPhyParserDictionary parser)` (`lphy-core`,
`codebuilder/CanonicalCodeBuilder.java`) is the existing script printer this design reuses without
modification. The following traces exactly what `getCode()` depends on, to establish that it works
on a tiling-built `PhyloSpecLPhyDictionary`, and what depends on §1.4's output-wiring step:

1. It starts from `parser.getDataModelSinks()`, the default method covered in §6.
2. For each sink, `ValueCreator.traverseGraphicalModel(value, visitor, post=true)` walks backward,
   using only two things: `value.getGenerator()` — the `Generator` that produced this `Value`, set
   automatically the moment a generator is constructed (for example `Normal.sample()` returns
   `new RandomVariable<>("x", x, this)`, and a deterministic function wraps its result via
   `ValueCreator.createValue(result, this)`) — and `generator.getParams()`, a plain map the
   generator's own constructor already populated from its stored fields (for example `Normal`'s
   `this.mean`/`this.sd`). Neither of these depends on `Value.getOutputs()`/`addOutput` or on how
   the object was constructed: a `Normal` instance built by `ReflectiveDistributionTile.applyTile()`
   via `ParserUtils.getMatchingGenerativeDistributions(...)` is, to this traversal, identical to one
   built by `LPhyListenerImpl` parsing real `.lphy` text.
3. `parser.isNamedDataValue(value)` — checking `!(value instanceof RandomVariable)` and that the
   value is keyed in `getDataDictionary()` — buckets each line into the printed `data { }` /
   `model { }` blocks, matching the split `ObservedAsTile` (§2) produces by writing the same id
   into both dictionaries. `Value.codeString()`/`Generator.codeString()` render each line from the
   object's own fields; again nothing here is tiling-specific.
4. `getDataModelSinks()` is the only step affected by §1.4's output wiring, and even then the
   printed text stays correct. Without the wiring, `Value.getOutputs()` is empty for every value,
   so every named value looks like a sink and `getCode()` starts one traversal per named value
   instead of one per true sink. Its `visited` set de-duplicates the values, so the later
   traversals add nothing. `computeLogPosterior()` is protected the same way, by
   `GraphicalModelUtils.getAllValues`'s `values.contains(node)` check. So omitting §1.4 would not
   corrupt a printed script or double-count a log-density, but `getDataModelSinks()` would be
   misleading. §1.4 is still required, so that tiled models do not depend on this incidental
   de-duplication.

A tiled `PhyloSpecLPhyDictionary` can therefore be exported to `.lphy` text via
`new CanonicalCodeBuilder().getCode(dictionary)`, usable for debugging, export, or as input to a
second, independent `.lphy` parse/run as a consistency check.

## 7. Registering the tiles

`LPhyCoreTileLibrary` (`tiling/LPhyCoreTileLibrary.java`) returns, from `getTiles()`, in order:

1. `LiteralTile` (§1.5) and the structural tiles (§2): `DrawTile`, `AssignmentTile`,
   `ObservedAsTile`.
2. `IIDTile` (§4), `OperatorTile` and the `MathFunctionTile` instances (§5).
3. The reflective sweep. Enumerate every `GenerativeDistribution`/`BasicFunction` class via
   `LPhyExtension.getDistributions()`/`getFunctions()`. This is the same source
   `ComponentLibraryExporter` already reflects; the generated JSON is not used, to avoid depending
   on that artifact being fresh. Then:
   - group the classes by PhyloSpec generator name: the LPhy `@GeneratorInfo.name()`, unless
     `@GeneratorInfo.phylospec()` is set for a differing name. Use the helper shared with
     `ComponentLibraryExporter`, and ignore `aliases()` (§1.1);
   - build one `ReflectiveDistributionTile`/`ReflectiveFunctionTile` per group (§1);
   - check that every class under one LPhy generator name resolves to the same PhyloSpec generator
     name, and fail loudly if not (§1.1);
   - check that, within each LPhy generator name, the PhyloSpec→LPhy parameter-name map is a
     function (§1.1), and fail loudly if it is not.
   
   Every LPhy generator is covered here. No other tile claims a PhyloSpec name that a reflective
   tile claims (Decision 3, §1.2; checked in item 5).
4. `MethodCallTile` instances (§3.3), one per qualifying `curated_equivalences.json`
   `methodCallEquivalents` entry, read at library-construction time — data-driven, not
   hand-enumerated.
5. Apply a name-collision safety net. Fail or log if an auto-derived PhyloSpec name collides with a
   different generator already declared in `phylospec-core-component-library.json`, or if two tiles
   in this library claim the same PhyloSpec name (e.g. a `MethodCallTile` and a reflective tile).

There is no per-generator tile bucket: the library sees generator mismatches only through the
annotations and constructors listed in §7.1.

### 7.1 `lphy-base` changes required

The generator-level mismatches in `model_coverage_gap.md`'s "In both" tables are fixed in
`lphy-base` (Decision 3):

| Generator | Decision 3 step | `lphy-base` change |
|---|---|---|
| `Yule`, `Coalescent`, `FossilizedBirthDeath`, `jc69`, `DiscreteGamma`, `f81`/`hky`/`k80`/`jtt`/`lg`/`wag` (`freq`→`baseFrequencies`), `Binomial`, `Categorical`, `Cauchy`, `Dirichlet`, `Poisson`, `BirthDeath`, `ExpMarkovChain`, population functions, ... | 1 — annotation | add `phylospec = ...` to the generator and its renamed parameters (§11) |
| `Exponential`, `Gamma`, `LogNormal` (2nd overload), `gtr` (6-rate overload) | 2 — ways A/B | see §3.2's table |
| `Coalescent(populationSize: PopulationFunction)` | 1 — annotation | annotate `CoalescentPopFunc` with `phylospec = "Coalescent"` (grouped with `Coalescent`, §1.6) |
| `gtr(relativeRates, baseFrequencies)` (2nd overload) | 1 — annotation | annotate `rates`→`relativeRates`, `freq`→`baseFrequencies` on `gtr`; optionally also group `generalTimeReversible` under `phylospec = "gtr"` for the n-state case |
| `numTaxa(tree \| alignment)`, `taxa(tree \| alignment)`, `species(taxon)`, `numSites(alignment)` | 2 — way A | new constructors on `ntaxa`/`taxa`/`species`/`nchar` taking PhyloSpec's argument types, where the existing parameter type does not already accept them |
| `mrca(clade, tree) → Age`, `age(node, tree)` | 3 — new generator | e.g. new functions annotated `phylospec = "mrca"`/`"age"` returning the age; or unsupported |
| `num(vector)` | 1 — annotation | annotate the generic `length(Object)` function `phylospec = "num"` (§3.3) |
| `lewisMK`/`mk` | open | PhyloSpec's `mk` infers `numStates` from context; preferred fix is LPhy-side (make `numStates` optional, resolved by `PhyloCTMC`) — §10 |
| `PhyloBrownian`/`PhyloOU` vs. `PhyloBM`/`PhyloOU` | 2 — way A or C, once designed | PhyloSpec uses per-site/per-branch rate vectors; LPhy is scalar-parameterized. A new vector-parameterized constructor or class, left as follow-up work |

This table supersedes `README.md`'s original mismatch checklist, which conflated renames with
algebra and missed the `gtr`, `LogNormal` and `lewisMK` cases.

## 8. Running a PhyloSpec script end to end

`PhyloSpecToLPhyRunner` (`runner/PhyloSpecToLPhyRunner.java`) executes the [Workflow](#workflow)
pipeline end to end:

1. Parse and type-check the `.phylospec` source with `phylospec-core`.
2. `new EvaluateTiles<>(TileLibrary.loadAll(...), variableResolver, ...).getBestTiling(...)`,
   which picks up `LPhyCoreTileLibrary` via SPI (§9).
3. Seed LPhy's random number generator (`RandomUtils`), then
   `applyBestTiling(new PhyloSpecLPhyDictionary(name))` (§11, seeding).
4. Save the current value of `LPhyParserDictionary.Utils.isSampleValuesUsingParser()`, set it to
   `false`, and restore it afterward (§6).
5. `new Sampler(dictionary)`, then `sample(seed)` / `sampleAll(n, loggers, seed)` to simulate and
   log values, and/or `new CanonicalCodeBuilder().getCode(dictionary)` to export `.lphy` (§6.1).

## 9. Module and service registration

`LPhyCoreTileLibrary` is registered as a Java SPI provider:
`META-INF/services/org.phylospec.tiling.TileLibrary` names it, and `module-info.java` declares
`provides org.phylospec.tiling.TileLibrary with LPhyCoreTileLibrary`.

`module-info.java` also `opens` the tiling package(s) to `org.phylospec.core`, so the framework's
field reflection (`Tile.getTileInputs()` for hand-written tiles, `GeneratorTile.toString()`) can
reach declared fields across the module boundary. The reflective tiles do not need this, since they build inputs without declared fields (§1.1). The
few hand-written, generator-agnostic tiles do: `IIDTile`'s `base`/`num` fields (§4) and the
structural tiles' inputs (§2).

## 10. Not yet covered

Cross-checking every table in `model_coverage_gap.md` (PhyloSpec core component library `1.4.0` /
LPhy component library `0.1.0`, exported from LPhy `1.8.1-SNAPSHOT`) against §1–§7 leaves the
following PhyloSpec constructs not yet covered. Each row names the fix that Decision 3 points to.
"LPhy-side" means a change in `lphy-base`/`lphy-core` (annotation, new constructor, or new
generator), not a tile. A tile is proposed only for syntax shapes.

| Report entry | What is missing |
|---|---|
| **`methodCallEquivalents` data** | `curated_equivalences.json` needs a `receiver` field per entry, and the non-qualifying entries removed (§3.3). |
| **Argument/return-type differences** | Planned in §7.1 (steps 2–3). Not yet in that table: `fromCSV`→`Vector<Map>` vs. `readDelim`→`Table`, and `fromTree`→one tree vs. `readTrees`→`TimeTree[]`. These need new generators annotated with the PhyloSpec name (step 3). No adapter tile. |
| **Combinators over distributions** | `Mixture(components: Vector<Distribution<T>>, weights)` takes distributions, which LPhy's `MixturePhyloCTMC(comp1: Alignment, ...)` does not. Like `IID`, it is a syntax-shape mismatch, so it would need a generic combinator tile; it stays open, together with `Truncated`, `Offset`, `RelaxedClock` and `StrictClock` (next rows). |
| **Data loading via `Parser`** | `fromFasta`/`fromNexus(age: Parser, speciesName: Parser)`, `discreteTraitsFromTaxa(taxa, Parser)` and `parse(...)` have no LPhy equivalent: LPhy uses an `options` map or `extractTrait(sep, i)`. LPhy-side: a `Parser` type plus a `parse(...)` function, and new `readFasta`/`readNexus`/`extractTrait` constructors taking `Parser` arguments (steps 2–3). Almost every real model loads data this way. |
| **PhyloSpec defaults** | `samplingProbability = 1`, `mk(rate = 1)`, `delimiter = ","` and similar. LPhy-side: make the matching LPhy parameter `optional` with the same default inside the class, so a call that omits it resolves unchanged. |
| **Context-inferred arguments** | `mk` needs `numStates` (§7.1). `PhyloCTMC` needs `L`/`dataType`, and `StrictClock`/`RelaxedClock` need the branch count. Preferred fix is LPhy-side, e.g. making `lewisMK.numStates` optional and resolving it in `PhyloCTMC`, rather than a tile that reaches across statements. |
| **The 18 PhyloSpec-only generators** | Close LPhy counterparts exist for: `DiscreteGammaInv` ≈ `bSiteRates`; `RelaxedClock(LogNormal)` ≈ `UCLN_Mean1`; `StrictClock` ≈ `rep(clockRate, nBranches)` (but a *distribution* in PhyloSpec); `Offset` (only the `offset` argument of `LogNormal`/`Poisson`); `linspace` ≈ `arange`; `subset` ≈ `copySites`/`charset`. Genuine LPhy gaps: `MultivariateNormal`, `Truncated`, `gy94`, `compoundPopulationFunction`, `taxon`, `name`, `env`, `numRows`/`numCols`, `continuousTraitsFromTaxa`. |
| **Math & logic** | `+` on strings and `log(x, base)`: LPhy-side, generalize `ExpressionNode2Args.plus()` and add a two-argument `log` (§5). `range` → `rangeInt`; `repeat` → `rep`: annotation. Unary `-x`, `:` (range), `@` decorators and `$` templates are syntax shapes, not addressed yet. |
| **Expression forms** | Vector literals (`Expr.Array`), index access (`Expr.Index`), ranges (`Expr.Range`, `1:n`) and inline draws (`Expr.DrawnArgument`, e.g. `f(kappa ~ LogNormal(0, 1))`) have no tile, yet they are common arguments. These are syntax shapes, so generic tiles are justified. The first three map onto existing LPhy generators: `ArrayFunction` subclasses such as `DoubleArray`, `ElementsAt`/`Slice`, and `rangeInt`. An inline draw has no LPhy syntax equivalent; a tile can create the drawn `RandomVariable` and register it in the model dictionary, exactly as a separate `~` statement would. |
| **Indexed statements** | `x[i] = ... for i in ...` is PhyloSpec's way of vectorizing, so it is common rather than an edge case. LPhy has no loop; it vectorizes through `replicates=n` or array arguments (`LPhyFramework.md` §4), and `phylospec-beast3` needed dedicated `IndexedTile`/`VectorTile`/`Repeat*Tile`s for the same reason (`tiling.md` §1). A common shape can be mapped: `x[i] ~ D(a[i]) for i in 1:n` becomes LPhy's vectorized `x ~ D(a)`, via a `VectorizeTile`. |
| **`observed between`** | `Stmt.ObservedBetween` (interval/censored observation) is visited by `EvaluateTiles.visitObservedBetweenStmt` but has no tile. It needs a `TemplateTile` like `ObservedAsTile` (§2.3) that clamps a `RandomVariable` to a range rather than a single value. |
| **String templates** | `Expr.StringTemplate` is unsupported by `EvaluateTiles` itself (`visitStringTemplate` throws `UnsupportedOperationException`), so no engine can tile it. A framework limitation, not an `lphy-phylospec` one. |

## 11. TODO

- **Rename data: the `@GeneratorInfo.phylospec()`/`@ParameterInfo.phylospec()` annotations are
  not populated yet.** Matching names need no annotation, since `name()` is used directly. Every
  name that differs depends on these annotations: §1's grouping and `getPhyloSpecGeneratorName()`,
  §1.1's reverse map, and the annotation rows of §7.1's table. Yet there are currently no
  `phylospec =` attributes anywhere in `lphy-base` (they were removed from `HKY.java`). §7.1's
  first row lists the known renames; it must be completed parameter by parameter against the
  report's In-both tables. Add a test asserting that every PhyloSpec overload in
  `phylospec-core-component-library.json` that LPhy claims to support resolves, by name or
  annotation, to exactly one LPhy constructor. The same check can run in
  `compare_component_libraries.py`, so the coverage report and the tile library cannot drift apart.
- **`lphy-base` changes from Decision 3, step 2** (§3.2, §7.1): `Exp(rate)`, `Gamma(rate)`,
  the `gtr` six-rate constructor, and optionally `LogNormal(mean, sdlog)`. Each needs unit tests for
  the four consistency rules in §3.2: `getParams`, `setParam`, lazy conversion, and the
  mutual-exclusion check.
- **Literal numeric types.** `LiteralTile` (§1.5) turns `kappa=2` into an
  `IntegerValue`, and vector literals have the same question (`Integer[]` vs. `Double[]`). LPhy
  generators handle this in their Java classes by calling `.doubleValue()` on `Number`-typed
  parameters. Verify that this covers every slot declared as `Value<Double>` rather than
  `Value<Number>` (e.g. `k80.kappa`, `ExpMarkovChain.initialMean`, `PhyloBrownian.diffRate`,
  `PhyloOU`'s parameters). If not, use the PhyloSpec static type, which `TypeResolver` already knows,
  to produce a `DoubleValue` for `Real`-typed literals.
- **Seeding.** `DrawTile` samples during `applyTile`, before the `Sampler` seed is set. Seed LPhy's
  RNG (`RandomUtils`) before `applyBestTiling` (§8, step 3) so the initial values can be reproduced.
  Confirm which `RandomUtils` call does this.

## Class diagram

See [`class-diagram2.svg`](class-diagram2.svg); open it in a browser for a zoomable view. It is
hand-written SVG, edited as text with no build step. Gray boxes are unchanged upstream code
(`phylospec-core`, `lphy-core`); teal boxes are new; the amber box holds `MethodCallTile` (§3.3)
and the `lphy-base` changes that Decision 3 moves out of tiling (§3.2, §7.1).

No tile in this design is specific to one generator. Every tile is either generated
(`ReflectiveDistributionTile`/`ReflectiveFunctionTile`, `MethodCallTile` instances), generic over all
generators (`LiteralTile`, the structural tiles, `IIDTile`, `OperatorTile`, `MathFunctionTile`
instances), or reused as-is.

![Class diagram](class-diagram2.svg)

## Appendix A: changes from v1

This document is a companion to `README.md` and supersedes `CLASS_DESIGN.md` (v1). It was derived
directly from `phylospec-core`'s tiling classes (`Tile`, `GeneratorTile`, `TileInput`, `TypeToken`,
`CandidateTile`) and LPhy's runtime classes (`ParserUtils`, `Sampler`, `GraphicalModel`,
`LPhyParserDictionary`), not from prose description alone. The table below summarizes how it
differs from v1.

| # | v1 (`CLASS_DESIGN.md`) | v2 (this document) | Rationale |
|---|---|---|---|
| 1 | Only generator-call tiling (`ReflectiveGeneratorTile`) | Adds a statement-level layer: `DrawTile`, `AssignmentTile`, `ObservedAsTile` (§2) | v1 had no way to turn a tiled distribution or value into a named, dictionary-resident `RandomVariable`/`Value`, or to clamp data — the `~`/`=`/`observed as` semantics were unaddressed. `phylospec-beast3` requires the same layer (`DrawTile`/`AssignmentTile`/`ObservedAsTile`). |
| 2 | One `ReflectiveGeneratorTile` class handling everything | Split into `ReflectiveDistributionTile` (produces `GenerativeDistribution<?>`, unsampled) and `ReflectiveFunctionTile` (produces `Value<?>`) | PhyloSpec distinguishes `Distribution<T>`-typed calls (the right-hand side of `~`, or a combinator argument such as `IID`'s `base`) from plain value-typed calls (usable anywhere, including nested). Two tiles match LPhy's own `GenerativeDistribution`/`DeterministicFunction` split. |
| 3 | Stated that `getTypeToken()` "must be supplied explicitly ... via `TypeToken.of(...)`", without addressing field reflection | Identifies the actual constraint (`resolveTypeFromField` is package-private) and the fix (override the public `TileInput.getTypeToken()` in a custom `TileInput` subclass) | The field-reflection route implied previously does not compile from a different package. |
| 4 | Treated PhyloSpec's numeric refinement lattice and `Vector<T>`/`Double[]` translation as necessary work feeding `TypeToken` construction | Argues this translation is unnecessary for this engine, and why (§3) — two raw `TypeToken` shapes, `Value` and `GenerativeDistribution` (Decision 1), deferring all resolution to `ParserUtils` | `TypeToken.isAssignableFrom`'s raw-`Class` branch, combined with LPhy having exactly one Java value family (unlike BEAST3, which needs `TypeToken` for real disambiguation), means the translation does not need to exist. |
| 5 | Left open whether `IID`/vectorization needs a `TemplateTile` | Splits the two cases: implicit vectorization needs no additional tile (falls out of §3's permissiveness and `ParserUtils`'s existing `VectorMatchUtils` fallback); explicit `IID(dist, n)` needs exactly one hand-written `IIDTile` (§4), not a `TemplateTile` and not one tile per distribution | Distinguishing "same AST shape, different runtime argument" from "different AST shape" resolves the open item. |
| 6 | Operator calls (`sqrt`, `+`, ...) not addressed | One `OperatorTile` (for `Expr.Binary`/`Expr.Unary`) plus generated `MathFunctionTile`s, reusing `ComponentLibraryExporter`'s existing `EXPRESSION_OPERATOR_SCRIPT_NAMES` map in reverse (§5) | Closes a gap `LPhyVsPhylospecDesign.md` had already identified on the export side. |
| 7 | Left the shape of the accumulator ("thin wrapper vs. new accumulator type") undecided | `PhyloSpecLPhyDictionary extends REPL` — LPhy's own concrete `LPhyParserDictionary` implementation — adding only a one-argument constructor (§6), wired to `Sampler` and its logger listeners | Reuses `REPL`'s already-tested dictionary/value-set bookkeeping outright instead of reimplementing `LPhyParserDictionary` from scratch, and makes "parse a PhyloSpec script, simulate values, and log the simulated values" work with no new sampling or logging code. |
| 8 | Presented the mismatch list (`Exponential`, `Yule`, `JC69`, ...) as one undifferentiated hand-written-tile bucket | Decision 3: every generator mismatch is fixed in `lphy-base`. Renames use `@GeneratorInfo.phylospec()`/`@ParameterInfo.phylospec()` annotations; reparameterizations use a new constructor or parameterization under the same LPhy name (§3.2); different return types use a new generator. No per-generator tile exists | Those annotation fields already exist in `lphy-core`. A conversion written in the LPhy class keeps the PhyloSpec parameter as a direct graph parameter, so resampling, the posterior and `.lphy` export need no helper nodes, and LPhy users gain the parameterization too. |
| 9 | Class diagram covered generator-call classes only | `class-diagram2.svg` covers all three regions: the `phylospec-core` framework, the full tile taxonomy (§1–§5), and `lphy-core`'s runtime | Keeps the sampling/logging half and the mismatch-tile taxonomy in the same diagram. |
| 10 | Literal handling not addressed | `LiteralTile` (§1.5) converts PhyloSpec literal expressions into anonymous LPhy constants | A prerequisite for any generator call with a literal argument to tile at all; without it, no script with a numeric or string literal argument would tile. |
| 11 | Output wiring not addressed | `applyTile` must additionally call `generator.setInput(name, value)` for each resolved argument (§1.4), matching a step `LPhyListenerImpl` performs after calling `ParserUtils` | Required so `Value.getOutputs()` is populated; without it, `GraphicalModel.getDataModelSinks()` cannot distinguish true sink values from intermediate ones, though §6.1 shows this does not corrupt script export or posterior computation. |
| 12 | `.lphy` export was asserted as "free" without analysis | The export path is traced end to end against `CanonicalCodeBuilder`/`ValueCreator`/`LPhyListenerImpl` (§6.1) | Establishes precisely which dependencies are satisfied by tiling-built objects and which require §1.4. |
| 13 | Ambiguity handling not addressed | Ambiguous multi-constructor matches resolve to the first match with a warning (§1.3), mirroring `LPhyListenerImpl` | Keeps tiled models consistent with how identical ambiguous calls resolve in hand-written `.lphy` scripts. |
| 14 | One reflective tile per LPhy generator name | One reflective tile per *PhyloSpec* name, grouping every LPhy class or name annotated with it (§1, §1.6) | Lets PhyloSpec overloads that LPhy splits across classes (`Coalescent`/`CoalescentPopFunc`) resolve by annotation alone. |

Everything not listed above — SPI registration and `module-info.java` — carries over from v1
unchanged. v1's principle that hand-written tiles are the exception is kept and made concrete by
Decision 3.
