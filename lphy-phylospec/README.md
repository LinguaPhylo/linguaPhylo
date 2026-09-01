# lphy-phylospec

Bridges LPhy and [PhyloSpec](https://github.com/CODEPhylo/phylospec). See `DESIGN.md` and
`CLASS_DESIGN.md` for the broader plan (running PhyloSpec scripts through LPhy). This module
currently contains one working piece of that plan: `ComponentLibraryExporter`.

## `ComponentLibraryExporter`

`src/main/java/lphy/phylospec/export/ComponentLibraryExporter.java`

Reflects over every LPhy generator and data type registered on the classpath and writes them
out as a **component library JSON file**, in the same format as PhyloSpec's own
`phylospec-core-component-library.json` (validated against
`../phylospec/schema/component-library.schema.json`). It serializes through `phylospec-core`'s
own generated POJOs (`ComponentLibrarySchema`, `Type`, `Generator`, `Argument`), so the output
is structurally guaranteed to match that schema rather than hand-rolled to match it.

### Prerequisite

`phylospec-core` must be installed in your local `.m2` repo first:

```bash
cd ../phylospec
mvn -pl core/java -am install -Dmaven.test.skip=true
```

`lphy`/`lphy-base` must also be installed to `.m2` — this module depends on their built jars,
**not** on the live source tree. If you edit an `@GeneratorInfo`/`@ParameterInfo` annotation (or
anything else) in `lphy` or `lphy-base` and don't reinstall first, the exporter will silently
run against the old compiled classes and the JSON won't reflect your change:

```bash
cd ..   # repo root
mvn -q -pl lphy,lphy-base -am install -Dmaven.test.skip=true
```

### Usage

From this module's own directory:

```bash
cd lphy-phylospec

# writes to the default output path (see below)
mvn exec:java

# writes to an explicit path instead
mvn exec:java -Dexporter.args=/path/to/output.json
```

### Input

No file input — the "input" is whatever LPhy generators and types are on the classpath at
run time, discovered via `lphy.core.spi.LoaderManager` (which loads every registered
`Extension`, e.g. `LPhyCoreImpl`, `LPhyBaseImpl`, and any third-party extension present on the
module/classpath). Running this from `lphy-phylospec` only picks up `lphy` + `lphy-base`; a
downstream extension module would need to depend on this module (or replicate the `main`) to
include its own generators.

For each generator class, the exporter reads:

- `@GeneratorInfo` (on the generator's `sample()`/`apply()` method) — `phylospec()` name
  override (falls back to `name()`), `description()`, `category()`.
- `@ParameterInfo` (on each constructor parameter) — `phylospec()` name override (falls back to
  `name()`), `description()`, `optional()`.
- The constructor's actual (generic) parameter types, and the generator's return type, via
  `GeneratorUtils`.

When a class or parameter carries an explicit `phylospec()` value — e.g. `HKY`'s `apply()`
method sets `phylospec = "hky"`, and its `freq` parameter sets `phylospec = "baseFrequencies"` —
that value is used for `name`/`type` as above, **and** also written verbatim into an additional
`"phylospec"` field on that `Generator`/`Argument` JSON entry (via `Generator`/`Argument`'s
existing `additionalProperties` passthrough — no change needed to `phylospec-core`'s POJOs).
This makes it visible in the JSON itself which names come from an explicit, hand-verified
PhyloSpec mapping versus which are just the LPhy name used as-is with no such annotation.
The field is omitted entirely when no `phylospec()` value was set (the common case today —
only `HKY` sets it so far).

A generator class with multiple public constructors produces multiple `Generator` entries
sharing one name — the same way PhyloSpec itself represents overloads (e.g. the two `Yule`
entries in its own core library).

### Output

A JSON file (default `src/main/resources/phylospec-lphy-component-library.json`, relative to
this module's own directory — pass an absolute path via `-Dexporter.args=...` to write
elsewhere) shaped like:

```json
{
  "componentLibrary": {
    "name": "LPhy",
    "version": "0.1.0",
    "description": "...",
    "types": [
      { "name": "Real", "namespace": "lphy.types", "description": "" }
    ],
    "generators": [
      {
        "name": "Exp",
        "description": "The exponential probability distribution.",
        "namespace": "lphy.distributions.prior",
        "generatedType": "Distribution<Real>",
        "arguments": [
          { "name": "mean", "type": "Real", "required": true, "description": "..." }
        ]
      },
      {
        "name": "hky",
        "description": "The HKY instantaneous rate matrix. ...",
        "namespace": "lphy.functions.rate_matrix",
        "generatedType": "Vector<Vector<Real>>",
        "arguments": [
          { "name": "kappa", "type": "Real", "required": true, "description": "...", "phylospec": "kappa" },
          { "name": "baseFrequencies", "type": "Vector<Real>", "required": true, "description": "...", "phylospec": "baseFrequencies" }
        ],
        "phylospec": "hky"
      }
    ]
  }
}
```

A run also prints a one-line summary (`Wrote N types and M generators to <path>`) and logs any
Java type it couldn't map to a PhyloSpec type name to stderr.

`src/main/resources/phylospec-lphy-component-library.json` is a **generated artifact** —
produced entirely by running `mvn exec:java` as above, never hand-edited. Regenerate it (after
the reinstall step above, if `lphy`/`lphy-base` changed) rather than editing the file directly.

### Known limitation: type-name mapping

PhyloSpec's numeric types form a refinement lattice (`Real` ⊃ `NonNegativeReal` ⊃
`PositiveReal`, `Probability`, ...) that plain Java types (`Double`, `Integer`) can't express —
there's no way to reflectively recover "this double is actually a `Probability`". The exporter
covers this with a small hand-maintained map (`JAVA_TO_PHYLOSPEC_TYPE` in
`ComponentLibraryExporter`); anything not in that map falls back to its Java simple name (e.g.
`TimeTree`, `Alignment`, `Taxa`, `SiteModel` currently do) and is logged to stderr so it can be
reviewed and either added to the map or reconsidered (e.g. via a future bounds-aware
`ParameterInfo` annotation).
