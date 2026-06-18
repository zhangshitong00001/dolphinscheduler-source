#!/usr/bin/env node
/**
 * Full-stack scaffolding harness CLI for DolphinScheduler.
 *
 * Usage:
 *   npx tsx tools/scaffold/src/index.ts <command> [options]
 *
 * Commands:
 *   rest-api     Scaffold REST API endpoint (controller + service + impl + mapper)
 *   vue-page     Scaffold Vue page (view + store + API service + route)
 *   full-stack   Scaffold full-stack CRUD (backend + frontend)
 *   task-plugin  Scaffold SPI task plugin module
 */

import { Command } from 'commander';
import { scaffoldRestApi } from './commands/rest-api.js';
import { scaffoldVuePage } from './commands/vue-page.js';
import { scaffoldFullStack } from './commands/full-stack.js';
import { scaffoldTaskPlugin } from './commands/task-plugin.js';

const program = new Command();

program
  .name('scaffold')
  .description('Full-stack scaffolding harness for DolphinScheduler development')
  .version('1.0.0');

// ---------------------------------------------------------------------------
// rest-api command
// ---------------------------------------------------------------------------
program
  .command('rest-api <EntityName>')
  .description('Scaffold REST API endpoint: controller + service interface + service impl + mapper')
  .requiredOption('--domain <domain>', 'Feature domain (e.g., "security", "project", "resource")')
  .requiredOption('--base-url <url>', 'REST base URL (e.g., "/alert-configs")')
  .requiredOption('--fields <json>', 'Field definitions as JSON array, e.g. \'[{"name":"name","type":"String","required":true}]\'')
  .option('--reference <path>', 'Reference controller for pattern inference')
  .option('--overwrite', 'Overwrite existing files', false)
  .option('--dry-run', 'Output what would be created without writing files', false)
  .action(async (entityName: string, options: any) => {
    try {
      await scaffoldRestApi({
        entityName,
        domain: options.domain,
        baseUrl: options.baseUrl,
        fields: options.fields,
        reference: options.reference,
        overwrite: options.overwrite,
        dryRun: options.dryRun,
      });
    } catch (error) {
      console.error('Scaffolding failed:', error);
      process.exit(1);
    }
  });

// ---------------------------------------------------------------------------
// vue-page command
// ---------------------------------------------------------------------------
program
  .command('vue-page <EntityName>')
  .description('Scaffold Vue page: view TSX + composables + store + API service + route entry')
  .requiredOption('--domain <domain>', 'Feature domain (e.g., "security", "project")')
  .requiredOption('--parent-route <name>', 'Parent route name (e.g., "security")')
  .requiredOption('--fields <json>', 'Field definitions as JSON array')
  .option('--route-title <title>', 'Route meta title')
  .option('--auth <roles>', 'Auth requirements, comma-separated (e.g., "ADMIN_USER")')
  .option('--reference <path>', 'Reference view for pattern inference')
  .option('--overwrite', 'Overwrite existing files', false)
  .option('--dry-run', 'Output what would be created without writing files', false)
  .action(async (entityName: string, options: any) => {
    try {
      await scaffoldVuePage({
        entityName,
        domain: options.domain,
        parentRoute: options.parentRoute,
        routeTitle: options.routeTitle,
        fields: options.fields,
        auth: options.auth,
        reference: options.reference,
        overwrite: options.overwrite,
        dryRun: options.dryRun,
      });
    } catch (error) {
      console.error('Scaffolding failed:', error);
      process.exit(1);
    }
  });

// ---------------------------------------------------------------------------
// full-stack command
// ---------------------------------------------------------------------------
program
  .command('full-stack <EntityName>')
  .description('Scaffold full-stack CRUD feature: backend (REST API) + frontend (Vue page)')
  .requiredOption('--domain <domain>', 'Feature domain')
  .requiredOption('--base-url <url>', 'REST base URL')
  .requiredOption('--parent-route <name>', 'Parent route name')
  .requiredOption('--fields <json>', 'Field definitions as JSON array')
  .option('--route-title <title>', 'Route meta title')
  .option('--auth <roles>', 'Auth requirements')
  .option('--reference-controller <path>', 'Reference controller for pattern inference')
  .option('--reference-view <path>', 'Reference view for pattern inference')
  .option('--overwrite', 'Overwrite existing files', false)
  .option('--dry-run', 'Output what would be created without writing files', false)
  .action(async (entityName: string, options: any) => {
    try {
      await scaffoldFullStack({
        entityName,
        domain: options.domain,
        baseUrl: options.baseUrl,
        parentRoute: options.parentRoute,
        routeTitle: options.routeTitle,
        fields: options.fields,
        auth: options.auth,
        referenceController: options.referenceController,
        referenceView: options.referenceView,
        overwrite: options.overwrite,
        dryRun: options.dryRun,
      });
    } catch (error) {
      console.error('Scaffolding failed:', error);
      process.exit(1);
    }
  });

// ---------------------------------------------------------------------------
// task-plugin command
// ---------------------------------------------------------------------------
program
  .command('task-plugin <TaskTypeName>')
  .description('Scaffold SPI task plugin module')
  .option('--description <text>', 'Task type description')
  .option('--parameters <json>', 'Custom parameter fields as JSON array')
  .option('--reference <path>', 'Reference task plugin for pattern inference')
  .option('--overwrite', 'Overwrite existing files', false)
  .option('--dry-run', 'Output what would be created without writing files', false)
  .action(async (taskTypeName: string, options: any) => {
    try {
      await scaffoldTaskPlugin({
        taskTypeName,
        description: options.description,
        parameters: options.parameters,
        reference: options.reference,
        overwrite: options.overwrite,
        dryRun: options.dryRun,
      });
    } catch (error) {
      console.error('Scaffolding failed:', error);
      process.exit(1);
    }
  });

// Parse CLI args
program.parse();
