#!/usr/bin/env python3
"""
CLI tool for compressing prompts using LLMLingua.

This tool uses Microsoft's LLMLingua to compress long prompts while preserving
key information, achieving up to 20x compression with minimal performance loss.
"""

import sys
from pathlib import Path

import click
from llmlingua import PromptCompressor


@click.group()
@click.version_option(version="1.0.0")
def cli():
    """Compress prompts using LLMLingua for more efficient LLM usage.
    
    Reduces token count by up to 20x while maintaining key information,
    lowering costs and improving context window utilization.
    """
    pass


@cli.command()
@click.argument('input_file', type=click.Path(exists=True, path_type=Path))
@click.option(
    '-o', '--output',
    type=click.Path(path_type=Path),
    help='Output file path. If not specified, prints to stdout.'
)
@click.option(
    '-r', '--rate',
    type=float,
    default=0.5,
    help='Compression rate (0.0-1.0). Lower = more compression. Default: 0.5'
)
@click.option(
    '-t', '--target-tokens',
    type=int,
    help='Target token count after compression (overrides --rate if specified).'
)
@click.option(
    '-m', '--model',
    default='microsoft/llmlingua-2-xlm-roberta-large-meetingbank',
    help='Model to use for compression. Default: llmlingua-2-xlm-roberta-large-meetingbank'
)
@click.option(
    '--llmlingua2/--llmlingua1',
    default=True,
    help='Use LLMLingua-2 (faster) or LLMLingua-1. Default: llmlingua2'
)
@click.option(
    '-q', '--question',
    default='',
    help='Optional question context for compression.'
)
@click.option(
    '-i', '--instruction',
    default='',
    help='Optional instruction context for compression.'
)
@click.option(
    '--force-tokens',
    default='\n,?',
    help='Comma-separated tokens to preserve during compression. Default: "\\n,?"'
)
@click.option(
    '--stats/--no-stats',
    default=True,
    help='Show compression statistics. Default: --stats'
)
def compress(
    input_file,
    output,
    rate,
    target_tokens,
    model,
    llmlingua2,
    question,
    instruction,
    force_tokens,
    stats
):
    """Compress a prompt file using LLMLingua.
    
    INPUT_FILE: Path to the file containing the prompt to compress.
    
    Examples:
    
      # Compress SYSTEM.md with 50% compression rate
      python compress.py compress SYSTEM.md
      
      # Compress to specific token count
      python compress.py compress SYSTEM.md --target-tokens 5000
      
      # Aggressive compression (70% reduction)
      python compress.py compress SYSTEM.md --rate 0.3 -o compressed.md
      
      # Use LLMLingua-1 instead
      python compress.py compress SYSTEM.md --llmlingua1
    """
    
    # Read input file
    try:
        prompt_text = input_file.read_text(encoding='utf-8')
    except Exception as e:
        click.echo(f"Error reading input file: {e}", err=True)
        sys.exit(1)
    
    if not prompt_text.strip():
        click.echo("Error: Input file is empty", err=True)
        sys.exit(1)
    
    # Initialize compressor
    click.echo(f"Initializing {model} on CPU...", err=True)
    try:
        if llmlingua2:
            compressor = PromptCompressor(
                model_name=model,
                device_map="cpu",
                use_llmlingua2=True
            )
        else:
            compressor = PromptCompressor(
                model_name=model,
                device_map="cpu"
            )
    except Exception as e:
        click.echo(f"Error initializing compressor: {e}", err=True)
        sys.exit(1)
    
    # Prepare force_tokens
    force_token_list = [t.strip() for t in force_tokens.split(',') if t.strip()]
    
    # Compress
    click.echo("Compressing prompt...", err=True)
    try:
        if target_tokens:
            result = compressor.compress_prompt(
                prompt_text,
                instruction=instruction,
                question=question,
                target_token=target_tokens,
                force_tokens=force_token_list if llmlingua2 else None
            )
        else:
            result = compressor.compress_prompt(
                prompt_text,
                instruction=instruction,
                question=question,
                rate=rate,
                force_tokens=force_token_list if llmlingua2 else None
            )
    except Exception as e:
        click.echo(f"Error during compression: {e}", err=True)
        sys.exit(1)
    
    compressed_prompt = result['compressed_prompt']
    
    # Output results
    if output:
        try:
            output.write_text(compressed_prompt, encoding='utf-8')
            click.echo(f"Compressed prompt written to {output}", err=True)
        except Exception as e:
            click.echo(f"Error writing output file: {e}", err=True)
            sys.exit(1)
    else:
        click.echo(compressed_prompt)
    
    # Show statistics
    if stats:
        click.echo("\n--- Compression Statistics ---", err=True)
        click.echo(f"Original tokens:    {result.get('origin_tokens', 'N/A')}", err=True)
        click.echo(f"Compressed tokens:  {result.get('compressed_tokens', 'N/A')}", err=True)
        click.echo(f"Compression ratio:  {result.get('ratio', 'N/A')}", err=True)
        if 'saving' in result:
            click.echo(f"Savings:           {result['saving']}", err=True)


@cli.command()
@click.option(
    '-m', '--model',
    default='microsoft/llmlingua-2-xlm-roberta-large-meetingbank',
    help='Model to download. Default: llmlingua-2-xlm-roberta-large-meetingbank'
)
@click.option(
    '--llmlingua2/--llmlingua1',
    default=True,
    help='Download LLMLingua-2 or LLMLingua-1 model. Default: llmlingua2'
)
def download(model, llmlingua2):
    """Download and cache the compression model.
    
    This is useful to pre-download models before first use.
    
    Examples:
    
      # Download default LLMLingua-2 model
      python compress.py download
      
      # Download smaller LLMLingua-2 model
      python compress.py download --model microsoft/llmlingua-2-bert-base-multilingual-cased-meetingbank
      
      # Download LLMLingua-1 model
      python compress.py download --model microsoft/phi-2 --llmlingua1
    """
    click.echo(f"Downloading {model} (CPU mode)...", err=True)
    try:
        if llmlingua2:
            compressor = PromptCompressor(
                model_name=model,
                device_map="cpu",
                use_llmlingua2=True
            )
        else:
            compressor = PromptCompressor(
                model_name=model,
                device_map="cpu"
            )
        click.echo(f"Successfully downloaded and cached {model}", err=True)
    except Exception as e:
        click.echo(f"Error downloading model: {e}", err=True)
        sys.exit(1)


@cli.command()
def models():
    """List available compression models.
    
    Shows recommended models for different use cases.
    """
    click.echo("\n=== LLMLingua-2 Models (Recommended) ===\n")
    
    click.echo("Large model (best quality):")
    click.echo("  microsoft/llmlingua-2-xlm-roberta-large-meetingbank")
    click.echo("  - Best compression quality")
    click.echo("  - 3-6x faster than LLMLingua-1")
    click.echo("  - ~1.2GB download size\n")
    
    click.echo("Small model (faster, less memory):")
    click.echo("  microsoft/llmlingua-2-bert-base-multilingual-cased-meetingbank")
    click.echo("  - Good quality with lower resource requirements")
    click.echo("  - ~700MB download size\n")
    
    click.echo("=== LLMLingua-1 Models ===\n")
    
    click.echo("Phi-2 (recommended for LLMLingua-1):")
    click.echo("  microsoft/phi-2")
    click.echo("  - Good balance of quality and speed")
    click.echo("  - ~5GB download size\n")
    
    click.echo("GPT2-small (lightweight):")
    click.echo("  gpt2")
    click.echo("  - Fast and lightweight")
    click.echo("  - Lower quality than larger models\n")
    
    click.echo("LLaMA-based models (quantized, <8GB GPU):")
    click.echo("  TheBloke/Llama-2-7b-Chat-GPTQ")
    click.echo("  - Requires: pip install optimum auto-gptq")
    click.echo("  - Good quality with GPU acceleration\n")


if __name__ == '__main__':
    cli()
