export const meta = {
  name: 'find-related',
  description: 'Find all code related to a feature, class, or concept across DolphinScheduler modules',
  phases: [
    { title: 'Search', detail: 'Multi-angle search across the codebase' },
    { title: 'Map', detail: 'Build a dependency and relationship map' },
    { title: 'Report', detail: 'Synthesize findings into a structured report' },
  ],
}

// args: { query: string, module?: string }
const query = args?.query
if (!query) {
  log('ERROR: No query specified. Usage: /find-related "ProcessInstance" or /find-related "task dispatch"')
  return { error: 'No search query specified' }
}

const targetModule = args?.module || ''

phase('Search')

const searchAngles = [
  {
    key: 'java-classes',
    prompt: `Search the DolphinScheduler codebase for Java classes/interfaces related to "${query}".
${targetModule ? `Focus on module: ${targetModule}` : 'Search across all modules.'}

Find:
- Class/interface declarations matching or related to the query
- Key methods and their signatures
- Annotations (@Service, @Component, @SPI, etc.)
- Inheritance hierarchy (extends/implements)
- File paths and module locations`,
    schema: {
      type: 'object',
      properties: {
        results: { type: 'array', items: {
          type: 'object',
          properties: {
            className: { type: 'string' },
            filePath: { type: 'string' },
            module: { type: 'string' },
            type: { type: 'string', enum: ['class', 'interface', 'enum', 'abstract class', 'annotation'] },
            keyMethods: { type: 'array', items: { type: 'string' } },
            extends: { type: 'string' },
            implements: { type: 'array', items: { type: 'string' } },
            annotations: { type: 'array', items: { type: 'string' } },
          },
          required: ['className', 'filePath', 'module']
        }}
      },
      required: ['results']
    }
  },
  {
    key: 'frontend',
    prompt: `Search the DolphinScheduler frontend (dolphinscheduler-ui/) for code related to "${query}".

Find:
- Vue components, Pinia stores, TypeScript types/interfaces
- API service functions (src/service/)
- Router definitions
- View components`,
    schema: {
      type: 'object',
      properties: {
        results: { type: 'array', items: {
          type: 'object',
          properties: {
            name: { type: 'string' },
            filePath: { type: 'string' },
            type: { type: 'string', enum: ['component', 'store', 'service', 'type', 'route', 'view', 'utility'] },
            description: { type: 'string' },
          },
          required: ['name', 'filePath', 'type']
        }}
      },
      required: ['results']
    }
  },
  {
    key: 'config-references',
    prompt: `Search the DolphinScheduler codebase for configuration, SQL, and documentation references to "${query}".

Check:
- Maven pom.xml files (dependencies, plugins)
- application.yaml / properties files
- MyBatis mapper XML files
- SQL migration scripts
- SPI service descriptor files (META-INF/services)
- Documentation (docs/, README files)
- Environment scripts (script/env/)`,
    schema: {
      type: 'object',
      properties: {
        results: { type: 'array', items: {
          type: 'object',
          properties: {
            description: { type: 'string' },
            filePath: { type: 'string' },
            type: { type: 'string', enum: ['pom', 'config', 'mapper-xml', 'sql', 'spi-service', 'docs', 'script'] },
            relevantContent: { type: 'string' },
          },
          required: ['description', 'filePath', 'type']
        }}
      },
      required: ['results']
    }
  },
]

const searchResults = await parallel(
  searchAngles.map(a => () =>
    agent(a.prompt, { label: `search:${a.key}`, phase: 'Search', schema: a.schema })
  )
)

const allJava = searchResults[0]?.results || []
const allFrontend = searchResults[1]?.results || []
const allConfig = searchResults[2]?.results || []

const totalFound = allJava.length + allFrontend.length + allConfig.length
log(`Found ${totalFound} references: ${allJava.length} Java, ${allFrontend.length} frontend, ${allConfig.length} config/docs`)

if (totalFound === 0) {
  return { query, results: [], summary: `No results found for "${query}".` }
}

phase('Map')

const relationshipMap = await agent(
  `Build a dependency and relationship map for "${query}" in DolphinScheduler.

Java classes found:
${allJava.map(j => `- ${j.className} (${j.module}/${j.type})${j.extends ? ` extends ${j.extends}` : ''}${j.implements?.length ? ` implements ${j.implements.join(', ')}` : ''}`).join('\n')}

Frontend references:
${allFrontend.map(f => `- ${f.name} (${f.type}) — ${f.filePath}`).join('\n')}

Configuration references:
${allConfig.map(c => `- ${c.description} (${c.type})`).join('\n')}

Map:
1. How these pieces connect to each other
2. The data/control flow between them
3. Which module owns the core logic
4. What patterns are used (SPI, event-driven, etc.)
5. Cross-cutting concerns (where does this touch multiple modules?)`,
  { label: 'map-relationships', phase: 'Map', schema: {
    type: 'object',
    properties: {
      coreModule: { type: 'string' },
      relatedModules: { type: 'array', items: { type: 'string' } },
      relationships: { type: 'array', items: {
        type: 'object',
        properties: {
          from: { type: 'string' },
          to: { type: 'string' },
          relationship: { type: 'string' },
          description: { type: 'string' },
        },
        required: ['from', 'to', 'relationship']
      }},
      patterns: { type: 'array', items: { type: 'string' } },
      dataFlow: { type: 'string' },
    },
    required: ['coreModule', 'relatedModules', 'relationships']
  }}
)

phase('Report')

const report = await agent(
  `Synthesize a comprehensive report on "${query}" in the DolphinScheduler codebase.

Java classes: ${allJava.length}
Frontend references: ${allFrontend.length}
Config/docs references: ${allConfig.length}

Core module: ${relationshipMap?.coreModule}
Related modules: ${relationshipMap?.relatedModules?.join(', ')}

Produce a structured reference report that a developer can use to understand how "${query}" works in this codebase. Include:
1. Overview — what it is and where it lives
2. Key classes/files with descriptions
3. Architecture — how pieces connect
4. Common modification patterns (if someone needs to change this, what do they touch?)
5. Gotchas and conventions to follow`,
  { label: 'synthesize-report', phase: 'Report', schema: {
    type: 'object',
    properties: {
      title: { type: 'string' },
      overview: { type: 'string' },
      keyFiles: { type: 'array', items: {
        type: 'object',
        properties: {
          path: { type: 'string' },
          role: { type: 'string' },
          description: { type: 'string' },
        },
        required: ['path', 'role']
      }},
      architecture: { type: 'string' },
      modificationGuide: { type: 'string' },
      gotchas: { type: 'array', items: { type: 'string' } },
    },
    required: ['title', 'overview', 'keyFiles', 'architecture']
  }}
)

return {
  query,
  javaClasses: allJava,
  frontend: allFrontend,
  config: allConfig,
  relationships: relationshipMap,
  report,
}
