# Sorting Complexity Analysis

Java implementation and empirical benchmark of five sorting algorithms on stock-market volume data.

## Overview

This project compares the running-time behavior of comparison-based and non-comparison-based sorting algorithms across random, sorted, and reversely sorted inputs. The experiments sort the `volume` column from a stock-market CSV dataset for increasing input sizes.

Implemented algorithms:

- Iterative Quick Sort
- Insertion Sort
- Iterative Merge Sort
- Shell Sort
- Radix Sort

## Technologies

- Java 11
- XChart for plotting benchmark results
- Maven for dependency management

## Dataset

The original dataset is not included in this repository.

Place the dataset here:

```text
data/all_stocks_5yr.csv
```

Expected CSV format includes a `volume` column as the 6th column:

```text
date,open,high,low,close,volume,Name
```

## Run with Maven

From this assignment directory:

```bash
mvn compile exec:java -Dexec.args="data/all_stocks_5yr.csv"
```

Generated plots are saved under:

```text
outputs/
```

## Run manually with XChart JAR

Download XChart and place the JAR under `lib/`, then run:

```bash
javac -cp "lib/xchart-3.8.1.jar" src/main/java/Main.java -d out
java -cp "out;lib/xchart-3.8.1.jar" Main data/all_stocks_5yr.csv
```

On Linux/macOS, replace `;` with `:` in the classpath.

## Project Structure

```text
sorting-complexity-analysis/
├── README.md
├── pom.xml
├── src/
│   └── main/
│       └── java/
│           └── Main.java
├── data/
│   └── README.md
├── outputs/
│   └── .gitkeep
└── docs/
    ├── problem-summary.md
    ├── cleanup-notes.md
    ├── cv-bullets.md
    ├── github-upload-checklist.md
    └── root-readme-entry.md
```

## Notes

The original submission report and assignment PDF are intentionally not included. The dataset is also excluded to keep the repository lightweight and avoid redistributing course or third-party files.
