# scaffold

Scaffold full-stack website development code for DolphinScheduler.

## Usage
```
/scaffold rest-api <EntityName> --domain <domain> --base-url <url> --fields '<json>' [--reference <file>] [--dry-run]
/scaffold vue-page <EntityName> --domain <domain> --parent-route <name> --fields '<json>' [--reference <file>] [--dry-run]
/scaffold full-stack <EntityName> --domain <domain> --base-url <url> --parent-route <name> --fields '<json>' [--dry-run]
/scaffold task-plugin <TaskType> --description <text> [--parameters '<json>'] [--dry-run]
```

## Instructions

When invoked:

1. **Validate naming**: EntityName must be PascalCase, consistent with Java conventions

2. **Auto-discover references**: If no --reference is provided:
   - For rest-api: use /find to find the simplest controller in the target domain
   - For vue-page: use /find to find the simplest view in the target domain
   - For task-plugin: default to referencing dolphinScheduler-task-shell

3. **Invoke the CLI tool** with the resolved arguments using Bash:
   ```
   npx tsx tools/scaffold/src/index.ts <command> <EntityName> <options...>
   ```

4. **If the CLI fails**, fall back to manual generation using patterns read from:
   - Controller pattern: read a reference controller
   - Service pattern: read a reference service + impl
   - View pattern: read a reference view TSX + use-table + use-modal
   - Store pattern: read an existing store
   - Route pattern: read the target route module file
   - Plugin pattern: read shell task plugin files

5. **Verify the generation**:
   - Each file exists at the expected path
   - Imports are resolvable
   - Format matches existing convention

6. **Post-generation checklist**:
   - For full-stack: build the API module with /build-module dolphinscheduler-api
   - For vue-page: check that import.meta.glob would find the new view
