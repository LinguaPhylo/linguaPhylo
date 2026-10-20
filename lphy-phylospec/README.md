# lphy-phylospec

Bridges LPhy and [PhyloSpec](https://github.com/CODEPhylo/phylospec). It does two things:

1. **Runs a PhyloSpec script as an LPhy model** (`PhyloSpecToLPhyRunner`): the script becomes live
   LPhy objects, which LPhy simulates and logs like any `.lphy` script.
2. **Measures the gap between LPhy and PhyloSpec** (`ComponentLibraryExporter` and a Python report):
   it lists what LPhy provides in PhyloSpec's component-library format, then compares it with
   PhyloSpec's own library.

**Before you start.** This module builds against installed jars, not the live source tree, so
install `phylospec-core` and `lphy`/`lphy-base` into your local `.m2` first. Reinstall `lphy`/`lphy-base`
after changing them (for example an `@GeneratorInfo` or `@ParameterInfo` annotation); otherwise both
tools run against the old classes.

```bash
# from the parent folder of both repositories
cd phylospec    && mvn -pl core/java -am install -Dmaven.test.skip=true && cd ..
cd linguaPhylo  && mvn -q -pl lphy,lphy-base -am install -Dmaven.test.skip=true
```

**SNAPSHOT dependencies.** This module depends on `phylospec-core` `0.0.1-SNAPSHOT` and
`lphy-base` `1.8.1-SNAPSHOT`. SNAPSHOT versions are not published to Maven Central, so Maven reads
them from the local repository, `~/.m2/repository`. The two `install` commands above put them
there. In the first command, `-pl core/java` selects the `phylospec-core` module, and `-am` also
installs its parent pom, which Maven needs to resolve it.

Run both commands once, and again after any change to the PhyloSpec or LPhy code. When these
dependencies move to released versions, the installs are no longer needed.

All commands below run from this module's folder, `linguaPhylo/lphy-phylospec`.

## 1. Run a PhyloSpec script as an LPhy model

**What it does.** `PhyloSpecToLPhyRunner` parses and type-checks a `.phylospec` script with
`phylospec-core`, converts it into an LPhy model, then simulates and logs it the same way `slphy`
does. It also writes the model as a `.lphy` script, so the result can be checked or rerun with
LPhy itself.

**Input.** One PhyloSpec script with the extension `.phylospec`.

**Output.** Written to the script's folder, or to the folder given by `-o`. `<name>` is the script's
file name without `.phylospec`:

| File | Content |
|---|---|
| `<name>.lphy` | the converted model, as an LPhy script |
| `<name>_<tree>.trees` | every simulated tree |
| `<name>_<alignment>.nexus` | the simulated alignment; one file per replicate, `<name>_r<i>_<alignment>.nexus`, when `-r` > 1 |
| `<name>.log` | the simulated numbers, one row per replicate (only if the model has random numbers) |

**Command.**

```bash
mvn -q compile exec:java@phylospec2lphy -Dexec.args="<file.phylospec> [options]"
```

| Option | Meaning |
|---|---|
| `-r`, `--replicates n` | number of simulations, default 1 |
| `-seed`, `--seed s` | random seed, for reproducible results |
| `-o`, `--outdir dir` | output folder, default the script's folder |
| `-No`, `--notlog "id;id"` | random variables not to log |
| `-h`, `--help` | show the usage |

### The two examples

Both examples need no data: the taxa are listed in the script with `taxon(...)`.

**`examples/simHKY.phylospec`.** This example simulates a 5-site alignment of 4 taxa
on a Yule tree under HKY. `kappa` is drawn from a `LogNormal` and the base frequencies `pi` from a
`Dirichlet`. The site rates are a literal vector.

```bash
mvn -q compile exec:java@phylospec2lphy \
    -Dexec.args="examples/simHKY.phylospec -seed 777 -o target/phylospec-out"
```

**`examples/simJCDiscreteGamma.phylospec`.** This example simulates 4 taxa on a Yule tree under
Jukes-Cantor, and draws 200 site rates from `DiscreteGammaInv`. It runs 3 replicates:

```bash
mvn -q compile exec:java@phylospec2lphy \
    -Dexec.args="examples/simJCDiscreteGamma.phylospec -r 3 -seed 777 -o target/phylospec-out"
```

The second example writes this `target/phylospec-out/simJCDiscreteGamma.lphy`. It shows how the
PhyloSpec names (`jc69`, `birthRate`, `qMatrix`, `DiscreteGammaInv`) map onto LPhy's:

```
model {
  q = jukesCantor();
  rates ~ DiscretizeGamma(shape=0.5, ncat=4, replicates=200);
  tree ~ Yule(lambda=1.0, taxa=taxa(names=["a", "b", "c", "d"]));
  sim ~ PhyloCTMC(Q=q, siteRates=rates, tree=tree);
}
```

### What is supported

The runner supports these forms:

- `~`, `=` and `observed as` statements;
- generator calls, with arguments renamed through `phylospec` annotations in `lphy-base`;
- `IID(...)`;
- arrays, including arrays of `taxon(...)`;
- operators (`+`, `<`, …), math functions (`sqrt`, …) and a few method calls (`rootAge`, `numBranches`).

A form LPhy cannot express yet fails with "Not supported by LPhy yet", pointing at the script line.
Examples are indexed statements, `-x` and units. A PhyloSpec name or argument that differs from
LPhy's needs a `phylospec` annotation in `lphy-base`, and most of these annotations are not written
yet. The design, its limits and the open items are in
[`CLASS_DESIGN3.md`](CLASS_DESIGN3.md).

## 2. Measure the gap between LPhy and PhyloSpec

The gap analysis has two steps: a Java exporter lists what LPhy provides, and a Python script compares
that list with PhyloSpec's.

```
ComponentLibraryExporter (Java)            compare_component_libraries.py (Python)
  LPhy classes  ──►  LPhy component library  ──►  model_coverage_gap.md
                     (+ method-call list)    ▲
                                             └── PhyloSpec component library, curated equivalences
```

### Step 1: export LPhy's component library

**What it does.** `ComponentLibraryExporter`
(`src/main/java/lphy/phylospec/export/ComponentLibraryExporter.java`) reflects over LPhy core and
`lphy-base` and lists every generator and data type in a JSON file. The file has the same format as
PhyloSpec's own `phylospec-core-component-library.json`, because it is written through
`phylospec-core`'s own schema classes.

- **Scope**: only these two extensions, named explicitly (`LPhyCoreImpl`, `LPhyBaseImpl`). An
  extension that happens to be on the classpath is not included.
- **LPhy's own names and types**: generators and arguments keep their LPhy names, for example
  `jukesCantor` and `meanlog`. Types keep LPhy's Java names, for example `Double` and `Boolean[]`,
  with no translation to `Real` or `Vector<Boolean>`. When a `phylospec` annotation sets a PhyloSpec
  name, it goes into a separate `phylospec` property. The converter in §1 reads the same annotations,
  so the two never disagree.
- **Operators and math functions** (`+`, `<=`, `sqrt`, …) are exported as generators too.
- **Method calls** (`tree.rootAge()`) have no PhyloSpec equivalent. They go into a separate file.

**Input.** The compiled LPhy classes (the installed `lphy`/`lphy-base` jars).

**Output.** Two generated files. Never edit them by hand; rerun the exporter instead.

| File | Content |
|---|---|
| `src/main/resources/phylospec-lphy-component-library.json` | LPhy's types and generators, in PhyloSpec's format |
| `src/main/resources/lphy-method-calls.json` | LPhy's method calls, in the same folder as the first file |

**Command.**

```bash
mvn -q compile exec:java                                   # default output path
mvn -q compile exec:java -Dexporter.args=/path/to/out.json  # another path
```

### Step 2: write the coverage report

**What it does.** `src/main/python/compare_component_libraries.py` matches LPhy's and PhyloSpec's types
and generators. It matches by exact name first, then through a hand-maintained map of concepts that are
named differently on each side, for example LPhy's `readFasta` and PhyloSpec's `fromFasta`. It then
writes a Markdown report of what is in both, in only LPhy, and in only PhyloSpec. It also lists
near-miss names worth a manual look. If a curated name no longer exists on either side, it stops
with an error rather than letting the map silently go stale.

**Inputs.**

| File | Content |
|---|---|
| `src/main/resources/phylospec-lphy-component-library.json` | step 1's output |
| `src/main/resources/lphy-method-calls.json` | step 1's output |
| `~/WorkSpace/phylospec/core/java/src/main/resources/phylospec-core-component-library.json` | PhyloSpec's own library, from the sibling repository |
| `src/main/python/curated_equivalences.json` | the hand-maintained map of equivalent names |
| `src/main/python/model_coverage_gap_intro.md` | the report's introduction |

**Output.** `src/main/python/model_coverage_gap.md`, the coverage report.

**Command.**

```bash
python3 src/main/python/compare_component_libraries.py
```

Every path above is the default. To use other paths, pass them in this order:
`[phylospec_json] [lphy_json] [output_md] [curated_json] [method_calls_json] [intro_md]`.

**When to rerun.** When `lphy` or `lphy-base` generators change, reinstall them, then rerun
step 1 and step 2. Rerun step 2 alone when PhyloSpec's library or `curated_equivalences.json`
changes. Nothing checks yet that the committed JSON is up to date.

How types, namespaces and the report's tables are worked out, and the known schema differences, are in
[`LPhyVsPhylospecDesign.md`](LPhyVsPhylospecDesign.md).
