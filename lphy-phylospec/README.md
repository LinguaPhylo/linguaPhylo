# lphy-phylospec

Bridges LPhy and [PhyloSpec](https://github.com/CODEPhylo/phylospec).

**Working today**: `ComponentLibraryExporter` — exports LPhy's own types/generators as a
PhyloSpec-schema-shaped JSON file, and a companion Python pipeline that compares it against
PhyloSpec's own component library to find model-coverage gaps. See
[`LPhyVsPhylospecDesign.md`](LPhyVsPhylospecDesign.md) for that pipeline's design.

**Design only, not yet built**: a tiling framework to run PhyloSpec scripts through LPhy directly.
See "Tiling framework" below and [`CLASS_DESIGN.md`](CLASS_DESIGN.md) for the per-class plan.

## Prerequisites

`phylospec-core` must be installed in your local `.m2` repo first:

```bash
cd ../phylospec
mvn -pl core/java -am install -Dmaven.test.skip=true
```

`lphy`/`lphy-base` must also be installed to `.m2` — this module depends on their built jars, not
the live source tree. If you edit an `@GeneratorInfo`/`@ParameterInfo` annotation (or anything
else) in `lphy` or `lphy-base`, reinstall before regenerating the exported JSON, or the exporter
will silently run against stale compiled classes:

```bash
cd ..   # repo root
mvn -q -pl lphy,lphy-base -am install -Dmaven.test.skip=true
```

## `ComponentLibraryExporter`

`src/main/java/lphy/phylospec/export/ComponentLibraryExporter.java`

Reflects over LPhy core + `lphy-base` (explicitly, by extension class name — not "whatever's on
the classpath") and writes every generator and data type out as a **component library JSON file**,
in the same format as PhyloSpec's own `phylospec-core-component-library.json` (serialized through
`phylospec-core`'s own generated POJOs, so it's structurally guaranteed to match that schema).

It reports LPhy's own literal types (`Double`, `Boolean[]`, `Map`, ...) — it does **not** translate
into PhyloSpec vocabulary (no `Double` → `Real`, no `Boolean[]` → `Vector<Boolean>`). That
translation is the tiling framework's job (see "Type-name mapping" below), not the exporter's.

```bash
cd lphy-phylospec
mvn exec:java                                          # default output path
mvn exec:java -Dexporter.args=/path/to/output.json      # explicit path
```

`src/main/resources/phylospec-lphy-component-library.json` is a **generated artifact** — produced
by the command above, never hand-edited.

For exactly how a type/generator and its namespace are determined, and the model-coverage-gap
comparison pipeline built on top of this JSON, see
**[`LPhyVsPhylospecDesign.md`](LPhyVsPhylospecDesign.md)**.

## Tiling framework (design only)

**Goal**: given a PhyloSpec script (parsed via `phylospec-core`), automatically construct the
corresponding LPhy objects (a live `GraphicalModel`), changing existing `lphy`/`lphy-base` code as
little as possible. Tiles (the PhyloSpec-AST-node → engine-object mapping units) should mostly be
**auto-generated**, not hand-written one Java class per generator.

### Key insight

LPhy's object model is structurally close to PhyloSpec's already: every generator is "a class with
named, reflection-discoverable constructor parameters," and LPhy's existing
`ParserUtils.getMatchingFunctions(name, args)` already does name+args → instance resolution. That
means **one generic, data-driven tile class, instantiated once per known generator name** (not one
hand-written Java file per generator) covers the large majority of cases — hand-written override
tiles are the exception, reserved for genuine parameterization mismatches (e.g. `rate` vs. `mean`).

This reuses `phylospec-core`'s existing, engine-agnostic tiling framework
(`org.phylospec.tiling`) as-is — lexer/parser, AST transforms, `VariableResolver`/`TypeResolver`/
`StochasticityResolver`, `EvaluateTiles`, `TileLibrary` SPI discovery — without any changes to that
module. The deprecated `org.phylospec.converters.LPhyConverter` (AST → LPhy source-code string via
a hardcoded switch table) is superseded by this design, though its mapping table is still a useful
reference for known parameterization mismatches (see below).

### Pipeline

```
.phylospec source
   │  Lexer/Parser, transformers, VariableResolver/TypeResolver, StochasticityResolver   ← phylospec-core, unchanged
   ▼
resolved/typed AST
   │  EvaluateTiles<LPhyState>                                                            ← phylospec-core, unchanged
   ▼
best Tile per statement
   │  Tile.apply(LPhyState, …)                                                            ← NEW, in lphy-phylospec
   ▼
LPhyState  (wraps a real lphy GraphicalModel: name → Value<?>, RandomVariables, …)
   │
   ├─► lphy Sampler → run inference directly
   └─► LPhy's existing script printer → .lphy text        (free; no re-implementation of LPhyConverter)
```

### Proposed module layout

```
lphy-phylospec/
  src/main/java/lphy/phylospec/
    tiling/
      LPhyState.java                 // accumulator: name -> Value<?>, wraps GraphicalModel
      ReflectiveGeneratorTile.java   // generic, data-driven Tile — the auto-generation engine
      LPhyCoreTileLibrary.java       // registers hand-written overrides + fills gaps reflectively
      tiles/                        // small — only genuine PhyloSpec<->LPhy parameterization mismatches
        ExponentialTile.java         // e.g. rate <-> mean = 1/rate
        ...
    runner/
      PhyloSpecToLPhyRunner.java     // orchestrates lex -> parse -> tile -> run/print
  src/main/resources/
    phylospec-lphy-component-library.json   // generated, checked in
    META-INF/services/org.phylospec.tiling.TileLibrary
  src/main/java/module-info.java      // provides TileLibrary with LPhyCoreTileLibrary;
                                       // opens tiling(.tiles) to org.phylospec.core
```

See [`CLASS_DESIGN.md`](CLASS_DESIGN.md) for what each of these classes does and how it plugs into
`phylospec-core`'s tiling framework.

### Minimal lphy-side code changes

1. `lphy/.../annotation/GeneratorInfo.java`: add
   `String phylospecName() default ""; String phylospecNamespace() default "";`
2. `lphy/.../annotation/ParameterInfo.java`: add `String phylospecName() default "";`

Everything else (tiling/runner code, `module-info`, service registration) lives inside this
module. `lphy-base`'s generator classes need no changes unless their PhyloSpec name/params
genuinely differ from LPhy's.

### Type-name mapping (feeds `TypeToken` construction in tiles)

Since the exporter doesn't translate LPhy types into PhyloSpec vocabulary, the tiling framework has
the **whole** translation job — most naturally inside `ReflectiveGeneratorTile`'s `getTypeToken()`
(see `CLASS_DESIGN.md`), working off the generator's reflected parameter/return `Class`, not off
the exported JSON's (LPhy-native) type strings. Three things this needs to handle:

- **PhyloSpec's numeric types form a refinement lattice** (`Real` ⊃ `NonNegativeReal` ⊃
  `PositiveReal`, `Probability`, ...) that LPhy's plain `Double`/`Number` can't express — no way to
  reflectively recover "this `Double` is actually a `Probability`". Whatever builds `TypeToken`s
  will need to fall back to the coarsest type (`Real`) for every LPhy `Double`/`Number`, with
  `TypeResolver` given an explicit rule that a PhyloSpec `Probability`/`NonNegativeReal`/... is
  assignable to that coarser `Real` (not yet decided where that rule should live).
- **LPhy has multiple Java types PhyloSpec considers the same type** — `Double` and `Number` (the
  latter exists purely so a constructor can accept either an int or double literal) both mean
  PhyloSpec `Real`. A reverse lookup (PhyloSpec name → LPhy Java type) must expect multiple LPhy
  types per PhyloSpec name and pick one deliberately (e.g. prefer `Double`), not assume 1:1.
- **LPhy has no `Vector` type** — vectorisation (`lphy.core.vectorization`: `replicates`, or
  passing a vector where a scalar is expected) is a language *mechanism* that produces a real array
  value (`Double[]`), not a distinct data type. Translating `Double[]` → PhyloSpec's `Vector<Real>`
  (and the reverse) is real, necessary work for the tiling framework, in both directions.

### Known mismatches to seed the first hand-written override tiles

Mined from the deprecated `LPhyGeneratorMapping` (`../phylospec/core/java/.../converters/`) — not
yet re-verified against current names in both repos, but a good starting checklist:

- `Exponential(rate)` (PhyloSpec) vs LPhy `Exp(mean = 1/rate)`
- `Gamma(shape, rate)` vs LPhy `Gamma(shape, scale = 1/rate)`
- `Yule(birthRate, taxa)` vs LPhy `Yule(lambda, taxa)` — may be a name-only rename
- `FossilBirthDeath(birthRate, deathRate, rho, samplingRate, taxa)` vs LPhy
  `FossilBirthDeathTree(lambda, mu, rho, psi, taxa)`
- `BirthDeath(birthRate, deathRate, rootHeight, taxa)` vs LPhy `BirthDeath(lambda, mu, rootAge, taxa)`
- `Coalescent(populationSize, taxa)` vs LPhy `Coalescent(theta, taxa)`
- Substitution models: `JC69`/`K80`/`F81`/`HKY`/`GTR` vs LPhy `jukesCantor`/`k80`/`f81`/`hky`/`gtr`
  — likely just name renames, worth checking before writing hand tiles
- `PhyloBM`/`PhyloOU`/`PhyloCTMC`: PhyloSpec `sigma`/`optimum`/`rootValue` vs LPhy
  `diffRate`/`theta`/`y0` — real renames
- `IID(base, n)`: PhyloSpec wraps a base distribution with a replicate count; LPhy instead adds
  `replicates=n` directly onto the base distribution's own call — a structural (AST-shape)
  difference, likely needs a `TemplateTile`, not a plain `GeneratorTile`

### Decisions made (confirmed with user)

1. **Name fallback**: `@GeneratorInfo.phylospecName()` / `@ParameterInfo.phylospecName()` default
   to `""` (same as the existing LPhy name) — auto-fallback, not opt-in. Only generators/params
   whose PhyloSpec name or parameterization differs need an annotation edit. Safety net: fail/log
   on any resulting name that collides with a *different*, unrelated PhyloSpec generator.
2. **Primary output**: real LPhy objects (a live `GraphicalModel`), not `.lphy` script text —
   `.lphy` text and direct execution both fall out of this for free via LPhy's existing script
   printer and sampler.

### Open items (not yet decided / not yet investigated)

- Where the "PhyloSpec refined-numeric-type assignable to LPhy's coarser `Real`" compatibility
  rule should live (in `phylospec-core`'s `TypeResolver` vs. a wrapper in `lphy-phylospec`).
- Whether `../phylospec`'s `phylospec-core` should move from a vendored system-scoped jar to a
  normal Maven dependency before or after this module is built.
- Target starter set of example `.phylospec` scripts to validate against — propose starting with
  the models in the mismatch list above (Yule/Coalescent/BirthDeath trees + HKY/GTR + a couple of
  continuous-trait models).
- Exact shape of `LPhyState` — thin wrapper vs. new accumulator type; needs a closer read of
  `lphy.core.parser.graphicalmodel.GraphicalModel` and LPhy's sampling flow.
- Whether `IID` and other structurally-different generators need `TemplateTile` (multi-node
  pattern match via `AstTemplateMatcher`) — only skimmed, not yet investigated.
- No CI check exists that the checked-in `phylospec-lphy-component-library.json` is actually
  up to date with `lphy`/`lphy-base` — regenerated and committed by hand today. Worth a
  `verify`-phase check (regenerate to a temp file, diff against the checked-in one, fail on drift)
  before the tiling framework starts depending on it.

### Next step

Scaffold: the two annotation fields, `ReflectiveGeneratorTile`, `LPhyState`, `LPhyCoreTileLibrary`
with the hand-written tiles from the mismatch list above (re-verified against current names
first), and `PhyloSpecToLPhyRunner`. Validate end-to-end against a small `.phylospec` script
exercising a Yule tree + HKY + PhyloCTMC (a common, well-understood model shape).

`lphy-studio` is out of scope for this design.
