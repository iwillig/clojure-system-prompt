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
