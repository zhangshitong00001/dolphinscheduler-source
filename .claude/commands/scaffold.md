# scaffold

Scaffold full-stack website development code for DolphinScheduler.

## Usage
```
/scaffold rest-api <EntityName> --domain <domain> --base-url <url> --fields '<json>' [--reference <file>] [--dry-run]
/scaffold vue-page <EntityName> --domain <domain> --parent-route <name> --fields '<json>' [--reference <file>] [--dry-run]
/scaffold full-stack <EntityName> --domain <domain> --base-url <url> --parent-route <name> --fields '<json>' [--dry-run]
/scaffold task-plugin <TaskType> --description <text> [--parameters '<json>'] [--dry-run]
```

## What This Creates

### rest-api (Java backend)
- Controller (extends BaseController, REST endpoints)
- Service interface + ServiceImpl (extends BaseServiceImpl)
- MyBatis Mapper interface
- Status code entries (merged into Status.java)
- Permission constants (merged into ApiFuncIdentificationConstant.java)

### vue-page (Vue 3 frontend)
- View TSX component with NaiveUI table + pagination
- useTable composable (pagination, search, CRUD)
- Modal component (create/edit form dialog)
- useDetail composable (form state, create/update API calls)
- Pinia store with persist
- Axios service module with types
- Route entry (merged into route module)
- Locale entries (merged into en_US + zh_CN locale files)

### full-stack
- All of the above, backend + frontend together

### task-plugin (Java SPI plugin)
- TaskChannelFactory (SPI entry point with @AutoService)
- TaskChannel implementation
- AbstractTask subclass with handle/cancel/init
- Parameters POJO with plugin params
- pom.xml for the module
- JUnit 5 + Mockito test skeleton
- Module registration in parent pom.xml

## Instructions

When invoked:

1. **Validate naming**: EntityName must be PascalCase (e.g., "AlertConfig", "DataSource")

2. **Auto-discover references** (if no --reference provided):
   - For `rest-api`: Search for the simplest controller in the target domain
   - For `vue-page`: Search for the simplest view in the target domain
   - For `task-plugin`: Default to referencing `dolphinscheduler-task-shell`

3. **Determine fields**: If --fields not provided, ask the user what fields the entity needs (name, type, required, description)

4. **Invoke the CLI tool** with the resolved arguments:
   ```bash
   npx tsx tools/scaffold/src/index.ts <command> <EntityName> <options...>
   ```

5. **If the CLI fails** (missing dependencies, broken templates), fall back to manual generation using:
   - Controller pattern: reference controller from the target domain
   - Service pattern: reference service + impl from the target domain
   - View pattern: reference view from the target domain (TSX + composables)
   - Store pattern: reference an existing store
   - Plugin pattern: reference shell task plugin files

6. **Post-generation verification**:
   - For Java files: verify imports resolve, class hierarchy matches BaseController/BaseServiceImpl
   - For Vue files: verify imports use @/ aliases, components match NaiveUI patterns
   - For all: verify naming convention (PascalCase classes, camelCase variables, kebab-case URLs/files)

7. **Build verification** (offer after generation):
   - For Java: `./mvnw clean compile -pl dolphinscheduler-api -am -DskipTests`
   - For frontend: `cd dolphinscheduler-ui && npx vue-tsc --noEmit`

## Examples

```bash
# Scaffold a backend REST API for alert configs
/scaffold rest-api AlertConfig --domain security --base-url /alert-configs \
  --fields '[{"name":"name","type":"String","required":true},{"name":"alertType","type":"String","required":true}]'

# Scaffold a Vue page for alert configs
/scaffold vue-page AlertConfig --domain security --parent-route security \
  --fields '[{"name":"name","type":"String","required":true},{"name":"alertType","type":"String","required":true}]'

# Scaffold full-stack CRUD in one command
/scaffold full-stack AlertConfig --domain security --base-url /alert-configs \
  --parent-route security --auth ADMIN_USER \
  --fields '[{"name":"name","type":"String","required":true}]'

# Scaffold a new task plugin
/scaffold task-plugin S3Upload --description "Upload files to S3"
```

## Reference Files

Pattern inference reads these files to learn conventions:
- **Controllers**: `dolphinscheduler-api/.../controller/QueueController.java`
- **Services**: `dolphinscheduler-api/.../service/QueueService.java`, `.../service/impl/QueueServiceImpl.java`
- **Views**: `dolphinscheduler-ui/src/views/security/yarn-queue-manage/index.tsx`
- **Stores**: `dolphinscheduler-ui/src/store/security/queue.ts`
- **Task plugins**: `dolphinscheduler-task-plugin/dolphinscheduler-task-shell/`
