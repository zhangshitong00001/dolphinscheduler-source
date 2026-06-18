/**
 * Vue/TypeScript file generator.
 * Renders EJS templates with scaffold context to produce Vue TSX components,
 * Pinia stores, Axios service modules, and route entries.
 */

import { renderTemplate, renderInline } from '../utils/template-engine.js';
import { ASL_HEADER_TS } from '../utils/file-utils.js';
import type { ScaffoldContext, FieldDefinition } from '../types/index.js';

/**
 * Generates the view index.tsx (main list page with NaiveUI table).
 */
export async function generateViewIndex(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('vue/view-index.ejs', ctx);
}

/**
 * Generates the useTable composable (pagination, search, CRUD operations).
 */
export async function generateUseTable(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('vue/use-table.ejs', ctx);
}

/**
 * Generates the useColumns composable (data table column definitions).
 */
export async function generateUseColumns(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('vue/use-columns.ejs', ctx);
}

/**
 * Generates the view types file (TableColumns type).
 */
export async function generateViewTypes(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('vue/view-types.ejs', ctx);
}

/**
 * Generates the modal component TSX (create/edit form dialog).
 */
export async function generateModalIndex(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('vue/modal.ejs', ctx);
}

/**
 * Generates the useDetail composable (form state, create/update API calls).
 */
export async function generateUseDetail(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('vue/use-detail.ejs', ctx);
}

/**
 * Generates the Pinia store.
 */
export async function generateStore(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('vue/store.ejs', ctx);
}

/**
 * Generates the store types file content for merging.
 */
export function generateStoreTypesContent(ctx: ScaffoldContext): string {
  const { naming } = ctx;
  return [
    `// SCAFFOLD:INSERT store-types-${naming.lower}`,
    `interface ${naming.pascal}State {`,
    `  // TODO: Add state fields`,
    `}`,
    `export { ${naming.pascal}State }`,
    `// SCAFFOLD:END store-types-${naming.lower}`,
  ].join('\n');
}

/**
 * Generates the Axios API service module (index.ts).
 */
export async function generateApiService(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('vue/service-index.ejs', ctx);
}

/**
 * Generates the API service types file.
 */
export async function generateApiServiceTypes(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('vue/service-types.ejs', ctx);
}
