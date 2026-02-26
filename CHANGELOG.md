# Changelog

All notable changes to this system prompt are documented in this file.

This project uses [Semantic Versioning](https://semver.org/) (MAJOR.MINOR.PATCH):
- **MAJOR**: Structural overhauls, new core mandates, or changes that fundamentally alter agent behavior
- **MINOR**: New sections, additional conventions, or expanded guidance on existing topics
- **PATCH**: Typo fixes, wording clarifications, minor formatting changes

Each version is an immutable snapshot. Never modify a released version; always create a new one.

## Guidelines for Updating

1. **Evaluate impact** -- Before changing any instruction, ask: "Would removing or changing this cause the agent to make mistakes?" If not, reconsider the change.
2. **Test changes** -- After modifying the prompt, validate against known tasks: REPL-first workflow, idiomatic Clojure output, output-style rules, clj-paren-repair usage.
3. **Document the change** -- Update this changelog with version, date, what changed, and rationale.
4. **Keep it concise** -- Research shows frontier LLMs reliably follow ~150-200 instructions. Every line added dilutes attention on existing instructions. Prefer removing outdated guidance over accumulating new guidance.
5. **One change per version** -- Avoid bundling unrelated changes. This makes rollback and regression diagnosis easier.

---

## [v1.8.0] - 2026-02-26

### Added
- **Prompt compression tool** (`compress.py`) for reducing SYSTEM.md token count:
  - CLI tool using Click framework and Microsoft's LLMLingua library
  - Achieves up to 20x compression with minimal performance loss
  - Three commands: `compress`, `models`, `download`
  - Configurable compression rates (default 50%)
  - Target-token mode for precise token budgets
  - Multiple model options (LLMLingua-2 default, 3-6x faster than LLMLingua-1)
  - Compression statistics output (tokens saved, ratio, cost savings)
  - Integration with justfile: `just compress`, `just models`, `just download`
  - Added `llmlingua` and `click` to Pipfile dependencies

### Rationale
The SYSTEM.md prompt (29,627 bytes) is comprehensive but consumes significant
context window space. For scenarios where token limits are tight or API costs
matter, users need a way to compress the prompt while preserving essential
instructions. LLMLingua uses trained language models to identify and remove
non-essential tokens intelligently, maintaining semantic meaning and structural
elements. This provides:
1. Lower API costs (fewer input tokens)
2. Compatibility with stricter context limits
3. Faster processing times
4. Optional compression without losing the detailed reference version

The tool is self-contained in a single Python script and integrates cleanly
with the existing Pipfile/justfile workflow.

---

## [v1.7.0] - 2026-02-23

### Changed
- **Major expansion of `<namespace-structure>` section** based on Stuart Sierra's authoritative "How to ns" guide:
  - Added mandatory docstring requirement for all namespace declarations
  - Complete 10-rule ordering: clause order, keywords vs symbols, parentheses vs vectors/lists
  - Explicit forbidden patterns: :use, :refer :all, :rename, prefix lists in :require
  - Sorting requirements: lexicographic sorting for namespaces, packages, classes, symbols
  - One-per-line formatting rules
  - :as before :refer convention
  - Guidance on :refer usage (sparingly, frequently-used symbols only)
  - Comprehensive examples showing good vs bad patterns
  - Added reference citation to Stuart Sierra's guide

### Rationale
The previous namespace structure section was minimal and didn't reflect community best practices. Stuart Sierra's "How to ns" is the de facto standard for Clojure namespace declarations. This update ensures LLM agents generate properly formatted namespace declarations that:
1. Include descriptive docstrings (improving codebase documentation)
2. Follow consistent formatting (easier code review and maintenance)
3. Use correct delimiter types (parentheses vs vectors/lists)
4. Sort declarations lexicographically (easier scanning and avoiding duplicates)
5. Avoid deprecated patterns (:use, :refer :all)

### References
- Stuart Sierra's "How to ns": https://stuartsierra.com/2016/clojure-how-to-ns.html

---

## [v1.6.0] - 2026-02-19

### Added
- Comprehensive clojure.test documentation in SYSTEM.md:
  - `<test-structure>` - Examples of deftest, testing blocks, is, and are
  - `<assertion-behavior>` - Critical warnings about is behavior (non-stopping, message evaluation)
  - `<fixture-aware-testing>` - Complete fixture system documentation with proper test runners
  - Test runner hierarchy (run-test-var, test-var, test-vars, run-tests, run-all-tests)
  - test-ns-hook compatibility warnings
- References in pseudo-XML format:
  - The Clojure Style Guide (global reference after `<identity>`)
  - clojure.test Official API documentation
  - ClojureDocs community examples
  - clojure-mcp-light tool repository (2 references)
- Tool installation instructions in README.md:
  - Complete prerequisites (Babashka, bbin, parinfer-rust)
  - Step-by-step installation for clj-nrepl-eval and clj-paren-repair
  - Verification steps for each tool
  - Attribution to Bruce Hauman and link to full documentation

### Changed
- Updated validation checklist to include test fixture validation
- Enhanced test examples with exception testing and template-based testing

### Rationale
Address the #1 issue where LLM agents call test functions directly instead of using fixture-aware test runners (run-test-var), causing unbound dynamic var errors. Provide properly cited documentation for all major guidance areas. Move installation instructions to README.md where they belong, keeping SYSTEM.md focused on operational guidance.

### References
- Official Clojure API: https://clojure.github.io/clojure/clojure.test-api.html
- ClojureDocs: https://clojuredocs.org/clojure.test
- The Clojure Style Guide: https://guide.clojure.style/
- clojure-mcp-light: https://github.com/bhauman/clojure-mcp-light

## [v1.5.0] - 2025-02-09

### Added
- `<prompt-version>` tag in SYSTEM.md to track the current version
- This CHANGELOG.md file with versioning scheme, review guidelines, and retroactive history

### Rationale
Enable systematic tracking of prompt evolution and regression detection.

## [v1.4.0] - Add clj-paren-tool

### Added
- `clj-paren-repair-tool` section for automatic delimiter repair using parinfer

## [v1.3.0] - Prevent emoji creation

### Changed
- `output-style` to enforce ASCII-only output, no emojis or unicode symbols

## [v1.2.0] - Add clojure.repl docs

### Added
- `runtime-exploration` section with guidance on `clojure.repl/doc`, `dir`, `apropos`, `source`

## [v1.1.0] - System prompt and readme

### Added
- Initial comprehensive system prompt with `core-mandate`, `idiomatic-clojure`, `code-quality`, `error-handling`, `testing`, and `tool-usage` sections

## [v1.0.0] - Initial commit

### Added
- Repository scaffolding
