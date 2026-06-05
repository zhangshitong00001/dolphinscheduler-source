# build-module

Build a specific DolphinScheduler Maven module with proper dependency resolution.

## Usage
```
/build-module <module-name> [--skip-tests] [--profile <profile>] [--test <TestClass>]
```

## Examples
- `/build-module dolphinscheduler-service` — build service module + dependencies, run tests
- `/build-module dolphinscheduler-api --skip-tests` — build API module, skip tests
- `/build-module dolphinscheduler-service --test ProcessServiceImplTest` — build and run single test

## Instructions

When invoked:

1. **Validate the module name** — check that `dolphinscheduler-<name>/pom.xml` exists. If not, suggest similar module names.

2. **Run the Maven build**:
   ```bash
   ./mvnw clean install -pl dolphinscheduler-<name> -am [-DskipTests] [-P<profile>] [-Dtest=<TestClass>]
   ```
   - `-pl`: target module only
   - `-am`: also-make (build dependencies)
   - Always use `-am` to ensure dependencies are built first

3. **Report results clearly**:
   - Build time
   - Success/failure
   - Test results (passed/failed/skipped count)
   - If failure, extract the relevant error lines (not the full Maven stack trace)

4. **On build failure**, analyze the error and suggest fixes:
   - Compilation errors → point to the file and line
   - Test failures → show the assertion error
   - Dependency issues → check if module exists in root pom.xml

## Module Reference

Common modules:
- `dolphinscheduler-common` — shared constants, enums, utilities
- `dolphinscheduler-dao` — MyBatis-Plus mappers, entities
- `dolphinscheduler-service` — business logic layer
- `dolphinscheduler-api` — REST API server
- `dolphinscheduler-master` — workflow scheduler
- `dolphinscheduler-worker` — task executor
- `dolphinscheduler-remote` — Netty RPC
- `dolphinscheduler-registry` — service discovery (ZK/MySQL/Etcd)
- `dolphinscheduler-alert` — alert server
- `dolphinscheduler-spi` — SPI framework
- `dolphinscheduler-task-plugin` — task type plugins
- `dolphinscheduler-datasource-plugin` — datasource plugins
- `dolphinscheduler-scheduler-plugin` — cron scheduler plugins
- `dolphinscheduler-standalone-server` — all-in-one dev server
- `dolphinscheduler-meter` — metrics
- `dolphinscheduler-data-quality` — data quality tasks
- `dolphinscheduler-tools` — CLI tools

## Build Profiles
- `release` — full release build (used for distribution)
- `zk-3.4` — use Zookeeper 3.4.x instead of default 3.8.0
- No profile — standard dev build
