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

## What This Prompt Provides

- **REPL-first enforcement**: Code is tested in the REPL before being written to files
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

## Project Structure

```
.
├── SYSTEM.md      # The system prompt (copy this to your projects)
├── research.md    # Research supporting custom prompts for niche languages
├── CHANGELOG.md   # Version history
└── LICENSE        # MIT License
```

## Requirements

This prompt assumes you have:

- A Clojure nREPL server running (the prompt will ask you to start it if not)
- The `clj-nrepl-eval` tool available (for REPL evaluation)
- The `clj-paren-repair` tool available (for fixing delimiter errors)

## Version

Current version: v1.5.0 (see CHANGELOG.md for details)

## License

MIT License - see LICENSE file for details.

## Contributing

This prompt was developed through extensive research on LLM behavior
with niche languages. See [research.md](research.md) for the evidence
base and citations.
