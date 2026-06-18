/**
 * Generic pattern extractor for Java and TypeScript source files.
 * Parses imports, annotations, class declarations, and method signatures
 * from reference files to infer structural patterns for code generation.
 */

import fs from 'fs-extra';
import type { ExtractedPattern, ImportInfo, AnnotationInfo, MethodInfo, ParameterInfo } from '../types/index.js';

/**
 * Parses a Java source file and extracts its structural patterns.
 */
export async function extractJavaPattern(filePath: string): Promise<ExtractedPattern> {
  const content = await fs.readFile(filePath, 'utf-8');
  const lines = content.split('\n');

  const packageName = extractPackage(lines);
  const imports = extractJavaImports(lines);
  const classAnnotations = extractJavaAnnotations(lines, findClassDeclarationLine(lines));
  const methods = extractJavaMethods(lines);

  // Determine class declaration info
  const classLine = findClassDeclarationLine(lines);
  const { className, extendsClass, implementsInterfaces } = parseClassDeclaration(lines[classLine] || '');

  // Determine source type from class name and content
  const sourceType = inferJavaSourceType(className, content);

  return {
    sourcePath: filePath,
    sourceType,
    packageName,
    imports,
    classAnnotations,
    className,
    extendsClass,
    implementsInterfaces,
    methods,
    fields: [],
  };
}

/**
 * Parses a TypeScript/TSX source file and extracts its structural patterns.
 */
export async function extractTypeScriptPattern(filePath: string): Promise<ExtractedPattern> {
  const content = await fs.readFile(filePath, 'utf-8');
  const lines = content.split('\n');

  const imports = extractTSImports(lines);
  const methods = extractTSExports(lines);

  const sourceType = inferTSSourceType(filePath, content);

  return {
    sourcePath: filePath,
    sourceType,
    imports,
    classAnnotations: [],
    className: '',
    extendsClass: undefined,
    implementsInterfaces: [],
    methods,
    fields: [],
  };
}

// ---------------------------------------------------------------------------
// Java parsing helpers
// ---------------------------------------------------------------------------

function extractPackage(lines: string[]): string | undefined {
  for (const line of lines) {
    const match = line.match(/^\s*package\s+([\w.]+)\s*;/);
    if (match) return match[1];
  }
  return undefined;
}

function extractJavaImports(lines: string[]): ImportInfo[] {
  const imports: ImportInfo[] = [];
  for (const line of lines) {
    const staticMatch = line.match(/^import\s+static\s+([\w.*]+);/);
    if (staticMatch) {
      imports.push({
        qualifiedName: staticMatch[1],
        isStatic: true,
        category: 'static',
      });
      continue;
    }
    const normalMatch = line.match(/^import\s+([\w.*]+);/);
    if (normalMatch) {
      const name = normalMatch[1];
      imports.push({
        qualifiedName: name,
        isStatic: false,
        category: categorizeJavaImport(name),
      });
    }
  }
  return imports;
}

function categorizeJavaImport(qualifiedName: string): ImportInfo['category'] {
  if (qualifiedName.startsWith('java.') || qualifiedName.startsWith('javax.')) return 'jdk';
  if (qualifiedName.startsWith('org.springframework') ||
      qualifiedName.startsWith('io.swagger') ||
      qualifiedName.startsWith('springfox')) return 'framework';
  return 'project';
}

function findClassDeclarationLine(lines: string[]): number {
  for (let i = 0; i < lines.length; i++) {
    if (/^\s*public\s+(abstract\s+)?class\s+\w+/.test(lines[i])) {
      return i;
    }
  }
  return -1;
}

function parseClassDeclaration(line: string): {
  className: string;
  extendsClass?: string;
  implementsInterfaces: string[];
} {
  const classMatch = line.match(/class\s+(\w+)(?:\s+extends\s+(\w+))?(?:\s+implements\s+(.+))?\s*\{?/);
  if (!classMatch) return { className: '', implementsInterfaces: [] };

  const implementsInterfaces = classMatch[3]
    ? classMatch[3].split(',').map(s => s.trim()).filter(s => s)
    : [];

  return {
    className: classMatch[1],
    extendsClass: classMatch[2] || undefined,
    implementsInterfaces,
  };
}

function extractJavaAnnotations(lines: string[], classLine: number): AnnotationInfo[] {
  const annotations: AnnotationInfo[] = [];
  // Look for annotations on the lines immediately before the class declaration
  for (let i = Math.max(0, classLine - 5); i < classLine; i++) {
    const ann = parseAnnotation(lines[i]);
    if (ann) annotations.push(ann);
  }
  return annotations;
}

function extractJavaMethods(lines: string[]): MethodInfo[] {
  const methods: MethodInfo[] = [];
  let i = 0;

  while (i < lines.length) {
    // Match method declarations: visibility [static] returnType methodName(params)
    const methodMatch = lines[i].match(
      /^\s*(public|protected|private)\s+(static\s+)?(\w+(?:<[^>]+>)?)\s+(\w+)\s*\(([^)]*)\)/
    );
    if (methodMatch && !lines[i].includes(' class ') && !lines[i].includes(' interface ')) {
      const visibility = methodMatch[1] as MethodInfo['visibility'];
      const returnType = methodMatch[3];
      const methodName = methodMatch[4];
      const paramsStr = methodMatch[5];

      // Collect annotations on preceding lines
      const annotations: AnnotationInfo[] = [];
      for (let j = Math.max(0, i - 5); j < i; j++) {
        const ann = parseAnnotation(lines[j]);
        if (ann) annotations.push(ann);
      }

      // Collect method-level annotations on the same line or next
      // (e.g., @GetMapping, @ApiOperation on the next line)
      if (i + 1 < lines.length) {
        const nextAnn = parseAnnotation(lines[i + 1]);
        if (nextAnn) annotations.push(nextAnn);
      }

      // Parse parameters
      const parameters = parseJavaParameters(paramsStr);

      methods.push({
        name: methodName,
        returnType,
        annotations,
        parameters,
        visibility,
        crudType: inferCrudType(methodName, annotations),
      });
    }
    i++;
  }

  return methods;
}

function parseJavaParameters(paramsStr: string): ParameterInfo[] {
  if (!paramsStr.trim()) return [];

  // Handle multi-parameter with annotations like:
  // @ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser
  // This is a simplified parser that handles the common patterns
  const params: ParameterInfo[] = [];

  // Split by comma, but be careful with generics
  const parts = splitParams(paramsStr);

  for (const part of parts) {
    const trimmed = part.trim();
    // Extract annotations before the type
    const annMatches = trimmed.match(/@(\w+)(?:\(([^)]*)\))?/g);
    const annotations: AnnotationInfo[] = (annMatches || []).map(a => parseAnnotation(a)!).filter(Boolean);

    // Extract type and name (last two tokens after stripping annotations)
    const cleanPart = trimmed.replace(/@\w+(?:\([^)]*\))?\s*/g, '').trim();
    const tokens = cleanPart.split(/\s+/);

    if (tokens.length >= 2) {
      const type = tokens.slice(0, -1).join(' ');
      const name = tokens[tokens.length - 1];
      params.push({ name, type, annotations });
    }
  }

  return params;
}

function splitParams(paramsStr: string): string[] {
  const parts: string[] = [];
  let depth = 0;
  let current = '';
  for (const ch of paramsStr) {
    if (ch === '<') depth++;
    else if (ch === '>') depth--;
    else if (ch === ',' && depth === 0) {
      parts.push(current);
      current = '';
      continue;
    }
    current += ch;
  }
  if (current.trim()) parts.push(current);
  return parts;
}

function parseAnnotation(line: string): AnnotationInfo | null {
  const trimmed = line.trim();
  // Match @Name or @Name(key = "value", key2 = "value2")
  const match = trimmed.match(/^@(\w+)(?:\(([^)]*)\))?/);
  if (!match) return null;

  const name = match[1];
  const parameters: Record<string, string> = {};

  if (match[2]) {
    // Parse annotation parameters like key = "value"
    const paramStr = match[2];
    const paramMatch = paramStr.match(/(\w+)\s*=\s*(.+?)(?:,\s*(?=\w+\s*=)|$)/g);
    if (paramMatch) {
      for (const p of paramMatch) {
        const [k, ...v] = p.split('=');
        if (k && v.length > 0) {
          parameters[k.trim()] = v.join('=').trim().replace(/^["']|["']$/g, '');
        }
      }
    }
  }

  return { name, parameters, raw: trimmed };
}

function inferJavaSourceType(className: string, content: string): ExtractedPattern['sourceType'] {
  if (className.endsWith('Controller')) return 'java-controller';
  if (className.includes('ServiceImpl')) return 'java-service-impl';
  if (className.endsWith('Service')) return 'java-service';
  if (className.endsWith('Mapper')) return 'java-mapper';
  return 'java-controller'; // default
}

function inferCrudType(methodName: string, annotations: AnnotationInfo[]): MethodInfo['crudType'] {
  // Infer from HTTP method annotations
  for (const ann of annotations) {
    if (ann.name === 'GetMapping' || ann.name === 'PostMapping' || ann.name === 'PutMapping' || ann.name === 'DeleteMapping') {
      if (ann.parameters['value']?.includes('/{')) return 'get';
      if (ann.parameters['value']?.includes('search') || ann.parameters['value']?.includes('list') || ann.parameters['value']?.includes('query')) return 'list';
      if (ann.name === 'PostMapping') return 'create';
      if (ann.name === 'PutMapping') return 'update';
      if (ann.name === 'DeleteMapping') return 'delete';
    }
  }

  // Infer from method name
  const lower = methodName.toLowerCase();
  if (lower.startsWith('query') || lower.startsWith('list') || lower.startsWith('get') && lower.includes('all')) return 'list';
  if (lower.startsWith('get') || lower.startsWith('query') || lower.startsWith('detail')) return 'get';
  if (lower.startsWith('create') || lower.startsWith('add') || lower.startsWith('save') || lower.startsWith('insert')) return 'create';
  if (lower.startsWith('update') || lower.startsWith('modify') || lower.startsWith('edit')) return 'update';
  if (lower.startsWith('delete') || lower.startsWith('remove')) return 'delete';

  return 'other';
}

// ---------------------------------------------------------------------------
// TypeScript parsing helpers
// ---------------------------------------------------------------------------

function extractTSImports(lines: string[]): ImportInfo[] {
  const imports: ImportInfo[] = [];
  for (const line of lines) {
    // import { X } from 'y'
    const namedMatch = line.match(/^import\s+\{([^}]+)\}\s+from\s+['"]([^'"]+)['"]/);
    if (namedMatch) {
      imports.push({
        qualifiedName: namedMatch[2],
        isStatic: false,
        category: categorizeTSImport(namedMatch[2]),
      });
      continue;
    }
    // import X from 'y'
    const defaultMatch = line.match(/^import\s+(\w+)\s+from\s+['"]([^'"]+)['"]/);
    if (defaultMatch) {
      imports.push({
        qualifiedName: defaultMatch[2],
        isStatic: false,
        category: categorizeTSImport(defaultMatch[2]),
      });
      continue;
    }
    // import 'y'
    const sideEffectMatch = line.match(/^import\s+['"]([^'"]+)['"]/);
    if (sideEffectMatch) {
      imports.push({
        qualifiedName: sideEffectMatch[1],
        isStatic: false,
        category: categorizeTSImport(sideEffectMatch[1]),
      });
    }
  }
  return imports;
}

function categorizeTSImport(modulePath: string): ImportInfo['category'] {
  if (modulePath.startsWith('vue') || modulePath.startsWith('@vue') ||
      modulePath.startsWith('pinia') || modulePath.startsWith('vue-router') ||
      modulePath.startsWith('naive-ui') || modulePath.startsWith('@vicons')) return 'framework';
  if (modulePath.startsWith('@/')) return 'project';
  return 'jdk'; // npm packages
}

function extractTSExports(lines: string[]): MethodInfo[] {
  const methods: MethodInfo[] = [];

  for (let i = 0; i < lines.length; i++) {
    // export function name(params): returnType {
    const fnMatch = lines[i].match(
      /^export\s+(?:async\s+)?function\s+(\w+)\s*\(([^)]*)\)(?:\s*:\s*(\w+(?:<[^>]+>)?))?\s*\{?/
    );
    if (fnMatch) {
      methods.push({
        name: fnMatch[1],
        returnType: fnMatch[3] || 'any',
        annotations: [],
        parameters: parseTSParameters(fnMatch[2]),
        visibility: 'public',
        crudType: inferTSCrudType(fnMatch[1]),
      });
    }
  }

  return methods;
}

function parseTSParameters(paramsStr: string): ParameterInfo[] {
  if (!paramsStr.trim()) return [];
  const params: ParameterInfo[] = [];
  const parts = splitParams(paramsStr);

  for (const part of parts) {
    const trimmed = part.trim();
    const colonIdx = trimmed.lastIndexOf(':');
    if (colonIdx >= 0) {
      const name = trimmed.slice(0, colonIdx).trim();
      const type = trimmed.slice(colonIdx + 1).trim();
      params.push({ name, type, annotations: [] });
    } else if (trimmed) {
      params.push({ name: trimmed, type: 'any', annotations: [] });
    }
  }

  return params;
}

function inferTSSourceType(filePath: string, content: string): ExtractedPattern['sourceType'] {
  if (filePath.includes('/store/')) return 'vue-store';
  if (filePath.includes('/service/modules/')) return 'vue-service';
  if (filePath.includes('/views/')) return 'vue-view';
  return 'typescript';
}

function inferTSCrudType(fnName: string): MethodInfo['crudType'] {
  const lower = fnName.toLowerCase();
  if (lower.startsWith('query') && (lower.includes('list') || lower.includes('paging'))) return 'list';
  if (lower.startsWith('query') || lower.startsWith('get')) return 'get';
  if (lower.startsWith('create') || lower.startsWith('add')) return 'create';
  if (lower.startsWith('update') || lower.startsWith('modify')) return 'update';
  if (lower.startsWith('delete') || lower.startsWith('remove')) return 'delete';
  return 'other';
}
