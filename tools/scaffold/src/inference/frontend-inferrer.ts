/**
 * Frontend pattern inferrer.
 * Reads reference Vue TSX views, stores, and API services to infer
 * structural patterns for generating new frontend components.
 */

import { extractTypeScriptPattern } from './pattern-extractor.js';
import type { ScaffoldContext, ExtractedPattern, ImportInfo } from '../types/index.js';

/**
 * Infers scaffold context for a Vue view component based on a reference page.
 */
export async function inferViewContext(
  referencePath: string,
  baseContext: ScaffoldContext,
): Promise<ScaffoldContext> {
  const pattern = await extractTypeScriptPattern(referencePath);

  // Infer the reference entity name from the view path
  // e.g., views/security/yarn-queue-manage/index.tsx -> refEntity = "yarn-queue"
  const refEntity = inferRefEntityFromPath(referencePath);

  return {
    ...baseContext,
    referencePath,
    extractedPattern: transformTSImports(pattern, refEntity, baseContext),
  };
}

/**
 * Infers scaffold context for a Pinia store based on a reference store.
 */
export async function inferStoreContext(
  referencePath: string,
  baseContext: ScaffoldContext,
): Promise<ScaffoldContext> {
  const pattern = await extractTypeScriptPattern(referencePath);
  const refEntity = inferRefEntityFromPath(referencePath);

  return {
    ...baseContext,
    referencePath,
    extractedPattern: transformTSImports(pattern, refEntity, baseContext),
  };
}

/**
 * Infers scaffold context for a Vue API service module based on a reference.
 */
export async function inferApiServiceContext(
  referencePath: string,
  baseContext: ScaffoldContext,
): Promise<ScaffoldContext> {
  const pattern = await extractTypeScriptPattern(referencePath);
  const refEntity = inferRefEntityFromPath(referencePath);

  return {
    ...baseContext,
    referencePath,
    extractedPattern: transformTSImports(pattern, refEntity, baseContext),
  };
}

/**
 * Infers which Naive UI components and composables should be imported
 * based on the reference view.
 */
export function inferUIComponents(pattern: ExtractedPattern): string[] {
  const naivComponents: string[] = [];
  if (!pattern) return naivComponents;

  for (const imp of pattern.imports) {
    if (imp.qualifiedName === 'naive-ui') {
      // Can't easily determine which specific components were imported
      // without parsing the import statement, but we can note it
      naivComponents.push('naive-ui');
    }
  }

  return naivComponents;
}

/**
 * Extracts the entity name from a file path.
 * e.g., views/security/yarn-queue-manage/index.tsx -> "yarn-queue"
 *       service/modules/data-source/index.ts -> "data-source"
 */
function inferRefEntityFromPath(filePath: string): string {
  const parts = filePath.split('/');
  // Find the directory named "xxx-manage" or the parent of index.tsx
  for (let i = parts.length - 1; i >= 0; i--) {
    if (parts[i] === 'index.tsx' || parts[i] === 'index.ts') {
      const parentDir = parts[i - 1];
      if (parentDir?.endsWith('-manage')) {
        return parentDir.replace(/-manage$/, '');
      }
      return parentDir || '';
    }
  }
  return '';
}

/**
 * Transforms TypeScript imports by replacing entity-specific path segments.
 */
function transformTSImports(
  pattern: ExtractedPattern,
  refEntity: string,
  ctx: ScaffoldContext,
): ExtractedPattern {
  return {
    ...pattern,
    imports: pattern.imports.map(imp => ({
      ...imp,
      qualifiedName: imp.qualifiedName
        .replace(new RegExp(refEntity, 'g'), ctx.naming.kebab)
        .replace(new RegExp(refEntity.replace(/-/g, '_'), 'g'), ctx.naming.kebab),
    })),
  };
}
