---
description: Find code related to a concept across DolphinScheduler modules
args: [query, module]
---

Search the DolphinScheduler codebase for everything related to "$query"$if(module)` in module $module`.

Run parallel searches:
1. **Java classes** — grep for class/interface declarations and usages
2. **Frontend** — grep in dolphinscheduler-ui/src/ for components, stores, services
3. **Configuration** — grep in XML, YAML, properties, and META-INF/services files
4. **MyBatis mappers** — grep in *Mapper.xml files in dolphinscheduler-dao

Organize results by module. For each finding show:
- Java: class name, package, module, key annotations, inheritance
- Frontend: name, file path, type (component/store/service/type)
- Config: description, file path, type (pom/config/mapper-xml/spi-service)

Build a relationship map showing how the pieces connect. Identify the core module owning the main logic and related modules.
