/**
 * EJS template engine wrapper with custom helpers for code generation.
 */

import ejs from 'ejs';
import path from 'path';
import fs from 'fs-extra';
import type { ScaffoldContext, FieldDefinition } from '../types/index.js';
import { ASL_HEADER_JAVA, ASL_HEADER_TS } from './file-utils.js';

const TEMPLATES_DIR = path.resolve(import.meta.dirname, '../templates');

/**
 * EJS helpers available in all templates.
 */
const EJS_HELPERS = {
  /** ASL license header for Java */
  aslJava: ASL_HEADER_JAVA,
  /** ASL license header for TypeScript */
  aslTs: ASL_HEADER_TS,
  /** Maps a Java type to its boxed wrapper (int -> Integer) */
  boxedType: (type: string): string => {
    const primitives: Record<string, string> = {
      'int': 'Integer',
      'long': 'Long',
      'boolean': 'Boolean',
      'double': 'Double',
      'float': 'Float',
      'char': 'Character',
      'byte': 'Byte',
      'short': 'Short',
    };
    return primitives[type] || type;
  },
  /** Converts a field definition to a Java method parameter name */
  paramName: (field: FieldDefinition): string => field.name,
  /** Generates a Javadoc @param line */
  javadocParam: (field: FieldDefinition): string => `     * @param ${field.name} ${field.description || field.name}`,
  /** Converts a Java type to TypeScript type */
  tsType: (javaType: string): string => {
    const mapping: Record<string, string> = {
      'String': 'string',
      'Integer': 'number',
      'int': 'number',
      'Long': 'number',
      'long': 'number',
      'Boolean': 'boolean',
      'boolean': 'boolean',
      'Double': 'number',
      'double': 'number',
      'Float': 'number',
      'float': 'number',
      'Date': 'string',
      'BigDecimal': 'number',
    };
    return mapping[javaType] || 'any';
  },
  /** Returns the NaiveUI table column type for a Java type */
  columnType: (javaType: string): string => {
    const mapping: Record<string, string> = {
      'String': 'string',
      'Integer': 'number',
      'int': 'number',
      'Long': 'number',
      'long': 'number',
      'Boolean': 'boolean',
      'boolean': 'boolean',
      'Date': 'date',
    };
    return mapping[javaType] || 'string';
  },
  /** Indents each line of a string by the given number of spaces */
  indent: (text: string, spaces: number = 4): string => {
    const prefix = ' '.repeat(spaces);
    return text.split('\n').map(line => line ? prefix + line : line).join('\n');
  },
};

/**
 * Renders an EJS template with the given context.
 *
 * @param templateName - Template name relative to templates/ directory (e.g., "java/controller.ejs")
 * @param context - The scaffold context to render with
 * @returns The rendered template string
 */
export async function renderTemplate(
  templateName: string,
  context: ScaffoldContext,
): Promise<string> {
  const templatePath = path.join(TEMPLATES_DIR, templateName);
  if (!(await fs.pathExists(templatePath))) {
    throw new Error(`Template not found: ${templatePath}`);
  }

  const template = await fs.readFile(templatePath, 'utf-8');
  return ejs.render(template, { ...context, ...EJS_HELPERS }, { async: false });
}

/**
 * Renders an inline EJS string (for simple templates that don't need a file).
 */
export function renderInline(templateStr: string, context: ScaffoldContext): string {
  return ejs.render(templateStr, { ...context, ...EJS_HELPERS }, { async: false });
}
