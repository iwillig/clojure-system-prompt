# Clojure System Prompt

A system prompt for LLM coding assistants working with
Clojure. Optimized for REPL-driven development, idiomatic functional
code, and the unique challenges niche languages face with AI
assistance.

## Quick Start

### With pi Agent

#### Option 1: Project-specific setup

Create a `.pi` directory in your project root and copy `SYSTEM.md` there:

```bash
mkdir -p /path/to/your/clojure/project/.pi
cp SYSTEM.md /path/to/your/clojure/project/.pi/SYSTEM.md
```

#### Option 2: Global setup (all projects)

Copy to pi's global system prompt location:

```bash
cp SYSTEM.md ~/.pi/agent/SYSTEM.md
```

#### Option 3: Append without replacing

To add Clojure guidance without replacing the default prompt:

```bash
cp SYSTEM.md /path/to/your/clojure/project/.pi/APPEND_SYSTEM.md
```

### With opencode

Add to your `opencode.json` configuration file (project root or
`~/.config/opencode/opencode.json`):

**Option 1: Define a Clojure agent with the system prompt:**

```json
{
  "$schema": "https://opencode.ai/config.json",
  "agent": {
    "clojure": {
      "description": "Expert Clojure developer with REPL-driven workflow",
      "model": "anthropic/claude-sonnet-4",
      "prompt": "{file:./SYSTEM.md}"
    }
  },
  "default_agent": "clojure"
}
```

**Option 2: Use instructions array to load the system prompt:**

```json
{
  "$schema": "https://opencode.ai/config.json",
  "instructions": ["./SYSTEM.md"]
}
```

The instructions array accepts paths and glob patterns to instruction
files. These are loaded as context for all conversations.

### With Claude Code / Anthropic Skills

This repository includes an [Anthropic
Skill](https://github.com/anthropics/skills) package in the
`clojure-repl-dev/` directory:

**Option 1: Global installation (all projects)**

```bash
cp -r clojure-repl-dev ~/.claude/skills/
```

**Option 2: Project-specific installation**

```bash
mkdir -p .claude/skills
cp -r clojure-repl-dev .claude/skills/
```

**Usage:**

Once installed, invoke the skill with:

```bash
/skill:clojure-repl-dev
```

Or reference it when starting a task:

```bash
claude /skill:clojure-repl-dev "Create a function to parse JSON"
```

The skill will auto-load when working with Clojure files (`.clj`, `.cljs`, `.cljc`, `.edn`).

### With pi Agent (Skills Mode)

The skill can also be used with pi's skill system:

```bash
# Global installation
cp -r clojure-repl-dev ~/.pi/agent/skills/

# Or project-specific
mkdir -p .pi/skills
cp -r clojure-repl-dev .pi/skills/

# Or use directly
pi --skill /path/to/clojure-repl-dev
```

### Prompt evals with llama-server

This repository includes a `vitest-evals` suite for checking how
`SYSTEM.md` influences generated Clojure code. The local eval harness
calls a running `llama-server` directly instead of using OpenAI or
Anthropic providers.

#### Local setup

Start `llama-server` with an OpenAI-compatible API on port `8080`.
The eval suite expects:

- `LLAMA_SERVER_BASE_URL=http://127.0.0.1:8080/v1`
- `LLAMA_SERVER_MODEL=unsloth/Qwen3.6-35B-A3B-MTP-GGUF:UD-Q8_K_XL`

If your server requires auth, also set:

- `LLAMA_SERVER_API_KEY=...`

Example launch command:

```bash
llama-server \
  -hf unsloth/Qwen3.6-35B-A3B-MTP-GGUF:UD-Q8_K_XL \
  --host 127.0.0.1 \
  --port 8080
```

This serves the model under the same identifier used by
`LLAMA_SERVER_MODEL`.

Install the Node dependencies:

```bash
npm install
```

Run the eval suite:

```bash
npm run evals
```

Generate JSON output for the local report UI or CI artifacts:

```bash
npm run evals:json
```

Open the local report UI after generating `vitest-results.json`:

```bash
npm run evals:ui
```

Override the default server or model for one run:

```bash
LLAMA_SERVER_BASE_URL=http://127.0.0.1:8080/v1 \
LLAMA_SERVER_MODEL=unsloth/Qwen3.6-35B-A3B-MTP-GGUF:UD-Q8_K_XL \
  npm run evals
```

#### What the evals cover

The current suites check prompt-sensitive Clojure behaviors such as:

- Java `:import` decisions for unqualified class usage
- Namespace docstrings
- Avoiding `!` suffixes on Clojure function names

#### CI configuration

GitHub Actions will run the evals only when these repository variables
are configured for a reachable llama-server endpoint:

- `LLAMA_SERVER_BASE_URL`
- `LLAMA_SERVER_MODEL`

If your endpoint requires auth, add this secret:

- `LLAMA_SERVER_API_KEY`

When those variables are absent, the normal Clojure CI still runs and
prompt evals are skipped.

## What This Prompt Provides

- **REPL-first enforcement**: Code is tested in the REPL before being written to files
- **Explicit agent loop**: Gather context, take focused action, and verify output before reporting success
- **Idiomatic Clojure guidance**: Threading macros, functional patterns, naming conventions
- **Anti-hallucination rules**: Forbidden patterns like `!` suffixes on function names
- **Code quality standards**: Docstrings, proper error handling, testing requirements
- **Tool integration**: Proper usage of `clj-nrepl-eval` and `clj-paren-repair`

## Why This Matters

Niche languages like Clojure face inherent disadvantages with LLMs due
to training data imbalances. Studies show Python dominates 90-97% of
LLM benchmark tasks. Custom system prompts like this one compensate
by:

1. Providing domain-specific knowledge the LLM may lack
2. Preventing hallucinations about non-existent functions
3. Enforcing functional programming idioms over imperative defaults
4. Enabling validation through Clojure's REPL-driven workflow
5. Structuring agent behavior around a gather, act, and verify loop

See [research.md](research.md) for detailed citations and evidence.

## Rationale: The REPL as Compiler

This approach is grounded in recent research demonstrating that LLMs
with access to external validation tools significantly outperform
model-only baselines. A 2026 study on 16 models (135M to 70B
parameters) found that compiler access improved code compilation rates
by 5.3 to 79.4 percentage points, with syntax errors dropping 75% and
undefined references dropping 87%.

The Clojure REPL serves the same function: it acts as a compiler and
runtime oracle that grounds the AI in executable truth. Rather than
generating code in a vacuum and hoping it works, the AI evaluates
expressions in the REPL first—verifying syntax, testing behavior,
and confirming correctness before writing to files. This shifts the AI
from a passive code generator to an active agent with feedback-driven
iteration, enabling smaller models to achieve results comparable to
much larger ones while reducing the energy footprint of AI-assisted
development.

**Reference:** Kjellberg, V., Staron, M., & Fotrousi, F. (2026). *From LLMs to Agents in Programming: The Impact of Providing an LLM with a Compiler*. arXiv:2601.12146v1. https://arxiv.org/html/2601.12146v1

## Project Structure

```
.
├── SYSTEM.md                    # The system prompt (copy this to your projects)
├── clojure-repl-dev/            # Anthropic/pi skill package
│   ├── SKILL.md                 # Core skill with essential workflow (168 lines)
│   └── references/
│       ├── tool-guide.md        # Complete tool documentation
│       └── idioms.md            # Idiomatic patterns and anti-patterns
├── agents.md                    # Instructions for maintaining SYSTEM.md and SKILL.md
├── research.md                  # Research supporting custom prompts for niche languages
├── CHANGELOG.md                 # Version history
└── LICENSE                      # MIT License
```

**Note:** `agents.md` contains synchronization instructions for keeping `SYSTEM.md` and `clojure-repl-dev/SKILL.md` consistent. See that file before modifying Clojure guidance.

The skill follows the [Anthropic Skills Specification](https://agentskills.io/specification) with **progressive disclosure**:
- Metadata (name + description) — always in context
- SKILL.md — core workflow loaded when skill triggers (~4KB)
- References — loaded only when needed by the agent

## Requirements

This prompt assumes you have:

- A Clojure nREPL server running (the prompt will ask you to start it if not)
- The `clj-nrepl-eval` tool installed (for REPL evaluation)
- The `clj-paren-repair` tool installed (for fixing delimiter errors)

### Installing Required Tools

Both tools are provided by [clojure-mcp-light](https://github.com/bhauman/clojure-mcp-light) by Bruce Hauman.

**Prerequisites:**
- [Babashka](https://github.com/babashka/babashka) v1.12.212 or later
- [bbin](https://github.com/babashka/bbin) (Babashka package manager)
- [parinfer-rust](https://github.com/eraserhd/parinfer-rust) (optional, for faster delimiter repair)

**Install clj-nrepl-eval:**

```bash
bbin install https://github.com/bhauman/clojure-mcp-light.git --tag v0.2.1 \
  --as clj-nrepl-eval \
  --main-opts '["-m" "clojure-mcp-light.nrepl-eval"]'
```

Verify installation:
```bash
clj-nrepl-eval -p 7889 "(+ 1 2 3)"
# => 6
```

**Install clj-paren-repair:**

```bash
bbin install https://github.com/bhauman/clojure-mcp-light.git --tag v0.2.1 \
  --as clj-paren-repair \
  --main-opts '["-m" "clojure-mcp-light.paren-repair"]'
```

Verify installation:
```bash
echo '(defn hello [x] (+ x 1)' | clj-paren-repair
# Auto-repairs and formats the code
```

**Full installation guide:** https://github.com/bhauman/clojure-mcp-light#quick-install

## Prompt Compression

The SYSTEM.md file is comprehensive but can consume significant context
window space. Use the included `compress.py` tool to reduce token count
by up to 20x while preserving key information:

```bash
# Install dependencies (first time only)
pipenv install

# Compress SYSTEM.md with default 50% compression
just compress SYSTEM.md

# Compress to specific token count
just compress SYSTEM.md --target-tokens 5000 -o compressed.md

# Aggressive compression (70% reduction)
just compress SYSTEM.md --rate 0.3 -o compressed.md

# List available models
just models

# Pre-download model for offline use
just download
```

**How it works:**

The tool uses [Microsoft's LLMLingua](https://github.com/microsoft/LLMLingua)
to identify and remove non-essential tokens using a trained language
model. It achieves high compression rates while maintaining semantic
meaning and preserving structural elements like XML tags.

**Available models:**

- **microsoft/llmlingua-2-xlm-roberta-large-meetingbank** (default, ~1.2GB)
  - Best compression quality, 3-6x faster than LLMLingua-1
- **microsoft/llmlingua-2-bert-base-multilingual-cased-meetingbank** (~700MB)
  - Good quality with lower resource requirements
- **microsoft/phi-2** (~5GB, LLMLingua-1)
  - Alternative compression approach

**Command options:**

```bash
pipenv run python compress.py compress --help

Options:
  -o, --output PATH            Output file path (default: stdout)
  -r, --rate FLOAT             Compression rate 0.0-1.0 (default: 0.5)
  -t, --target-tokens INTEGER  Target token count (overrides --rate)
  -m, --model TEXT             Model to use for compression
  --llmlingua2/--llmlingua1    Use LLMLingua-2 or LLMLingua-1
  --force-tokens TEXT          Tokens to preserve (default: "\n,?")
  --stats/--no-stats           Show compression statistics
```

**Example output:**

```
--- Compression Statistics ---
Original tokens:    7500
Compressed tokens:  3750
Compression ratio:  2.0x
Savings:            Saving $0.04 in GPT-4
```

**Benefits:**

- Lower API costs (fewer input tokens)
- Fit within stricter context limits
- Faster processing times
- Minimal performance loss (maintains key instructions)

## Version

Current version: v1.9.1 (see CHANGELOG.md for details)

## License

MIT License - see LICENSE file for details.

## Contributing

This prompt was developed through extensive research on LLM behavior
with niche languages. See [research.md](research.md) for the evidence
base and citations.
