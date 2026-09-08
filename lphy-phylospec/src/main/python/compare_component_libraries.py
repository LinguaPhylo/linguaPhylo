#!/usr/bin/env python3
"""
Compares the LPhy and PhyloSpec component libraries and generates a Markdown
report of the model coverage gap between the two.

Matching happens in two layers:
  1. Exact name match (automatic).
  2. A curated equivalence map, loaded from curated_equivalences.json next to
     this script, for cases where the same concept has a different name on
     each side, including one-to-many cases (e.g. LPhy's single
     `SequenceType` corresponds to PhyloSpec's `Character` type plus its
     `Nucleotide` / `AminoAcid` subtypes). This layer is hand-maintained: name
     similarity alone produces both false positives (e.g. "sort" ~ "sqrt")
     and false negatives (e.g. "readFasta" / "fromFasta" don't share enough
     characters to score highly, but a human recognizes them instantly), so
     it can't be inferred reliably from the JSON alone.

LPhy has a third generator kind alongside constructor-based generators and symbol-bound
operators: a "method call", an instance method invoked via dot-call syntax on a value (e.g.
`tree.rootAge()`), exposed wherever a public method carries `@MethodInfo`
(`lphy.core.model.annotation.MethodInfo`; see `MethodCall` in `lphy-core` for the runtime
dispatcher). PhyloSpec has no equivalent construct at all -- no dot-call syntax exists in its
grammar -- so these are exported by `ComponentLibraryExporter`'s `buildMethodCalls()` into their
own file (`lphy-method-calls.json`, a plain classpath scan, not part of the PhyloSpec-schema-shaped
component library) and rendered here as a standalone "Method calls" table, not matched against
anything on the PhyloSpec side.

Inputs:
  - PhyloSpec core:  <phylospec repo>/core/java/src/main/resources/phylospec-core-component-library.json
  - LPhy (exported):  lphy-phylospec/src/main/resources/phylospec-lphy-component-library.json
  - LPhy method calls (exported):  lphy-phylospec/src/main/resources/lphy-method-calls.json
  - Curated equivalences:  lphy-phylospec/src/main/python/curated_equivalences.json

Output:
  - lphy-phylospec/src/main/python/model_coverage_gap.md

Usage:
  python3 compare_component_libraries.py [phylospec_json] [lphy_json] [output_md] [curated_json] [method_calls_json]
"""
import html
import json
import os
import re
import sys
from collections import defaultdict
from pathlib import Path

HOME = Path(os.environ.get("HOME", str(Path.home())))
DEFAULT_PHYLOSPEC = HOME / "WorkSpace" / "phylospec" / "core" / "java" / "src" / "main" / "resources" / "phylospec-core-component-library.json"
DEFAULT_LPHY = Path(__file__).resolve().parents[1] / "resources" / "phylospec-lphy-component-library.json"
DEFAULT_OUT = Path(__file__).resolve().parent / "model_coverage_gap.md"
DEFAULT_CURATED = Path(__file__).resolve().parent / "curated_equivalences.json"
DEFAULT_METHOD_CALLS = Path(__file__).resolve().parents[1] / "resources" / "lphy-method-calls.json"


def load(path: Path) -> dict:
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def esc(s) -> str:
    """HTML-escapes dynamic content before it goes into a raw tag. Required
    because most of what's escaped here is a PhyloSpec/LPhy type expression
    (e.g. `Distribution<Alignment<Character; numSites=siteRates.num>>`),
    which is full of literal `<`/`>` -- without escaping, a browser parsing
    the surrounding raw HTML <table> (or an inline HTML tag inside a plain
    Markdown table cell) would try to read that as more HTML tags instead of
    displaying it as text, silently mangling the row."""
    return html.escape(str(s), quote=False)


def clean_description(s) -> str:
    """Collapses whitespace runs (including literal newlines -- some LPhy
    @GeneratorInfo descriptions embed real \\n characters from Java text
    blocks) into single spaces. A raw newline inside a plain Markdown pipe-
    table cell would terminate the row and break the table, since a pipe-
    table row must be exactly one physical line; some descriptions also
    already contain a deliberate literal `<br>` (baked into the source text
    itself, e.g. BirthDeath's), which this leaves untouched since it isn't
    whitespace. Not HTML-escaped: descriptions never contain any tag other
    than that intentional `<br>` (verified against both source JSONs), so
    escaping would incorrectly turn it into literal visible text."""
    return re.sub(r"\s+", " ", s or "").strip()


def fmt_group_description(entries: list) -> str:
    """Descriptions for a generator's overloads are almost always identical
    (only 5 of 160 LPhy generators differ across overloads) -- dedupe and
    join the distinct ones with " / " rather than repeating or picking one
    arbitrarily."""
    seen = []
    for e in entries:
        d = clean_description(e.get("description"))
        if d and d not in seen:
            seen.append(d)
    return " / ".join(seen)


def index_side_notes(entries: list) -> dict:
    """Converts curated_equivalences.json's generatorNotes/typeNotes list
    ([{side, name, note}, ...]) into {"lphy": {name: note}, "phylospec": {name:
    note}} for lookup while rendering the LPhy-only/PhyloSpec-only tables.
    Unlike the equivalence lists elsewhere in that file, these entries are
    never matched into a "both" row -- the note just records something worth
    knowing about an unmatched name (e.g. why there's no counterpart)."""
    by_side = {"lphy": {}, "phylospec": {}}
    for entry in entries:
        by_side[entry["side"]][entry["name"]] = entry["note"]
    return by_side


def validate_side_notes(side_notes: dict, lphy_only: list, phylo_only: list, label: str):
    """Mirrors build_match_groups's stale-mapping check: a generatorNotes/
    typeNotes entry that no longer names a current LPhy-only/PhyloSpec-only
    name (renamed, removed, or newly matched by a curated equivalence) would
    otherwise just silently stop appearing anywhere -- fail loudly instead."""
    for name in side_notes["lphy"]:
        if name not in lphy_only:
            raise ValueError(f"{label}: note for LPhy name '{name}' no longer applies (renamed, removed, or now matched?)")
    for name in side_notes["phylospec"]:
        if name not in phylo_only:
            raise ValueError(f"{label}: note for PhyloSpec name '{name}' no longer applies (renamed, removed, or now matched?)")


class MatchConsistencyChecker:
    """Guards against one specific class of bug: a name that a "matched" table (an "In both"
    table, or the Method calls table's PhyloSpec-equivalent column) reports as matched, that a
    *different, independently-computed* "only" (gap) table still lists too -- so the finished
    report shows the same LPhy/PhyloSpec name twice, once as covered and once as a gap. This
    report has several such matched/only pairs (Types In both vs. Types only; Generators'
    Distributions/Deterministic functions/Math & Logic In both vs. Generators only; Method calls'
    PhyloSpec-equivalent column vs. PhyloSpec-only generators) built by different code paths, so
    there's no single piece of logic that already guarantees this by construction the way
    build_match_groups's own set-subtraction does *within* one call -- this exists to check the
    outcome directly instead, independent of how each side got there, so it still catches a
    regression even if some future change breaks an exclude-set computation upstream.

    Usage: call `.matched(source, side, names)` once per matched table/column to register what
    it claims is covered, then `.check(lphy_only, phylospec_only)` against the final "only" name
    sets it must not overlap with; raises with every violation listed (not just the first) if
    any name leaked through."""

    def __init__(self, section: str):
        self.section = section
        self._matched = []  # [(source_label, side, {names})]

    def matched(self, source_label: str, side: str, names) -> "MatchConsistencyChecker":
        if side not in ("lphy", "phylospec"):
            raise ValueError(f"MatchConsistencyChecker.matched: side must be 'lphy' or 'phylospec', got {side!r}")
        self._matched.append((source_label, side, set(names)))
        return self

    def check(self, lphy_only, phylospec_only):
        only_by_side = {"lphy": set(lphy_only), "phylospec": set(phylospec_only)}
        problems = []
        for source_label, side, names in self._matched:
            overlap = names & only_by_side[side]
            if overlap:
                problems.append(f"{source_label} ({side} side) also in '{side} only': {sorted(overlap)}")
        if problems:
            raise ValueError(
                f"{self.section}: name(s) reported as matched still appear in an 'only' table "
                f"they should have been excluded from -- " + "; ".join(problems)
            )


# Statement-level LPhy symbols that are real tokens in LPhy.g4 but, like their
# PhyloSpec counterparts, are never exported as generator entries in either
# component-library JSON -- so they're exempt from the "does this LPhy name
# still exist in the library" check that operators otherwise get.
NON_GENERATOR_LPHY_SYMBOLS = {"~", "=", ":"}


def validate_operators(operators: list, lphy_gen_names: set):
    """Curated_equivalences.json's operators list is hand-written (PhyloSpec
    has no component-library entries for operators at all, so there's
    nothing to cross-check it against on that side) -- but its LPhy side
    mostly does name real generator entries, so catch the same kind of
    staleness build_match_groups guards against: an LPhy operator symbol
    that's been renamed or removed would otherwise go silently missing from
    both this table and the main generators tables."""
    for op in operators:
        name = op.get("lphy")
        if name and name not in lphy_gen_names and name not in NON_GENERATOR_LPHY_SYMBOLS:
            raise ValueError(f"operators: LPhy name '{name}' not found in current LPhy library (stale mapping?)")


def operator_lphy_names(operators: list) -> set:
    """The set of every LPhy operator name that appears in curated_equivalences.json's
    operators list (all 17 symbolic operators, whether or not they have a
    PhyloSpec counterpart -- see render_operators_table). Used to keep those
    names out of the "LPhy only" generators table: they already have a
    complete, dedicated comparison in the Math & Logic section's operator
    table, so repeating them in the generic gap table would just be the same
    fact told twice -- once as "here's its PhyloSpec equivalent (or lack of
    one)", once as an apparently-unmatched row with no PhyloSpec counterpart
    shown at all. This is the single place that list of names is derived, so
    adding a new operator to curated_equivalences.json is enough on its own
    to keep it out of "LPhy only" too -- nothing else needs updating by hand."""
    return {op["lphy"] for op in operators if op.get("lphy")}


def method_call_phylospec_names(method_call_equivalent_entries: list) -> set:
    """The set of every PhyloSpec generator name referenced by curated_equivalences.json's
    methodCallEquivalents list (e.g. "age", "numBranches", "rootAge", "num"). Mirrors
    operator_lphy_names's role exactly, one layer over: used to keep those names out of the
    "PhyloSpec only" generators table, since the Method calls table's PhyloSpec-equivalent
    column already covers them -- without this, a name curated as matched there would still
    show up in "PhyloSpec only" too, reported as an apparent gap it isn't (this is the bug
    MatchConsistencyChecker's Method-calls check below exists to catch if it recurs)."""
    return {p for entry in method_call_equivalent_entries for p in entry["phylospec"]}


def render_operators_table(operators: list) -> str:
    """Raw HTML table, like render_html_table's other callers -- NOT a plain
    Markdown pipe-table. A plain Markdown table row is split on every
    unescaped `|` in the raw line with no regard for surrounding backticks
    (an earlier version of this function assumed a backtick code span would
    protect a literal `|`/`||` inside it; it doesn't -- the row for those two
    LPhy operators rendered with extra, broken columns). A real `<table>` has
    no such ambiguity: a `|` inside a `<td>` is just a character."""
    headers = ["LPhy", "PhyloSpec", "Category", "Note"]
    widths = ["10%", "10%", "20%", "60%"]
    rows = []
    for op in operators:
        p = code(op["phylospec"]) if op.get("phylospec") else em("none")
        l = code(op["lphy"]) if op.get("lphy") else em("none")
        note = esc(op.get("note", ""))
        rows.append([l, p, op["kind"], note])
    return render_html_table(headers, rows, widths)


def append_note(description: str, note: str) -> str:
    """Appends an italicized supplementary note (see index_side_notes) to a
    Description cell, escaped since it's free-form hand-written prose that
    could contain stray '<'/'>' (unlike clean_description's inputs, which are
    machine-generated and already verified not to). Source descriptions don't
    reliably end with a full stop (e.g. taxon's doesn't), so one is inserted
    before the note if missing -- otherwise the two run together as one
    unpunctuated sentence."""
    if not note:
        return description
    noted = f"*{esc(note)}*"
    if not description:
        return noted
    sep = "" if description.rstrip().endswith((".", "!", "?")) else "."
    return f"{description}{sep} {noted}"


def strong(s: str) -> str:
    return f"<strong>{esc(s)}</strong>"


def code(s: str) -> str:
    return f"<code>{esc(s)}</code>"


def em(s: str) -> str:
    return f"<em>{esc(s)}</em>"


def md_table_cell(s: str) -> str:
    """Backslash-escapes a literal '|' so it survives as plain text inside a
    plain-Markdown pipe-table cell instead of being read as an extra column
    delimiter -- GFM table row splitting is lexical on the raw line and does
    NOT know about the HTML tags wrapping it (e.g. strong()'s <strong>|
    </strong>), so this has to run on top of strong()/code()/em() output, not
    instead of it. Needed for LPhy's two pipe-shaped generator names, `|` and
    `||` (bitwise/logical OR) -- without it their row in a plain-Markdown
    table silently gets read as having extra columns."""
    return s.replace("|", "\\|")


def render_html_table(headers: list, rows: list, widths: list) -> str:
    """A raw HTML <table> with an explicit <colgroup> so columns get a fixed
    share of the table width regardless of content (a plain Markdown pipe
    table has no width syntax at all -- only left/center/right alignment
    colons -- so getting real width control means dropping to HTML here).
    Uses inline HTML tags for bold/code (via strong()/code()/em() above)
    rather than Markdown **/backtick syntax throughout the report, since a
    raw HTML block is opaque to the Markdown parser: **bold** typed literally
    inside a <table> would render as literal asterisks, not bold text."""
    colgroup = "".join(f'<col width="{w}" style="width:{w}">' for w in widths)
    thead = "<tr>" + "".join(f"<th>{h}</th>" for h in headers) + "</tr>"
    tbody = "\n".join("<tr>" + "".join(f"<td>{c}</td>" for c in row) + "</tr>" for row in rows)
    return (
        f"<table>\n<colgroup>{colgroup}</colgroup>\n"
        f"<thead>\n{thead}\n</thead>\n<tbody>\n{tbody}\n</tbody>\n</table>"
    )


def fmt_args(args: list) -> str:
    """Bold required args; leave optional args plain. Show type in <code>."""
    if not args:
        return em("(no arguments)")
    parts = []
    for a in args:
        name = a.get("name", "?")
        typ = a.get("type", "?")
        required = a.get("required", True)
        default = a.get("default", None)
        if required:
            token = f"{strong(name)}: {code(typ)}"
        else:
            if default is not None:
                token = f"{name}: {code(typ)} = {code(default)}"
            else:
                token = f"{name}: {code(typ)}"
        parts.append(token)
    return ", ".join(parts)


def fmt_overload(entry: dict) -> str:
    args_str = fmt_args(entry.get("arguments", []))
    ret = entry.get("generatedType", "?")
    return f"({args_str}) &rarr; {code(ret)}"


def fmt_overloads(entries: list) -> str:
    """Renders one line per overload, de-duplicating overloads that render
    identically (the source JSON lists one entry per underlying Java class,
    so the same LPhy call shape sometimes appears 2-3x in a row -- that's
    not extra information, just noise that bloats the cell)."""
    lines = []
    for e in entries:
        line = fmt_overload(e)
        if line not in lines:
            lines.append(line)
    if len(lines) == 1:
        return lines[0]
    return "<br>".join(f"{i+1}. {line}" for i, line in enumerate(lines))


def group_by_name(items: list) -> dict:
    grouped = defaultdict(list)
    for item in items:
        grouped[item["name"]].append(item)
    return grouped


def fmt_method_signature(m: dict) -> str:
    """A dot-call signature, e.g. `.taxa() -> Taxa` or `.charset(String) -> Alignment`. Unlike
    fmt_overload's constructor arguments, method-call arguments have no real names to show:
    LPhy method calls are always positional (see MethodCall#argParamName, "arg" + index, which
    is the only name they ever get even internally -- there's no @ParameterInfo-equivalent for
    them), so only the bare argument types are rendered."""
    args_str = ", ".join(code(a["type"]) for a in m.get("arguments", []))
    return f"{strong('.' + m['name'])}({args_str}) &rarr; {code(m['returnType'])}"


def truncate_words(text: str, limit: int = 15) -> str:
    """Caps a description cell at `limit` words, for a table where every row's Description column
    should read as one short phrase rather than the full prose lifted from a Java doc comment
    (some @MethodInfo descriptions run 2-3 sentences, e.g. MetaDataAlignment's charset()). Applied
    to the whole already-joined/deduped string, not per source description, so a two-description
    row (e.g. "the taxa of the tree. / the taxa of the alignment.") is capped as one phrase rather
    than each half being separately truncated."""
    words = text.split()
    if len(words) <= limit:
        return text
    return " ".join(words[:limit]) + " …"


def fmt_method_description(classes: list) -> str:
    """Same dedupe-and-join convention as fmt_group_description (distinct descriptions joined
    by " / ", identical ones collapsed) -- but escaped here, unlike that one: constructor
    descriptions are machine-generated and verified tag-free, while these are hand-written prose
    from many different contributors across lphy/lphy-base's whole source tree, with no such
    guarantee. Truncated to keep the merged Description column skimmable (see truncate_words)."""
    seen = []
    for c in classes:
        d = clean_description(c.get("description"))
        if d and d not in seen:
            seen.append(d)
    return truncate_words(" / ".join(esc(d) for d in seen))


def method_call_key(m: dict) -> tuple:
    """(name, returnType): identifies one method-call row. Not full signature (argument types
    omitted) -- resolve_method_call_equivalents only needs enough to disambiguate the one current
    same-name collision (nodeCount: TimeTree -> Integer, Taxa -> int), and requiring callers to
    additionally spell out argument types for every curated entry, including the ~37 with none,
    would be pure friction for no real benefit today. If a future same-name-and-return-type but
    different-arguments collision ever appears, extend this (and lphyReturnType's role in
    curated_equivalences.json) to include argument types then, not preemptively now."""
    return (m["name"], m["returnType"])


def resolve_method_call_equivalents(entries: list, method_calls: list, phylo_gen_names: set) -> dict:
    """Turns curated_equivalences.json's methodCallEquivalents list into a {method_call_key():
    entry} lookup, validating as it goes -- same stale-mapping philosophy as build_match_groups
    and validate_operators: a curated entry naming an LPhy method or PhyloSpec generator that no
    longer exists should fail loudly, not silently stop appearing anywhere."""
    by_name = defaultdict(list)
    for m in method_calls:
        by_name[m["name"]].append(m)

    resolved = {}
    for entry in entries:
        name = entry["lphyMethod"]
        candidates = by_name.get(name, [])
        if not candidates:
            raise ValueError(f"methodCallEquivalents: LPhy method '{name}' not found in lphy-method-calls.json (renamed, removed, or now matched?)")
        if len(candidates) > 1:
            return_type = entry.get("lphyReturnType")
            if not return_type:
                raise ValueError(
                    f"methodCallEquivalents: '{name}' is ambiguous in lphy-method-calls.json "
                    f"({len(candidates)} return types) -- add \"lphyReturnType\" to disambiguate"
                )
            candidates = [c for c in candidates if c["returnType"] == return_type]
            if len(candidates) != 1:
                raise ValueError(f"methodCallEquivalents: '{name}' with returnType '{return_type}' matches {len(candidates)} entries, expected exactly 1")
        for p in entry["phylospec"]:
            if p not in phylo_gen_names:
                raise ValueError(f"methodCallEquivalents: PhyloSpec name '{p}' not found in current PhyloSpec library (stale mapping?)")
        resolved[method_call_key(candidates[0])] = entry
    return resolved


def render_method_calls_table(method_calls: list, phylo_grouped: dict, method_call_equivalents: dict) -> str:
    """Raw HTML table (see render_operators_table's docstring for why: a plain Markdown pipe-table
    row silently breaks on an unescaped `|` even inside backticks, and while no method name here
    happens to contain one today, there's no reason to leave that trap for whatever gets added to
    lphy-method-calls.json next). One row per distinct (name, return type, argument types) shape,
    already grouped that way on the Java side (ComponentLibraryExporter#buildMethodCalls) -- not
    grouped by a shared Java declaration, so two unrelated classes that happen to both provide the
    same-shaped call (e.g. TimeTree.taxa() and Alignment.taxa() -- unrelated types, no common
    ancestor declaring "taxa") collapse into one row here exactly like a true override chain would
    (e.g. NChar.nchar() overridden by AbstractAlignment.nchar()). That's deliberate: from an LPhy
    script's point of view "what does .taxa() do, and on what" is the useful question, regardless of
    whether the classes that answer it share an ancestor -- but it does mean the implementing-class
    list means "declares this exact call shape," not "implements a common interface method." The
    signature and its implementing classes share one "LPhy method call" cell (signature first, one
    class per line below) rather than two separate columns: a reader's first question is "what is
    this call, and on what" as one thought, and splitting it across columns just makes that one
    lookup into two. PhyloSpec equivalent comes from method_call_equivalents (see
    resolve_method_call_equivalents) -- hand-curated, since PhyloSpec's side of any of these is
    always a plain function, never a real syntax match, so nothing here is auto-detectable the way
    an exact-name generator match is; left blank (not "none") when there isn't one, matching how an
    absent Notes cell reads elsewhere in this report. Description is one merged column: a mapped
    row's short, hand-written equivalence note (already <= 15 words in curated_equivalences.json, so
    shown as-is) takes priority over the LPhy-side description there, since it's the more useful of
    the two once a PhyloSpec equivalent is already shown alongside it; an unmapped row falls back to
    the LPhy-side description, word-capped by fmt_method_description/truncate_words instead."""
    headers = ["LPhy method call", "PhyloSpec equivalent", "Description"]
    widths = ["32%", "24%", "44%"]
    rows = []
    for m in method_calls:
        classes_lines = "<br>".join(
            f"{strong(c['class'])} — {code(c['namespace'])}" for c in m["classes"]
        )
        lphy_cell = f"{fmt_method_signature(m)}<br>{classes_lines}"
        equivalent = method_call_equivalents.get(method_call_key(m))
        if equivalent:
            phylospec_cell = "<br>".join(
                f"{strong(p)}<br>{fmt_overloads(phylo_grouped[p])}" for p in equivalent["phylospec"]
            )
            description = esc(equivalent.get("note", ""))
        else:
            phylospec_cell = ""
            description = fmt_method_description(m["classes"])
        rows.append([lphy_cell, phylospec_cell, description])
    return render_html_table(headers, rows, widths)


def type_namespace_and_extra(t: dict) -> tuple:
    """Splits a type's info into (namespace, extra), where extra is whatever
    of extends/alias/typeParameters/typeProperties it carries, comma-joined
    into one string ("" if none). Kept separate from namespace so a cell
    listing several types can group by namespace independently of whether
    their extra info happens to match too (see fmt_grouped_type_cell)."""
    extra_parts = []
    if t.get("extends"):
        extra_parts.append(f"extends {code(t['extends'])}")
    if t.get("alias"):
        extra_parts.append(f"alias of {code(t['alias'])}")
    if t.get("typeParameters") and t["typeParameters"] != ["T"]:
        extra_parts.append(f"params: {', '.join(t['typeParameters'])}")
    if t.get("typeProperties"):
        extra_parts.append(f"props: {', '.join(t['typeProperties'])}")
    return t.get("namespace", ""), ", ".join(extra_parts)


def fmt_type_line(ns: str, extra: str) -> str:
    """Namespace plus any extra info, always on one line (comma/dash-joined,
    never <br>-stacked) -- used for a single, non-grouped type."""
    if not ns:
        return extra
    return f"{code(ns)} — {extra}" if extra else code(ns)


def fmt_grouped_type_cell(names: list, info_fn) -> str:
    """Renders a (possibly grouped) type cell, e.g. the LPhy array types that
    all map to PhyloSpec's Vector. Two independent groupings, not one
    all-or-nothing match on a combined detail string:
      1. Names are grouped by namespace alone (comma-joined names, namespace
         shown once per *group* rather than once per name) -- so e.g. 5 of 7
         array types sharing `java.lang` still collapse together even though
         the other 2 live elsewhere, rather than nothing collapsing just
         because not every single item matches.
      2. Any extends/alias/params/props a name carries is listed separately,
         on a further comma-joined line -- so a shared namespace still
         collapses even when per-item details differ (e.g. Nucleotide/
         AminoAcid extend Character but Character itself doesn't).
    A single-name cell degrades to the same one-line "namespace — extra"
    shape fmt_type_line uses, rather than a separate line for the extra."""
    infos = [info_fn(n) for n in names]
    if len(names) == 1:
        ns, extra = infos[0]
        line = fmt_type_line(ns, extra)
        return f"{strong(names[0])} — {line}" if line else strong(names[0])
    groups = []  # [(namespace, [names])], first-seen order
    for n, (ns, _) in zip(names, infos):
        for g in groups:
            if g[0] == ns:
                g[1].append(n)
                break
        else:
            groups.append((ns, [n]))
    lines = [
        f"{', '.join(strong(n) for n in group_names)} — {code(ns)}" if ns
        else ", ".join(strong(n) for n in group_names)
        for ns, group_names in groups
    ]
    extras = [f"{n}: {extra}" for n, (_, extra) in zip(names, infos) if extra]
    if extras:
        lines.append(", ".join(extras))
    return "<br>".join(lines)


def build_match_groups(lphy_names: set, phylo_names: set, curated: list, label: str):
    """Combines exact-name matches with the curated equivalence list into a
    single list of {"lphy": [...], "phylospec": [...], "note": optional}
    groups, and returns the remaining unmatched name sets. Validates that
    curated entries still refer to real, currently-unmatched names (so a
    rename or removal in the source JSON surfaces as a loud error instead of
    silently going stale).

    A name referenced anywhere in the curated list (either side) is excluded
    from the automatic exact-name pass, even if it would otherwise auto-match
    itself -- this lets a curated entry absorb an exact match into a bigger
    group instead of conflicting with it, e.g. LPhy's Integer auto-matches
    PhyloSpec's Integer, but the curated list folds NonNegativeInteger /
    PositiveInteger / Count into that same row, so the entry lists
    ["Integer", "NonNegativeInteger", "PositiveInteger", "Count"] on the
    PhyloSpec side including "Integer" itself, and the automatic pass steps
    aside for that name entirely."""
    curated_lphy_referenced = {n for entry in curated for n in entry["lphy"]}
    curated_phylo_referenced = {n for entry in curated for n in entry["phylospec"]}
    auto_exact = sorted(
        (lphy_names & phylo_names) - curated_lphy_referenced - curated_phylo_referenced, key=str.lower
    )
    groups = [{"lphy": [n], "phylospec": [n], "note": None} for n in auto_exact]
    matched_lphy = set(auto_exact)
    matched_phylo = set(auto_exact)

    for entry in curated:
        for n in entry["lphy"]:
            if n not in lphy_names:
                raise ValueError(f"{label}: curated LPhy name '{n}' not found in current LPhy library (stale mapping?)")
            if n in matched_lphy:
                raise ValueError(f"{label}: curated LPhy name '{n}' already matched elsewhere")
        for n in entry["phylospec"]:
            if n not in phylo_names:
                raise ValueError(f"{label}: curated PhyloSpec name '{n}' not found in current PhyloSpec library (stale mapping?)")
            if n in matched_phylo:
                raise ValueError(f"{label}: curated PhyloSpec name '{n}' already matched elsewhere")
        groups.append({"lphy": entry["lphy"], "phylospec": entry["phylospec"], "note": entry.get("note")})
        matched_lphy.update(entry["lphy"])
        matched_phylo.update(entry["phylospec"])

    groups.sort(key=lambda g: (g["lphy"][0].lower(), g["phylospec"][0].lower()))
    lphy_only = sorted(lphy_names - matched_lphy, key=str.lower)
    phylo_only = sorted(phylo_names - matched_phylo, key=str.lower)
    return groups, lphy_only, phylo_only


def build_types_tables(lphy_types: list, phylospec_types: list, curated_types: list, type_notes: dict):
    """Returns (both_md, lphy_only_md, phylospec_only_md, lphy_only_names,
    phylospec_only_names). The "both" table has two name columns (LPhy /
    PhyloSpec) so a same-named match still shows both cells explicitly, and a
    curated one-to-many match (e.g. SequenceType -> Character, Nucleotide,
    AminoAcid) lists every item on its side of the row, stacked with <br>,
    rather than a rowspanned cell -- the "both" table is real HTML (see
    render_html_table) so rowspan is technically available, but stacking
    keeps one row per matched concept, consistent with the plain-Markdown
    LPhy-only/PhyloSpec-only tables below it."""
    lphy_by_name = {t["name"]: t for t in lphy_types}
    phylo_by_name = {t["name"]: t for t in phylospec_types}
    groups, lphy_only_names, phylo_only_names = build_match_groups(
        set(lphy_by_name), set(phylo_by_name), curated_types, "types"
    )

    has_notes = any(g["note"] for g in groups)
    headers = ["LPhy", "PhyloSpec", "Notes"] if has_notes else ["LPhy", "PhyloSpec"]
    widths = ["35%", "35%", "30%"] if has_notes else ["50%", "50%"]
    both_table_rows = []
    for g in groups:
        l_cell = fmt_grouped_type_cell(g["lphy"], lambda n: type_namespace_and_extra(lphy_by_name[n]))
        p_cell = fmt_grouped_type_cell(g["phylospec"], lambda n: type_namespace_and_extra(phylo_by_name[n]))
        row = [l_cell, p_cell]
        if has_notes:
            row.append(esc(g["note"]) if g["note"] else "")
        both_table_rows.append(row)
    both_md = render_html_table(headers, both_table_rows, widths)

    lphy_only_rows = ["| Type | LPhy | Description |", "|---|---|---|"]
    for name in lphy_only_names:
        l = lphy_by_name[name]
        description = append_note(clean_description(l.get("description")), type_notes["lphy"].get(name))
        lphy_only_rows.append(
            f"| {strong(name)} | {fmt_type_line(*type_namespace_and_extra(l))} | {description} |"
        )

    phylo_only_rows = ["| Type | PhyloSpec | Description |", "|---|---|---|"]
    for name in phylo_only_names:
        p = phylo_by_name[name]
        description = append_note(clean_description(p.get("description")), type_notes["phylospec"].get(name))
        phylo_only_rows.append(
            f"| {strong(name)} | {fmt_type_line(*type_namespace_and_extra(p))} | {description} |"
        )

    return (
        both_md, "\n".join(lphy_only_rows), "\n".join(phylo_only_rows),
        lphy_only_names, phylo_only_names, len(groups),
    )


def is_lphy_distribution(entries: list) -> bool:
    """LPhy is the identifier for the Distribution/DeterministicFunction split:
    ComponentLibraryExporter wraps a GenerativeDistribution's generatedType as
    `Distribution<T>` and leaves a DeterministicFunction's as plain `T` (see
    buildGenerators's isDistribution flag) -- that wrapper is reliably present
    regardless of what PhyloSpec calls the matched concept, so check it rather
    than inventing a second classification off the PhyloSpec side."""
    return any(e.get("generatedType", "").startswith("Distribution<") for e in entries)


def is_phylospec_math_logic(entries: list) -> bool:
    """PhyloSpec files its symbolic operators and elementary math functions
    under one `phylospec.functions.math` namespace (log, exp, sqrt, sum,
    range, repeat, linspace) -- check that namespace on the PhyloSpec side
    rather than inventing a parallel LPhy-side classification, since LPhy's
    own GeneratorCategory enum has no math bucket at all (these generators
    all fall under its catch-all NONE/unclassified category there). Only
    ever reached for matched ("both") groups: LPhy's raw operators (+, -,
    ==, &&, !, ...) never appear here, because PhyloSpec has no generator
    counterpart for them at all -- it resolves them in the parser's operator
    rule table (Parser.visitBinary/visitUnary via TypeResolver), not as
    named, callable generators -- so they only ever show up in the LPhy-only
    table, not this one."""
    return any(e.get("namespace", "") == "phylospec.functions.math" for e in entries)


def build_generators_tables(
    lphy_gens: list, phylo_gens: list, curated_generators: list, generator_notes: dict,
    exclude_lphy_only: set = frozenset(), exclude_phylo_only: set = frozenset(),
):
    """Returns the same shape as build_types_tables, except the "both" slot is
    itself a (distributions_md, functions_md, math_logic_md) triple: the "In
    both" table is split first into Distribution vs DeterministicFunction
    generators, classified by the LPhy side (see is_lphy_distribution) since
    that's the side with an unambiguous, already-exported signal for it, and
    then the non-distribution remainder is further split into ordinary
    DeterministicFunction generators vs "Math & Logic" ones, classified by
    the PhyloSpec side (see is_phylospec_math_logic) since PhyloSpec is the
    side with an explicit namespace for that grouping. LPhy-only/PhyloSpec-only
    stay single tables, unsplit -- except exclude_lphy_only/exclude_phylo_only,
    which drop any name already covered by its own dedicated table elsewhere
    in the report (currently: operator_lphy_names() on the LPhy side, since
    the Math & Logic section's operator table is a complete, standalone
    comparison for those names; method_call_phylospec_names() on the
    PhyloSpec side, since the Method calls table's PhyloSpec-equivalent
    column already covers those) rather than also listing them here with
    nothing on the other side to show. A MatchConsistencyChecker verifies
    that exclusion actually took (see its own docstring for why that's worth
    checking independently rather than trusting the subtraction below)."""
    lphy_grouped = group_by_name(lphy_gens)
    phylo_grouped = group_by_name(phylo_gens)
    groups, lphy_only_names, phylo_only_names = build_match_groups(
        set(lphy_grouped), set(phylo_grouped), curated_generators, "generators"
    )
    lphy_only_names = [n for n in lphy_only_names if n not in exclude_lphy_only]
    phylo_only_names = [n for n in phylo_only_names if n not in exclude_phylo_only]

    has_notes = any(g["note"] for g in groups)
    headers = ["LPhy", "PhyloSpec", "Notes"] if has_notes else ["LPhy", "PhyloSpec"]
    widths = ["35%", "35%", "30%"] if has_notes else ["50%", "50%"]

    def render_group_table(group_subset):
        rows = []
        for g in group_subset:
            l_cell = "<br><br>".join(f"{strong(n)}<br>{fmt_overloads(lphy_grouped[n])}" for n in g["lphy"])
            p_cell = "<br><br>".join(f"{strong(n)}<br>{fmt_overloads(phylo_grouped[n])}" for n in g["phylospec"])
            row = [l_cell, p_cell]
            if has_notes:
                row.append(esc(g["note"]) if g["note"] else "")
            rows.append(row)
        return render_html_table(headers, rows, widths)

    # groups is already sorted alphabetically by g["lphy"][0] (build_match_groups);
    # partitioning preserves that order in each part.
    distribution_groups = []
    math_logic_groups = []
    function_groups = []
    for g in groups:
        if is_lphy_distribution(lphy_grouped[g["lphy"][0]]):
            distribution_groups.append(g)
        elif is_phylospec_math_logic(phylo_grouped[g["phylospec"][0]]):
            math_logic_groups.append(g)
        else:
            function_groups.append(g)
    both_distributions_md = render_group_table(distribution_groups)
    both_functions_md = render_group_table(function_groups)
    both_math_logic_md = render_group_table(math_logic_groups)

    lphy_only_rows = ["| Generator | LPhy signature(s) &rarr; return type | Description |", "|---|---|---|"]
    for name in lphy_only_names:
        description = append_note(fmt_group_description(lphy_grouped[name]), generator_notes["lphy"].get(name))
        lphy_only_rows.append(
            f"| {md_table_cell(strong(name))} | {fmt_overloads(lphy_grouped[name])} | {description} |"
        )

    phylo_only_rows = ["| Generator | PhyloSpec signature(s) &rarr; return type | Description |", "|---|---|---|"]
    for name in phylo_only_names:
        description = append_note(fmt_group_description(phylo_grouped[name]), generator_notes["phylospec"].get(name))
        phylo_only_rows.append(
            f"| {md_table_cell(strong(name))} | {fmt_overloads(phylo_grouped[name])} | {description} |"
        )

    checker = MatchConsistencyChecker("Generators")
    for label, group_subset in (
        ("Distributions (In both)", distribution_groups),
        ("Deterministic functions (In both)", function_groups),
        ("Math & Logic (In both)", math_logic_groups),
    ):
        checker.matched(label, "lphy", (n for g in group_subset for n in g["lphy"]))
        checker.matched(label, "phylospec", (n for g in group_subset for n in g["phylospec"]))
    checker.check(lphy_only_names, phylo_only_names)

    return (
        both_distributions_md, both_functions_md, both_math_logic_md,
        "\n".join(lphy_only_rows), "\n".join(phylo_only_rows),
        lphy_only_names, phylo_only_names,
        len(distribution_groups), len(function_groups), len(math_logic_groups),
    )


def main():
    phylospec_path = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_PHYLOSPEC
    lphy_path = Path(sys.argv[2]) if len(sys.argv) > 2 else DEFAULT_LPHY
    out_path = Path(sys.argv[3]) if len(sys.argv) > 3 else DEFAULT_OUT
    curated_path = Path(sys.argv[4]) if len(sys.argv) > 4 else DEFAULT_CURATED
    method_calls_path = Path(sys.argv[5]) if len(sys.argv) > 5 else DEFAULT_METHOD_CALLS

    phylospec = load(phylospec_path)["componentLibrary"]
    lphy = load(lphy_path)["componentLibrary"]
    curated = load(curated_path)
    method_calls = load(method_calls_path).get("methodCalls", [])
    curated_types = curated.get("types", [])
    curated_generators = curated.get("generators", [])
    type_notes = index_side_notes(curated.get("typeNotes", []))
    generator_notes = index_side_notes(curated.get("generatorNotes", []))
    operators = curated.get("operators", [])
    method_call_equivalent_entries = curated.get("methodCallEquivalents", [])

    phylo_types = phylospec.get("types", [])
    lphy_types = lphy.get("types", [])
    phylo_gens = phylospec.get("generators", [])
    lphy_gens = lphy.get("generators", [])

    lphy_type_names = {t["name"] for t in lphy_types}
    phylo_type_names = {t["name"] for t in phylo_types}
    lphy_gen_names = {g["name"] for g in lphy_gens}
    phylo_gen_names = {g["name"] for g in phylo_gens}
    phylo_grouped = group_by_name(phylo_gens)
    method_call_equivalents = resolve_method_call_equivalents(
        method_call_equivalent_entries, method_calls, phylo_gen_names
    )

    (types_both_md, types_lphy_md, types_phylo_md,
     types_lphy_only, types_phylo_only, types_both_count) = build_types_tables(
        lphy_types, phylo_types, curated_types, type_notes)
    (gens_both_distributions_md, gens_both_functions_md, gens_both_math_logic_md, gens_lphy_md, gens_phylo_md,
     gens_lphy_only, gens_phylo_only, gens_both_dist_count, gens_both_func_count, gens_both_math_logic_count
     ) = build_generators_tables(
        lphy_gens, phylo_gens, curated_generators, generator_notes,
        operator_lphy_names(operators), method_call_phylospec_names(method_call_equivalent_entries),
    )
    gens_both_count = gens_both_dist_count + gens_both_func_count + gens_both_math_logic_count

    MatchConsistencyChecker("Generators / Method calls").matched(
        "Method calls (PhyloSpec equivalent)", "phylospec", method_call_phylospec_names(method_call_equivalent_entries)
    ).check(lphy_only=[], phylospec_only=gens_phylo_only)

    validate_side_notes(type_notes, types_lphy_only, types_phylo_only, "types")
    validate_side_notes(generator_notes, gens_lphy_only, gens_phylo_only, "generators")
    validate_operators(operators, lphy_gen_names)
    operators_md = render_operators_table(operators)

    lines = []
    lines.append("# LPhy vs PhyloSpec Model Coverage Gap")
    lines.append("")
    lines.append(f"- PhyloSpec core library version: `{phylospec.get('version')}`")
    lines.append(f"- LPhy exported library version: `{lphy.get('version')}`")
    lines.append("")
    lines.append(
        "This report matches types/generators between LPhy and PhyloSpec first by "
        "**exact name**, then by a small hand-curated equivalence list for concepts "
        "that carry a different name on each side (see `curated_equivalences.json` "
        "next to the script) -- string similarity alone can't be trusted for this "
        "(it both misses real renames like `readFasta` / `fromFasta` and flags "
        "coincidental non-matches like `sort` / `sqrt`), so this layer is maintained "
        "by hand and reviewed for correctness, not generated. In a \"both\" row, the "
        "LPhy and PhyloSpec cells are always shown side by side even when the name is "
        "identical; a one-to-many equivalence (e.g. LPhy's single `SequenceType` "
        "against PhyloSpec's `Character`/`Nucleotide`/`AminoAcid`) stacks every item "
        "in that side's cell rather than merging table cells. Where a name has "
        "multiple overloads (different argument lists), all overloads are listed in "
        "the same cell, numbered. Required arguments are shown in **bold**; optional "
        "arguments are shown plain (with `= default` when a default value is defined)."
    )
    lines.append("")
    lines.append("## Summary")
    lines.append("")
    lines.append("| | LPhy | PhyloSpec | In both | LPhy only | PhyloSpec only |")
    lines.append("|---|---|---|---|---|---|")
    lines.append(
        f"| **Types** | {len(lphy_type_names)} | {len(phylo_type_names)} | "
        f"{types_both_count} | {len(types_lphy_only)} | {len(types_phylo_only)} |"
    )
    lines.append(
        f"| **Generators** (distinct names; overloads collapsed) | {len(lphy_gen_names)} | "
        f"{len(phylo_gen_names)} | {gens_both_count} | {len(gens_lphy_only)} | {len(gens_phylo_only)} |"
    )
    lines.append(
        f"| **Generators** (including overloads) | {len(lphy_gens)} | {len(phylo_gens)} | | | |"
    )
    lines.append(
        f"| **Method calls** (distinct call shapes) | {len(method_calls)} | *(n/a)* | *(n/a)* | *(n/a)* | *(n/a)* |"
    )
    lines.append("")
    lines.append(
        "*(n/a): PhyloSpec has no dot-call method syntax at all, so LPhy's method calls -- unlike "
        "its types and constructor-based generators -- have nothing on the PhyloSpec side to be "
        "matched against or missing from; see the Method calls subsection under Generators below.*"
    )
    lines.append("")

    lines.append("## Types")
    lines.append("")
    lines.append(f"### In both ({types_both_count})")
    lines.append("")
    lines.append(types_both_md)
    lines.append("")
    lines.append(f"### LPhy only ({len(types_lphy_only)})")
    lines.append("")
    lines.append(types_lphy_md)
    lines.append("")
    lines.append(f"### PhyloSpec only ({len(types_phylo_only)})")
    lines.append("")
    lines.append(types_phylo_md)
    lines.append("")

    lines.append("## Generators")
    lines.append("")
    lines.append(
        "*Note: some `Number` arguments in LPhy accept either a fixed literal or a "
        "random variable / expression at runtime; PhyloSpec's stricter types "
        "(e.g. `PositiveReal`, `Rate`, `Probability`) are the closest static "
        "equivalent, not a 1:1 match.*"
    )
    lines.append("")
    lines.append(f"### In both ({gens_both_count})")
    lines.append("")
    lines.append(
        "Split by generator kind, as identified on the LPhy side (whether the "
        "implementing class is a `GenerativeDistribution` or a `DeterministicFunction`"
        " -- see `is_lphy_distribution()`), since that's an unambiguous, already-"
        "exported signal regardless of what PhyloSpec calls the matched concept."
    )
    lines.append("")
    lines.append(f"#### Distributions ({gens_both_dist_count})")
    lines.append("")
    lines.append(gens_both_distributions_md)
    lines.append("")
    lines.append(f"#### Deterministic functions ({gens_both_func_count})")
    lines.append("")
    lines.append(gens_both_functions_md)
    lines.append("")
    lines.append(f"#### Method calls ({len(method_calls)})")
    lines.append("")
    lines.append(
        "LPhy's third generator kind is the **method call** -- an instance method invoked with dot "
        "syntax on a value, like `tree.rootAge()` or `alignment.taxa()`, rather than as a "
        "stand-alone function. PhyloSpec has no dot-call syntax at all, so these aren't matched the "
        "way types and generators are above; instead, the \"PhyloSpec equivalent\" column below shows "
        "one directly wherever a close match exists -- usually the same idea as a plain function that "
        "takes the object as its first argument, e.g. `age(taxon)` instead of `taxon.age()`. A row "
        "can list more than one implementing class when several classes provide the exact same call: "
        "sometimes because one really overrides another's, sometimes just because two unrelated types "
        "happen to offer the same-named, same-shaped method (e.g. both `TimeTree` and `Alignment` have "
        "a `.taxa()`, with no shared ancestor behind it)."
    )
    lines.append("")
    lines.append(render_method_calls_table(method_calls, phylo_grouped, method_call_equivalents))
    lines.append("")
    lines.append(f"#### Math & Logic ({gens_both_math_logic_count})")
    lines.append("")
    lines.append("LPhy and PhyloSpec handle operators (`+`, `<`, `&&`, ...) very differently:")
    lines.append("")
    lines.append(
        "- **LPhy** treats every operator as an ordinary function named after its symbol -- "
        "`+` is really just a function called \"+\", the same way `abs` or `hky` are functions. "
        "It shows up in LPhy's library like any other generator, with its own argument and "
        "return types."
    )
    lines.append(
        "- **PhyloSpec** treats operators as part of the language grammar, not as functions. "
        "Only 11 operators are built in (`+ - * / == != > >= < <= !`); a separate type-checking "
        "step decides the result type for each one (e.g. `PositiveReal + PositiveReal` stays "
        "`PositiveReal`, but `Real + Real` only gives `Real`)."
    )
    lines.append(
        "- Because PhyloSpec's operators aren't functions, they're never listed in its "
        "component library -- so they can never show up as a \"matched\" row anywhere in the "
        "Generators section of this report, no matter how the names line up."
    )
    lines.append("")
    lines.append(
        "The table below maps every operator PhyloSpec supports to its LPhy equivalent. The "
        "last six rows are LPhy operators (`% ** & && | ||`) that PhyloSpec doesn't support at "
        "all -- not just unmatched, but not valid syntax in a PhyloSpec model. Notably, that "
        "means PhyloSpec currently has no way to combine two `Boolean` conditions into one (no "
        "`&&` or `||`)."
    )
    lines.append("")
    lines.append(operators_md)
    lines.append("")
    lines.append(
        f"Since operators are never generators in PhyloSpec, the {gens_both_math_logic_count} "
        f"matched rows below aren't operators at all -- they're LPhy's *named* math functions "
        f"(`exp`, `log`, `sqrt`, `sum`, `range`, `repeat`) that happen to also exist as callable "
        f"generators in PhyloSpec."
    )
    lines.append("")
    lines.append(gens_both_math_logic_md)
    lines.append("")
    lines.append(f"### LPhy only ({len(gens_lphy_only)})")
    lines.append("")
    lines.append(
        "LPhy's 17 symbolic operators (`+ - * / % ** == != < <= > >= && || ! & |`) are excluded "
        "from this table -- they're already covered, matched or not, by the operator table in the "
        "Math & Logic section above."
    )
    lines.append("")
    lines.append(gens_lphy_md)
    lines.append("")
    lines.append(f"### PhyloSpec only ({len(gens_phylo_only)})")
    lines.append("")
    lines.append(gens_phylo_md)
    lines.append("")

    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {out_path}")
    print(f"Types: both={types_both_count} lphy_only={len(types_lphy_only)} phylospec_only={len(types_phylo_only)}")
    print(f"Generators: both={gens_both_count} lphy_only={len(gens_lphy_only)} phylospec_only={len(gens_phylo_only)}")
    print(f"Method calls: {len(method_calls)}")


if __name__ == "__main__":
    main()
