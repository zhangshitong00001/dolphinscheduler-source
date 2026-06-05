/**
 * Vue page scaffolding command.
 * Generates view TSX component, composables, Pinia store,
 * Axios service module, and merges route + locale entries.
 */

import fs from 'fs-extra';
import type { ScaffoldContext } from '../types/index.js';
import { resolveVuePagePaths } from '../generators/path-resolver.js';
import {
  generateViewIndex,
  generateUseTable,
  generateUseColumns,
  generateViewTypes,
  generateModalIndex,
  generateUseDetail,
  generateStore,
  generateStoreTypesContent,
  generateApiService,
  generateApiServiceTypes,
} from '../generators/vue-generator.js';
import {
  generateRouteMergeContent,
  generateLocaleMergeContent,
} from '../generators/config-generator.js';
import { safeWriteFile, mergeInsertIntoFile } from '../utils/file-utils.js';

export interface VuePageOptions {
  entityName: string;
  domain: string;
  parentRoute: string;
  routeTitle?: string;
  fields: string;
  auth?: string;
  reference?: string;
  overwrite?: boolean;
  dryRun?: boolean;
}

export async function scaffoldVuePage(opts: VuePageOptions): Promise<string[]> {
  const created: string[] = [];

  // Parse fields
  let fields: any[] = [];
  try {
    fields = JSON.parse(opts.fields || '[]');
  } catch (e) {
    console.error('Failed to parse --fields JSON:', e);
    throw e;
  }

  const auth = opts.auth ? opts.auth.split(',').map(s => s.trim()) : [];

  // Build context
  const naming = computeNaming(opts.entityName);
  const ctx: ScaffoldContext = {
    entityName: naming.pascal,
    naming,
    domain: opts.domain,
    baseUrl: `/${naming.kebab}s`,
    apiTag: '',
    localePrefix: `${opts.domain}.${naming.kebab}`,
    permissionPrefix: '',
    fields: fields.map((f: any) => ({
      name: f.name || '',
      type: f.type || 'String',
      required: f.required === true,
      searchable: f.searchable === true,
      description: f.description || f.name,
    })),
    referencePath: opts.reference,
    javaPackage: '',
    apiPackage: '',
    daoPackage: '',
    parentRoute: opts.parentRoute,
    routeTitle: opts.routeTitle || naming.pascal,
    auth,
    overwrite: opts.overwrite || false,
    dryRun: opts.dryRun || false,
  };

  const paths = resolveVuePagePaths(ctx);

  console.log(`\nScaffolding Vue page for "${ctx.entityName}"...`);
  if (ctx.dryRun) console.log('  (DRY RUN — no files will be written)\n');

  // Generate view index.tsx
  const viewContent = await generateViewIndex(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.viewIndex!, viewContent, ctx.overwrite)) {
    created.push(paths.viewIndex!);
  }

  // Generate use-table composable
  const useTableContent = await generateUseTable(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.useTable!, useTableContent, ctx.overwrite)) {
    created.push(paths.useTable!);
  }

  // Generate use-columns composable
  const useColumnsContent = await generateUseColumns(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.useColumns!, useColumnsContent, ctx.overwrite)) {
    created.push(paths.useColumns!);
  }

  // Generate view types
  const viewTypesContent = await generateViewTypes(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.viewTypes!, viewTypesContent, ctx.overwrite)) {
    created.push(paths.viewTypes!);
  }

  // Generate modal component
  const modalContent = await generateModalIndex(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.modalIndex!, modalContent, ctx.overwrite)) {
    created.push(paths.modalIndex!);
  }

  // Generate use-detail composable
  const useDetailContent = await generateUseDetail(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.useDetail!, useDetailContent, ctx.overwrite)) {
    created.push(paths.useDetail!);
  }

  // Generate Pinia store
  const storeContent = await generateStore(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.store!, storeContent, ctx.overwrite)) {
    created.push(paths.store!);
  }

  // Generate store types file (create directly, not merge)
  const storeTypesContent = generateStoreTypesContent(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.storeTypes!, storeTypesContent, ctx.overwrite)) {
    created.push(paths.storeTypes!);
  }

  // Generate API service module
  const apiServiceContent = await generateApiService(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.apiService!, apiServiceContent, ctx.overwrite)) {
    created.push(paths.apiService!);
  }

  // Generate API service types
  const apiServiceTypesContent = await generateApiServiceTypes(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.apiServiceTypes!, apiServiceTypesContent, ctx.overwrite)) {
    created.push(paths.apiServiceTypes!);
  }

  // Merge route entry
  const routeContent = generateRouteMergeContent(ctx);
  if (!ctx.dryRun) {
    await mergeInsertIntoFile(
      paths.routeModule!,
      `route-${naming.lower}`,
      `${naming.kebab} route`,
      routeContent,
    );
  }
  created.push(`${paths.routeModule} (merged route entry)`);

  // Merge locale entries
  const { enUS, zhCN } = generateLocaleMergeContent(ctx);
  if (!ctx.dryRun) {
    await mergeInsertIntoFile(
      paths.localeEnUS!,
      `locales-${naming.lower}`,
      `${naming.kebab} en_US locales`,
      enUS,
    );
    await mergeInsertIntoFile(
      paths.localeZhCN!,
      `locales-${naming.lower}`,
      `${naming.kebab} zh_CN locales`,
      zhCN,
    );
  }
  created.push(`${paths.localeEnUS} (merged en_US locales)`);
  created.push(`${paths.localeZhCN} (merged zh_CN locales)`);

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
