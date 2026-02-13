# CSP Validator

A Clojure-based validator for SKILL.md and SYSTEM.md files, ensuring compliance with the Anthropic skill specification and internal quality standards.

## Features

- **SKILL.md Validation**
  - YAML frontmatter structure (name, description, optional fields)
  - Naming conventions (kebab-case, length limits)
  - Description constraints (no angle brackets, length limits)
  - Reference file existence checks
  
- **SYSTEM.md Validation**
  - Required XML-like tags (system-prompt, identity, summary)
  - Version format (semantic versioning: v1.2.3)
  - Priority attributes validation
  - Code example presence detection

- **Malli Schemas**
  - Composable, data-driven validation rules
  - Human-readable error messages
  - Extensible for custom validation logic

## Installation

The validator is built into this project. No additional installation required.

## Usage

### Command Line Interface

Validate a SKILL.md file:
```bash
bb main validate skill
bb main validate skill -p path/to/skill/directory
```

Validate a SYSTEM.md file:
```bash
bb main validate system
bb main validate system -p path/to/SYSTEM.md
```

Validate both:
```bash
bb main validate all
bb main validate all -s skill-dir -p SYSTEM.md
```

### Programmatic API

```clojure
(require '[csp.validator.core :as validator])

;; Validate a skill
(validator/validate-skill "clojure-repl-dev")
;; => {:valid? true
;;     :errors []
;;     :warnings []
;;     :skill-path "clojure-repl-dev"
;;     :frontmatter {:name "clojure-repl-dev" :description "..."}}

;; Validate a system prompt
(validator/validate-system "SYSTEM.md")
;; => {:valid? true
;;     :errors []
;;     :warnings []
;;     :system-path "SYSTEM.md"
;;     :version "v1.5.0"
;;     :tags #{:system-prompt :identity ...}
;;     :code-blocks 25}

;; Validate both
(validator/validate-all {:skill-path "clojure-repl-dev"
                         :system-path "SYSTEM.md"})
;; => {:valid? true
;;     :skill {...}
;;     :system {...}}
```

## Validation Rules

### SKILL.md

**Required Fields:**
- `name`: 1-64 characters, kebab-case (lowercase, hyphens only)
- `description`: 1-1024 characters, no angle brackets

**Optional Fields:**
- `license`: string
- `allowed-tools`: array of strings
- `metadata`: map
- `compatibility`: string (max 500 characters)

**Structural Checks:**
- File must start with `---` delimiter
- YAML frontmatter must be valid
- No unexpected keys in frontmatter
- Referenced files (references/*.md) must exist

### SYSTEM.md

**Required Tags:**
- `<system-prompt>...</system-prompt>`
- `<identity>...</identity>`
- `<summary>...</summary>`

**Version:**
- Must have `<prompt-version>vX.Y.Z</prompt-version>`
- Format: semantic versioning (e.g., v1.5.0)

**Priority Attributes:**
- Valid values: `critical`, `high`, `medium`, `low`
- Example: `<core-mandate priority="critical">...</core-mandate>`

**Code Examples:**
- Detects presence of Clojure code blocks
- Warns if none found

## Architecture

```
src/csp/validator/
├── core.clj          # Public API orchestration
├── schemas.clj       # Malli schemas for validation
├── markdown.clj      # CommonMark parsing
├── skill.clj         # SKILL.md validation logic
└── system.clj        # SYSTEM.md validation logic

src/csp/
├── cli.clj           # cli-matic command definitions
└── main.clj          # Entry point
```

## Dependencies

- **metosin/malli**: Schema validation with human-readable errors
- **org.commonmark/commonmark**: Markdown parsing
- **org.commonmark/commonmark-ext-yaml-front-matter**: YAML frontmatter extraction
- **cli-matic/cli-matic**: Hierarchical CLI interface

## Testing

Run the test suite:
```bash
bb test :unit
```

Test coverage report is generated at `target/coverage/index.html`.

Current coverage: **66% overall** (80%+ for core validator modules)

### Test Structure

```
test/csp/validator/
├── schemas_test.clj  # Malli schema validation tests
├── skill_test.clj    # SKILL.md validation tests
└── system_test.clj   # SYSTEM.md validation tests

test/fixtures/
└── invalid-skill/    # Test fixtures for validation failures
```

## Extending

### Adding Custom Validation Rules

1. Define Malli schemas in `schemas.clj`:
```clojure
(def custom-field-schema
  [:and
   [:string {:min 1 :max 100}]
   [:re #"^[a-z0-9-]+$"]])
```

2. Add validation logic in `skill.clj` or `system.clj`:
```clojure
(defn validate-custom-field [data]
  (schemas/validate-with-humanized-errors
    custom-field-schema
    (:custom-field data)))
```

3. Integrate into main validation function:
```clojure
(let [custom-check (validate-custom-field parsed-data)
      all-errors (concat (:errors custom-check) ...)]
  ...)
```

### Adding New CLI Commands

Extend `cli-config` in `cli.clj`:
```clojure
{:command "new-command"
 :description "Description of new command"
 :opts [{:as "Option description"
         :option "option-name"
         :type :string}]
 :runs new-command-fn}
```

## Comparison with Python Script

This validator is a port of the [Anthropic skill validator](https://github.com/anthropics/skills/blob/main/skills/skill-creator/scripts/quick_validate.py) with enhancements:

| Feature | Python Script | Clojure Validator |
|---------|---------------|-------------------|
| SKILL.md validation | Yes | Yes |
| SYSTEM.md validation | No | Yes |
| Malli schemas | No | Yes |
| Hierarchical CLI | No | Yes (cli-matic) |
| Reference file checks | No | Yes |
| REPL-testable | No | Yes |
| Human-readable errors | Yes | Yes |
| Extensible | Limited | Yes (data-driven) |

## Future Enhancements

- [ ] Auto-fix mode (`validate skill --fix`)
- [ ] Watch mode for continuous validation
- [ ] Git pre-commit hook integration
- [ ] Validate Clojure code examples with edamame parser
- [ ] Synchronization checks between SKILL.md and SYSTEM.md
- [ ] JSON output format for CI integration
- [ ] Performance optimizations (schema compilation)

## License

Same as parent project.
