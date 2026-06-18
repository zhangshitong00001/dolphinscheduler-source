---
description: Scaffold a new DolphinScheduler task plugin following SPI pattern
args: [name, description]
---

Scaffold a new DolphinScheduler task plugin called "$name"$if(description)` ($description)`.

Follow the exact SPI plugin pattern used by existing task plugins:

1. Determine naming:
   - Java class prefix: PascalCase of $name (e.g., "DataSync")
   - Module name: `dolphinscheduler-task-<kebab-case>`
   - Package: `org.apache.dolphinscheduler.plugin.task.<lowercase>`

2. Create the module structure under `dolphinscheduler-task-plugin/`:
   ```
   dolphinscheduler-task-<name>/
   ├── pom.xml
   └── src/main/java/org/apache/dolphinscheduler/plugin/task/<name>/
       ├── <Name>TaskChannelFactory.java  — SPI entry point
       ├── <Name>Task.java                 — task execution logic
       └── <Name>Parameters.java           — task configuration POJO
   ```

3. For each file, follow the exact patterns from existing plugins:
   - **TaskChannelFactory**: Implement `TaskChannelFactory`, annotate with `@AutoService(TaskChannelFactory.class)`
   - **Task**: Extend `AbstractTask`, override `handle()` method
   - **Parameters**: POJO with `@Data`, validation annotations

4. Reference existing plugins for patterns:
   - Simple: `dolphinscheduler-task-shell`
   - HTTP: `dolphinscheduler-task-http`
   - External: `dolphinscheduler-task-datax`

5. Register the new module in root `pom.xml` `<modules>` section.
