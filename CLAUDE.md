# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build fat JAR (includes all dependencies)
mvn clean package

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=BigDecimalUtilsTest

# Run a single test method
mvn test -Dtest=BigDecimalUtilsTest#testAverageWhenNoValue

# Launch the GUI (after building)
java -jar target/acquisition-delay-*-jar-with-dependencies.jar
```

The deployable artifact is `target/acquisition-delay-*-jar-with-dependencies.jar` (fat JAR built by `maven-assembly-plugin`). The plain JAR in `target/` is not runnable on its own.

## Architecture

The project is a JavaFX desktop application that computes camera acquisition delay from Tangra light curve CSV files. It has three layers:

**`ui/`** — JavaFX GUI entry point. `GuiLauncher` is the manifest main class; it delegates to `Gui` (a `javafx.application.Application`). The GUI persists the last-opened directory in `~/.acquisition-delay.properties`. A disabled `Cli` class exists for historical reference.

**`service/`** — Core computation. `AcquisitionDelay.calculate()` is the single entry point called by the GUI. It:
1. Reads the CSV via `TangraCsvFileReader`
2. For each tracked object, calls `calculatePrecisely()` to find PPS (Pulse Per Second) LED rise/fall times from signal transitions (illuminance 20–80% range)
3. When ≥2 objects are measured and a Y position is set, fits a `LinearTrend` (for rolling-shutter sensors) to extrapolate delay at an arbitrary Y row

Supporting classes: `BigDecimalUtils` (statistical ops — average, RMS, covariance, Student t-factors), `IntStatistics`, `IntUtils` (median), `ObjectResult` (per-object output), `Delay` (delay + Y position pair), `LinearTrend` (y = ax + b).

**`dao/`** — Tangra CSV parsing. `TangraCsvFileReader` implements `FileReader` and populates `ObjectInfomation` (Y coordinate + list of `MeasurePoint`s). `MeasurePoint` holds `timeInMs`, `signalInAdu`, and `background`.

## Key Domain Concepts

- **PPS LED**: lights up for exactly 100 ms each UTC second; frames capturing the LED partially lit give the acquisition delay.
- **Baseline / top-line**: computed from median-based filtering — signals below `median + (median - min)` form the dark baseline; signals above `max - 5σ` form the fully-lit top line.
- **Rolling shutter**: cameras with a rolling shutter have a different delay at each Y row; the linear trend over multiple tracked objects corrects for this.
- **Y position = -1**: disables the rolling-shutter linear trend.

## Dependencies & Java Version

- Compiled and run with Java 11 (source/target in pom.xml); Maven build works with JDK ≥ 8.
- JavaFX 21 is bundled in the fat JAR (via `org.openjfx:javafx-controls`).
- Logging: Log4j 2 (API + Core).
- Tests: JUnit Jupiter 5.

## CI / Release

GitHub Actions (`.github/workflows/maven-publish.yml`) triggers on release creation, builds with Maven, and publishes to GitHub Packages.
