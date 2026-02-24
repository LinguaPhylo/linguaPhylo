# LinguaPhylo (LPhy)

Probabilistic model specification language for phylogenetics. Java 17, Maven, JPMS modules.

## Modules

- `lphy/` — Core: ANTLR parser, model graph, SPI framework
- `lphy-base/` — Standard library: distributions, functions, evolution models
- `lphy-studio/` — Swing GUI for model visualization

## Build & Test

Uses Maven wrapper (`./mvnw`) for reproducible builds. Plain `mvn` also works.
The project version is set by the `<revision>` property in the root `pom.xml` (currently `1.7.0`).

```bash
# Build all modules (creates assembly at lphy-studio/target/lphy-studio-<version>/)
./mvnw clean install -DskipTests

# Run all tests
./mvnw test

# Run tests for a single module (after initial install)
./mvnw -pl lphy test
./mvnw -pl lphy-base test
./mvnw -pl lphy-studio test

# Run tests for a single module (without prior install, -am builds dependencies)
./mvnw -pl lphy-base -am test

# Run a single test class
./mvnw -pl lphy-base -am test -Dtest=BetaTest
```

Note: `lphy` has no internal dependencies, so `-pl lphy test` always works.
`lphy-base` and `lphy-studio` depend on upstream modules, so they need either
a prior `install` (which populates the local Maven repo) or `-am` to build dependencies from source.

## Run from Command Line

Requires the assembly built by `./mvnw clean install -DskipTests`.

```bash
export LPHY=lphy-studio/target/lphy-studio-1.7.0

# Launch LPhy Studio (GUI)
$LPHY/bin/lphystudio

# Run SLPhy (command-line simulator)
$LPHY/bin/slphy -h
$LPHY/bin/slphy examples/coalescent/hkyCoalescent.lphy
```

## Coding Conventions

- Java 17, 4-space indentation, braces on same line
- Distributions extend `GenerativeDistribution<T>`, implement `sample()`
- Functions extend `DeterministicFunction<T>`, implement `apply()`
- Return values wrapped in `RandomVariable<T>` or `Value<T>`
- Annotate with `@GeneratorInfo`, `@ParameterInfo`, `@GeneratorCategory`, `@Citation`
- Register new distributions/functions in SPI provider classes (e.g., `LPhyBaseImpl`)

## Module System (JPMS)

Each module has a `module-info.java`. Exports must be declared explicitly.
Extensions use Java `ServiceLoader` (SPI), not classpath scanning.

## Key Packages

- `lphy.core.model` — Value, Generator, RandomVariable, GraphicalModelNode
- `lphy.core.parser` — ANTLR grammar (`LPhy.g4`), graphical model parsing
- `lphy.base.distribution` — Probability distributions
- `lphy.base.function` — Deterministic functions
- `lphy.base.evolution.*` — Trees, alignments, substitution models, coalescent, birth-death

## LPhy Language

Two blocks: `data { ... }` (observed values) and `model { ... }` (probabilistic model).
`=` for deterministic assignment, `~` for stochastic sampling from distributions.
Grammar: `lphy/src/main/java/lphy/core/parser/antlr/LPhy.g4`

## ANTLR Grammar

The parser is generated from `LPhy.g4`. Generated Java files (`LPhyParser.java`, `LPhyLexer.java`,
`LPhyListener.java`, `LPhyVisitor.java`, etc.) are checked into the repo — there is no Maven plugin
for ANTLR generation. If you modify `LPhy.g4`, regenerate with:

```bash
cd lphy/src/main/java/lphy/core/parser/antlr
antlr4 -visitor -listener LPhy.g4
```

Runtime dependency is `antlr4-runtime:4.11.1` (in `lphy/pom.xml`).

## Examples

`examples/` contains LPhy scripts organized by topic (coalescent, birth-death, clock-model, etc.).
These are useful for testing changes — run them with SLPhy:

```bash
$LPHY/bin/slphy examples/coalescent/hkyCoalescent.lphy
```

## Related Projects

- [LPhyBeast](https://github.com/LinguaPhylo/LPhyBeast) — converts LPhy models to BEAST 2 XML
- Sibling checkout expected at `../LPhyBeast/`
