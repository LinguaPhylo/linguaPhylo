# lphy-phylospec: design notes

Status: **design only, no code written yet**. This file is the handoff document for the next
session — read this first, it captures the investigation, the architecture decision, and the
concrete next steps.

Companion repo: `../phylospec` (sibling directory to this repo, `linguaPhylo/`). Its `core/java`
module is `phylospec-core` (not yet published to a Maven repo). Install it into the local `.m2`
repo before building `lphy-phylospec` against it:

```bash
cd ~/WorkSpace/phylospec 
mvn -pl core/java -am install -Dmaven.test.skip=true
```

## Goal

Given a PhyloSpec script (parsed via `phylospec-core`), automatically construct the corresponding
LPhy objects (a live `GraphicalModel`), changing existing `lphy`/`lphy-base` code as little as
possible. Tiles (the PhyloSpec-AST-node → engine-object mapping units) should mostly be
**auto-generated**, not hand-written one Java class per generator.

## Prior art found in `../phylospec` (reuse as-is, do not reimplement)

`phylospec-core` already has a full, generic, engine-agnostic **tiling framework** under
`org.phylospec.tiling`:

- `org.phylospec.lexer.Lexer` / `org.phylospec.parser.Parser` → AST (`org.phylospec.ast.Stmt` /
  `Expr`).
- AST transformers: `RemoveGroupings`, `EvaluateLiterals`, `EvaluateScalarFunctions`.
- `VariableResolver`, `TypeResolver` (type-checks against a `ComponentLibrary`, a JSON resource
  matching `../phylospec/schema/component-library.schema.json`, loaded via
  `ComponentResolver.loadCoreComponentLibraries()` from classpath resource
  `/phylospec-core-component-library.json`), `StochasticityResolver`.
- `org.phylospec.tiling.EvaluateTiles<S>`: walks the resolved AST; for each node, asks every
  registered `CandidateTile<S>` (`GeneratorTile<T,S>` matches a single `Expr.Call` by PhyloSpec
  generator name; `TemplateTile<T,S>` matches a multi-node pattern) whether it can cover that node;
  picks the lowest-weight globally-consistent combination across all statements.
- `Tile<T,S>.apply(S state, …)`: builds real engine objects into an accumulator state object `S`
  (for this project, `S` = `LPhyState`, described below). Declares its inputs as
  `GeneratorTileInput<?,S>` fields keyed by **PhyloSpec argument name**; `Tile.getTileInputs()`
  finds these via reflection over `this.getClass().getDeclaredFields()` — but this is a `protected`
  method, **designed to be overridden** (see decision below).
- `TileLibrary<S>`: registers a list of tiles, discovered engine-wide via `ServiceLoader`
  (`META-INF/services/org.phylospec.tiling.TileLibrary`, declared in `module-info.java` as
  `provides org.phylospec.tiling.TileLibrary with ...`, with the tile packages `opens ... to
  org.phylospec.core` so the framework's reflection can reach them).
- `EngineSpecGenerator`: reflects over a `TileLibrary`'s **registered tile instances** (not the
  target engine's native classes) to emit a JSON "engine specification" describing what that engine
  supports, validated against the `ComponentResolver`. This generates *from* tiles; it does not
  generate tiles.
- Hand-written tiles (plain `GeneratorTile<T,S>` subclasses) are the framework's escape hatch for
  generators whose PhyloSpec argument shape doesn't map straight onto the target engine's — needed
  here too, for the LPhy generators listed in "Known mismatches to seed the first hand-written
  override tiles" below.
- **`org.phylospec.converters.LPhyConverter` / `LPhyGeneratorMapping` / `LPhyMethodsMapping`**:
  an existing, **`@Deprecated`** ("not updated to 03-2026"), *separate* approach that converts the
  AST directly into an **LPhy source-code string** via a hardcoded `switch` name/argument mapping
  table, e.g. `Exponential(rate=r)` → `Exp(mean=1/r)`, `Gamma(shape, rate)` →
  `Gamma(shape, scale=1/rate)`. It does not use the tiling framework and isn't type-checked.
  **Treat this as superseded** — the design below supersedes it, and it can eventually be deleted
  from `phylospec-core` once `lphy-phylospec` lands. Its mapping table is still a useful reference
  for which LPhy generators have real parameterization mismatches with PhyloSpec (see "Known
  mismatches to seed hand-written tiles" below).

## Prior art found in `linguaPhylo` (this repo)

- `lphy/src/main/java/lphy/core/model/annotation/GeneratorInfo.java` and
  `.../annotation/ParameterInfo.java`: the two annotations LPhy generators already carry
  (`name()`, `description()`, etc.) — `@GeneratorInfo` on the generator class's execution method
  (`sample()` for a distribution, `apply()` for a deterministic function), `@ParameterInfo` on its
  constructor's parameters. No PhyloSpec-name field on either yet.
- `lphy/src/main/java/lphy/core/parser/ParserUtils.java`:
  `getMatchingFunctions(String name, Map<String, Value> arguments)` — **already public**, already
  the exact mechanism LPhy's own text parser uses to resolve a generator by name + named `Value`
  arguments and instantiate it via reflection. This is the reuse hook that makes auto-generated
  tiles possible for LPhy (see decision below) — no need to reimplement constructor-matching logic.
- `lphy-phylospec/`: no tiling code yet, but **not just a stub any more** — `ComponentLibraryExporter`
  (`src/main/java/lphy/phylospec/export/`) is implemented, checked in, and has been through several
  rounds of correctness fixes since it was first written. It reflects over LPhy core + `lphy-base`
  only — explicitly, by extension class name via `LPhyCoreLoader.getExtensionMap(...)`, not
  whatever else happens to be on the classpath — and writes
  `src/main/resources/phylospec-lphy-component-library.json`.
  **Important, changed from earlier design assumptions here**: the exporter no longer translates
  LPhy Java type names into PhyloSpec vocabulary at all — no `Double` → `Real`, no `Boolean[]` →
  `Vector<Boolean>`, no `Object` → `Any`. It deliberately mirrors
  `lphystudio.app.docgenerator.GenerateDocs`'s own type-naming (`Class::getSimpleName()`, no
  translation, no collapsing) instead, because that translation wasn't accurate to what LPhy
  actually is: LPhy has no `Vector` data type at all — vectorisation
  (`lphy.core.vectorization`: `replicates`, or passing a vector where a scalar is expected) is a
  language *mechanism* that produces a real array value, e.g. `Double[]` — and no `Value` ever
  holds a runtime `Object`, so `"Any"` wasn't a real LPhy type either (see
  `../LPHY1_FRAMEWORK.md` §2). The JSON is now a **literal, direct description of LPhy's own object
  model**, not a PhyloSpec translation of it — which means the tiling framework now has the *whole*
  LPhy→PhyloSpec translation job ahead of it, not a partly-solved one (see "Type-name mapping"
  below, substantially rewritten from earlier assumptions). Several PhyloSpec `Generator`/`Type`
  schema fields (`examples`, `constraints`, `typeParameters`, `typeProperties`) are also
  intentionally omitted rather than emitted as empty arrays — validated as schema-optional, so this
  doesn't break anything (`phylospec-lphy-component-library.json` validates cleanly, 0 errors,
  against `../phylospec/schema/component-library.schema.json`). See `PHYLOSPEC_SCHEMA_GAPS.md` for
  the full, current diff between what this file says and what `phylospec-core-component-library.json`
  populates, with real examples.
  Also still present: `pom.xml` (vendors `phylospec-core` as a system-scoped local jar),
  `schema/types.json`, and a handful of PhyloSpec type-wrapper classes (`PrimitiveType`, `Vector`,
  `BoundedReal`, `BoundedNumber`, `BoundedRealImpl`) under `lphy/phylospec/types/`.

## Key architectural insight

LPhy's object model is structurally close to PhyloSpec's already: every generator is "a class with
named, reflection-discoverable constructor parameters," and `ParserUtils.getMatchingFunctions`
already does name+args → instance resolution. That means **one generic, data-driven tile class,
instantiated once per known generator name at library-build time (not one hand-written Java file
per generator)** covers the large majority of cases. Hand-written override tiles are the exception,
reserved for genuine parameterization mismatches (rate vs. mean, etc.).

This works without any changes to `phylospec-core`: `Tile.getTileInputs()` is `protected`, not
`final`, so a generic tile subclass can override it to build `GeneratorTileInput`s dynamically from
a generator's `ParameterInfo` list (looked up at construction time) instead of relying on declared
Java fields, which is what the base class's reflection-over-fields implementation assumes for
hand-written tiles.

## Type-name mapping (feeds into TypeResolver and tile TypeTokens)

`ComponentLibraryExporter` deliberately does **not** attempt the LPhy→PhyloSpec type-name
translation any more (see "Prior art found in `linguaPhylo`" above) — it emits LPhy's own, literal
type names (`Double`, `Number`, `Boolean[]`, `Object`, ...) with a schema-shaped container around
them, nothing more. That means the tiling framework has the **whole** translation job ahead of it,
not a partly-solved one:

- **The LPhy→PhyloSpec name translation has to happen somewhere** — most naturally inside
  `ReflectiveGeneratorTile`'s `getTypeToken()` (see `CLASS_DESIGN.md`) or a shared helper it calls,
  working directly off the generator's reflected parameter/return `Class`, not off the exported
  JSON's type strings (those are LPhy-native now, not PhyloSpec-native). A reflected `Double[]`
  parameter needs to produce the `TypeToken` for PhyloSpec's `Vector<Real>`; a reflected `Boolean`
  needs `Boolean`; a reflected `Object` (from a genuinely unbound generic like `Sample<T>`'s
  `array` or `IfElse<T>`'s branches) needs whatever PhyloSpec's actual top/any-type equivalent
  turns out to be — open question, since PhyloSpec's own core library has no such type today.
- **PhyloSpec's numeric types form a refinement lattice** (`Real` ⊃ `NonNegativeReal` ⊃
  `PositiveReal`, `Probability`, ...) that LPhy's plain `Double`/`Number` can't express — there is
  no way to reflectively recover "this `Double` is actually a `Probability`" from LPhy alone.
  Whatever builds `TypeToken`s will need to fall back to the coarsest PhyloSpec numeric type
  (`Real`) for every LPhy `Double`/`Number`/`Float`, and `phylospec-core`'s `TypeResolver` (which
  type-checks the AST *before* tiling runs, see pipeline above) will need an explicit rule that a
  PhyloSpec argument typed `Probability`/`NonNegativeReal`/... is assignable to that coarser
  `Real`. This should be a decided compatibility rule in `TypeResolver` (or a documented loosening
  in the tiling framework), not something left to fail type-checking or pass silently by accident.
- **LPhy genuinely has multiple, separate Java types PhyloSpec would consider the same type** —
  `Double` and `Number` (the latter exists purely so a constructor can accept either an int or
  double literal, e.g. `LogNormal`'s `M`) both mean PhyloSpec `Real`. A reverse lookup (PhyloSpec
  name → candidate LPhy Java type, needed when a tile constructs a Java value from a resolved
  PhyloSpec-typed argument) must expect **multiple LPhy Java types per PhyloSpec name** and pick
  one deliberately (e.g. prefer `Double` over `Number` as the concrete instantiation type), not
  assume a 1:1 mapping.
- **LPhy has no `Vector` type** (see "Prior art" above) — so translating *to* PhyloSpec's
  `Vector<T>` convention (`Double[]` → `Vector<Real>`) is a real, necessary step for the tiling
  framework now, and translating *from* it (a PhyloSpec `Vector<Real>` argument → an LPhy
  `Double[]` value) is equally real and necessary going the other direction. Neither direction can
  be skipped by reusing exporter logic any more, since the exporter doesn't do this translation.
- **11 LPhy generators currently under-report their own shape**: `Sample<T>`, `sort`, `unique`,
  `slice`, `rep`, `repArray`, `concatArray`, `elementsAt`, `intersect`, `setUnion`,
  `setDifference` all declare an unbound generic array type (`Value<T[]>`) but LPhy core's
  `GeneratorUtils.getClass(Type)` collapses that to plain `Object.class` instead of
  `Object[].class`, losing the array-ness — so the exported JSON currently shows e.g. `sort`'s
  argument as bare `Object` rather than (the more accurate) "array of Object". Root-caused this
  session; left unfixed, since it requires either a `lphy` core change or a `lphy-phylospec`-local
  workaround (re-deriving array depth from the raw generic type-name string) that wasn't in scope.
  Whatever builds `TypeToken`s for these 11 generators should expect this gap.

See `PHYLOSPEC_SCHEMA_GAPS.md` for the full, current diff between what
`phylospec-lphy-component-library.json` populates and what `phylospec-core-component-library.json`'s
schema supports (e.g. `Type.extends`/`alias`, `Generator.ioHints`, `Argument.default` — none
currently populated on the LPhy side), and `../LPHY1_FRAMEWORK.md` §2 for how LPhy's own
(Java-reflection-based) type system works underneath all of this.

## Decisions made (confirmed with user)

1. **Name fallback**: `@GeneratorInfo.phylospecName()` / `@ParameterInfo.phylospecName()` default
   to `""`, meaning "same as the existing LPhy name." The reflective tile registers every LPhy
   generator under `phylospecName().isEmpty() ? name() : phylospecName()` — auto-fallback, not
   opt-in. Only generators/params whose PhyloSpec name or parameterization differs from LPhy's need
   any annotation edit at all.
   - Safety net: the `LPhyCoreTileLibrary` build/registration step should log or fail on any
     resulting name that collides with a *different* generator already declared in
     `phylospec-core-component-library.json` (i.e. an LPhy name that accidentally matches an
     unrelated PhyloSpec generator with different semantics).
2. **Primary output**: real LPhy objects (a live `GraphicalModel`), not `.lphy` script text.
   `LPhyState` wraps a real `GraphicalModel`; `.lphy` script text and direct execution both fall out
   of this for free via LPhy's existing script printer and sampler — no string-templating layer to
   maintain (this is what makes `LPhyConverter` supersedable).

## Proposed pipeline

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

## Proposed module layout for `lphy-phylospec`

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
      PhyloSpecToLPhyRunner.java     // orchestrates lex -> parse -> tile -> run/print (see Proposed pipeline above)
  src/main/resources/
    phylospec-lphy-component-library.json   // generated, checked in
    META-INF/services/org.phylospec.tiling.TileLibrary
  src/main/java/module-info.java      // provides TileLibrary with LPhyCoreTileLibrary;
                                       // opens tiling(.tiles) to org.phylospec.core
```

## Minimal lphy-side code changes (the actual diff footprint)

1. `lphy/.../annotation/GeneratorInfo.java`: add
   `String phylospecName() default ""; String phylospecNamespace() default "";`
2. `lphy/.../annotation/ParameterInfo.java`: add `String phylospecName() default "";`

Everything else (all new tiling/runner code, the generated component-library JSON, module-info,
service registration) lives inside the new `lphy-phylospec` module. `lphy-base`'s generator classes
themselves need **no changes** unless their PhyloSpec name/params genuinely differ from their LPhy
name/params.

## Known mismatches to seed the first hand-written override tiles

Mined from the deprecated `LPhyGeneratorMapping` (`../phylospec/core/java/.../converters/`) —
these are confirmed real parameterization/name differences, a good starting checklist:

- `Exponential(rate)` (PhyloSpec) vs LPhy `Exp(mean = 1/rate)`
- `Gamma(shape, rate)` vs LPhy `Gamma(shape, scale = 1/rate)`
- `Yule(birthRate, taxa)` vs LPhy `Yule(lambda, taxa)` (name-only rename, may not even need a
  hand-written tile — check if a `phylospecName`/`phylospecName` annotation rename alone suffices)
- `FossilBirthDeath(birthRate, deathRate, rho, samplingRate, taxa)` vs LPhy
  `FossilBirthDeathTree(lambda, mu, rho, psi, taxa)`
- `BirthDeath(birthRate, deathRate, rootHeight, taxa)` vs LPhy `BirthDeath(lambda, mu, rootAge,
  taxa)`
- `Coalescent(populationSize, taxa)` vs LPhy `Coalescent(theta, taxa)`
- Substitution models: `JC69`/`K80`/`F81`/`HKY`/`GTR` (PhyloSpec) vs LPhy `jukesCantor`/`k80`/`f81`/
  `hky`/`gtr` — likely just capitalization/name renames via `phylospecName`, worth checking before
  writing hand tiles.
- `PhyloBM`/`PhyloOU`/`PhyloCTMC`: PhyloSpec `sigma`/`optimum`/`rootValue` vs LPhy
  `diffRate`/`theta`/`y0` — real renames.
- `IID(base, n)`: PhyloSpec wraps a base distribution with a replicate count; LPhy instead adds a
  `replicates=n` argument directly onto the base distribution's own call. This is a structural
  (AST-shape) difference, not just a name/value difference — likely needs a `TemplateTile`, not a
  plain `GeneratorTile`.

Not yet checked against the current `../phylospec` component-library JSON or current lphy-base
class names — this list is from a possibly-stale deprecated converter and should be re-verified
against current names in both repos before implementation.

## Open items for next session (not yet decided / not yet investigated)

- Whether `TypeResolver` needs an explicit "PhyloSpec refined-numeric-type is assignable to LPhy's
  coarser `Real`" compatibility rule, and where it should live (in `phylospec-core`'s `TypeResolver`
  itself, vs. a wrapper/override in `lphy-phylospec`) — see "Type-name mapping" above; not yet
  investigated whether `TypeResolver` already has an extension point for this.

- Whether `../phylospec`'s `phylospec-core` should move from a vendored system-scoped jar here to a
  normal Maven dependency (needs `phylospec-core` published somewhere reachable) before or after
  this module is built.
- Whether to target a specific starter set of example `.phylospec` scripts to validate against —
  propose starting with the models covered by the mismatch list above (Yule/Coalescent/BirthDeath
  trees + HKY/GTR + a couple of continuous-trait models), since those are already known-quantity
  from the deprecated converter.
- Exact shape of `LPhyState` — how closely it should just be a thin wrapper around LPhy's existing
  `GraphicalModel`/sampler-building code vs. a new accumulator type; needs a closer read of
  `lphy.core.parser.graphicalmodel.GraphicalModel` and however `lphy` currently drives sampling, which wasn't
  investigated yet in this session.
- Whether `IID` and other structurally-different generators need `TemplateTile` (multi-node pattern
  match via `AstTemplateMatcher`) — only skimmed `TemplateTile.java`, haven't looked at
  `AstTemplateMatcher` itself yet.
- Whether to fix the `Sample<T>`/`sort`/`unique`/... array-shape-collapses-to-scalar-`Object` gap
  (see "Type-name mapping" above) in `lphy` core (`GeneratorUtils.getClass`) or with a
  `lphy-phylospec`-local workaround — deliberately left open this session, core changes were out of
  scope.
- No CI check exists that the checked-in `phylospec-lphy-component-library.json` is actually
  up to date with `lphy`/`lphy-base` — it's regenerated and committed by hand. Worth a `verify`-phase
  check (regenerate to a temp file, diff against the checked-in one, fail the build on drift) before
  the tiling framework starts depending on it, so a future core/lphy-base change can't silently go
  unreflected in the exported JSON.

## lphy-studio keeps as it is at the moment

No changes to `lphy-studio` are part of this design — it is out of scope for now.

## Next step

Scaffold: the two annotation fields, `ReflectiveGeneratorTile`, `LPhyState`, `LPhyCoreTileLibrary`
with the hand-written tiles from the mismatch list above (re-verified against current names first),
and `PhyloSpecToLPhyRunner`. Validate end-to-end against a small `.phylospec` script exercising a
Yule tree + HKY + PhyloCTMC (a common, well-understood model shape).
