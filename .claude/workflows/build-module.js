export const meta = {
  name: 'build-module',
  description: 'Build a specific DolphinScheduler Maven module and optionally run its tests',
  phases: [
    { title: 'Analyze', detail: 'Identify module dependencies and test configuration' },
    { title: 'Build', detail: 'Run Maven build for the target module' },
    { title: 'Test', detail: 'Run tests and collect results' },
  ],
}

// args: { module: string, skipTests?: boolean, testClass?: string, profile?: string }
const module = args?.module
if (!module) {
  log('ERROR: No module specified. Usage: /build-module dolphinscheduler-service')
  return { error: 'No module specified' }
}

const profile = args?.profile || ''
const skipTests = args?.skipTests || false
const testClass = args?.testClass || ''

phase('Analyze')

const analysis = await agent(
  `Analyze the DolphinScheduler module "${module}" for build readiness:
1. Check if the module directory exists
2. List its dependencies from its pom.xml
3. Check if there are any test classes
4. Identify any special build requirements

Return a structured analysis with: exists (boolean), dependencies (string[]), testCount (number), specialNotes (string).`,
  { label: `analyze:${module}`, phase: 'Analyze', schema: {
    type: 'object',
    properties: {
      exists: { type: 'boolean' },
      dependencies: { type: 'array', items: { type: 'string' } },
      testCount: { type: 'number' },
      specialNotes: { type: 'string' },
    },
    required: ['exists', 'dependencies', 'testCount']
  }}
)

if (!analysis || !analysis.exists) {
  log(`ERROR: Module "${module}" does not exist in the project.`)
  return { error: `Module "${module}" not found` }
}

log(`Module: ${module}, Dependencies: ${analysis.dependencies?.length || 0}, Tests: ${analysis.testCount || 0}`)
if (analysis.specialNotes) log(`Notes: ${analysis.specialNotes}`)

phase('Build')

let buildCmd = `./mvnw clean install -pl ${module} -am`
if (profile) buildCmd += ` -P${profile}`
if (skipTests) buildCmd += ` -DskipTests`
buildCmd += ` -Dmaven.test.skip=${skipTests}`

const buildResult = await agent(
  `Run this build command and report the result: \`${buildCmd}\`

Execute it and tell me:
- Did the build succeed?
- What was the build output (summary)?
- If it failed, what were the errors?`,
  { label: `build:${module}`, phase: 'Build', schema: {
    type: 'object',
    properties: {
      success: { type: 'boolean' },
      summary: { type: 'string' },
      errors: { type: 'array', items: { type: 'string' } },
      buildTime: { type: 'string' },
    },
    required: ['success', 'summary']
  }}
)

if (!buildResult?.success && !skipTests) {
  log(`Build failed: ${buildResult?.summary}`)
  return { success: false, build: buildResult }
}

log(`Build ${buildResult?.success ? 'succeeded' : 'completed (tests skipped)'}: ${buildResult?.summary}`)

if (skipTests || !testClass) {
  return { success: true, build: buildResult, analysis }
}

phase('Test')

const testCmd = testClass
  ? `./mvnw test -pl ${module} -Dtest=${testClass} ${profile ? `-P${profile}` : ''}`
  : `./mvnw test -pl ${module} ${profile ? `-P${profile}` : ''}`

const testResult = await agent(
  `Run this test command and report the result: \`${testCmd}\`

Execute it and tell me:
- Did all tests pass?
- How many tests ran?
- Any failures or errors with details?`,
  { label: `test:${module}`, phase: 'Test', schema: {
    type: 'object',
    properties: {
      passed: { type: 'boolean' },
      testCount: { type: 'number' },
      failures: { type: 'array', items: { type: 'string' } },
      errors: { type: 'array', items: { type: 'string' } },
      summary: { type: 'string' },
    },
    required: ['passed', 'testCount', 'summary']
  }}
)

return {
  success: buildResult?.success && (testResult?.passed ?? true),
  module,
  build: buildResult,
  test: testResult,
  analysis,
}
