# Problem Summary

This assignment implements **MediPlan: Intelligent Diagnostic Planner**, a dynamic-programming based system for evaluating diagnostic test dependency graphs.

## Core Tasks

- Parse `diagnostic_catalogue.xml` and `diagnostic_requests.xml` with Java standard XML parsing APIs.
- Build a dependency graph where diagnostic tests depend on lower-level tests.
- Compute processing costs for `DERIVED` tests from distinct non-`NONE` sample types among their direct inputs.
- Use top-down memoized dynamic programming to compute patient burden for a target test.
- Use bottom-up tabulation dynamic programming and topological ordering to compute naive hospital costs for multiple targets.
- Build an optimized diagnostic plan by tracing reachable tests and counting each test once.

## Algorithmic Concepts

- Directed acyclic dependency graphs
- Dynamic programming with memoization
- Dynamic programming with tabulation
- DFS and topological sorting
- Reachability analysis
- Cost aggregation and redundancy elimination
