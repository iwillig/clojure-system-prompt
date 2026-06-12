---
name: clojure-repl-dev
description: REPL-driven Clojure development for writing, editing, and debugging code. Triggers when working with Clojure files (.clj, .cljs, .cljc, .edn), handling namespaces, functions, or tooling. Provides idiomatic functional programming guidance through the REPL workflow.
---

# Clojure REPL-Driven Development

## Core Workflow

**Never write code without REPL validation.**

Every coding task follows this loop:
1. Gather context
2. Take action
3. Verify output

Before modifying any file:

1. **Read existing code** - Use `read` to examine target file and related files
2. **Verify nREPL connection** - Test: `clj-nrepl-eval -p 7889 "(+ 1 1)"`
3. **If connection fails, discover ports** - `clj-nrepl-eval --discover-ports`
4. **Initialize dev environment if available** - `clj-nrepl-eval -p 7889 "(fast-dev)"`
5. **Explore unfamiliar functions** - `clj-nrepl-eval -p 7889 "(clojure.repl/doc function-name)"`
6. **Test in REPL** - Define and validate functions before saving
7. **Check edge cases** - nil, empty collections, invalid inputs
8. **Save only after validation** - Use `edit` or `write`
9. **Reload before verifying edits** - `clj-nrepl-eval -p 7889 "(require '[project.core] :reload)"`
10. **Do not report success before verification** - changed functions and relevant tests must pass

If nREPL fails, first run `clj-nrepl-eval --discover-ports` to find existing servers. Only ask the user to start a server if none are found.

If `fast-dev` is unavailable, continue without it.

### Agent Loop

Use this loop for every coding task:

- **Gather context** - Read the target file, related code, call sites, dependencies, and tests
- **Take action** - Make the smallest focused change that solves the task; avoid unrelated refactors
- **Verify output** - Reload affected namespaces, test changed functions, validate edge cases, and run relevant tests

If verification fails, return to gather/action and fix the problem before reporting success.

### Failure Recovery

If REPL evaluation, test execution, or namespace loading fails:

1. Read the exact error message
2. Isolate the failing expression or function
3. Fix the root cause
4. Reload affected namespaces
5. Rerun verification

If delimiter errors occur, use `clj-paren-repair` instead of manual repair.

### Task Communication

For multi-step tasks, briefly communicate:
- what you are reading
- what you are changing
- how you will verify it

Ask for clarification when requirements are ambiguous, multiple approaches have materially different trade-offs, or an architectural decision is required.

## Essential Patterns

### Threading Macros (always prefer over nesting)

```clojure
;; -> for transformations
(-> user
    (assoc :last-login (Instant/now))
    (update :login-count inc))

;; ->> for sequences
(->> users
     (filter active?)
     (map :email)
     (str/join ", "))

;; some-> for nil-safe navigation
(some-> user :address :postal-code (subs 0 5))

;; cond-> for conditional changes
(cond-> request
  authenticated? (assoc :user current-user))
```

### Naming Rules

| Pattern | Example |
|---------|---------|
| kebab-case | `calculate-total`, `max-retries` |
| predicates end with `?` | `valid?`, `active?` |
| conversions use `->` | `map->vector`, `string->int` |
| NEVER use `!` suffix | Bad: `save-user!` Good: `save-user` |

### Control Flow

```clojure
;; when for side effects
(when (valid? data)
  (log "Processing")
  (process data))

;; cond for multiple branches
(cond
  (< n 0) :negative
  (= n 0) :zero
  :else   :positive)
```

### Docstrings (required for public functions)

```clojure
(defn calculate-total
  "Calculate total price including tax.

   Args:
     price - base price as BigDecimal
     rate  - tax rate as decimal (0.08 = 8%)

   Returns:
     BigDecimal total price

   Example:
     (calculate-total 100.00M 0.08) => 108.00M"
  [price rate]
  ...)
```

### Namespace Template

```clojure
(ns project.module
  "Module description. Document the namespace purpose and scope."
  (:require
   [clojure.string :as str]
   [clojure.set :as set])
  (:import
   (java.time LocalDate)))

(set! *warn-on-reflection* true)
```

When adding Java interop:
- If you introduce an unqualified Java class or static member usage such as `Instant/now`, `UUID/randomUUID`, `LocalDate/now`, or `(Foo. ...)`, update the namespace `:import` clause in the same edit.
- Prefer `:import` for Java classes used more than once or used unqualified in the namespace.
- Use fully qualified Java classes intentionally when avoiding an import is clearer, such as `java.io.FileNotFoundException` in a single `catch`.
- Reload the namespace after edits and fix any `Unable to resolve classname` or related interop errors before reporting success.

## Tools

### clj-nrepl-eval

```shell
# Test expressions
clj-nrepl-eval -p 7889 "(+ 1 2 3)"

# Define and test functions
clj-nrepl-eval -p 7889 "(defn sum [nums] (reduce + nums))"
clj-nrepl-eval -p 7889 "(sum [1 2 3])"

# Discover functions
clj-nrepl-eval -p 7889 "(clojure.repl/dir clojure.string)"
clj-nrepl-eval -p 7889 "(clojure.repl/doc map)"
clj-nrepl-eval -p 7889 "(clojure.repl/apropos \"split\")"
clj-nrepl-eval -p 7889 "(clojure.repl/source filter)"

# Load project code
clj-nrepl-eval -p 7889 "(require '[project.core :as core] :reload)"
```

### clj-paren-repair

```shell
# Fix delimiter errors
clj-paren-repair src/core.clj
clj-paren-repair src/*.clj test/*.clj
```

**Never** manually fix parenthesis errors—use this tool.

## Validation Checklist

Before saving any code:

- [ ] Tested happy path in REPL
- [ ] Tested nil handling
- [ ] Tested empty collection handling
- [ ] Used threading macros over deep nesting
- [ ] Added docstring if public function
- [ ] Checked naming conventions (no `!` suffix)
- [ ] Code under 80 characters per line
- [ ] Closing parens on single line

## Code Review Workflow

Before modifying code:

1. `read` the target file
2. `bash: rg "require.*target.ns" --type clj` - find related files
3. `bash: rg "function-name" --type clj` - find call sites
4. Review namespace imports and patterns
5. Match codebase conventions

## Detailed References

- **Tool usage**: See [references/tool-guide.md](references/tool-guide.md) for complete clj-nrepl-eval and clj-paren-repair documentation
- **Idiomatic patterns**: See [references/idioms.md](references/idioms.md) for threading macros, control flow, data structures, error handling, and anti-patterns

Load these references when you need:
- Detailed tool commands
- Advanced idioms or patterns
- Error handling examples
- Testing patterns
- Research citations
