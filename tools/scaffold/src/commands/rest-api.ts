/**
 * REST API scaffolding command.
 * Generates controller, service interface, service impl, and mapper
 * for a new entity, plus merges status codes and permission constants.
 */

import path from 'path';
import fs from 'fs-extra';
import type { ScaffoldContext } from '../types/index.js';
import { resolveRestApiPaths } from '../generators/path-resolver.js';
import {
  generateController,
  generateServiceInterface,
  generateServiceImpl,
  generateMapper,
} from '../generators/java-generator.js';
import {
  generateStatusMergeContent,
  generatePermissionMergeContent,
} from '../generators/config-generator.js';
import { safeWriteFile, mergeInsertIntoFile } from '../utils/file-utils.js';

export interface RestApiOptions {
  entityName: string;
  domain: string;
  baseUrl: string;
  fields: string;        // JSON array of FieldDefinition
  reference?: string;    // Path to reference controller
  overwrite?: boolean;
  dryRun?: boolean;
}

export async function scaffoldRestApi(opts: RestApiOptions): Promise<string[]> {
  const created: string[] = [];

  // Parse fields
  let fields: any[] = [];
  try {
    fields = JSON.parse(opts.fields || '[]');
  } catch (e) {
    console.error('Failed to parse --fields JSON:', e);
    throw e;
  }

  // Build context
  const naming = computeNaming(opts.entityName);
  const ctx: ScaffoldContext = {
    entityName: naming.pascal,
    naming,
    domain: opts.domain,
    baseUrl: opts.baseUrl || `/${naming.kebab}s`,
    apiTag: `${naming.snake}_TAG`,
    localePrefix: `${opts.domain}.${naming.kebab}`,
    permissionPrefix: `${opts.domain}:${naming.kebab}`,
    fields: fields.map((f: any) => ({
      name: f.name || '',
      type: f.type || 'String',
      required: f.required === true,
      searchable: f.searchable === true,
      description: f.description || f.name,
    })),
    referencePath: opts.reference,
    javaPackage: 'org.apache.dolphinscheduler',
    apiPackage: 'org.apache.dolphinscheduler.api',
    daoPackage: 'org.apache.dolphinscheduler.dao',
    parentRoute: '',
    routeTitle: naming.pascal,
    auth: [],
    overwrite: opts.overwrite || false,
    dryRun: opts.dryRun || false,
  };

  const paths = resolveRestApiPaths(ctx);

  console.log(`\nScaffolding REST API for "${ctx.entityName}"...`);
  if (ctx.dryRun) console.log('  (DRY RUN — no files will be written)\n');

  // Generate controller
  const controllerContent = await generateController(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.controller!, controllerContent, ctx.overwrite)) {
    created.push(paths.controller!);
  }

  // Generate service interface
  const serviceInterfaceContent = await generateServiceInterface(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.serviceInterface!, serviceInterfaceContent, ctx.overwrite)) {
    created.push(paths.serviceInterface!);
  }

  // Generate service impl
  const serviceImplContent = await generateServiceImpl(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.serviceImpl!, serviceImplContent, ctx.overwrite)) {
    created.push(paths.serviceImpl!);
  }

  // Generate mapper
  const mapperContent = await generateMapper(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.mapper!, mapperContent, ctx.overwrite)) {
    created.push(paths.mapper!);
  }

  // Merge status codes into Status.java
  const statusContent = generateStatusMergeContent(ctx);
  if (!ctx.dryRun) {
    await mergeInsertIntoFile(
      paths.statusEnum!,
      `status-codes-${naming.lower}`,
      `${naming.kebab} status codes`,
      statusContent,
    );
  }
  created.push(`${paths.statusEnum} (merged status codes)`);

  // Merge permission constants
  const permContent = generatePermissionMergeContent(ctx);
  if (!ctx.dryRun) {
    await mergeInsertIntoFile(
      paths.permissionConstants!,
      `permissions-${naming.lower}`,
      `${naming.kebab} permissions`,
      permContent,
    );
  }
  created.push(`${paths.permissionConstants} (merged permissions)`);

  console.log(`\nDone! Created ${created.length} files/merges.`);

  return created;
}

function computeNaming(pascalName: string) {
  const pascal = pascalName;
  const camel = pascal.charAt(0).toLowerCase() + pascal.slice(1);
  const kebab = pascal.replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase();
  const snake = pascal.replace(/([a-z0-9])([A-Z])/g, '$1_$2').toUpperCase();
  const lower = pascal.toLowerCase();
  return { pascal, camel, kebab, snake, lower };
}
