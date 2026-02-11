# Agent Instructions: Maintaining SYSTEM.md and SKILL.md

This document provides instructions for maintaining consistency between the system prompt (`SYSTEM.md`) and the Anthropic/pi skill (`clojure-repl-dev/SKILL.md`).

## File Relationship

| File | Purpose | Format |
|------|---------|--------|
| `SYSTEM.md` | System prompt for pi agent | XML-style tags with priorities |
| `clojure-repl-dev/SKILL.md` | Anthropic/pi skill - core workflow | Markdown with frontmatter |
| `clojure-repl-dev/references/*.md` | Detailed reference material | Markdown (loaded on demand) |

**SKILL.md contains essential workflow** (~170 lines) while **references/** contains detailed documentation (~400 lines) following the [progressive disclosure principle](https://github.com/anthropics/skills/blob/main/skills/skill-creator/SKILL.md).

Both `SYSTEM.md` and the skill files share the same Clojure development guidance, but formatted for their respective systems.

## Validating Clojure Code Examples

All Clojure code examples in SYSTEM.md and SKILL.md must be syntactically valid and runnable. Use `clj-nrepl-eval` to verify code before committing changes.

### Basic Validation Workflow

```bash
# 1. Start an nREPL server if not running
#    bb nrepl, lein repl :headless, or clj -M:mcp/nrepl

# 2. Test simple expressions
clj-nrepl-eval -p 7889 "(+ 1 2 3)"
# => 6

# 3. Test function definitions from your edits
clj-nrepl-eval -p 7889 "(defn greet [name] (str \"Hello, \" name))"
# => #'user/greet

clj-nrepl-eval -p 7889 "(greet \"World\")"
# => "Hello, World"

# 4. Test threading macro examples from the skills
clj-nrepl-eval -p 7889 "(-> {:count 0} (update :count inc) (assoc :active true))"
# => {:count 1, :active true}

clj-nrepl-eval -p 7889 "(->> [1 2 3 4] (filter even?) (map #(* % 2)))"
# => (4 8)

# 5. Test destructuring examples
clj-nrepl-eval -p 7889 "(let [{:keys [a b]} {:a 1 :b 2}] (+ a b))"
# => 3
```

### Validation Checklist for Code Examples

Before committing changes to Clojure code examples:

- [ ] Start nREPL: `bb nrepl` or equivalent
- [ ] Test each new function definition with `clj-nrepl-eval`
- [ ] Test each threading macro example (`->`, `->>`, `some->`, `cond->`)
- [ ] Verify destructuring syntax works
- [ ] Check that namespace examples use correct require/import syntax
- [ ] Ensure regex literals work: `#"pattern"`

### Common Issues to Catch

```bash
# Test edge cases shown in examples
clj-nrepl-eval -p 7889 "(sum-evens [])"  # empty collection handling
clj-nrepl-eval -p 7889 "(sum-evens nil)" # nil handling

# Verify metadata/accessor syntax
clj-nrepl-eval -p 7889 "(:arglists (meta #'map))"

# Test cond/case examples
clj-nrepl-eval -p 7889 "(cond (< -1 0) :negative (= 0 0) :zero :else :positive)"
```

### When clj-paren-repair is Needed

If you edit Clojure files and encounter delimiter errors:

```bash
# Fix unbalanced parentheses/brackets/braces
clj-paren-repair src/example.clj

# Or via stdin for quick testing
echo '(defn broken [x] (+ x 1)' | clj-paren-repair
```

**Never commit code that hasn't been validated in the REPL.**

## Synchronization Rules

### Rule 1: Update Both Files Together

When modifying Clojure development guidance, **ALWAYS update both files** unless the change is format-specific.

**Examples of shared content (update both):**
- REPL-first development workflow
- Threading macro patterns
- Naming conventions (kebab-case, `?` suffix, no `!` suffix)
- Tool usage instructions (`clj-nrepl-eval`, `clj-paren-repair`)
- Code quality standards (docstrings, line length, indentation)
- Validation checklists
- Research references (compiler feedback paper)

**Examples of format-specific content (update only one):**
- SYSTEM.md: XML tag structure, `priority="critical"` attributes
- SKILL.md: YAML frontmatter, relative path references

### Rule 2: Content Parity Checklist

Before considering any edit complete, verify:

- [ ] Core mandates match (REPL-first, no ! suffix, threading macros)
- [ ] Tool documentation is identical in substance
- [ ] Naming conventions are consistent
- [ ] Code quality standards align
- [ ] Research references match (arXiv paper)
- [ ] Version numbers are synchronized

### Rule 3: Version Synchronization

Both files must carry the same version number:

- `SYSTEM.md`: `<prompt-version>v1.X.X</prompt-version>`
- `SKILL.md`: `**Version:** v1.X.X` at end of file
- `README.md`: `Current version: v1.X.X`
- `CHANGELOG.md`: Version history for both files

**When bumping version:**
1. Update `CHANGELOG.md` with changes to both files
2. Update version in `SYSTEM.md`
3. Update version in `SKILL.md`
4. Update version in `README.md`

### Rule 4: Structural Mapping

| SYSTEM.md Section | SKILL.md | references/ |
|-------------------|----------|-------------|
| `<core-mandate>` | ## Core Workflow | — |
| `<clj-nrepl-eval-tool>` | ## Tools (brief) | tool-guide.md (detailed) |
| `<clj-paren-repair-tool>` | ## Tools (brief) | tool-guide.md (detailed) |
| `<idiomatic-clojure>` | ## Essential Patterns (examples) | idioms.md (complete reference) |
| `<code-quality>` | ## Naming Rules, Docstrings | idioms.md |
| `<testing>` | ## Validation Checklist | idioms.md |
| `<code-review-workflow>` | ## Code Review Workflow | — |
| `<error-handling>` | — | idioms.md |
| `<tool-usage>` | ## Tools | tool-guide.md |
| `<summary>` | ## Core Workflow intro | — |

**Progressive Disclosure:**
- SKILL.md (~170 lines) contains only essential workflow and examples
- references/idioms.md (~260 lines) contains complete patterns, anti-patterns, and research
- references/tool-guide.md (~150 lines) contains complete tool documentation

When adding detailed content (>50 lines), prefer adding to `references/` and linking from SKILL.md.

## Edit Workflow

When asked to modify Clojure guidance:

1. **Read both files** to understand current state
   ```bash
   read SYSTEM.md
   read clojure-repl-dev/SKILL.md
   ```

2. **Identify which sections** need updates in both files

3. **Make edits to SYSTEM.md first** (it's the source of truth)

4. **Mirror changes to SKILL.md** adapting format as needed:
   - Convert XML tags to Markdown headers
   - Preserve code examples exactly
   - Adapt priority markers to prose emphasis

5. **Verify synchronization** using the checklist in Rule 2

6. **Update CHANGELOG.md** with the changes

7. **Bump version** if this is a release (all three files)

## Content Guidelines

### What Belongs in SKILL.md (Core, ~170 lines)

Keep SKILL.md lean with only essential workflow:

- **Core workflow**: REPL-first validation steps
- **Key examples**: Threading macros, naming rules, control flow
- **Tool basics**: Common clj-nrepl-eval commands
- **Validation checklist**: Quick reference before saving
- **Code review steps**: Before-any-changes workflow
- **Reference links**: Pointers to `references/` files

### What Belongs in references/idioms.md

Detailed reference material loaded only when needed:

- **Complete idioms**: All threading patterns, data structures, destructuring
- **Advanced patterns**: Multi-arity, variadic, memoization, composition
- **Anti-patterns**: Common mistakes to avoid with examples
- **Research citation**: Compiler feedback paper (arXiv:2601.12146v1)
- **Error handling**: Complete try-catch patterns
- **Testing**: Full test structure examples

### What Belongs in references/tool-guide.md

Complete tool documentation:

- **All clj-nrepl-eval options**: Port, host, full command reference
- **Discovery commands**: doc, dir, apropos, find-doc, source
- **Debugging patterns**: Inspecting data, testing pipelines
- **Troubleshooting**: Connection issues, namespace errors
- **Project loading**: require, reload, namespace operations

### What is Format-Specific

**SYSTEM.md only:**
- XML tag structure (`<system-prompt>`, `<identity>`, etc.)
- Priority attributes (`priority="critical"`)
- Tool-specific XML documentation blocks
- Summary tag at end

**SKILL.md only:**
- YAML frontmatter (name, description)
- Relative path references to `references/`

## Anti-Patterns to Avoid

### NEVER
- Update one file without checking if the other needs updates
- Change naming conventions in one file but not the other
- Add tool examples to SKILL.md that aren't in SYSTEM.md
- Remove the research reference from only one file
- Change version in one file but not others

### ALWAYS
- Keep code examples identical between files (copy-paste)
- Maintain the same sequence of concepts (REPL → Idioms → Quality → Testing)
- Preserve the no-`!`-suffix rule with same emphasis
- Include the arXiv paper reference with identical phrasing

## Quick Reference: Common Edits

### Adding a New Tool Pattern

For patterns used frequently (add to both):

1. Add brief example to SYSTEM.md under `<clj-nrepl-eval-tool>`:
   ```xml
   <new-pattern>
   ```shell
   clj-nrepl-eval -p 7889 "(new-example)"
   ```
   </new-pattern>
   ```

2. Add brief example to SKILL.md under `## Tools`:
   ```markdown
   ```shell
   clj-nrepl-eval -p 7889 "(new-example)"
   ```
   ```

3. Add complete documentation to `references/tool-guide.md`

### Adding Idiomatic Patterns

For patterns rarely needed (references only):

1. Add complete documentation to `references/idioms.md`

For patterns used constantly (all three):

1. Add to SYSTEM.md under `<idiomatic-clojure>`
2. Add brief example to SKILL.md under `## Essential Patterns`
3. Add complete docs to `references/idioms.md`

### Updating a Naming Rule

1. Edit both files simultaneously (all naming rules are core)
2. Use identical examples
3. Preserve the "NEVER use ! suffix" prohibition
4. Update `references/idioms.md` if table format differs

### Changing Version

1. Edit `CHANGELOG.md` first
2. Update `SYSTEM.md`: `<prompt-version>vX.Y.Z</prompt-version>`
3. Update `README.md`: `Current version: vX.Y.Z`
4. Note: SKILL.md intentionally has no version (per skill spec)

## Validation Commands

To verify both files are synchronized:

```bash
# Check version alignment
grep -E "v1\.[0-9]+\.[0-9]+" SYSTEM.md README.md
grep "Version:" clojure-repl-dev/SKILL.md

# Check for compiler paper reference
grep "2601.12146v1" SYSTEM.md clojure-repl-dev/SKILL.md README.md

# Check no-!-suffix rule presence
grep -A2 "FORBIDDEN.*!" SYSTEM.md
grep -B1 -A1 "NEVER use !" clojure-repl-dev/SKILL.md
```

## Summary

- **SYSTEM.md** is the source of truth for content
- **SKILL.md** is the redistributed format for Anthropic/pi skills
- **Always edit both** when changing Clojure guidance
- **Keep code examples identical** between files
- **Synchronize versions** across all files
- **Reference this document** when unsure about synchronization
