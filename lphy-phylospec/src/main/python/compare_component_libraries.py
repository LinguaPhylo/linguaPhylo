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
     it can't be inferred reliably from the JSON alone. See the
     `find_near_matches` heuristic for a *candidate* list to promote into
     that file after a manual check.

Inputs:
  - PhyloSpec core:  <phylospec repo>/core/java/src/main/resources/phylospec-core-component-library.json
  - LPhy (exported):  lphy-phylospec/src/main/resources/phylospec-lphy-component-library.json
  - Curated equivalences:  lphy-phylospec/src/main/python/curated_equivalences.json

Output:
  - lphy-phylospec/src/main/python/model_coverage_gap.md

Usage:
  python3 compare_component_libraries.py [phylospec_json] [lphy_json] [output_md] [curated_json]
"""
import html
import json
import os
import re
import sys
from collections import defaultdict
from difflib import SequenceMatcher
from pathlib import Path

HOME = Path(os.environ.get("HOME", str(Path.home())))
DEFAULT_PHYLOSPEC = HOME / "WorkSpace" / "phylospec" / "core" / "java" / "src" / "main" / "resources" / "phylospec-core-component-library.json"
DEFAULT_LPHY = Path(__file__).resolve().parents[1] / "resources" / "phylospec-lphy-component-library.json"
DEFAULT_OUT = Path(__file__).resolve().parent / "model_coverage_gap.md"
DEFAULT_CURATED = Path(__file__).resolve().parent / "curated_equivalences.json"


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


def find_near_matches(lphy_only_names, phylospec_only_names, ratio_threshold=0.72):
    """Heuristic: flag lphy-only / phylospec-only names that are suspiciously
    similar (case-insensitive string similarity, or one contains the other as
    a whole word/stem) -- CANDIDATES worth a manual look, to promote into
    CURATED_*_EQUIVALENCES above once verified, or discard as coincidence."""
    best_for_lphy = {}
    for ln in lphy_only_names:
        ll = ln.lower()
        best = None
        for pn in phylospec_only_names:
            pl = pn.lower()
            if ll == pl:
                continue
            shorter, longer = (ll, pl) if len(ll) <= len(pl) else (pl, ll)
            prefix = longer.startswith(shorter) and len(shorter) >= 3
            contains = shorter in longer and len(shorter) >= 4
            ratio = SequenceMatcher(None, ll, pl).ratio()
            structural = (len(shorter) / len(longer)) if (prefix or contains) else 0.0
            score = max(ratio, structural)
            if prefix or contains or ratio >= ratio_threshold:
                if best is None or score > best[1]:
                    best = (pn, score)
        if best:
            best_for_lphy[ln] = best[0]
    return sorted(best_for_lphy.items())


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


def build_generators_tables(lphy_gens: list, phylo_gens: list, curated_generators: list, generator_notes: dict):
    """Returns the same shape as build_types_tables, except the "both" slot is
    itself a (distributions_md, functions_md) pair: the "In both" table is
    split into Distribution and DeterministicFunction generators, classified
    by the LPhy side (see is_lphy_distribution) since that's the side with an
    unambiguous, already-exported signal for it. LPhy-only/PhyloSpec-only
    stay single tables, unsplit."""
    lphy_grouped = group_by_name(lphy_gens)
    phylo_grouped = group_by_name(phylo_gens)
    groups, lphy_only_names, phylo_only_names = build_match_groups(
        set(lphy_grouped), set(phylo_grouped), curated_generators, "generators"
    )

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
    # partitioning preserves that order in each half.
    distribution_groups = [g for g in groups if is_lphy_distribution(lphy_grouped[g["lphy"][0]])]
    function_groups = [g for g in groups if not is_lphy_distribution(lphy_grouped[g["lphy"][0]])]
    both_distributions_md = render_group_table(distribution_groups)
    both_functions_md = render_group_table(function_groups)

    lphy_only_rows = ["| Generator | LPhy signature(s) &rarr; return type | Description |", "|---|---|---|"]
    for name in lphy_only_names:
        description = append_note(fmt_group_description(lphy_grouped[name]), generator_notes["lphy"].get(name))
        lphy_only_rows.append(
            f"| {strong(name)} | {fmt_overloads(lphy_grouped[name])} | {description} |"
        )

    phylo_only_rows = ["| Generator | PhyloSpec signature(s) &rarr; return type | Description |", "|---|---|---|"]
    for name in phylo_only_names:
        description = append_note(fmt_group_description(phylo_grouped[name]), generator_notes["phylospec"].get(name))
        phylo_only_rows.append(
            f"| {strong(name)} | {fmt_overloads(phylo_grouped[name])} | {description} |"
        )

    return (
        both_distributions_md, both_functions_md, "\n".join(lphy_only_rows), "\n".join(phylo_only_rows),
        lphy_only_names, phylo_only_names, len(distribution_groups), len(function_groups),
    )


def main():
    phylospec_path = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_PHYLOSPEC
    lphy_path = Path(sys.argv[2]) if len(sys.argv) > 2 else DEFAULT_LPHY
    out_path = Path(sys.argv[3]) if len(sys.argv) > 3 else DEFAULT_OUT
    curated_path = Path(sys.argv[4]) if len(sys.argv) > 4 else DEFAULT_CURATED

    phylospec = load(phylospec_path)["componentLibrary"]
    lphy = load(lphy_path)["componentLibrary"]
    curated = load(curated_path)
    curated_types = curated.get("types", [])
    curated_generators = curated.get("generators", [])
    type_notes = index_side_notes(curated.get("typeNotes", []))
    generator_notes = index_side_notes(curated.get("generatorNotes", []))

    phylo_types = phylospec.get("types", [])
    lphy_types = lphy.get("types", [])
    phylo_gens = phylospec.get("generators", [])
    lphy_gens = lphy.get("generators", [])

    lphy_type_names = {t["name"] for t in lphy_types}
    phylo_type_names = {t["name"] for t in phylo_types}
    lphy_gen_names = {g["name"] for g in lphy_gens}
    phylo_gen_names = {g["name"] for g in phylo_gens}

    (types_both_md, types_lphy_md, types_phylo_md,
     types_lphy_only, types_phylo_only, types_both_count) = build_types_tables(
        lphy_types, phylo_types, curated_types, type_notes)
    (gens_both_distributions_md, gens_both_functions_md, gens_lphy_md, gens_phylo_md,
     gens_lphy_only, gens_phylo_only, gens_both_dist_count, gens_both_func_count
     ) = build_generators_tables(lphy_gens, phylo_gens, curated_generators, generator_notes)
    gens_both_count = gens_both_dist_count + gens_both_func_count

    validate_side_notes(type_notes, types_lphy_only, types_phylo_only, "types")
    validate_side_notes(generator_notes, gens_lphy_only, gens_phylo_only, "generators")

    type_near_matches = find_near_matches(set(types_lphy_only), set(types_phylo_only))
    gen_near_matches = find_near_matches(set(gens_lphy_only), set(gens_phylo_only))

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
    lines.append(f"### LPhy only ({len(gens_lphy_only)})")
    lines.append("")
    lines.append(gens_lphy_md)
    lines.append("")
    lines.append(f"### PhyloSpec only ({len(gens_phylo_only)})")
    lines.append("")
    lines.append(gens_phylo_md)
    lines.append("")

    lines.append("## Unmatched near-miss candidates")
    lines.append("")
    lines.append(
        "Output of the string-similarity heuristic over what's *still* left in the "
        "LPhy-only / PhyloSpec-only tables above, after the curated equivalences are "
        "already applied. These are candidates for a manual look -- some will be real "
        "renames worth promoting into the curated list, most will be coincidence "
        "(e.g. `sort` / `sqrt`)."
    )
    lines.append("")
    lines.append("### Types")
    lines.append("")
    if type_near_matches:
        lines.append("| LPhy name | PhyloSpec name |")
        lines.append("|---|---|")
        for ln, pn in type_near_matches:
            lines.append(f"| `{ln}` | `{pn}` |")
    else:
        lines.append("*(none detected)*")
    lines.append("")
    lines.append("### Generators")
    lines.append("")
    if gen_near_matches:
        lines.append("| LPhy name | PhyloSpec name |")
        lines.append("|---|---|")
        for ln, pn in gen_near_matches:
            lines.append(f"| `{ln}` | `{pn}` |")
    else:
        lines.append("*(none detected)*")
    lines.append("")

    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {out_path}")
    print(f"Types: both={types_both_count} lphy_only={len(types_lphy_only)} phylospec_only={len(types_phylo_only)}")
    print(f"Generators: both={gens_both_count} lphy_only={len(gens_lphy_only)} phylospec_only={len(gens_phylo_only)}")
    print(f"Near-match candidates: types={len(type_near_matches)} generators={len(gen_near_matches)}")


if __name__ == "__main__":
    main()
