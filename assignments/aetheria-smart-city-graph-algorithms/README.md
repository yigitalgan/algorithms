# Aetheria Smart City Graph Algorithms

This project implements graph-based infrastructure analysis for the fictional smart city of Aetheria. The system parses an XML city blueprint and produces a full system report covering power-grid optimization, boot-sequence validation, secure communication hubs, and leak-containment analysis.

## Problem Scope

The assignment consists of four graph algorithm modules:

1. **Power Grid**
   - Checks whether the city graph is connected.
   - Computes a minimum spanning tree to minimize high-voltage cable cost.
   - Uses the provided weighted `Graph` and `Edge` APIs.

2. **Master Boot Sequence**
   - Computes a valid system activation order with topological sorting.
   - Detects circular dependencies and reports infeasible boot sequences.
   - Uses the provided `Digraph` API.

3. **Secure Communication Hubs**
   - Finds strongly connected components in a directed fiber network.
   - Reports multi-station SCCs as high-interaction zones.

4. **Bio-Leak Containment Protocol**
   - Uses DFS to list all stations reachable from a leak source.
   - Uses BFS to group reachable stations by minimum hop distance from the source.

## Technologies

- Java 11
- XML parsing with standard `javax.xml.parsers`
- Graph and digraph APIs from the course starter code

## Project Structure

```text
src/
├── AetheriaCity.java
├── PowerGrid.java
├── BootSequence.java
├── CommHubs.java
└── BioLeakContainment.java
examples/
└── aetheria_sample.xml
outputs/
└── .gitkeep
docs/
├── problem-summary.md
├── cleanup-notes.md
├── cv-bullets.md
├── github-upload-checklist.md
└── root-readme-entry.md
```

## Required Starter-Code Classes

The submitted ZIP contained the solution classes only. To compile and run the project locally, the following starter-code classes are also required in `src/`:

```text
Bag.java
Edge.java
Graph.java
Digraph.java
```

These classes are referenced by the implementation and were provided separately in the course starter code.

## Build and Run

Place the starter-code classes listed above into `src/`, then run:

```bash
cd src
javac *.java
java AetheriaCity ../examples/aetheria_sample.xml
```

## Main Algorithms

- Depth-first search for graph connectivity and leak reachability
- Kruskal-style minimum spanning tree construction with union-find
- Topological sorting with cycle detection for boot dependencies
- Kosaraju-style strongly connected component detection
- Breadth-first search for minimum-hop containment layers

## Notes

- Original assignment PDFs and submission archives are intentionally excluded.
- Compiled `.class` files and local outputs are ignored.
- The repository is structured for portfolio presentation while preserving the original source files.
