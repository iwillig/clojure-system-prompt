# Tool Guide: clj-nrepl-eval and clj-paren-repair

Complete reference for Clojure development tools.

## clj-nrepl-eval

Interface to a running Clojure nREPL server.

### Basic Syntax

```shell
clj-nrepl-eval [options] "clojure-expression"
```

Options:
- `-p, --port PORT` - nREPL port (default: 7889)
- `-h, --host HOST` - nREPL host (default: localhost)

### Evaluating Code

```shell
# Simple expressions
clj-nrepl-eval -p 7889 "(+ 1 2 3)"

# Define functions
clj-nrepl-eval -p 7889 "(defn greet [name] (str \"Hello, \" name))"
clj-nrepl-eval -p 7889 "(greet \"World\")"

# Work with collections
clj-nrepl-eval -p 7889 "(map inc [1 2 3])"
clj-nrepl-eval -p 7889 "(filter even? [1 2 3 4])"

# Require and use namespaces
clj-nrepl-eval -p 7889 "(require '[clojure.string :as str])"
clj-nrepl-eval -p 7889 "(str/upper-case \"hello\")"
```

### Discovery Commands

```shell
# List all public functions in a namespace
clj-nrepl-eval -p 7889 "(clojure.repl/dir clojure.string)"

# Get function documentation
clj-nrepl-eval -p 7889 "(clojure.repl/doc map)"

# Search by name pattern
clj-nrepl-eval -p 7889 "(clojure.repl/apropos \"split\")"

# Search documentation text
clj-nrepl-eval -p 7889 "(clojure.repl/find-doc \"regular expression\")"

# Read function source
clj-nrepl-eval -p 7889 "(clojure.repl/source filter)"

# Get function signatures
clj-nrepl-eval -p 7889 "(:arglists (meta #'reduce))"
```

### Loading Project Code

```shell
# Load a namespace
clj-nrepl-eval -p 7889 "(require '[project.core :as core] :reload)"

# List namespace contents
clj-nrepl-eval -p 7889 "(clojure.repl/dir project.core)"

# Test loaded functions
clj-nrepl-eval -p 7889 "(core/my-function test-data)"
```

### Debugging

```shell
# Inspect data structures
clj-nrepl-eval -p 7889 "(type [1 2 3])"
clj-nrepl-eval -p 7889 "(keys my-map)"

# Test pipeline steps
clj-nrepl-eval -p 7889 "(def data [1 2 3 4 5])"
clj-nrepl-eval -p 7889 "(filter even? data)"
clj-nrepl-eval -p 7889 "(map #(* % 2) (filter even? data))"

# Check predicates
clj-nrepl-eval -p 7889 "(even? 4)"
clj-nrepl-eval -p 7889 "(nil? [])"
clj-nrepl-eval -p 7889 "(empty? [])"
```

## clj-paren-repair

Fixes delimiter errors (mismatched parentheses, brackets, braces) in Clojure files.

### Usage

```shell
# Fix a single file
clj-paren-repair path/to/file.clj

# Fix multiple files
clj-paren-repair src/core.clj src/util.clj test/core_test.clj

# Check code without file (stdin mode)
echo '(defn hello [x] (+ x 1)' | clj-paren-repair
```

The tool:
1. Detects delimiter errors using edamame parser
2. Repairs them using parinfer-rust (or parinferish fallback)
3. Formats with cljfmt
4. Reports what was fixed

### When to Use

- Parse error or unbalanced delimiter after editing
- REPL eval fails with unexpected delimiter/EOF error
- Editing multiple files and want to verify they're well-formed

### Supported File Types

.clj, .cljs, .cljc, .bb, .edn, .lpy

## Troubleshooting

### Connection refused

Ask user to start nREPL:
```bash
bb nrepl
lein repl :headless
clj -Sdeps '{:deps {nrepl/nrepl {:mvn/version "1.0.0"}}}' -M -m nrepl.cmdline
```

### Wrong port

Check actual port and adjust:
```shell
clj-nrepl-eval -p <correct-port> "(...)"
```

### Namespace not found

Require it first:
```shell
clj-nrepl-eval -p 7889 "(require '[namespace.name])"
```

### Expression errors

Test simpler expressions to isolate, then use `(clojure.repl/doc ...)` to verify signatures.
