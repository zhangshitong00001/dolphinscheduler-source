/**
 * Task plugin pattern inferrer.
 * Reads reference task plugin files and infers patterns for generating
 * new SPI-based task plugins.
 */

import { extractJavaPattern } from './pattern-extractor.js';
import type { ScaffoldContext, ExtractedPattern, MethodInfo } from '../types/index.js';

/**
 * Infers scaffold context for a task plugin based on a reference plugin.
 */
export async function inferTaskPluginContext(
  referencePath: string,
  baseContext: ScaffoldContext,
): Promise<ScaffoldContext> {
  const pattern = await extractJavaPattern(referencePath);

  // Reference plugin name inferred from package path or class name
  // e.g., ShellTaskChannelFactory -> refEntity = "Shell"
  const refEntity = inferRefPluginName(pattern);

  // Transform methods: replace reference plugin name with target
  const transformedMethods = pattern.methods.map(m => ({
    ...m,
    name: m.name.replace(new RegExp(refEntity, 'g'), baseContext.naming.pascal),
    returnType: m.returnType.replace(new RegExp(refEntity, 'g'), baseContext.naming.pascal),
    parameters: m.parameters.map(p => ({
      ...p,
      type: p.type.replace(new RegExp(refEntity, 'g'), baseContext.naming.pascal),
    })),
  }));

  return {
    ...baseContext,
    referencePath,
    extractedPattern: {
      ...pattern,
      methods: transformedMethods,
      imports: pattern.imports.map(imp => ({
        ...imp,
        qualifiedName: imp.qualifiedName.replace(new RegExp(refEntity.toLowerCase(), 'g'), baseContext.naming.lower),
      })),
    },
  };
}

/**
 * Extracts the essential task methods that a new plugin must implement.
 */
export function inferRequiredMethods(pattern: ExtractedPattern): MethodInfo[] {
  // Task plugins must implement: constructor, handle(), cancel(), getTaskChannel()
  return pattern.methods.filter(m =>
    m.name === 'handle' ||
    m.name === 'cancel' ||
    m.name === 'getTaskChannel' ||
    m.name === 'init' ||
    m.name === 'setTaskParams'
  );
}

/**
 * Infers the reference plugin name from the extracted pattern.
 */
function inferRefPluginName(pattern: ExtractedPattern): string {
  // Try to extract from TaskChannelFactory class name (e.g., "ShellTaskChannelFactory" -> "Shell")
  if (pattern.className.includes('TaskChannelFactory')) {
    return pattern.className.replace('TaskChannelFactory', '');
  }
  // Try from Task class name (e.g., "ShellTask" -> "Shell")
  if (pattern.className.includes('Task') && !pattern.className.includes('TaskChannel')) {
    return pattern.className.replace(/Task$/, '');
  }
  // Try from Parameters class name (e.g., "ShellParameters" -> "Shell")
  if (pattern.className.includes('Parameters')) {
    return pattern.className.replace('Parameters', '');
  }
  return pattern.className;
}
