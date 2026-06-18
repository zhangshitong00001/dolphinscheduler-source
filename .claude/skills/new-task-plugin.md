# new-task-plugin

Scaffold a new task plugin for DolphinScheduler following the SPI pattern.

## Usage
```
/new-task-plugin <TaskType> [--description "<description>"]
```

## Example
```
/new-task-plugin DataSync --description "Sync data between two datasources"
```

## What This Creates

DolphinScheduler task plugins follow a strict SPI pattern. Each plugin consists of:

1. **TaskChannelFactory** — SPI entry point, registers the task type
2. **TaskParameters** — task configuration POJO
3. **AbstractTask subclass** — actual task execution logic
4. **META-INF/services file** — SPI service registration

## Instructions

When invoked:

1. **Determine the task type name** (PascalCase, e.g., `DataSync`, `HttpCheck`):
   - Plugin module name: `dolphinscheduler-task-<kebab-case>`
   - Java package: `org.apache.dolphinscheduler.plugin.task.<lowercase>`

2. **Create the module structure** under `dolphinscheduler-task-plugin/`:

```
dolphinscheduler-task-plugin/
└── dolphinscheduler-task-<name>/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/org/apache/dolphinscheduler/plugin/task/<name>/
        │   │   ├── <Name>TaskChannelFactory.java
        │   │   ├── <Name>Task.java
        │   │   └── <Name>Parameters.java
        │   └── resources/
        │       └── META-INF/
        │           └── services/
        │               └── org.apache.dolphinscheduler.spi.task.TaskChannelFactory
        └── test/
            └── java/org/apache/dolphinscheduler/plugin/task/<name>/
                └── <Name>TaskTest.java
```

3. **Generate each file following existing patterns**:

### TaskChannelFactory
- Implement `TaskChannelFactory` interface
- Annotate with `@AutoService(TaskChannelFactory.class)` (auto-generates META-INF/services)
- Return task type name, required resource types (CPU/MEMORY), task channel

### TaskParameters
- POJO with `@Data` Lombok annotation
- Fields for all task-specific configuration
- Validation annotations where appropriate

### Task (extends AbstractTask)
- Constructor takes `TaskRequest`
- Override `handle()` method — main execution logic
- Use `log.info/error` for logging
- Set `exitStatusCode` and `exitStatusMessage` on the task request
- Return `processTaskResult()` at the end

4. **Register in root pom.xml** — add `<module>` entry

5. **Add dependency to dolphinscheduler-dist** if needed for packaging

## Reference Files to Follow

Look at these existing task plugins for patterns to copy:
- **Simple**: `dolphinscheduler-task-shell` — simplest task type
- **HTTP**: `dolphinscheduler-task-http` — HTTP-based task
- **External system**: `dolphinscheduler-task-datax` — interacts with external tool
- **With resources**: `dolphinscheduler-task-spark` — requires compute resources

## Key Interfaces
- `TaskChannelFactory` (in `dolphinscheduler-task-plugin/dolphinscheduler-task-api/`)
- `AbstractTask` (in `dolphinscheduler-task-plugin/dolphinscheduler-task-api/`)
- `TaskRequest` — input to task execution
- `TaskResponse` — output from task execution
- `TaskConstants` — standard exit codes and constants
