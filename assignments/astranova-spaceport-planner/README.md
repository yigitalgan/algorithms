# AstraNova Spaceport Planner

Java 11 implementation of a launch preparation scheduler and spaceport transit planner.

## Problem Overview

This assignment has two algorithmic components:

1. **Launch Operations Timeline**: computes the earliest feasible begin and finish times for launch operations with prerequisite constraints.
2. **Spaceport Transit Planner**: computes the fastest route between two points by combining walking and directed autonomous shuttle corridors.

The implementation uses graph-based scheduling, shortest path computation, and regular-expression based parsing.

## Features

- XML parsing for multiple launch plans.
- Earliest-start schedule computation over prerequisite-constrained operations.
- Readiness time computation for each launch plan.
- Robust `.dat` parsing with regular expressions.
- Fastest-route computation over a mixed walking/shuttle graph.
- Route instruction generation with walking and shuttle segments.

## Project Structure

```text
astranova-spaceport-planner/
├── README.md
├── Makefile
├── src/
│   ├── Main.java
│   ├── LaunchOperationsTimeline.java
│   ├── LaunchPlan.java
│   ├── Operation.java
│   ├── SpaceportTransitNetwork.java
│   ├── SpaceportTransitPlanner.java
│   ├── ShuttleCorridor.java
│   ├── Station.java
│   ├── Point.java
│   ├── Edge.java
│   └── RouteInstruction.java
├── examples/
│   ├── launch_plans_sample.xml
│   └── spaceport_transit_sample.dat
├── outputs/
└── docs/
```

## Build and Run

From this assignment directory:

```bash
make run
```

Manual compilation:

```bash
javac -d build src/*.java
java -cp build Main examples/launch_plans_sample.xml examples/spaceport_transit_sample.dat
```

The original grading configuration uses:

```bash
javac *.java
java Main <launchPlansXMLFile> <spaceportTransitDATFile>
```

For that layout, place the `.java` files directly in one directory.

## Notes

- Original assignment PDFs and submission archives are excluded.
- Build artifacts such as `.class` files are ignored.
- External libraries are not required.
- Example inputs are included only for local sanity checks.
