# find-code

Find relevant code across the DolphinScheduler codebase using multiple search strategies.

## Usage
```
/find-code <query> [--module <module>] [--type <java|frontend|config|sql|all>]
```

## Examples
- `/find-code ProcessInstance` — find all references to ProcessInstance
- `/find-code "task dispatch" --module dolphinscheduler-master` — search master module
- `/find-code DataSourceChannelFactory --type java` — Java-only search
- `/find-code "Pinia store" --type frontend` — frontend-only search

## Instructions

When invoked:

1. **Run parallel searches** across relevant scopes:

### Java Backend Search
```bash
# Find class declarations
grep -rn "class\|interface\|enum" --include="*.java" | grep -i "<query>"

# Find usage references
grep -rn "<query>" --include="*.java" dolphinscheduler-*/
```

### Frontend Search
```bash
# Find in Vue components, stores, services
grep -rn "<query>" --include="*.ts" --include="*.vue" dolphinscheduler-ui/src/
```

### Configuration Search
```bash
# Find in XML, YAML, properties
grep -rn "<query>" --include="*.xml" --include="*.yaml" --include="*.properties"
```

### MyBatis Mapper Search
```bash
# Find in mapper XML files
grep -rn "<query>" --include="*Mapper.xml" dolphinscheduler-dao/
```

2. **Organize results by module** and file type.

3. **For Java results**, show:
   - Class/interface/enum name
   - Package and module
   - Key annotations (@Service, @Component, @SPI)
   - Inheritance (extends/implements)

4. **For frontend results**, show:
   - Component/store/service name
   - File path
   - Type (component, store, service, type, route)

5. **Highlight the most important/relevant results first** — the core definition/implementation, not just references.

## Module Map

| Module | Purpose |
|--------|---------|
| `dolphinscheduler-common` | Constants, enums, utilities |
| `dolphinscheduler-dao` | MyBatis mappers, entities, repositories |
| `dolphinscheduler-service` | Business logic services |
| `dolphinscheduler-api` | REST controllers, interceptors |
| `dolphinscheduler-master` | Workflow scheduling, state machine |
| `dolphinscheduler-worker` | Task execution |
| `dolphinscheduler-remote` | Netty RPC communication |
| `dolphinscheduler-registry` | Service discovery (ZK/MySQL/Etcd) |
| `dolphinscheduler-alert` | Alert notifications |
| `dolphinscheduler-spi` | SPI framework interfaces |
| `dolphinscheduler-task-plugin` | All task type implementations |
| `dolphinscheduler-datasource-plugin` | All datasource implementations |
| `dolphinscheduler-scheduler-plugin` | Quartz cron scheduler |
| `dolphinscheduler-ui` | Vue 3 frontend |
| `dolphinscheduler-python` | Python SDK |
