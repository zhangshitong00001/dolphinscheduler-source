---
description: Review current changes for bugs, patterns, and architecture issues
---

Review the current git diff (staged and unstaged changes) in this DolphinScheduler project.

Perform a comprehensive code review across these dimensions:

1. **Bugs & Correctness**
   - NullPointerException risks (Java)
   - Race conditions in multi-threaded code (Master/Worker)
   - Incorrect state machine transitions
   - SQL injection in mapper XML or string concatenation
   - Resource leaks (connections, streams not closed)
   - TypeScript `any` type usage (not allowed)
   - Swallowed exceptions without logging

2. **Code Patterns & Conventions**
   - Lombok usage (@Data, @Slf4j) — consistent with project?
   - Naming: PascalCase components, camelCase variables
   - Vue 3 Composition API with `<script setup lang="ts">`
   - Pinia store conventions (camelCase filenames)
   - No `any` type in TypeScript

3. **Architecture & Layering**
   - Module dependency direction: dao ← common, service ← dao/remote/common
   - SPI plugin contract preservation
   - State machine integrity
   - RPC backward compatibility
   - Database compatibility (migrations needed?)

For each finding, verify it by reading the actual file. Report only confirmed issues, grouped by severity (critical/high/medium/low).
