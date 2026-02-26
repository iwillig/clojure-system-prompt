# justfile for clojure-system-prompt
# Exposes Babashka tasks as just recipes

# List available recipes
default:
    @just --list

# Check for outdated dependencies
outdated:
    bb outdated

# Run tests with kaocha
test *ARGS:
    bb test {{ARGS}}

# Lint codebase with clj-kondo
lint:
    bb lint

# Run the main CLI
main *ARGS:
    bb main {{ARGS}}

# Run CI pipeline (lint + test + validate)
ci:
    bb ci

# Start nREPL server
nrepl:
    bb nrepl

# Validate all files using the main CLI
validate:
    bb main validate all

# Format code (if formatting task exists)
fmt:
    bb fmt || echo "No fmt task defined in bb.edn"

# Compress prompt with LLMLingua
compress INPUT *ARGS:
    pipenv run python compress.py compress {{INPUT}} {{ARGS}}

# List available LLMLingua models
models:
    pipenv run python compress.py models

# Download LLMLingua model
download *ARGS:
    pipenv run python compress.py download {{ARGS}}
