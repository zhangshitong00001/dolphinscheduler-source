---
description: Build a DolphinScheduler Maven module
args: [module, skipTests, profile]
---

Build the DolphinScheduler module `$module`.

Steps:
1. Verify the module exists: check that `dolphinscheduler-$module/pom.xml` exists
2. Run: `./mvnw clean install -pl dolphinscheduler-$module -am` $if(skipTests)`-DskipTests` $if(profile)`-P$profile`
3. Report build result clearly: success/failure, time taken, test results
4. If failure, extract the relevant error lines and suggest fixes

Use `./mvnw` (Maven wrapper), not system `mvn`. Always use `-am` to build dependencies first.
