# MediPlan Diagnostic Planner

A Java 11 implementation of a dynamic-programming based diagnostic planning system. The program parses a diagnostic catalogue and a request file from XML, builds a dependency graph of diagnostic tests, and computes patient burden and hospital cost under different execution models.

## Problem Overview

The diagnostic catalogue contains three kinds of tests:

- **RAW** tests collected directly from the patient, with explicit collection costs and sample types.
- **DERIVED** tests computed from one or more RAW tests, with processing costs inferred from the distinct sample types of their direct inputs.
- **COMPOSITE** tests aggregated from DERIVED or COMPOSITE tests.

The planner computes:

1. **Patient burden**: the number of distinct non-`NONE` physical collection procedures required for a target diagnostic score.
2. **Naive hospital cost**: an aggregated dependency-chain cost computed with bottom-up dynamic programming.
3. **Optimized hospital cost**: a traceback-based execution cost where each reachable test is counted once.

## Implemented Algorithms

- XML parsing and dependency graph construction using Java standard libraries.
- Top-down memoized dynamic programming for patient burden analysis.
- Bottom-up tabulation dynamic programming for naive hospital cost computation.
- DFS-based topological sorting of the diagnostic dependency graph.
- Traceback-based diagnostic plan construction with optimized execution cost.

## Project Structure

```text
mediplan-diagnostic-planner/
├── README.md
├── Makefile
├── src/
│   ├── Main.java
│   ├── DiagnosticCatalogue.java
│   ├── DiagnosticRequest.java
│   └── MediPlanDP.java
├── examples/
│   ├── diagnostic_catalogue_simple.xml
│   └── diagnostic_requests_simple.xml
├── outputs/
│   └── .gitkeep
└── docs/
    ├── problem-summary.md
    ├── cleanup-notes.md
    ├── cv-bullets.md
    ├── github-upload-checklist.md
    └── root-readme-entry.md
```

## Build and Run

Compile:

```bash
make compile
```

Run with the included example XML files:

```bash
make run
```

Equivalent manual commands:

```bash
mkdir -p bin
javac -d bin src/*.java
java -cp bin Main examples/diagnostic_catalogue_simple.xml examples/diagnostic_requests_simple.xml
```

## Input Files

The program expects two command-line arguments:

```bash
java Main <diagnostic_catalogue.xml> <diagnostic_requests.xml>
```

The first XML file defines diagnostic tests and dependencies. The second XML file defines one `single_target` and a list of `all_targets`.

## Notes

- The assignment PDF and original submission ZIP are excluded from this repository.
- `.class` files and other generated build artifacts are ignored.
- The source files use no Java packages, matching the original assignment submission style.
