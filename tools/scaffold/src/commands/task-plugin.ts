/**
 * Task plugin scaffolding command.
 * Generates a complete SPI task plugin module with TaskChannelFactory,
 * TaskChannel, AbstractTask subclass, Parameters, pom.xml, and test skeleton.
 */

import fs from 'fs-extra';
import path from 'path';
import type { ScaffoldContext } from '../types/index.js';
import { resolveTaskPluginPaths } from '../generators/path-resolver.js';
import {
  generateTaskChannelFactory,
  generateTaskChannel,
  generateTask,
  generateTaskParameters,
  generateTaskPluginPom,
} from '../generators/java-generator.js';
import { generatePomModuleEntry } from '../generators/config-generator.js';
import { safeWriteFile, mergeInsertIntoFile, ASL_HEADER_JAVA } from '../utils/file-utils.js';

export interface TaskPluginOptions {
  taskTypeName: string;
  description?: string;
  parameters?: string;
  reference?: string;
  overwrite?: boolean;
  dryRun?: boolean;
}

export async function scaffoldTaskPlugin(opts: TaskPluginOptions): Promise<string[]> {
  const created: string[] = [];

  // Parse custom parameters if provided
  let customParams: any[] = [];
  try {
    customParams = JSON.parse(opts.parameters || '[]');
  } catch (e) {
    console.error('Failed to parse --parameters JSON:', e);
    throw e;
  }

  const naming = computeNaming(opts.taskTypeName);
  const ctx: ScaffoldContext = {
    entityName: naming.pascal,
    naming,
    domain: 'task-plugin',
    baseUrl: '',
    apiTag: '',
    localePrefix: '',
    permissionPrefix: '',
    fields: customParams.map((p: any) => ({
      name: p.name || '',
      type: p.type || 'String',
      required: p.required || false,
      description: p.description || '',
    })),
    referencePath: opts.reference,
    javaPackage: `org.apache.dolphinscheduler.plugin.task.${naming.lower}`,
    apiPackage: '',
    daoPackage: '',
    parentRoute: '',
    routeTitle: opts.description || `${naming.pascal} task plugin`,
    auth: [],
    overwrite: opts.overwrite || false,
    dryRun: opts.dryRun || false,
  };

  const paths = resolveTaskPluginPaths(ctx);

  console.log(`\nScaffolding task plugin "${naming.pascal}"...`);
  if (ctx.dryRun) console.log('  (DRY RUN — no files will be written)\n');

  // Generate pom.xml
  const pomContent = await generateTaskPluginPom(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.taskPluginPom!, pomContent, ctx.overwrite)) {
    created.push(paths.taskPluginPom!);
  }

  // Generate TaskChannelFactory
  const factoryContent = await generateTaskChannelFactory(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.taskChannelFactory!, factoryContent, ctx.overwrite)) {
    created.push(paths.taskChannelFactory!);
  }

  // Generate TaskChannel
  const channelContent = await generateTaskChannel(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.taskChannel!, channelContent, ctx.overwrite)) {
    created.push(paths.taskChannel!);
  }

  // Generate AbstractTask subclass
  const taskContent = await generateTask(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.taskImpl!, taskContent, ctx.overwrite)) {
    created.push(paths.taskImpl!);
  }

  // Generate Parameters
  const paramsContent = await generateTaskParameters(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.taskParameters!, paramsContent, ctx.overwrite)) {
    created.push(paths.taskParameters!);
  }

  // Generate test skeleton
  const testContent = generateTaskTest(ctx);
  if (ctx.dryRun || await safeWriteFile(paths.taskTest!, testContent, ctx.overwrite)) {
    created.push(paths.taskTest!);
  }

  // Merge module entry into parent pom.xml
  const moduleEntry = generatePomModuleEntry(ctx);
  if (!ctx.dryRun) {
    await mergeInsertIntoFile(
      paths.taskPluginParentPom!,
      `module-${naming.lower}`,
      `Add ${naming.lower} task plugin module`,
      moduleEntry,
    );
  }
  created.push(`${paths.taskPluginParentPom} (merged module entry)`);

  console.log(`\nDone! Created ${created.length} files/merges.`);

  return created;
}

/**
 * Generates a simple JUnit test skeleton for the task.
 */
function generateTaskTest(ctx: ScaffoldContext): string {
  const { naming } = ctx;
  return `${ASL_HEADER_JAVA}
package org.apache.dolphinscheduler.plugin.task.${naming.lower};

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ${naming.pascal}TaskTest {

    @Mock
    private TaskExecutionContext taskExecutionContext;

    private ${naming.pascal}Task task;

    @BeforeEach
    public void setUp() {
        task = new ${naming.pascal}Task(taskExecutionContext);
    }

    @Test
    public void testInit() {
        // TODO: Implement test
    }

    @Test
    public void testHandle() throws Exception {
        // TODO: Implement test
    }

    @Test
    public void testCancel() throws Exception {
        // TODO: Implement test
    }
}
`;
}

function computeNaming(pascalName: string) {
  const pascal = pascalName;
  const camel = pascal.charAt(0).toLowerCase() + pascal.slice(1);
  const kebab = pascal.replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase();
  const snake = pascal.replace(/([a-z0-9])([A-Z])/g, '$1_$2').toUpperCase();
  const lower = pascal.toLowerCase();
  return { pascal, camel, kebab, snake, lower };
}
