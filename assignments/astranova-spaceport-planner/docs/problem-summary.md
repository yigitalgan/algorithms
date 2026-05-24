# Problem Summary

## Launch Operations Timeline

The first part models launch-preparation operations as a prerequisite-constrained workflow. Each operation has a code, label, execution time, and a list of prerequisite operations. The goal is to compute the earliest possible begin and finish time for every operation and determine the minimum launch-readiness time.

Key concepts:

- Directed acyclic dependency graph
- Earliest-start scheduling
- Critical-chain style readiness-time computation
- XML parsing

## Spaceport Transit Planner

The second part models movement inside a spaceport. Walking is allowed between any two points, while autonomous shuttle corridors are directed and connect predefined stations. The goal is to compute the fastest route from origin to destination.

Key concepts:

- Weighted graph construction
- Directed shuttle edges
- Complete walking graph over stations
- Shortest path computation
- Regular-expression based input parsing
