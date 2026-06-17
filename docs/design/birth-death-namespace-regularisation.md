# Regularising the Birth–Death Generator Namespace

**Status:** proposal / for discussion
**Scope:** `lphy.base.evolution.birthdeath` generators in `lphy-base`
**Audience:** LPhy developers

## 1. Problem

The birth–death (BD) generators have grown organically. There are ~16 classes
spanning ~10 user-facing names, and the names + exposed arguments are
heterogeneous in ways that are not driven by the underlying models:

- **Name conventions vary:** `Tree` suffix on some (`FossilBirthDeathTree`,
  `RhoSampleTree`) but not others (`Yule`, `BirthDeath`); a `Sim` prefix on some
  (`SimFBDAge`, `SimBDReverse`); `FBD` abbreviated in one place, spelled out in
  another.
- **Conditioning is per-class, not per-process:** `Yule` accepts
  `rootAge`/`n`/`taxa`; `BirthDeathTree` *requires* `rootAge`; `FullBirthDeath`
  offers `rootAge` XOR `originAge`; the FBD classes offer neither. There is no
  principled reason a process supports only its particular subset.
- **One genuine parameter inconsistency:** extant sampling is `rho` everywhere
  except `SimFBDAge`/`SimFBDAgeDT`, which call it `frac`.
- **Helper/building-block classes are registered as top-level generators**
  (`SimBDReverse`, `RhoSampleTree`, `SimFossilsPoisson`) alongside the
  user-facing distributions, with no signal that they are internals.

Note the parameter *vocabulary* is otherwise excellent and should be preserved:
`lambda`, `mu`, `psi`, `diversification`, `turnover`, `rootAge`, `originAge` are
already 100% consistent across the classes that use them.

## 2. Key insight: 5 orthogonal axes, not 16 models

Almost every BD generator is a point in the same design space:

| Axis | Values present in code |
|---|---|
| **Process** | pure-birth (Yule) · birth–death · birth–death + sampling |
| **Sampling** | none · ρ extant-only · ψ serial/fossil · ρ+ψ |
| **Tree returned** | reconstructed (sampled tips only) · complete (incl. extinct) |
| **Parameterisation** | (λ, μ) · (diversification, turnover) · GLM-driven λ |
| **Conditioning** | `rootAge` · `originAge` · `n` · `taxa` · `ages` · clade calibrations |

The current classes are a hand-enumerated, *partial* cross-product. The
regularisation is to make the axes explicit:

- **Process** → the user-facing generator name.
- **Parameterisation** → overloaded constructors under that one name.
- **Conditioning** → a shared, optional argument set from a common base.
- **Sampling** → optional `rho`/`psi` args (a process degrades gracefully when
  they are omitted), promoted to a distinct name only when the *tree type*
  changes.

## 3. The enabling mechanism already (mostly) exists

LPhy dispatch already supports overloading. From `ParserUtils`:

- `LoaderManager` maps a `@GeneratorInfo.name()` to a **set** of classes — many
  classes may share one name.
- `getMatchingGenerativeDistributions(name, args)` collects every constructor
  (across all classes sharing the name) whose `@ParameterInfo` arguments match
  the supplied argument *names*.
- `match()` succeeds when **all required arg names are present** and **every
  leftover key is an optional arg** (or the `replicates` keyword).

Two consequences that constrain the design:

1. **Dispatch is by argument-name *set*, not by type.** Type checks are deferred
   to construction. So two overloads under one name **must have disjoint
   required-argument name sets**, or `match()` will return multiple and the
   parser logs *"Picking first one!"*. `(lambda, mu, …)` vs
   `(diversification, turnover, …)` are disjoint → safe. This is already how the
   `…DT` classes coexist with their `(λ, μ)` siblings.
2. **Mutual-exclusivity (e.g. `rootAge` XOR `originAge`) is not expressible in
   `match()`.** Both must be declared `optional`, and the constraint enforced at
   construction (as `FullBirthDeathTree` already does, and as
   `TaxaConditionedTreeGenerator.checkTaxaParameters()` does for `n`/`taxa`/`ages`).

What does **not** exist yet: **any alias / deprecation mechanism.** Neither
`@GeneratorInfo` nor `@ParameterInfo` has an alias field, and there is no
fallback for renamed names. Renaming anything today is a hard, script-breaking
change. §6 proposes the small infra to fix this.

## 4. Proposed canonical namespace

One user-facing name per *process*. Naming rules:

- **No `Tree` suffix** (the return type is already a tree — redundant).
- **No `Sim` prefix** on user-facing distributions; reserve `Sim*` for
  building-block simulators that are *not* registered as top-level generators.
- **`FBD` is the canonical abbreviation** (field-standard), with
  `FossilBirthDeath` accepted as an alias.

| Canonical name | Process / tree | Parameterisations (overloads) | Conditioning (optional, ≥1 where required) |
|---|---|---|---|
| `Yule` | pure-birth, reconstructed | `(lambda)` | `rootAge`, `n`, `taxa` |
| `CalibratedYule` | Yule + clade calibrations | `(lambda)` | `cladeTaxa`+`cladeMRCAAge`, `otherTaxa`, `n`, `rootAge` |
| `BirthDeath` | birth–death, reconstructed extant | `(lambda, mu, [rho])` · `(diversification, turnover, [rho])` | `rootAge`, `n`, `taxa` |
| `FullBirthDeath` | complete tree incl. extinct | `(lambda, mu)` · `(diversification, turnover)` | `rootAge` XOR `originAge` |
| `BirthDeathSerial` | serial-sampled, reconstructed | `(lambda, mu, psi, [rho])` · `(diversification, turnover, samplingProportion, [rho])` | `rootAge`, `n`, `taxa`, `ages` |
| `FBD` | fossilised BD (sampled ancestors) | `(lambda, mu, psi, [rho])` · `(diversification, turnover, samplingProportion, [rho])` | `rootAge` XOR `originAge`, `n`, `taxa` |
| `GLMBirthDeath` | GLM-driven λ, complete tree | `(beta, x0, diffRate, mu)` | `originAge` |

Building blocks **removed from the top-level registry** (kept as classes, used
internally; optionally re-exposed under an `advanced`/`Sim` category later):
`SimBDReverse`, `RhoSampleTree`, `SimFossilsPoisson`.

### Consolidations this implies

- **`BirthDeathSampling` folds into `BirthDeath`** as an optional `rho`
  (ρ = 1 ⇒ plain birth–death). Required-arg set is unchanged (`lambda, mu`), so
  no dispatch ambiguity — `rho` is simply an added optional arg.
- **`BirthDeathSerialSampling` becomes `BirthDeathSerial`** (name only).
- **`SimFBDAge` folds into `FBD`** — it is the same FBD model conditioned on
  `originAge` instead of on `taxa`/`rootAge`. This is exactly the *conditioning*
  axis, so it becomes an `originAge` conditioning option on `FBD` rather than a
  separate name. (`frac` → `rho` in the process.)
- **`…DT` classes stay as separate classes but lose nothing** — they already
  register under the sibling name; we keep that and just align the names per the
  table.

## 5. Parameter fixes

| Issue | Fix |
|---|---|
| `frac` (in `SimFBDAge`/`SimFBDAgeDT`) vs `rho` everywhere else | canonical `rho`; accept `frac` as a deprecated alias |
| conditioning args ad-hoc per class | draw `n`/`taxa`/`ages`/`rootAge`/`originAge` from a shared base; validate the legal combinations at construction |
| `samplingProportion` (= ψ/(ψ+μ)) only on some DT classes | keep name; it is the turnover-parameterisation analogue of `psi` and is already consistent where present |

Conditioning unification: `TaxaConditionedTreeGenerator` already owns
`n`/`taxa`/`ages` with lazy `Taxa` construction and `checkTaxaParameters()`.
Extend it (or a thin subclass) to also own `rootAge`/`originAge` with a single
`checkAgeParameters()` validator, then have every BD generator extend it so the
conditioning vocabulary is identical everywhere it is meaningful.

## 6. Backward compatibility (required infra)

Because no alias mechanism exists, **none of §4–§5 can ship without breaking
existing scripts** unless we add one. Recommended minimal infra:

1. **`String[] aliases() default {};` on `@GeneratorInfo`.** In `LoaderManager`
   registration, index a class under its `name()` *and* each `aliases()` entry.
   When a script resolves a generator via an alias, log a one-time deprecation
   warning.
2. **`String[] aliases() default {};` on `@ParameterInfo`.** In
   `ArgumentUtils.getArguments()` / the `Argument` model, treat an alias as a
   match for the canonical name and warn. This covers `frac` → `rho`.
3. Keep the **old class names** compilable for one release (thin
   `@Deprecated` subclasses, or just the alias on the renamed class) so any Java
   callers and serialized references keep working.

Alternative (lower-infra, higher-clutter): keep every old class registered under
its old name as a deprecated shim. The annotation approach is cleaner and is
reusable for every future rename, so it is recommended.

## 7. Old → new mapping (every generator)

| Current class | Current LPhy name | → Canonical name | Notes |
|---|---|---|---|
| `Yule` | `Yule` | `Yule` | unchanged |
| `CalibratedYule` | `CalibratedYule` | `CalibratedYule` | unchanged |
| `BirthDeathTree` | `BirthDeath` | `BirthDeath` | `(lambda, mu)` overload |
| `BirthDeathTreeDT` | `BirthDeath` | `BirthDeath` | `(diversification, turnover)` overload |
| `BirthDeathSamplingTree` | `BirthDeathSampling` | `BirthDeath` | now `(lambda, mu, rho)`; old name → alias |
| `BirthDeathSamplingTreeDT` | `BirthDeathSampling` | `BirthDeath` | `(diversification, turnover, rho)`; old name → alias |
| `FullBirthDeathTree` | `FullBirthDeath` | `FullBirthDeath` | drop `Tree` (class only) |
| `BirthDeathSerialSamplingTree` | `BirthDeathSerialSampling` | `BirthDeathSerial` | old name → alias |
| `FossilBirthDeathTree` | `FossilBirthDeathTree` | `FBD` | `FossilBirthDeath`/old name → aliases |
| `FossilBirthDeathTreeDT` | `FossilBirthDeathTreeDT` | `FBD` | DT overload; aliases as above |
| `SimFBDAge` | `SimFBDAge` | `FBD` | `originAge` conditioning overload; `frac`→`rho`; old name → alias |
| `SimFBDAgeDT` | `SimFBDAge` | `FBD` | DT + `originAge` overload |
| `GLMBirthDeathTree` | `GLMBirthDeathTree` | `GLMBirthDeath` | drop `Tree`; old name → alias |
| `SimBDReverse` | `SimBDReverse` | *(unregistered)* | internal building block |
| `RhoSampleTree` | `RhoSampleTree` | *(unregistered)* | internal building block |
| `SimFossilsPoisson` | `SimFossilsPoisson` | *(unregistered)* | internal building block |

## 8. Migration plan

1. ✅ **Infra (no behaviour change):** add `aliases` to `@GeneratorInfo` and
   `@ParameterInfo`; wire into `LoaderManager` + `ArgumentUtils`; add the
   deprecation-warning path. Land + test in isolation. **Done** — generator-name
   aliases indexed in `LoaderManager`/`LPhyCoreImpl`, parameter aliases resolved
   and canonicalised in `ParserUtils`/`Argument`, warn-once deprecation logging.
2. ✅ **Parameter fix:** `frac` → `rho` with `frac` as alias. **Done** for
   `SimFBDAge`/`SimFBDAgeDT`; bundled examples updated to `rho`.
3. ✅ **Conditioning base:** lift `rootAge`/`originAge` into the shared base with a
   validator. **Done** — new `AgeConditionedTreeGenerator` owns the age
   vocabulary + `checkAgeParameters()`; `TaxaConditionedTreeGenerator` extends it
   (3-arg constructor preserved so coalescents are untouched). Migrated:
   `Yule`, `BirthDeathTree`, `BirthDeathSerialSamplingTree` (taxa+age) and
   `FullBirthDeathTree`, `BirthDeathSamplingTree`, `SimFBDAge`,
   `GLMBirthDeathTree` (pure-age). **Deferred:** the DT wrappers
   (`BirthDeathTreeDT`, `BirthDeathSamplingTreeDT`, `FossilBirthDeathTreeDT`,
   `SimFBDAgeDT`) — they only forward age to a wrapped generator and don't extend
   a tree base, so migrating yields little for non-trivial risk.
4. ✅ **Renames + consolidations:** apply §4/§7 with old names as aliases. **Done.**
   Decisions taken: canonical fossil name is **`FossilBirthDeath`** (`FBD` and old
   names as aliases); **full fold** chosen. Folded by argument dispatch (verified
   unambiguous, disjoint required-arg sets):
   - `BirthDeathSampling`(+DT) → **`BirthDeath`** (selected by presence of `rho`).
   - `BirthDeathSerialSampling` → **`BirthDeathSerial`** (alias kept).
   - `FossilBirthDeathTree`, `FossilBirthDeathTreeDT`, `SimFBDAge`, `SimFBDAgeDT`
     → **`FossilBirthDeath`** (taxa- vs origin-conditioned selected by `originAge`;
     parameterisation by `diversification`/`turnover`). Aliases: old names + `FBD`.
   - `GLMBirthDeathTree` annotation renamed to `GLMBirthDeath` for consistency, but
     note it is **not registered** in `LPhyBaseImpl` (never script-callable).
   Building blocks `SimBDReverse`, `RhoSampleTree`, `SimFossilsPoisson` unregistered
   (internal composition only). Example/tutorial scripts updated to canonical names;
   `simFossils.lphy` removed (its building-block pipeline is no longer
   script-expressible; `simFossilsCompact.lphy` covers fossil simulation).
   Caveat: `FBD` is wired as a deprecated-style alias, so it emits the standard
   deprecation warning steering users to `FossilBirthDeath`.
5. **Deprecation window:** keep aliases for one release cycle; then remove.

## 9. Open questions

- **`FBD` vs `FossilBirthDeath` as the canonical** — abbreviation is
  field-standard but less discoverable. (Default chosen: `FBD` canonical,
  `FossilBirthDeath` alias.)
- **Should `rho` be optional on `BirthDeath`/`FBD`/`BirthDeathSerial`** (graceful
  ρ = 1 default), or kept required to force explicit modelling?
- **Do we keep the building blocks (`SimBDReverse`, `RhoSampleTree`,
  `SimFossilsPoisson`) discoverable** under an `advanced` category, or fully
  internal?
- **Alias deprecation policy:** how many releases before removal; warn-once vs
  warn-always.
