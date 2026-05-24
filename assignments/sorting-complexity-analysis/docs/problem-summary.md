# Problem Summary

The assignment studies empirical algorithm complexity through sorting benchmarks.

## Task

Implement and compare five sorting algorithms:

- Iterative Quick Sort
- Insertion Sort
- Iterative Merge Sort
- Shell Sort
- Radix Sort

## Experiment Design

The benchmark uses the `volume` column from a historical stock-market CSV dataset. For each algorithm, the program measures average running time over 10 runs for the following input sizes:

```text
500, 1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000, 250000
```

The same algorithms are evaluated under three input conditions:

- Original/random input order
- Already sorted input
- Reversely sorted input

## Outputs

The program prints timing tables to stdout and generates plots comparing algorithm performance across input sizes.
