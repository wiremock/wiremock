---
name: breaking-change-report
description: Run the japicmp binary-compatibility report for wiremock-core and produce a filtered summary covering only @PublishedAPI-annotated classes. Use this when asked to generate, refresh, or summarise the breaking-changes report.
allowed-tools: Bash, Read, Write
---

Generate a filtered breaking-change summary for `@PublishedAPI`-annotated classes by following these steps in order.

## 1 — Run the report

```bash
./gradlew :wiremock-core:japicmp --no-daemon -q
```

If the build fails, diagnose and fix before continuing.

## 2 — Collect the published API surface

Run this to get the fully-qualified class names of every `@PublishedAPI`-annotated type:

```bash
grep -rl "@PublishedAPI" wiremock-core/src/main/java --include="*.java" \
  | sed 's|.*/java/||; s|\.java$||; s|/|.|g' | sort
```

Keep this list in mind — it is the filter for the next step.

## 3 — Read the raw report

Read the full text report at:

```
wiremock-core/build/reports/japicmp/breaking-changes.txt
```

## 4 — Produce the filtered summary

Cross-reference the raw report against the `@PublishedAPI` class list. Include an entry if:
- The class name matches a `@PublishedAPI` class exactly, **or**
- The class is a public inner class of a `@PublishedAPI` class (indicated by `$` in the name).

Exclude entries where the only change is the class-file format version bump
(Java version upgrade) — note this once at the top of the output instead.

Write the filtered summary as Markdown to:

```
wiremock-core/build/reports/japicmp/breaking-changes-published-api.md
```

Use the following structure:

```
# Breaking changes in published API — WireMock <old-version> → <new-version>

> Java class-file version bump (X → Y) affects every class; Java N is now
> the minimum runtime.

---

## <Logical group (e.g. Core server & configuration)>

### `ClassName`
- **Removed method** `methodSignature()`
- **Return type changed** `method()`: `OldType` → `NewType`
- ...

(repeat per class, grouped sensibly)
```

Group classes by area rather than listing them alphabetically:
- Core server & configuration (`WireMockServer`, `WireMockConfiguration`, `Options`, `MappingsSaver`)
- Client DSL (`WireMock`, `WireMockBuilder`, etc.)
- Stub mapping model (`StubMapping`, `StubMappings`, etc.)
- HTTP model (`ResponseDefinition`, `Request`, `RequestMethod`, etc.)
- Extension SPI (`StubLifecycleListener`, `HttpServerFactory`, etc.)
- Common / shared types (`Metadata`, `Parameters`, etc.)
- Recording API (`SnapshotRecordResult`, `SnapshotOutputFormatter`, etc.)
- Any other areas that have changes

Omit groups that have no breaking changes in `@PublishedAPI` classes.

Once the file is written, print the path and a one-line count of how many
`@PublishedAPI` classes had breaking changes.
