/**
 * Service pattern inferrer.
 * Reads reference service interface and implementation files and maps their
 * structural patterns for generating new service layers.
 */

import { extractJavaPattern } from './pattern-extractor.js';
import type { ScaffoldContext, ExtractedPattern } from '../types/index.js';

/**
 * Infers scaffold context for a service interface based on a reference.
 */
export async function inferServiceInterfaceContext(
  referencePath: string,
  baseContext: ScaffoldContext,
): Promise<ScaffoldContext> {
  const pattern = await extractJavaPattern(referencePath);

  // Extract reference entity name
  const refEntity = pattern.className.replace(/Service$/, '');

  const methodTemplates = pattern.methods.map(m => ({
    ...m,
    name: transformEntityInName(m.name, refEntity, baseContext.naming.pascal),
  }));

  return {
    ...baseContext,
    referencePath,
    extractedPattern: {
      ...pattern,
      methods: methodTemplates,
    },
  };
}

/**
 * Infers scaffold context for a service implementation based on a reference.
 */
export async function inferServiceImplContext(
  referencePath: string,
  baseContext: ScaffoldContext,
): Promise<ScaffoldContext> {
  const pattern = await extractJavaPattern(referencePath);
  const refEntity = pattern.className.replace(/ServiceImpl$/, '');

  return {
    ...baseContext,
    referencePath,
    extractedPattern: {
      ...pattern,
      methods: pattern.methods.map(m => ({
        ...m,
        name: transformEntityInName(m.name, refEntity, baseContext.naming.pascal),
      })),
    },
  };
}

/**
 * Transforms entity references in method names.
 * e.g., "createQueue" -> "createAlertConfig" when refEntity is "Queue" and target is "AlertConfig"
 */
function transformEntityInName(name: string, refEntity: string, targetEntity: string): string {
  // Replace PascalCase entity reference in camelCase method name
  return name.replace(new RegExp(refEntity, 'g'), targetEntity);
}
