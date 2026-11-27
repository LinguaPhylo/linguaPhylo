# LPhy 1 Framework Reference

Purpose: a single map of LPhy 1's design and architecture, written from reading the source
(`lphy`, `lphy-base`) rather than from any one existing doc. It exists so that LPhy 2 design work
has one place to start from. It does **not** re-explain anything already documented elsewhere in
this repo — each section links to the authoritative doc for detail and states only what's needed
to navigate there with the right mental model.

## 1. Core concepts: Value and Generator

LPhy scripts describe a **probabilistic graphical model**: a DAG of two node kinds, both
implementing `GraphicalModelNode<T>` (`lphy/core/model/GraphicalModelNode.java`) — `getInputs()`,
`getUniqueId()`, `value()`.

- **`Value<T>`** (`lphy/core/model/Value.java`) — a *wrapper* around one actual piece of data of
  type `T`. `.value()` returns the raw `T`; `.getType()` returns `value.getClass()`. A `Value` is
  the thing scripts name (`lambda = ...`) and the thing arguments are (every `@ParameterInfo`
  constructor argument is a `Value<X>`, never a raw `X`). A `Value` is either:
  - a **constant** — `getGenerator() == null`, e.g. a literal `2.0`;
  - the output of a **`DeterministicFunction`** — `getGenerator()` returns that function;
  - a **`RandomVariable<T>`** (`lphy/core/model/RandomVariable.java`, extends `Value<T>`) — the
    output of a `GenerativeDistribution`, reached via `getGenerativeDistribution()`.

- **`Generator<T>`** (`lphy/core/model/Generator.java`) — the thing that *produces* a `Value<T>`.
  Holds named parameters as a `Map<String, Value>` (`getParams()`/`setParam()`), and its
  `getInputs()` is exactly that parameter map's values — this is what makes the model a DAG.
  Three kinds, all reflection-discovered from `@GeneratorInfo`/`@ParameterInfo` annotations (how
  to implement each is fully covered in [`DEV_NOTE2.md`](DEV_NOTE2.md) — not repeated here):
  - **`GenerativeDistribution<T>`** — `sample()` → `RandomVariable<T>`; script operator `~`.
  - **`DeterministicFunction<T>`** (extends `BasicFunction`) — `apply()` → `Value<T>`; script
    operator `=`.
  - **Method call** — not a distinct Java type; a plain method on an existing LPhy object's class
    annotated `@MethodInfo`, invoked via `.` in script (e.g. `D.taxa()`). Conceptually a
    deterministic function whose first argument is the receiver object.

Language-level treatment of these same concepts (script syntax, `~`/`=`, replicates) is in
[`docs/language-features.md`](docs/language-features.md) and
[`language_specification.md`](language_specification.md).

## 2. LPhy data type system

LPhy is dynamically typed: a script never declares a type, and every runtime value is a `Value<T>`
— so **the LPhy "data type" of a value is just the Java class `T` it is wrapped around**
(`Value.getType()`). There is no separate type-tag or type-registry object; the type system rides
entirely on Java's own class/generics reflection. `DEV_NOTE2.md`'s "LPhy data type" section covers
the same ground from the how-to-implement-a-generator angle; this section covers what the type
*is*, structurally.

The `T` a generator returns/consumes falls into one of these shapes:

- **Boxed primitive** — `Boolean`, `Integer`, `Double`, `String`. `Number` also appears as a
  parameter type where a constructor deliberately accepts either `Integer` or `Double` input
  (e.g. `LogNormal`'s `M` argument) — it is not a distinct concept from `Double`, just a wider
  Java bound.
- **Array = "vector"** — `T[]` (e.g. `Double[]`), `T[][]` for a 2D array (e.g. `Double[][]` for a
  rate matrix). This is what the language calls a vector/matrix; in Java it is simply an array
  type, not a dedicated class.
- **Domain object** — an ordinary Java class/interface with no LPhy-specific marker, e.g.
  `TimeTree`, `Alignment`, `Taxa`, `PopulationFunction`, `SiteModel`, `Table`. Anything can become
  an LPhy data type this way; there is no interface to implement to "opt in."

Where these live:
- Generic containers, in `lphy/src/main/java/lphy/core/model/datatype/`: `Vector<T>` (an
  accessor-mixin interface — `getComponentType()`/`getComponent(i)`/`size()` — implemented by
  `VectorValue<T>`, itself just `Value<T[]>` with those convenience accessors),
  `*ArrayValue`/`*Array2DValue` (typed `VectorValue`/`Value<T[][]>` subclasses per primitive),
  `MapValue`, `Table`/`TableValue`.
- Domain types live with the code that uses them, mostly under `lphy-base`'s
  `lphy.base.evolution.*` and sibling packages (`TimeTree`, `Alignment` and its subclasses,
  `Taxa`, `NChar`, `Mpileup`, `BModelSet`, `SVSPopulation`, `Variant`, ...).

`SequenceType` (from the `jebl` library) is a different concept entirely and is **not** an LPhy
data type — it describes a sequence alphabet (nucleotide/amino acid/binary/continuous), registered
via a separate `SequenceTypeBaseImpl` SPI path. `DEV_NOTE2.md` calls this out explicitly ("LPhy
data type is not sequence type").

## 3. Registration / extension mechanism (SPI)

Generators and types are not declared in one central catalog — they are **collected by
reflection over whatever generator classes each extension registers**:

- `Extension` (`lphy/core/spi/Extension.java`) — base SPI interface, one `register()` method.
- `LPhyExtension` (`lphy/core/spi/LPhyExtension.java`) — adds `declareDistributions()`,
  `declareFunctions()`, `getTypes()`. Concrete "container" classes per module implement this:
  `LPhyCoreImpl` (core built-ins), `LPhyBaseImpl extends LPhyCoreImpl` (the standard library,
  lists every distribution/function class by hand in two `Arrays.asList(...)` calls), and any
  third-party extension.
- On `register()`, each container reflects each declared generator class's return type and every
  constructor parameter's type (`GeneratorUtils.getReturnType`, `NarrativeUtils.getParameterTypes`)
  and adds them all to a `TreeSet<Class<?>> types`. **This means the "LPhy type list" is not an
  explicit registry — it is incidentally derived from whatever types the currently-registered
  generators happen to use.** A type with no generator referencing it does not exist as far as the
  framework is concerned.
- `LoaderManager` (`lphy/core/spi/LoaderManager.java`) is the process-wide singleton that runs
  `ServiceLoader` over all `Extension` providers (JPMS `module-info.java` `uses`/`provides`, plus
  `META-INF/services`) and merges their distributions/functions/types into one place —
  `getGenDistDictionary()`/`getFunctionDictionary()` (name → `Set<Class<?>>`, the `Set` is how
  overloading is represented) and `getTypes()`.

Full step-by-step for adding a new generator or extension module (annotations, SPI registration,
`module-info.java`, `META-INF/services`) is in `DEV_NOTE2.md`'s "Write your LPhy object in Java"
and "Registration" sections — not repeated here.

## 4. Module layout

Strict dependency chain, enforced by JPMS `module-info.java` at compile time (details, build/run
commands: [`DEV_NOTE3.md`](DEV_NOTE3.md); package purpose per module: [`lphy/README.md`](lphy/README.md)):

```
lphy          core: ANTLR parser, graphical-model (Value/Generator), vectorization, SPI
   ↑
lphy-base     standard library: distributions, functions, evolution domain types (TimeTree, Alignment, ...)
   ↑
lphy-studio   Swing GUI
```

Within `lphy.core`: `model` (Value/Generator/datatype — §1–2), `parser` (ANTLR grammar → object
graph; subpackages `antlr`, `graphicalmodel`, `function`, `argument`), `vectorization` (§5),
`spi` (§3), plus `simulator`, `logger`, `exception`, `codebuilder`, `io`.

## 5. Vectorization

Two script-level mechanisms — the `replicates=n` argument, and passing vector arguments directly
to a scalar generator — both described language-side in `language_specification.md`'s "Implicit
Vectorization" section. The Java-side machinery lives in `lphy.core.vectorization`: `IID` (repeats
a distribution `n` times), `VectorizedDistribution`/`VectorizedFunction`/`VectorizedRandomVariable`
(wrap a scalar generator to run over vector arguments element-wise), `VectorMatchUtils` (decides
whether/how a generator's declared parameter types match vectorized argument shapes at
parse/instantiation time).

## 6. Known design tensions (relevant to LPhy 2)

Observations from reading the code, worth a deliberate decision in LPhy 2 rather than carrying
forward as-is:

- **The type catalog is derived, not declared.** `LoaderManager.getTypes()` (§3) is a side effect
  of scanning registered generators' signatures, not an explicit list of "these are LPhy's data
  types." There is no single source of truth to ask "what are all valid LPhy types" independent of
  which generators happen to be loaded.
- **No bounded/refined numeric types.** LPhy has no representation for "a `Double` that must be
  non-negative" or "a `Double` that must be a probability" — `@ParameterInfo` carries no bounds, so
  such constraints (when checked at all) live in each generator's own validation code rather than
  in the type system.
- **`Number` vs `Double` is an ad hoc "accepts either" escape hatch**, not a first-class optional/
  union-type concept — it works only because `Number` happens to be a common Java supertype.
- **Overloading is "however many public constructors a class has,"** discovered by reflection at
  parse time, not a declared set of signatures.
