# Repository Guidelines

## Project Structure & Modules
- Build tool: Mill (`build.mill`). Cross-built for Scala 2.13.16 and 3.3.6, with JVM and Scala.js targets.
- Module: `simple-form/`
  - Shared sources: `simple-form/src/io/github/nafg/simpleform/...`
  - Scala 2 specifics: `simple-form/src-2/io/github/nafg/simpleform/...`
  - Scala 3 specifics: `simple-form/src-3/io/github/nafg/simpleform/...`
  - Tests: `simple-form/test/src/io/github/nafg/simpleform/...` (ZIO Test)

## Build, Test, and Tasks (Mill)
- Compile all: `mill --no-server --jobs 0 _.compile`
- Test all: `mill --no-server --jobs 0 _.test`
- Example per-target:
  - JVM, Scala 2.13: `mill simple-form[2.13.16].jvm.test`
  - JVM, Scala 3: `mill simple-form[3.3.6].jvm.test`
  - JS, Scala 2.13: `mill simple-form[2.13.16].js.test`
- Clean: `rm -rf out` (or `git clean -fdx` if appropriate)

## Local Setup
- JDK: Use a JDK compatible with CI. CI uses Temurin 17 for tests and Temurin 11 for publish.
- Install Mill: download from Mill releases and put on `PATH`.
- Optional cache: set `COURSIER_CACHE=$PWD/.cs-cache` to keep downloads within the repo.
- Sanity check: `mill --no-server --jobs 0 _.compile && mill --no-server --jobs 0 _.test`.

## Coding Style & Naming
- Formatting: Scalafmt configured in `.scalafmt.conf` (IntelliJ preset, maxColumn 120). Format before committing (via IDE or `scalafmt` CLI; no Mill task wired here).
- Scala style: 2‑space indents; classes/traits `UpperCamelCase`; methods/vals `lowerCamelCase`.
- Packages: library code under `io.github.nafg.simpleform`; tests mirror package structure and end with `*Tests.scala`.

## Testing Guidelines
- Framework: ZIO Test. Mill config uses `zio.test.sbt.ZTestFramework`.
- Structure: prefer `ZIOSpecDefault` with `suite`/`test`.
- Naming: one suite per behavior; end files with `*Tests.scala` (e.g., `FormTypeTests.scala`).
- Run a single project’s tests with Mill module paths (see examples above).

## Commit & Pull Requests
- Commits: concise, imperative subject (<=72 chars) with a contextual body.
- PRs: include summary, linked issues (`#123`), screenshots/logs when relevant, and notes on breaking changes.
- Before opening PR: compile, test (all cross variants), and format code.

## Architecture Overview
- Core typeclass: `io.github.nafg.simpleform.FormType[A]` encodes/decodes values to/from `SimpleForm`, with `run` to pair errors with the original form.
- String layer: `FormType.StringCodec[A]` for primitives/domain types (e.g., `Boolean`, `Int`, `LocalDate`), with `xmap` for adaptations.
- Collections/options: `FormType[Option[A]]`, `FormType[List[A]]`; boolean checkbox semantics handled explicitly.
- Derivation: Magnolia-based. Scala 2 uses macro `FormTypeDerive.derived[A]`; Scala 3 uses `FormTypeDerive` with `join` for products.
- Cross-platform: shared sources compile to JVM and Scala.js; put platform specifics under `simple-form/{jvm,js}/src/...` if/when needed.

## Environment Tips
- Scala.js: version `1.19.0`. On CI, `ubuntu-latest` provides Node; locally ensure Node 18+ is installed to run JS tests.
- Coursier: use the cache action/`COURSIER_CACHE` to speed up dependency resolution.
