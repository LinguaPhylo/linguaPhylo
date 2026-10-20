# Class design v2 vs. v3: tiled conversion vs. direct conversion

Both designs turn a PhyloSpec script into a live LPhy model that can be simulated, logged and
exported as `.lphy`. v2 ([`CLASS_DESIGN2.md`](CLASS_DESIGN2.md)) was never built; v3
([`CLASS_DESIGN3.md`](CLASS_DESIGN3.md)) is the one implemented in `lphy.phylospec.convert`.

**In short:** v2 converts the script through `phylospec-core`'s tiling framework. v3 drops tiling and
converts the script with one visitor. LPhy has only one translation for each PhyloSpec construct, so
tiling has nothing to choose between.

## v3 replaces tiling with one visitor

| | v2 (design only) | v3 (implemented) |
|---|---|---|
| **How the script is converted** | `EvaluateTiles` collects candidate tiles for each AST node, type-checks them and picks the cheapest combination | One visitor, `PhyloSpecToLPhy`, walks the type-checked AST in source order and builds the LPhy objects directly |
| **Classes** | About a dozen tiles, plus `LPhyCoreTileLibrary`, `PhyloSpecLPhyDictionary` and `LPhyCall`/`LPhyCallBuilder` | Three: `PhyloSpecNames` (name index), `PhyloSpecToLPhy` (converter), `PhyloSpecToLPhyRunner` (command line) |
| **Where the model is kept** | `PhyloSpecLPhyDictionary`, a subclass of `REPL` | LPhy's own `REPL`, used as it is |
| **Argument binding** | All overloads merged into one list, every argument optional, so `Exponential(1.0)` without a name fails | Binds against PhyloSpec's own overloads, so omitted argument names work as PhyloSpec defines them |
| **`IID` and method calls** | Need `LPhyCall` wrappers, because a tile cannot see the call inside it | The visitor reads the inner call directly |
| **`observed as`** | Scalar values only | Scalars and arrays (`IID` or vectorized) |
| **`y = x` and `observed as someVariable`** | Silently rename the existing value | Rejected with a clear error |
| **Where errors point** | At the root of a tile | At the innermost AST node that failed |
| **Discoverable by a generic PhyloSpec front end** | Yes, registered as a `TileLibrary` | No. A small `LPhyTileLibrary` adapter is described as optional but not built |

## What v3 keeps from v2

| Kept from v2 | How |
|---|---|
| Name mismatches are fixed in LPhy, not in conversion code (Decision 3) | `phylospec` annotations or new constructors in `lphy-base`, e.g. `hky`'s `freq` is annotated `baseFrequencies` |
| LPhy's own resolver builds each generator | `ParserUtils` picks the overload and handles `replicates` and vectorization |
| LPhy's existing tools run the model | `Sampler`, the file loggers and `CanonicalCodeBuilder` work unchanged |

## Tiling is not used at all in v3

v3 has no tile classes, no tile library and no service registration. The only thing it takes from
`phylospec-core`'s tiling package is the error class `TileApplicationError`, a plain
`RuntimeException`.

## Why BEAST 3 needs tiling and LPhy does not

PhyloSpec's BEAST 3 integration (`phylospec/integrations/beast3`) does use tiling: its runner calls
`EvaluateTiles`, and it has 82 tile classes.

| | BEAST 3 | LPhy |
|---|---|---|
| **Translations per construct** | Several, e.g. `LogNormalTile` vs. `LogNormalRealSpaceTile`, strict vs. relaxed clock | Exactly one: each PhyloSpec name maps to one LPhy generator (Decision 3) |
| **Type checks** | Decide which tile fits | Always pass: every input is a raw `Value` or `GenerativeDistribution` |
| **Extra choices** | Operators and loggers must be picked to suit the model | None: LPhy samples and logs any model the same way |
| **What tiling does** | Searches for the best translation | Would always return its only candidate |

Tiling is worth bringing back only if LPhy one day has several translations for the same PhyloSpec
construct that must be chosen by type.
