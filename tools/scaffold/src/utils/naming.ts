/**
 * Naming utilities for case transformations.
 * Supports the full set of naming conventions used across the DolphinScheduler stack.
 */

/**
 * Transforms a PascalCase string to kebab-case.
 * e.g., "AlertConfig" -> "alert-config"
 */
export function pascalToKebab(input: string): string {
  return input
    .replace(/([a-z0-9])([A-Z])/g, '$1-$2')
    .replace(/([A-Z]+)([A-Z][a-z])/g, '$1-$2')
    .toLowerCase();
}

/**
 * Transforms a PascalCase string to camelCase.
 * e.g., "AlertConfig" -> "alertConfig"
 */
export function pascalToCamel(input: string): string {
  return input.charAt(0).toLowerCase() + input.slice(1);
}

/**
 * Transforms a PascalCase string to snake_case (UPPER).
 * e.g., "AlertConfig" -> "ALERT_CONFIG"
 */
export function pascalToSnake(input: string): string {
  return input
    .replace(/([a-z0-9])([A-Z])/g, '$1_$2')
    .replace(/([A-Z]+)([A-Z][a-z])/g, '$1_$2')
    .toUpperCase();
}

/**
 * Transforms a PascalCase string to lowercase.
 * e.g., "AlertConfig" -> "alertconfig"
 */
export function pascalToLower(input: string): string {
  return input.toLowerCase();
}

/**
 * Transforms a kebab-case string to PascalCase.
 * e.g., "alert-config" -> "AlertConfig"
 */
export function kebabToPascal(input: string): string {
  return input
    .split('-')
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join('');
}

/**
 * Transforms a snake_case string to PascalCase.
 * e.g., "ALERT_CONFIG" -> "AlertConfig"
 */
export function snakeToPascal(input: string): string {
  return input
    .toLowerCase()
    .split('_')
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join('');
}

/**
 * Returns all naming variants for a given PascalCase entity name.
 */
export function getAllVariants(pascalName: string): NamingVariants {
  return {
    pascal: pascalName,
    camel: pascalToCamel(pascalName),
    kebab: pascalToKebab(pascalName),
    snake: pascalToSnake(pascalName),
    lower: pascalToLower(pascalName),
  };
}

export interface NamingVariants {
  pascal: string;
  camel: string;
  kebab: string;
  snake: string;
  lower: string;
}
