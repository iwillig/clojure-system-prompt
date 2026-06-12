# justfile for clojure-system-prompt
# Exposes Babashka tasks as just recipes

default:
    @just --list

outdated:
    bb outdated

test *ARGS:
    bb test {{ARGS}}

lint:
    bb lint

main *ARGS:
    bb main {{ARGS}}

ci:
    bb ci

nrepl:
    bb nrepl

validate:
    bb main validate all

fmt:
    bb fmt || echo "No fmt task defined in bb.edn"

build-evals:
    npx shadow-cljs compile evals

watch-evals:
    npx shadow-cljs watch evals

run-evals:
    just build-evals
    npx vitest run --dir out/cljs --testTimeout 120000

run-evals-json:
    just build-evals
    npx vitest run --dir out/cljs --testTimeout 120000 --reporter=vitest-evals/reporter --reporter=json --outputFile.json=vitest-results.json

evals-ui:
    npx vitest-evals serve vitest-results.json

compress INPUT *ARGS:
    pipenv run python compress.py compress {{INPUT}} {{ARGS}}

models:
    pipenv run python compress.py models

download *ARGS:
    pipenv run python compress.py download {{ARGS}}
