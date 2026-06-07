# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Compile, run tests, and create the jlink image + zip
mvn package javafx:jlink

# Run tests only
mvn test

# Run a single test class
mvn test -Dtest=BigDecimalUtilsTest

# Run a single test method
mvn test -Dtest=BigDecimalUtilsTest#testAverageWhenNoValue

# Launch the GUI from the jlink image (Windows)
target\acquisition-delay\bin\acquisition-delay.bat

# Create a native installer from the jlink image (requires WiX on Windows; adapt --type for each OS)
jpackage --type app-image --name "Acquisition Delay" --app-version 1.0.0 \
  --runtime-image target/acquisition-delay \
  --module fr.walliang.astronomy.acquisitiondelay/fr.walliang.astronomy.acquisitiondelay.ui.GuiLauncher \
  --dest target/installer --vendor "Didier Walliang"
# Replace --type app-image with: msi (Windows, needs WiX), dmg (macOS), deb (Linux)
```

**Deployable artifacts** — both are produced per platform by the CI:
- `target/acquisition-delay-*-<platform>.zip` — portable archive (jlink); extract and run `bin/acquisition-delay[.bat]`
- `target/installer/Acquisition Delay.<ext>` — native installer (jpackage); `.msi` on Windows, `.dmg` on macOS, `.deb` on Linux

Both embed a JRE 21; no prior Java installation required. The Maven profile for the current OS activates automatically (`platform-win`, `platform-linux`, `platform-mac`, `platform-mac-aarch64`).

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

- Java 21 source/target (`maven.compiler.release=21` in pom.xml); JDK 21+ required to build.
- JavaFX 21 is bundled in the jlink image via platform-specific Maven artifacts (`-win`, `-linux`, `-mac`, `-mac-aarch64` classifiers).
- Logging: Log4j 2 (API + Core, both compile scope so jlink includes the implementation).
- Tests: JUnit Jupiter 5.
- JPMS: `src/main/java/module-info.java` declares the module `fr.walliang.astronomy.acquisitiondelay`.

## CI / Release

GitHub Actions (`.github/workflows/maven-publish.yml`) triggers on release creation and runs a matrix build on `windows-latest`, `ubuntu-latest`, and `macos-latest` (JDK 21). Each runner produces a platform-specific zip (`acquisition-delay-<version>-<platform>.zip`) uploaded as a release asset.
