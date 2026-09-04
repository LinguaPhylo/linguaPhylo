# LPhy vs PhyloSpec: model coverage gap pipeline — design

Status: **implemented, working pipeline**. This is the design/handoff doc for that pipeline
specifically — not the broader tiling-framework project (`README.md` covers that; this doc is
narrower in scope and doesn't depend on it).

## Goal

Design a pipeline that analyses the model-coverage and data-type gap between LPhy and PhyloSpec —
which types and generators exist on each side, which match, which don't — and renders it as a
human-readable report (`model_coverage_gap.md`).

## Top design rules

### Rule 1 — how to determine an LPhy type (and its namespace)

> The correct code should find the return type T inside `Value<T>` returned by `apply()` for all
> `DeterministicFunction<T>`, see `MapFunction`. And T of `RandomVariable<T>` returned by
> `sample()` for all `GenerativeDistribution<T>`, see `lphy.base.distribution.Normal`. Also
> correct all wrong namespace, e.g. `"lphy.types"`, to use the java package of the
> `DeterministicFunction` or `GenerativeDistribution` implementing it, and a type can be referred
> to by its own java package as well.

An LPhy **type** is the set of distinct return types across every exported generator — nothing
else. Not constructor-parameter types (those describe inputs, not what LPhy *produces*), not a
hand-maintained list. But "every exported generator" turned out, over the course of this design,
to mean **four** architecturally different sources, not the two the quoted rule literally names —
each needs its own extraction logic, and each is at a different stage of being covered:

| # | Source | Return shape | Status |
|---|---|---|---|
| 1 | `DeterministicFunction<T>.apply()` (e.g. `MapFunction`) | `Value<T>` | ✅ implemented |
| 2 | `GenerativeDistribution<T>.sample()` (e.g. `lphy.base.distribution.Normal`) | `RandomVariable<T>` | ✅ implemented |
| 3 | `@MethodInfo` instance methods — a **method call** on an existing value (e.g. `someTaxa.setTaxaAges(ages)`), not a top-level generator call | `T` directly, no `Value<T>` wrapper | ⚠️ known gap |
| 4 | `ExpressionNode1Arg`/`ExpressionNode2Args` — **calculation expressions**: ~30 unary math functions (`abs`, `sqrt`, `log`, ...) and ~17 binary operators (`+`, `-`, `<=`, `==`, ...) | `T` directly, via a `Function<A,R>`/`BiFunction<A,B,R>` factory method, no `Value<T>` wrapper | ✅ export implemented; ⚠️ tiling-side still open (see below) |

For (1) and (2): a **generator**'s namespace is the real Java package of the class implementing it
(e.g. `lphy.base.distribution` for `Normal`, `lphy.core.parser.function` for `MapFunction`) — not a
synthetic category string. A **type**'s namespace is that type's own real Java package (e.g.
`java.util` for `Map`, `lphy.base.evolution.tree` for `TimeTree`) — not a blanket constant.
Implemented in `lphy-phylospec/src/main/java/lphy/phylospec/export/ComponentLibraryExporter.java`:
`buildTypes()` (return-type collection + type namespace) and `buildGenerators(Class, boolean)`
(generator namespace), both reflecting via `GeneratorUtils.getReturnType()` /
`GeneratorUtils.getClass()` (`lphy/src/main/java/lphy/core/model/GeneratorUtils.java`).

**(3) `@MethodInfo` — known gap, not yet covered.** Instance methods annotated `@MethodInfo` (not
`@GeneratorInfo`) on domain interfaces like `Taxa`, `Alignment`, `TimeTree`, `SiteModel`, `Table` —
callable in LPhy script syntax as a method call on an existing value. Example, `Taxa.setTaxaAges()`:
```java
@MethodInfo(description = "set the ages to the taxa")
default Taxa setTaxaAges(Double[] ages) { ... }
```
Verified that `ComponentLibraryExporter` does **not** catch this: `buildTypes()` / `buildGenerators()`
only iterate `extension.getDistributions()` / `extension.getFunctions()` — classes registered as a
`GenerativeDistribution` or `BasicFunction` — and `Taxa` (an interface, not either of those) is
never in that list. At runtime, every `@MethodInfo` call instead resolves through one generic
dispatcher, `lphy.core.parser.function.MethodCall` (itself a `DeterministicFunction`, but never
registered in `declareFunctions()` — it looks up the target method by name at construction time),
which the exporter also never reflects into individual entries. Two consequences, distinct from
each other:
- **Types**: an `@MethodInfo` method's return type is the value type *directly* — architecturally
  different from `apply()`/`sample()`'s `Value<T>`/`RandomVariable<T>` wrapping, so
  `GeneratorUtils.getReturnType()`'s unwrapping logic doesn't apply as-is. In the `Taxa` example the
  gap is invisible in the output today (`Taxa` already appears in the type catalog via the
  unrelated top-level `taxa()` function returning it) — but that's coincidence, not coverage: a
  type returned *only* by `@MethodInfo` methods, never by any top-level generator, would be
  silently missing from the catalog.
- **Generators**: `setTaxaAges` (and every other `@MethodInfo` method) has no entry in the
  `generators` list at all — LPhy's whole method-call surface is currently invisible to this
  export, independent of the type-catalog question above.
Not fixed yet. Would need: enumerating `@MethodInfo` methods (likely via `getMethods()` over each
already-collected type's class, mirroring how `GeneratorUtils.getGeneratorInfo()` finds
`@GeneratorInfo`), extracting their return type directly (no `Value<T>` unwrap) for the type
catalog, and deciding how/whether to represent them in the `generators` list (a method call isn't
quite the same shape as a top-level generator call).

**(4) `ExpressionNode1Arg`/`ExpressionNode2Args` — export side done, tiling side still open.**
`ExpressionNode<T> extends DeterministicFunction<T>`, but its two concrete subclasses are generic
*wrapper* classes, not one class per operator: each operator (`abs`, `sqrt`, `+`, `<=`, ...) is a
`public static Function<A,R>`/`BiFunction<A,B,R>` factory method on `ExpressionNode1Arg` or
`ExpressionNode2Args`. There's no per-operator class, no `declareFunctions()` registration, and no
`@GeneratorInfo` per operator (the one `@GeneratorInfo` present, on
`ExpressionNode2Args#getParams()`, is a structural placeholder `name="expression"` for the whole
wrapper). The only place an operator's script name is bound to its implementation at all is a
hardcoded `switch` in the hand-written parser listener, `lphy.core.parser.LPhyListenerImpl`.

- **Export (done)**: `ComponentLibraryExporter.buildExpressionOperatorGenerators()` reflectively
  scans both wrapper classes for `public static Function`/`BiFunction` factory methods and reads
  the operator's arg/return types straight off `Function<A,R>`'s/`BiFunction<A,B,R>`'s own generic
  signature — no `Value<T>` unwrap needed, these return raw types directly. Naming needs one
  hand-maintained map, `EXPRESSION_OPERATOR_SCRIPT_NAMES`: LPhy's ~30 unary math functions already
  have their script name matching the Java method name exactly (`sqrt`→`sqrt()`), but the ~17
  binary operators (and unary `!`) are bound to a *symbol*, not the method name (`+`→`plus()`,
  `<=`→`le()`, `!`→`not()`) — mined once from `LPhyListenerImpl`'s switch (small, effectively
  frozen). Each exported entry also carries `"implementedVia": "ExpressionNode1Arg"` (or
  `"ExpressionNode2Args"`) as an `additionalProperty`, so a JSON consumer isn't left wondering why
  ~48 distinct names all report the identical namespace (`lphy.core.parser.function`) — unlike
  every other generator, these share their implementing class instead of having one each. Return
  types feed into the type catalog too, via `expressionOperatorReturnTypes()` — didn't add any new
  type this session (all return already-catalogued `Double`/`Integer`/`Number`/`Boolean`), but
  keeps Rule 1's definition ("distinct return types across every exported generator") honest.
  Closed three real gaps immediately: LPhy's `exp`/`log`/`sqrt` now exact-match PhyloSpec's own
  math functions of the same names, which were previously invisible to this whole comparison.
- **Tiling (still open — this is the "fit into the PhyloSpec spec" half)**: exporting these makes
  them visible to `TypeResolver` (so a PhyloSpec script referencing `sqrt`/`+`/`<=` type-checks
  against a real LPhy generator), but nothing yet *constructs* an LPhy value from one when actually
  running a PhyloSpec script through LPhy. `ReflectiveGeneratorTile` (see `README.md`'s tiling
  framework design) assumes the opposite shape from these operators: a name resolves to *a class*
  to instantiate via `ParserUtils.getMatchingFunctions(name, args)`. There's no "the `sqrt` class"
  here — only "the `sqrt` function object to pass into `ExpressionNode1Arg`'s constructor," e.g.
  `new ExpressionNode1Arg(exprText, ExpressionNode1Arg.sqrt(), argValue)`, exactly as
  `LPhyListenerImpl` does it. Needed: a dedicated tile/converter (not `ReflectiveGeneratorTile`)
  that, given a resolved operator name and its argument `Value`s, reuses
  `EXPRESSION_OPERATOR_SCRIPT_NAMES` in reverse for the symbol-bound half and looks up + calls the
  matching static factory method. Also not yet investigated: whether `phylospec-core`'s own AST
  even models `+`/`sqrt(...)` as generator-call-shaped nodes (`Expr.Call`, the shape `GeneratorTile`
  matches) at all, or as a distinct expression-node category needing its own `CandidateTile`
  matcher.

### Rule 2 — report formatting: no repeated identical values, no `<br>` for short values

Problem this rule fixes: a table cell listing several types must not repeat an identical
namespace once per item (e.g. `java.lang` five times in a row), and must not stack short, atomic
values one per `<br>` line when they'd read fine on one line, comma-joined.

Resolution — two *independent* groupings, not one all-or-nothing match on a combined string:
1. Group the cell's names by **namespace alone**. Each group renders as its comma-joined,
   bolded names followed by the namespace shown once (`**A**, **B**, **C** — `ns``). Groups with a
   different namespace get their own line — so e.g. 5 of 7 items sharing `java.lang` still
   collapse together even though the other 2 don't, rather than nothing collapsing just because
   not *every* item matches.
2. Independently, whatever extra info a name carries (`extends`/`alias`/`typeParameters`/
   `typeProperties`) is listed on a further comma-joined line, name-prefixed only when the cell
   has more than one name — so a shared namespace still collapses even when per-item extras
   differ.

`<br>` is reserved for genuinely separate lines (a different namespace group, the extras line, one
generator overload per line); `,` is used within a line for short/atomic values.

Implemented in `lphy-phylospec/src/main/python/compare_component_libraries.py`:
`type_namespace_and_extra()`, `fmt_type_line()`, `fmt_grouped_type_cell()`.

## Pipeline

```
1. Java: regenerate the LPhy-side component library JSON
   mvn -pl lphy-phylospec exec:java -Dexporter.args=<path>
     runs lphy.phylospec.export.ComponentLibraryExporter
     -> lphy-phylospec/src/main/resources/phylospec-lphy-component-library.json

2. Python: regenerate the comparison report
   python3 lphy-phylospec/src/main/python/compare_component_libraries.py
     reads:
       - phylospec-lphy-component-library.json          (step 1's output)
       - ../phylospec/core/java/.../phylospec-core-component-library.json  (sibling repo, PhyloSpec's own)
       - lphy-phylospec/src/main/python/curated_equivalences.json          (hand-maintained name-equivalence map)
     -> lphy-phylospec/src/main/python/model_coverage_gap.md
```

Re-run step 1 whenever `lphy`/`lphy-base` generator code changes. Re-run step 2 whenever step 1's
JSON changes, PhyloSpec's own JSON changes, or `curated_equivalences.json` is hand-edited.
`curated_equivalences.json` maps concepts that carry a different name on each side (e.g. LPhy's
`readFasta` ↔ PhyloSpec's `fromFasta`) — step 2 validates every entry against the current JSONs on
each run and fails loudly if a curated name has gone stale (renamed/removed upstream), rather than
silently drifting.

## Known schema-field gaps (LPhy export vs PhyloSpec's schema)

*(Merged in from the now-deleted `PHYLOSPEC_SCHEMA_GAPS.md`, Sep 2 commit `fdd40ab1`.)* A
structural comparison of `phylospec-core-component-library.json` (and the full
`org.phylospec.components` POJO set it's generated from) against
`phylospec-lphy-component-library.json` — every JSON concept the former has that the latter
doesn't, with a real example from `phylospec-core-component-library.json` wherever one exists.

**Present in phylospec-core, missing from phylospec-lphy-component-library.json (real examples exist):**
- **`Type.extends`** — subtype relationship, e.g. `{ "name": "NonNegativeReal", "extends": "Real" }`
- **`Type.alias`** — alternate name, e.g. `{ "name": "Rate", "alias": "PositiveReal" }`
- **`Generator.ioHints`** — file I/O metadata (role, extensions, file-path argument), e.g.
  `"fromNexus" -> "ioHints": { "role": "dataInput", "extensions": [".nex", ...], "fileArgument": "file" }`.
  LPhy's equivalents (`readNexus`, `readFasta`, `readTrees`, `readDelim`, `readMpileup`) carry no
  such structured metadata — just a `@ParameterInfo` description string.
- **`Argument.default`** — e.g. `"fromCSV" -> { "name": "delimiter", "default": "," }`. LPhy's
  `@ParameterInfo` has `optional()` (boolean) but no field for *what* the default value is.

**Declared in the schema, but not actually used in phylospec-core's own library either** (no real
example to show, only the schema's stated intent, 0 non-null/non-empty occurrences there):
`Argument.dimension`, `Argument.recommended`, `Argument.uiHints`, `Type.properties`.

**Genuinely populated in phylospec-core, intentionally not carried onto the LPhy side:**
- `Generator.constraints` — e.g. `f81 -> ["baseFrequencies.num == 4"]`
- `Generator.examples` — declared, though empty for every generator even in phylospec-core's own
  file (0 non-empty occurrences there too)
- `Generator.typeParameters` — e.g. `IID -> ["T"]` (a generic *generator*, not a generic type;
  unaffected by Rule 1 above, which is only about `Type.typeParameters`)

**Updated by this session's Rule 1 fix** (was listed as "deliberately removed" here before): `Type.
typeParameters` is now populated — but only for a type that genuinely declares its own generic
parameters (`Map -> ["K", "V"]`, reflecting `java.util.Map<K,V>`), not for LPhy's monomorphic JDK
scalar types (`Double`, `String`, `Boolean`, `Object` — none of these declare type parameters).
`Type.typeProperties` is still omitted everywhere (unaffected by Rule 1).

**Already matched**: `ComponentLibrary`'s `name`/`version`/`description`/`authors`/`license`/
`types`/`generators`, and `Argument`'s `name`/`type`/`required`/`description`.

## Suggested improvements (not yet done)

- **Single command for the whole pipeline** — a thin wrapper script (or a Maven `exec` execution
  chained to the Python step) so "regenerate the report" is one command, not two run in sequence
  by hand.
- **CI drift check** — regenerate the JSON to a temp file and diff against the checked-in one on
  `verify`, so a `lphy`/`lphy-base` change can't silently go unreflected (already flagged as an
  open item in `README.md`; applies equally to this pipeline).
- **Promote from `find_near_matches()`'s candidate list** — the script's string-similarity
  heuristic (`compare_component_libraries.py`) surfaces name pairs worth a manual look; a periodic
  pass to promote real renames into `curated_equivalences.json` (or confirm-and-discard
  coincidences like `sort`/`sqrt`) would keep the "In both" tables as complete as they can be.
- **Decide the generic-preserving type-string question flagged earlier** — `generatedType`/
  argument `type` strings still show the raw erased class (`map`'s own `generatedType` is bare
  `"Map"`, `readFasta`'s `options` argument is bare `"Map"` rather than `"Map<String, String>"`) —
  noted but deliberately left open pending a decision on whether it's worth a second, string-
  preserving reflection path alongside `GeneratorUtils.getClass()`'s erasing one.
