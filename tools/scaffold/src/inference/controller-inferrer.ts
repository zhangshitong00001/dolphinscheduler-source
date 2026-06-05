/**
 * Controller pattern inferrer.
 * Reads a reference controller and maps its structural patterns to template
 * context variables for generating a new controller.
 */

import { extractJavaPattern } from './pattern-extractor.js';
import type { ScaffoldContext, ExtractedPattern, MethodInfo, ImportInfo } from '../types/index.js';

/**
 * Infers the scaffold context for a new controller based on a reference controller.
 * Transforms entity-specific references (names, URLs, status codes) from the
 * reference entity to the target entity.
 */
export async function inferControllerContext(
  referencePath: string,
  baseContext: ScaffoldContext,
): Promise<ScaffoldContext> {
  const pattern = await extractJavaPattern(referencePath);

  // Extract the reference entity name from the class name (e.g., "QueueController" -> "Queue")
  const refEntity = pattern.className.replace(/Controller$/, '');
  const refLower = refEntity.toLowerCase();
  const refCamel = refEntity.charAt(0).toLowerCase() + refEntity.slice(1);

  // Transform imports: replace reference entity references with target entity
  const transformedImports = transformJavaImports(pattern.imports, refEntity, refLower, refCamel, baseContext);

  // Build method templates from reference
  const methodTemplates = pattern.methods.map(m => ({
    ...m,
    // Replace entity references in annotations
    annotations: m.annotations.map(a => ({
      ...a,
      parameters: transformAnnotationParams(a.parameters, refEntity, refLower, refCamel, baseContext),
      raw: transformString(a.raw, refEntity, refLower, refCamel, baseContext),
    })),
  }));

  return {
    ...baseContext,
    referencePath,
    extractedPattern: {
      ...pattern,
      imports: transformedImports,
      methods: methodTemplates,
    },
  };
}

/**
 * Extracts the service variable name pattern from the reference controller.
 * e.g., if the reference uses "queueService" and the new entity is "AlertConfig",
 * this infers the variable should be "alertConfigService".
 */
export function inferServiceVariableName(pattern: ExtractedPattern, entityCamel: string): string {
  return `${entityCamel}Service`;
}

/**
 * Extracts the URL base path from the reference controller's @RequestMapping annotation.
 */
export function inferBaseUrl(pattern: ExtractedPattern): string {
  for (const ann of pattern.classAnnotations) {
    if (ann.name === 'RequestMapping') {
      return ann.parameters['value'] || ann.parameters['path'] || '';
    }
  }
  return '';
}

/**
 * Extracts the API tag from the reference controller's @Api annotation.
 */
export function inferApiTag(pattern: ExtractedPattern): string {
  for (const ann of pattern.classAnnotations) {
    if (ann.name === 'Api' && ann.parameters['tags']) {
      return ann.parameters['tags'];
    }
  }
  return '';
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function transformJavaImports(
  imports: ImportInfo[],
  refEntity: string,
  refLower: string,
  refCamel: string,
  ctx: ScaffoldContext,
): ImportInfo[] {
  return imports.map(imp => ({
    ...imp,
    qualifiedName: transformString(imp.qualifiedName, refEntity, refLower, refCamel, ctx),
  }));
}

function transformAnnotationParams(
  params: Record<string, string>,
  refEntity: string,
  refLower: string,
  refCamel: string,
  ctx: ScaffoldContext,
): Record<string, string> {
  const result: Record<string, string> = {};
  for (const [key, value] of Object.entries(params)) {
    result[key] = transformString(value, refEntity, refLower, refCamel, ctx);
  }
  return result;
}

function transformString(
  value: string,
  refEntity: string,
  refLower: string,
  refCamel: string,
  ctx: ScaffoldContext,
): string {
  let result = value;

  // Replace PascalCase references
  result = result.replace(new RegExp(refEntity, 'g'), ctx.naming.pascal);

  // Replace lowercase references
  result = result.replace(new RegExp(refLower, 'g'), ctx.naming.lower);

  // Replace camelCase references
  result = result.replace(new RegExp(refCamel, 'g'), ctx.naming.camel);

  // Replace uppercase SNAKE_CASE references
  const refSnake = refEntity.replace(/([a-z0-9])([A-Z])/g, '$1_$2').toUpperCase();
  result = result.replace(new RegExp(refSnake, 'g'), ctx.naming.snake);

  // Replace kebab-case in URLs (e.g., /queues -> /alert-configs)
  const refKebab = refEntity.replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase();
  result = result.replace(new RegExp(refKebab, 'g'), ctx.naming.kebab);
  result = result.replace(new RegExp(`/${refLower}s`, 'g'), ctx.baseUrl);

  return result;
}
