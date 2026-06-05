/**
 * Full-stack scaffolding command.
 * Orchestrates both REST API (backend) and Vue page (frontend) generation
 * for a complete CRUD feature.
 */

import { scaffoldRestApi, type RestApiOptions } from './rest-api.js';
import { scaffoldVuePage, type VuePageOptions } from './vue-page.js';

export interface FullStackOptions {
  entityName: string;
  domain: string;
  baseUrl: string;
  parentRoute: string;
  routeTitle?: string;
  fields: string;
  auth?: string;
  referenceController?: string;
  referenceView?: string;
  overwrite?: boolean;
  dryRun?: boolean;
}

export async function scaffoldFullStack(opts: FullStackOptions): Promise<{
  backendFiles: string[];
  frontendFiles: string[];
}> {
  console.log(`\n========================================`);
  console.log(`Full-Stack Scaffolding: ${opts.entityName}`);
  console.log(`========================================`);

  // 1. Scaffold backend (REST API)
  console.log(`\n--- Backend (REST API) ---`);
  const restApiOpts: RestApiOptions = {
    entityName: opts.entityName,
    domain: opts.domain,
    baseUrl: opts.baseUrl,
    fields: opts.fields,
    reference: opts.referenceController,
    overwrite: opts.overwrite,
    dryRun: opts.dryRun,
  };
  const backendFiles = await scaffoldRestApi(restApiOpts);

  // 2. Scaffold frontend (Vue page)
  console.log(`\n--- Frontend (Vue Page) ---`);
  const vuePageOpts: VuePageOptions = {
    entityName: opts.entityName,
    domain: opts.domain,
    parentRoute: opts.parentRoute,
    routeTitle: opts.routeTitle,
    fields: opts.fields,
    auth: opts.auth,
    reference: opts.referenceView,
    overwrite: opts.overwrite,
    dryRun: opts.dryRun,
  };
  const frontendFiles = await scaffoldVuePage(vuePageOpts);

  // 3. Summary
  console.log(`\n========================================`);
  console.log(`Full-Stack Scaffolding Complete!`);
  console.log(`  Backend files:  ${backendFiles.length}`);
  console.log(`  Frontend files: ${frontendFiles.length}`);
  console.log(`========================================\n`);

  if (!opts.dryRun) {
    console.log('Next steps:');
    console.log('  1. Build the backend:  ./mvnw clean compile -pl dolphinscheduler-api -am -DskipTests');
    console.log('  2. Build the frontend: cd dolphinscheduler-ui && npx vue-tsc --noEmit');
    console.log('  3. Add the DB entity in dolphinscheduler-dao if needed');
    console.log('  4. Update the DAO entity to match your field definitions\n');
  }

  return { backendFiles, frontendFiles };
}
