export const meta = {
  name: 'scaffold',
  description: 'Full-stack code scaffolding for DolphinScheduler',
  phases: [
    { title: 'Validate', detail: 'Validate entity name and options' },
    { title: 'Generate', detail: 'Run scaffold CLI to generate code' },
    { title: 'Verify', detail: 'Verify generated files' },
  ],
}

// ---------------------------------------------------------------------------
// This workflow wraps the scaffold CLI tool with structured result reporting.
// It can be invoked as: /workflow scaffold --args '{"command":"full-stack","entityName":"AlertConfig",...}'
// ---------------------------------------------------------------------------

phase('Validate')
const { command, entityName, domain, baseUrl, parentRoute, fields, auth, reference, dryRun } = args

log(`Scaffolding: ${command} "${entityName}"`)

if (!entityName || !/^[A-Z][a-zA-Z0-9]*$/.test(entityName)) {
  log('ERROR: entityName must be PascalCase (e.g., "AlertConfig")')
  return { success: false, error: 'Invalid entity name' }
}

phase('Generate')
const result = await agent(
  `Run the scaffold CLI to generate ${command} code for entity "${entityName}".

Command to execute:
\`\`\`bash
npx tsx tools/scaffold/src/index.ts ${command} ${entityName} --domain ${domain || 'default'} --base-url ${baseUrl || '/' + entityName.toLowerCase() + 's'} --parent-route ${parentRoute || domain || 'default'} --fields '${fields || '[]'}' ${auth ? `--auth ${auth}` : ''} ${reference ? `--reference ${reference}` : ''} ${dryRun ? '--dry-run' : ''}
\`\`\`

Run this command and report:
1. What files were created (full paths)
2. What shared files were modified (Status.java, route modules, locale files, pom.xml)
3. Any warnings or errors
4. Whether the generation was successful`,
  { label: 'scaffold-cli', phase: 'Generate' }
)

phase('Verify')
const verification = await agent(
  `Verify the generated files for ${command} "${entityName}":

1. Check that all expected files exist at their paths:
${result}
2. Verify imports are resolvable
3. Verify Java files follow the BaseController/BaseServiceImpl pattern
4. Verify Vue files follow the defineComponent/Pinia/axios module pattern
5. Verify naming conventions (PascalCase classes, camelCase variables, kebab-case URLs)

Report a summary of findings.`,
  { label: 'verify', phase: 'Verify' }
)

const report = {
  command,
  entityName,
  files: result,
  verification,
  success: true,
}

log(`\nScaffold complete: ${command} "${entityName}"`)
log(`Generated files: ${result.split('\n').filter(l => l.trim()).length}`)

return report
