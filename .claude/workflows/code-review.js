export const meta = {
  name: 'code-review',
  description: 'Multi-dimensional code review for DolphinScheduler changes — checks bugs, patterns, SPI compliance, and module layering',
  phases: [
    { title: 'Scope', detail: 'Identify changed files and affected modules' },
    { title: 'Review', detail: 'Review across dimensions: bugs, patterns, SPI, layering' },
    { title: 'Verify', detail: 'Adversarially verify each finding' },
    { title: 'Report', detail: 'Synthesize final review report' },
  ],
}

phase('Scope')

const scope = await agent(
  `Analyze the current git diff (staged + unstaged changes) in this DolphinScheduler project:
1. List all changed files with their full paths
2. Identify which Maven modules are affected
3. Classify changes by type: Java backend, frontend (Vue/TS), Python SDK, config, docs
4. Note if any SPI/plugin files are changed (META-INF/services, TaskChannelFactory, etc.)
5. Note if module layering boundaries are crossed (e.g., dao importing from service)

Return structured scope.`,
  { label: 'scope-analysis', phase: 'Scope', schema: {
    type: 'object',
    properties: {
      changedFiles: { type: 'array', items: { type: 'string' } },
      affectedModules: { type: 'array', items: { type: 'string' } },
      changeTypes: { type: 'array', items: { type: 'string' } },
      spiAffected: { type: 'boolean' },
      layeringConcern: { type: 'boolean' },
      summary: { type: 'string' },
    },
    required: ['changedFiles', 'affectedModules', 'changeTypes', 'summary']
  }}
)

if (!scope || scope.changedFiles.length === 0) {
  log('No changes to review.')
  return { findings: [], summary: 'No changes detected.' }
}

log(`${scope.changedFiles.length} files changed across ${scope.affectedModules.length} modules: ${scope.affectedModules.join(', ')}`)
if (scope.spiAffected) log('⚠️  SPI/plugin files affected — will verify contract compliance')
if (scope.layeringConcern) log('⚠️  Cross-module layering detected — will verify dependency direction')

phase('Review')

const dimensions = [
  {
    key: 'bugs',
    prompt: `Review these DolphinScheduler changes for BUGS and correctness issues:

Changed files: ${scope.changedFiles.join(', ')}
Change summary: ${scope.summary}

Look for:
- NullPointerException risks (Java)
- Race conditions in multi-threaded code (Master/Worker components)
- Incorrect state machine transitions
- SQL injection in mapper XML or string concatenation
- Resource leaks (connections, streams not closed)
- TypeScript type errors or `any` usage
- Incorrect error handling (swallowed exceptions without logging)
- MyBatis-Plus mapper issues
- Vue reactivity issues (Pinia store mutations outside actions)`,
    schema: {
      type: 'object',
      properties: {
        findings: { type: 'array', items: {
          type: 'object',
          properties: {
            title: { type: 'string' },
            file: { type: 'string' },
            line: { type: 'number' },
            severity: { type: 'string', enum: ['critical', 'high', 'medium', 'low'] },
            description: { type: 'string' },
            fix: { type: 'string' },
          },
          required: ['title', 'file', 'severity', 'description']
        }}
      },
      required: ['findings']
    }
  },
  {
    key: 'patterns',
    prompt: `Review these DolphinScheduler changes for CODE PATTERNS and conventions:

Changed files: ${scope.changedFiles.join(', ')}
Change summary: ${scope.summary}

Check against DolphinScheduler coding standards:
- Lombok usage (@Data, @Slf4j, etc.) — are they used consistently?
- Naming conventions (PascalCase components, camelCase variables)
- Vue 3 Composition API with <script setup lang="ts"> for new components
- Pinia store conventions (camelCase filenames)
- No `any` type in TypeScript
- Following existing patterns in each module (don't invent new patterns)
- Commit message format (type: description)
- Proper use of SPI PrioritySPIFactory pattern
- MyBatis-Plus @Mapper annotation on mapper interfaces`,
    schema: {
      type: 'object',
      properties: {
        findings: { type: 'array', items: {
          type: 'object',
          properties: {
            title: { type: 'string' },
            file: { type: 'string' },
            line: { type: 'number' },
            severity: { type: 'string', enum: ['high', 'medium', 'low'] },
            description: { type: 'string' },
            suggestion: { type: 'string' },
          },
          required: ['title', 'file', 'severity', 'description']
        }}
      },
      required: ['findings']
    }
  },
  {
    key: 'architecture',
    prompt: `Review these DolphinScheduler changes for ARCHITECTURAL issues:

Changed files: ${scope.changedFiles.join(', ')}
Affected modules: ${scope.affectedModules.join(', ')}

Check:
1. Module dependency direction: dao ← common, service ← dao/remote/common, api/master/worker ← service. No upward references.
2. SPI plugin contracts: if task/datasource/registry plugins changed, is the interface contract preserved?
3. State machine integrity: if state handlers changed, are all transitions valid?
4. RPC compatibility: if remote module changed, is Netty serialization backward-compatible?
5. Database compatibility: if entity/dao changed, are migrations needed?
6. Frontend API contracts: if REST endpoints changed, is the frontend service layer updated?
7. Thread safety: Master (WorkflowExecuteRunnable, StateWheelExecuteThread) and Worker (WorkerManagerThread) changes`,
    schema: {
      type: 'object',
      properties: {
        findings: { type: 'array', items: {
          type: 'object',
          properties: {
            title: { type: 'string' },
            file: { type: 'string' },
            severity: { type: 'string', enum: ['critical', 'high', 'medium', 'low'] },
            description: { type: 'string' },
            recommendation: { type: 'string' },
          },
          required: ['title', 'severity', 'description']
        }}
      },
      required: ['findings']
    }
  },
]

const reviewResults = await pipeline(
  dimensions,
  d => agent(d.prompt, { label: `review:${d.key}`, phase: 'Review', schema: d.schema })
)

const allFindings = reviewResults.filter(Boolean).flatMap(r => r.findings || [])
log(`Found ${allFindings.length} potential issues across ${dimensions.length} dimensions`)

if (allFindings.length === 0) {
  return { findings: [], summary: 'No issues found. Code looks good!', scope }
}

phase('Verify')

const verified = await parallel(
  allFindings.map(f => () =>
    agent(
      `Adversarially verify this DolphinScheduler code review finding. Try to REFUTE it:

Finding: ${f.title}
File: ${f.file}${f.line ? `:${f.line}` : ''}
Severity: ${f.severity}
Description: ${f.description}
${f.fix ? `Suggested fix: ${f.fix}` : ''}
${f.suggestion ? `Suggestion: ${f.suggestion}` : ''}
${f.recommendation ? `Recommendation: ${f.recommendation}` : ''}

Read the actual file and verify:
1. Is this finding accurate?
2. Is the severity appropriate?
3. Is there context that makes this a non-issue?
4. Would the fix actually improve things?

Default to confirmed=true if you cannot clearly refute.`,
      { label: `verify:${f.title.substring(0, 40)}`, phase: 'Verify', schema: {
        type: 'object',
        properties: {
          isReal: { type: 'boolean', description: 'True if the finding is accurate and actionable' },
          adjustedSeverity: { type: 'string', enum: ['critical', 'high', 'medium', 'low', 'info'] },
          reasoning: { type: 'string' },
        },
        required: ['isReal', 'reasoning']
      }}
    ).then(v => ({ ...f, verdict: v }))
  )
)

const confirmed = verified.filter(Boolean).filter(f => f.verdict?.isReal)
const refuted = verified.filter(Boolean).filter(f => !f.verdict?.isReal)
log(`Verified: ${confirmed.length} confirmed, ${refuted.length} refuted`)

phase('Report')

const report = await agent(
  `Synthesize a final code review report for these DolphinScheduler changes.

Original scope: ${scope.summary}
Modules affected: ${scope.affectedModules.join(', ')}

Confirmed findings:
${confirmed.map((f, i) => `${i + 1}. [${f.verdict?.adjustedSeverity || f.severity}] ${f.title} (${f.file}${f.line ? ':' + f.line : ''})
   ${f.description}
   ${f.fix || f.suggestion || f.recommendation || 'No fix suggested'}`).join('\n')}

Refuted findings (do NOT include in final report):
${refuted.map((f, i) => `${i + 1}. ${f.title} — refuted: ${f.verdict?.reasoning}`).join('\n')}

Produce a concise, actionable code review report. Group by severity. Include an overall assessment.`,
  { label: 'synthesize-report', phase: 'Report', schema: {
    type: 'object',
    properties: {
      overallAssessment: { type: 'string', enum: ['approve', 'approve_with_comments', 'request_changes'] },
      summary: { type: 'string' },
      criticalCount: { type: 'number' },
      highCount: { type: 'number' },
      mediumCount: { type: 'number' },
      lowCount: { type: 'number' },
      report: { type: 'string' },
    },
    required: ['overallAssessment', 'summary', 'report']
  }}
)

return {
  findings: confirmed,
  refutedCount: refuted.length,
  report,
  scope,
}
