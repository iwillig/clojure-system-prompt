# Research: Why Custom System Prompts for Niche Languages Matter

This document contains the research and evidence supporting the use of custom system prompts for niche programming languages like Clojure.

## The Training Data Bias Problem

Research has documented significant programming language bias in LLMs:

- **Python dominance**: Studies show LLMs use Python in [90-97% of benchmark tasks](https://arxiv.org/html/2503.17181v1), even for language-agnostic problems. For high-performance tasks where Python is not optimal, it remains the dominant choice in 58% of cases.

- **Training data imbalance**: The StarCoder dataset shows Python alone accounts for nearly 40% of its training corpus, while many other languages appear only marginally. Users in communities like StackOverflow concentrate on certain languages (Python, JavaScript), which degrades diversity when collecting training data.

- **The "Matthew Effect"**: [Research suggests](https://arxiv.org/html/2509.23261v1) that AI programming assistance may systematically influence which languages, frameworks, and paradigms thrive or decline—mainstream ecosystems get reinforced while niche languages receive weaker support.

- **Functional language challenges**: LLMs "frequently hallucinate functions that don't exist and have more trouble writing good Clojure code" according to [analysis of LLM Clojure generation](https://mccormick.cx/news/entries/advantages-of-generating-clojure-with-llms).

## Why Custom Prompts Help

Custom system prompts (like `CLAUDE.md` files) compensate for training data gaps by:

1. **Providing domain-specific knowledge**: Including idioms, conventions, and best practices the LLM may not have encountered frequently in training data.

2. **Preventing hallucinations**: Explicitly documenting which libraries, functions, and patterns actually exist in your ecosystem.

3. **Enforcing paradigm consistency**: Ensuring the LLM generates idiomatic functional code rather than defaulting to imperative patterns from more common languages.

4. **Context efficiency**: [Clojure's concise syntax](https://mccormick.cx/news/entries/advantages-of-generating-clojure-with-llms) means less context space is needed for code examples, allowing more room for guidance and conventions.

## Best Practices for Language-Specific Prompts

Based on [Anthropic's recommendations](https://www.anthropic.com/engineering/claude-code-best-practices) and [community research](https://arize.com/blog/claude-md-best-practices-learned-from-optimizing-claude-code-with-prompt-learning/):

- **Keep it concise**: Research indicates frontier LLMs can follow ~150-200 instructions reliably. Since Claude Code's system prompt already contains ~50 instructions, your custom prompt should contain as few additional instructions as possible.

- **Use pointers, not copies**: Don't include code snippets that will become outdated. Instead, reference `file:line` locations to point to authoritative context.

- **Avoid redundant style guidelines**: Let linters and formatters handle code style. LLMs are slow and expensive compared to traditional tooling for these tasks.

- **Prioritize correctness over completeness**: For each line, ask "Would removing this cause Claude to make mistakes?" If not, remove it.

- **Add emphasis for critical rules**: Use "IMPORTANT" or "YOU MUST" for instructions that require strict adherence.

## Clojure-Specific Advantages

Despite training data challenges, Clojure has characteristics that work well with LLM-assisted development:

- **Easier validation**: Consistent syntax and functional code enable easier linting and testing. LLMs perform better in loops where generated code is validated and errors are fed back.

- **REPL-driven development**: [Current LLMs work well with the Clojure REPL](https://github.com/bhauman/clojure-mcp), enabling interactive validation of generated code.

- **Data-oriented design**: Immutable state and pure functions make LLM-generated agents testable, traceable, and straightforward to reason about.

- **Homoiconicity**: The "data = code" feature of Lisp has potential for automatic program generation and manipulation.

## Quantifiable Improvements from Custom Prompts

Studies show substantial accuracy gains from well-engineered prompts:

- **57-67% accuracy improvements**: [Research on 26 prompting principles](https://codingscape.com/blog/26-principles-for-prompt-engineering-to-increase-llm-accuracy) found that well-engineered prompts can increase accuracy by 57% on LLaMA models and 67% on GPT-4.

- **High sensitivity to instructions**: LLM performance is [highly sensitive to prompt choices](https://arxiv.org/abs/2402.07927)—"reordering examples in a prompt produced accuracy shifts of more than 40 percent."

- **Domain-specific gains**: [Classification tasks showed](https://medium.com/tr-labs-ml-engineering-blog/optimizing-prompts-across-llms-a-comprehensive-overview-part-1-3ae4b0a2ff51) "providing clear category definitions before examples improved accuracy by an average of 18% across all models."

## Why Custom Instructions Beat Defaults

LLMs have known limitations that custom prompts address:

- **Verbosity by design**: [Models are trained to be helpful](https://community.openai.com/t/custom-instructions-to-make-gpt-4o-concise/905595) through comprehensive answers, but custom prompts can guide more concise, targeted responses.

- **Missing domain context**: "LLMs lack intrinsic knowledge of research... this limitation emphasizes the importance of domain expertise in crafting prompts" according to [prompt engineering research](https://pmc.ncbi.nlm.nih.gov/articles/PMC12191768/).

- **Coding-specific benefits**: [Addy Osmani notes](https://addyo.substack.com/p/my-llm-coding-workflow-going-into) that providing "in-line examples of the output format or approach you want" dramatically improves results—"LLMs are great at mimicry."

- **GitHub Copilot evidence**: [Developers report](https://www.nathannellans.com/post/all-about-github-copilot-custom-instructions) being "shocked... how few people use custom instructions, given how effective they are—he could guide the AI to output code matching his team's idioms."

## Why CLAUDE.md Outperforms Skills and AGENTS.md

For language-specific conventions, always-loaded prompts have structural advantages over on-demand mechanisms.

### The Skill Activation Reliability Problem

Skills rely on the LLM to decide when to invoke them—and this is unreliable:

- **Non-deterministic activation**: "[The skill selection mechanism has no algorithmic routing](https://paddo.dev/blog/claude-skills-controllability-problem/)... it's pure LLM reasoning—no regex, no keyword matching."

- **Low success rates**: Testing found ["the simple instruction approach gives you 20% success"](https://scottspence.com/posts/how-to-make-claude-code-skills-activate-reliably). Even forced hooks only achieve 80-84%.

- **Documented failures**: GitHub issues show ["Claude systematically fails to invoke the Skill tool even when requests clearly match"](https://github.com/anthropics/claude-code/issues/19308).

- **Unstable activation**: ["Even after explicitly stating it in the prompt"](https://github.com/orgs/community/discussions/182117), skills may load 0-3 times when 5+ are requested.

### Cognitive Science: The "Lost in the Middle" Effect

Research on LLM attention explains why always-loaded context works better:

- **U-shaped attention**: [Studies show](https://milvus.io/ai-quick-reference/how-does-the-length-of-retrieved-context-fed-into-the-prompt-affect-the-llms-performance-and-the-risk-of-it-ignoring-some-parts-of-the-context) "information at the beginning and end of a context window is more reliably processed than information in the middle."

- **Recency bias**: ["Transformers naturally weight recent tokens more heavily"](https://blog.promptlayer.com/disadvantage-of-long-prompt-for-llm/)—a 10,000-token prompt might effectively operate on just the last 2,000 tokens.

- **System prompt advantage**: CLAUDE.md appears at the beginning of every conversation, benefiting from the primacy effect.

### Context Length vs. Retrieval Research

Academic research comparing always-in-context vs. retrieved-on-demand:

- **Length alone hurts performance**: [Research reveals](https://arxiv.org/html/2510.05381v1) "the sheer length of the input alone can hurt LLM performance, independent of retrieval quality."

- **Retrieval matches long context**: [Studies found](https://arxiv.org/abs/2310.03025) 4K context with retrieval matches 16K fine-tuned context while using less computation.

- **Context stuffing degrades quality**: ["Answer quality decreases, and hallucination risk increases"](https://www.pinecone.io/blog/why-use-retrieval-instead-of-larger-context/) with stuffing approaches.

### Comparison Table

| Aspect | CLAUDE.md | Skills | AGENTS.md |
|--------|-----------|--------|-----------|
| **Loading** | Always loaded | On-demand, LLM decides | Cross-tool standard |
| **Reliability** | 100% (guaranteed) | ~20-84% activation | Varies by tool |
| **Position** | Beginning (primacy effect) | Mid-conversation | Tool-dependent |
| **Best for** | Language conventions | Complex workflows | Multi-tool compatibility |

### Practical Recommendations

For language-specific guidance like Clojure idioms:

1. **Put critical conventions in CLAUDE.md** (always loaded, 100% reliability)
2. **Keep CLAUDE.md under ~500 lines** to avoid attention dilution
3. **Use skills only for optional workflows** you'll invoke explicitly with slash commands
4. **Don't rely on automatic skill activation** for anything critical

## Further Reading

### Training Data Bias
- [LLMs Love Python: A Study of LLMs' Bias for Programming Languages](https://arxiv.org/html/2503.17181v1)
- [The Matthew Effect of AI Programming Assistants](https://arxiv.org/html/2509.23261v1)

### Prompt Engineering Research
- [26 Principles for Prompt Engineering](https://codingscape.com/blog/26-principles-for-prompt-engineering-to-increase-llm-accuracy)
- [A Systematic Survey of Prompt Engineering in LLMs](https://arxiv.org/abs/2402.07927)
- [Unleashing the Potential of Prompt Engineering](https://pmc.ncbi.nlm.nih.gov/articles/PMC12191768/)

### CLAUDE.md Best Practices
- [Claude Code Best Practices](https://www.anthropic.com/engineering/claude-code-best-practices)
- [Writing a Good CLAUDE.md](https://www.humanlayer.dev/blog/writing-a-good-claude-md)
- [CLAUDE.md Best Practices from Arize](https://arize.com/blog/claude-md-best-practices-learned-from-optimizing-claude-code-with-prompt-learning/)

### Skills and Context Engineering
- [Claude Skills: The Controllability Problem](https://paddo.dev/blog/claude-skills-controllability-problem/)
- [How to Make Claude Code Skills Activate Reliably](https://scottspence.com/posts/how-to-make-claude-code-skills-activate-reliably)
- [Context Length Alone Hurts LLM Performance](https://arxiv.org/html/2510.05381v1)
- [Retrieval Meets Long Context LLMs](https://arxiv.org/abs/2310.03025)

### Clojure-Specific
- [Advantages of Generating Clojure with LLMs](https://mccormick.cx/news/entries/advantages-of-generating-clojure-with-llms)
- [Clojure MCP - LLM-friendly Clojure development](https://github.com/bhauman/clojure-mcp)
- [LLM Agents on the JVM: Clojure vs Python](https://www.freshcodeit.com/blog/llm-agents-in-clojure)
