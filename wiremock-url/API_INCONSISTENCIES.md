# API Inconsistencies in wiremock-url

This document catalogs API inconsistencies found across the wiremock-url codebase.

## Factory Method Inconsistencies

### 1. Segment lacks static factory methods

**Issue:** `Segment` is a `PercentEncoded` type but has no `parse()` or `encode()` static factory methods.

**Current state:**
- Segment only provides constants: `EMPTY`, `DOT`, `DOT_DOT`
- No way to parse or encode arbitrary segment strings

**Comparison:**
All other `PercentEncoded` types have both methods:
- `Path.parse()` / `Path.encode()` ✓
- `Query.parse()` / `Query.encode()` ✓
- `Fragment.parse()` / `Fragment.encode()` ✓
- `Host.parse()` / `Host.encode()` ✓
- `Username.parse()` / `Username.encode()` ✓
- `Password.parse()` / `Password.encode()` ✓
- `UserInfo.parse()` / `UserInfo.encode()` ✓
- `Segment.parse()` / `Segment.encode()` ✗

**Location:** Segment.java

---

### 2. Scheme uses `register()` instead of `of()`

**Issue:** `Scheme` uses `register()` for factory construction while other types use `of()`.

**Current state:**
- `Scheme.register(String)` - Scheme.java:93
- `Scheme.register(String, Port)` - Scheme.java:112

**Comparison:**
- `Port.of(int)` ✓
- `Authority.of(Host)` / `Authority.of(Host, Port)` / etc. ✓
- `HostAndPort.of(Host)` / `HostAndPort.of(Host, Port)` ✓
- `Scheme.of(String)` ✗ (uses `register()` instead)

**Note:** Scheme also has `parse()`, which has different semantics from `register()` (parse preserves casing, register canonicalizes).

**Location:** Scheme.java

---

### 3. Inconsistent encode() method presence

**Issue:** Not all `PercentEncoded` types have `encode()` factory methods.

**Types with encode():**
- Path ✓
- Query ✓
- Fragment ✓
- Host ✓
- Username ✓
- Password ✓
- UserInfo ✓

**Types without encode():**
- Segment ✗
- Authority ✗ (composite type, may be intentional)
- Port ✗ (not PercentEncoded)
- Scheme ✗ (not PercentEncoded)
- HostAndPort ✗ (composite type, may be intentional)

**Note:** Composite types (Authority, HostAndPort) may intentionally lack `encode()` since they're built from other components.

---

## Normalization Method Inconsistencies

### 4. Username and Password lack normalise() methods

**Issue:** `Username` and `Password` don't have `normalise()` methods despite being `PercentEncoded` types.

**Types with normalise():**
- Path ✓
- Query ✓
- Fragment ✓
- Host ✓
- UserInfo ✓
- Authority ✓
- Port ✓
- Scheme ✓
- HostAndPort ✓

**Types without normalise():**
- Username ✗
- Password ✗
- Segment ✗

**Location:** Username.java, Password.java, Segment.java

**Note:** Username and Password are likely normalised through their parent `UserInfo.normalise()`, but the API is inconsistent.

---

### 5. HostAndPort normalise() covariant return type not declared

**Issue:** `HostAndPort` implements `normalise()` methods that return `HostAndPort`, but these covariant overrides aren't declared in the interface.

**Current state:**
- `HostAndPort.normalise()` - returns `HostAndPort` (implementation detail)
- Interface declares: `Authority normalise()` (inherited from Authority)

**Expected:**
```java
@Override
HostAndPort normalise();

@Override
HostAndPort normalise(Scheme canonicalScheme);
```

**Location:** HostAndPort.java:98-111

**Impact:** Callers must cast when calling `normalise()` on a `HostAndPort` reference, even though the implementation always returns `HostAndPort`.

---

### 6. Scheme uses getCanonical() instead of normalise()

**Issue:** `Scheme.normalise()` exists but the naming is inconsistent with historical usage.

**Note:** This was previously identified and Scheme now has `normalise()`, maintaining consistency with other types. No action needed.

**Location:** Scheme.java:52

---

## Helper Method Inconsistencies

### 7. Inconsistent isNormalForm() presence

**Issue:** Only some normalizable types provide `isNormalForm()` helper methods.

**Types with isNormalForm():**
- Host ✓
- Port ✓
- Scheme ✓
- HostAndPort ✓ (with scheme parameter: `isNormalForm(Scheme)`)

**Types without isNormalForm():**
- Path ✗
- Query ✗ (has scheme-dependent normalization but no `isNormalForm(Scheme)`)
- Fragment ✗
- Username ✗
- Password ✗
- UserInfo ✗
- Segment ✗
- Authority ✗

**Pattern:** No clear pattern for which types should have `isNormalForm()`. Types that normalise based on their own state (Host, Port, Scheme) have it, but types that undergo transformation (Path with dot segment removal) don't.

**Location:** Various files

---

## Builder Pattern Inconsistencies

### 8. Builder availability varies

**Issue:** Only some types provide builder patterns.

**Types with builders:**
- UriReference ✓
- Uri ✓
- Url ✓

**Types without builders:**
- RelativeRef ✗
- Origin ✗
- All component types (Authority, Path, Query, etc.) ✗

**Note:** This may be intentional - only top-level URI types have builders, while components use static factory methods. However, the lack of builder for `RelativeRef` is inconsistent with `Url` having one.

---

## Summary

The main categories of inconsistency are:

1. **Factory methods:** Incomplete coverage of `parse()`, `encode()`, and `of()` methods
2. **Normalization:** Inconsistent presence of `normalise()` and `isNormalForm()` methods
3. **Return types:** Missing covariant return type declarations
4. **Naming:** Use of `register()` instead of `of()` for Scheme

These inconsistencies suggest the API evolved organically. Standardization would improve developer experience and API predictability.
