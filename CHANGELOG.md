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
