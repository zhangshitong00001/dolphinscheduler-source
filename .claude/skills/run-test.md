# run-test

Run tests for a DolphinScheduler module with proper configuration.

## Usage
```
/run-test <module-name> [--class <TestClass>] [--method <testMethod>] [--profile <profile>]
```

## Examples
- `/run-test dolphinscheduler-service` — run all tests in service module
- `/run-test dolphinscheduler-service --class ProcessServiceImplTest` — run single test class
- `/run-test dolphinscheduler-service --class ProcessServiceImplTest --method testCreateProcess` — run single test method
- `/run-test dolphinscheduler-dao --profile release` — run DAO tests with release profile

## Instructions

When invoked:

1. **Validate the module and test class**:
   - Check module exists: `ls dolphinscheduler-<name>/`
   - If `--class` specified, find the test file: `find dolphinscheduler-<name> -name "<TestClass>.java"`

2. **Build the module first** (tests need compiled classes):
   ```bash
   ./mvnw clean install -pl dolphinscheduler-<name> -am -DskipTests
   ```

3. **Run the test**:
   ```bash
   # All tests in module
   ./mvnw test -pl dolphinscheduler-<name> [-P<profile>]

   # Single test class
   ./mvnw test -pl dolphinscheduler-<name> -Dtest=<TestClass> [-P<profile>]

   # Single test method
   ./mvnw test -pl dolphinscheduler-<name> -Dtest=<TestClass>#<testMethod> [-P<profile>]
   ```

4. **Report results**:
   - Tests run, passed, failed, skipped, errors
   - Duration
   - For failures: show the assertion error message and stack trace
   - For errors: show the exception and stack trace

5. **On failure, analyze and suggest**:
   - Read the failing test to understand what it tests
   - Check if it's a flaky test (timing-dependent, external dependency)
   - Suggest whether the test or the code needs fixing

## Test Framework
- JUnit 5 (Jupiter) — `org.junit.jupiter.api`
- Mockito 3.9.0 for mocking
- Tests are in `src/test/java/` within each module
- Integration tests may need running services (ZK, DB)

## Common Test Issues
- **Port conflicts**: Some tests start embedded servers. Check for port clashes.
- **Database state**: DAO tests may need specific DB state. Use H2 in-memory for unit tests.
- **Zookeeper**: Registry tests may need ZK. Use `-Dzk-3.4` profile if needed.
- **Timeout**: Master/Worker tests involving async operations may have timing sensitivity.
