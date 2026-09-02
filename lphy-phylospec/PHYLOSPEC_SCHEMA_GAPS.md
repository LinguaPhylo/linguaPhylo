# PhyloSpec schema concepts not in phylospec-lphy-component-library.json

A structural comparison of `phylospec-core-component-library.json` (and the full
`org.phylospec.components` POJO set it's generated from) against
`phylospec-lphy-component-library.json` — every JSON concept the former has that the latter
doesn't, with a real example from `phylospec-core-component-library.json` wherever one exists.

## Present in phylospec-core, missing from phylospec-lphy-component-library.json (with real examples)

**`Type.extends`** — declares a subtype relationship between types.
```json
{ "name": "NonNegativeReal", "extends": "Real" }
```

**`Type.alias`** — an alternate name for a type.
```json
{ "name": "Rate", "alias": "PositiveReal" }
{ "name": "Taxa", "alias": "Vector<Taxon>" }
```

**`Generator.ioHints`** — file I/O metadata for load/save-type functions (role, accepted
extensions, which argument takes the path).
```json
"fromNexus" -> "ioHints": { "role": "dataInput", "extensions": [".nex", ".nexus", ".nxs"], "fileArgument": "file" }
```
LPhy's equivalent generators (`readNexus`, `readFasta`, `readTrees`, `readDelim`, `writeFasta`,
`readMpileup`) carry no such structured metadata today — just a `@ParameterInfo` description
string on the file-path argument.

**`Argument.default`** — the argument's default value if omitted.
```json
"fromCSV" -> { "name": "delimiter", "default": ",", ... }
"BirthDeath" -> { "name": "samplingProbability", "default": 1, ... }
```
LPhy's `@ParameterInfo` has `optional()` (a boolean) but no field for *what* the default actually
is.

## Present in phylospec-core's schema, but not actually used there either

These fields are declared in the schema, but `phylospec-core`'s own current library leaves them
empty/absent everywhere — so there's no real example to show, only the schema's stated intent:

- **`Argument.dimension`** — *"Expected dimension of the argument... can reference other parts of
  the model"* (e.g. a hypothetical `"tree.numBranches"`). Zero non-null occurrences in the file.
- **`Argument.recommended`** — *"recommended but not required"*. Zero `true` occurrences.
- **`Argument.uiHints`** (`widget`/`order`/`group`) — zero occurrences.
- **`Type.properties`** — a named-sub-property map (e.g. giving `Sequence` an `alphabet`
  property). Zero non-empty occurrences.

## Present there, but the ones already deliberately removed from phylospec-lphy-component-library.json

These genuinely exist and are sometimes populated in `phylospec-core-component-library.json`
(unlike the four above) — listed here as a factual record of the comparison, not a suggestion to
bring them back, since removing them was a deliberate instruction:

- **`Type.typeParameters`** / **`Generator.typeParameters`** — `Vector -> ["T"]`, `IID -> ["T"]`
- **`Type.typeProperties`** — `Vector -> ["num"]`
- **`Generator.constraints`** — `f81 -> ["baseFrequencies.num == 4"]`
- **`Generator.examples`** — declared, though empty for every generator even in phylospec-core's
  own file (0 non-empty occurrences there too).

## Already matched

Everything else in the schema is already populated in `phylospec-lphy-component-library.json`:
`ComponentLibrary`'s `name`/`version`/`description`/`authors`/`license`/`types`/`generators`, and
`Argument`'s `name`/`type`/`required`/`description`.
