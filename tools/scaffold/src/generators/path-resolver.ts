/**
 * Maps entity definitions to output file paths across the full stack.
 */

import path from 'path';
import type { ScaffoldContext } from '../types/index.js';

/** Project root relative to this source file (tools/scaffold/src/generators -> project root) */
const PROJECT_ROOT = path.resolve(import.meta.dirname, '../../../..');

/** Standard Java source root for the API module */
const API_SRC = 'dolphinscheduler-api/src/main/java/org/apache/dolphinscheduler/api';
/** Standard Java source root for the DAO module */
const DAO_SRC = 'dolphinscheduler-dao/src/main/java/org/apache/dolphinscheduler/dao';
/** Standard Vue source root */
const VUE_SRC = 'dolphinscheduler-ui/src';
/** Standard task plugin root */
const TASK_PLUGIN_ROOT = 'dolphinscheduler-task-plugin';

/** Maps component type to output file path within the project. */
export interface OutputPaths {
  controller?: string;
  serviceInterface?: string;
  serviceImpl?: string;
  mapper?: string;
  viewIndex?: string;
  useTable?: string;
  useColumns?: string;
  viewTypes?: string;
  modalIndex?: string;
  useDetail?: string;
  store?: string;
  storeTypes?: string;
  apiService?: string;
  apiServiceTypes?: string;
  routeModule?: string;       // File to merge route entry into
  localeEnUS?: string;        // File to merge en_US locale into
  localeZhCN?: string;        // File to merge zh_CN locale into
  statusEnum?: string;        // File to merge status codes into (Status.java)
  permissionConstants?: string; // File to merge permissions into
  taskPluginPom?: string;
  taskChannelFactory?: string;
  taskChannel?: string;
  taskImpl?: string;
  taskParameters?: string;
  taskTest?: string;
  taskPluginParentPom?: string; // Parent pom for module registration
}

/**
 * Resolves output paths for a REST API scaffolding.
 */
export function resolveRestApiPaths(ctx: ScaffoldContext): OutputPaths {
  const { naming } = ctx;
  return {
    controller: path.join(PROJECT_ROOT, API_SRC, 'controller', `${naming.pascal}Controller.java`),
    serviceInterface: path.join(PROJECT_ROOT, API_SRC, 'service', `${naming.pascal}Service.java`),
    serviceImpl: path.join(PROJECT_ROOT, API_SRC, 'service', 'impl', `${naming.pascal}ServiceImpl.java`),
    mapper: path.join(PROJECT_ROOT, DAO_SRC, 'mapper', `${naming.pascal}Mapper.java`),
    statusEnum: path.join(PROJECT_ROOT, API_SRC, 'enums', 'Status.java'),
    permissionConstants: path.join(PROJECT_ROOT, API_SRC, 'constants', 'ApiFuncIdentificationConstant.java'),
  };
}

/**
 * Resolves output paths for a Vue page scaffolding.
 */
export function resolveVuePagePaths(ctx: ScaffoldContext): OutputPaths {
  const { naming, domain } = ctx;
  const viewDir = path.join(PROJECT_ROOT, VUE_SRC, 'views', domain, `${naming.kebab}-manage`);
  return {
    viewIndex: path.join(viewDir, 'index.tsx'),
    useTable: path.join(viewDir, 'use-table.ts'),
    useColumns: path.join(viewDir, 'use-columns.ts'),
    viewTypes: path.join(viewDir, 'types.ts'),
    modalIndex: path.join(viewDir, 'components', `${naming.kebab}-modal.tsx`),
    useDetail: path.join(viewDir, 'components', 'use-detail.ts'),
    store: path.join(PROJECT_ROOT, VUE_SRC, 'store', domain, `${naming.camel}.ts`),
    storeTypes: path.join(PROJECT_ROOT, VUE_SRC, 'store', domain, `${naming.camel}Types.ts`),
    apiService: path.join(PROJECT_ROOT, VUE_SRC, 'service', 'modules', naming.kebab, 'index.ts'),
    apiServiceTypes: path.join(PROJECT_ROOT, VUE_SRC, 'service', 'modules', naming.kebab, 'types.ts'),
    routeModule: path.join(PROJECT_ROOT, VUE_SRC, 'router', 'modules', `${domain}.ts`),
    localeEnUS: path.join(PROJECT_ROOT, VUE_SRC, 'locales', 'en_US', `${domain}.ts`),
    localeZhCN: path.join(PROJECT_ROOT, VUE_SRC, 'locales', 'zh_CN', `${domain}.ts`),
  };
}

/**
 * Resolves output paths for a task plugin scaffolding.
 */
export function resolveTaskPluginPaths(ctx: ScaffoldContext): OutputPaths {
  const { naming } = ctx;
  const pluginDir = path.join(PROJECT_ROOT, TASK_PLUGIN_ROOT, `dolphinscheduler-task-${naming.lower}`);
  const javaSrc = path.join(pluginDir, 'src', 'main', 'java', 'org', 'apache', 'dolphinscheduler', 'plugin', 'task', naming.lower);
  const testSrc = path.join(pluginDir, 'src', 'test', 'java', 'org', 'apache', 'dolphinscheduler', 'plugin', 'task', naming.lower);
  return {
    taskPluginPom: path.join(pluginDir, 'pom.xml'),
    taskChannelFactory: path.join(javaSrc, `${naming.pascal}TaskChannelFactory.java`),
    taskChannel: path.join(javaSrc, `${naming.pascal}TaskChannel.java`),
    taskImpl: path.join(javaSrc, `${naming.pascal}Task.java`),
    taskParameters: path.join(javaSrc, `${naming.pascal}Parameters.java`),
    taskTest: path.join(testSrc, `${naming.pascal}TaskTest.java`),
    taskPluginParentPom: path.join(PROJECT_ROOT, TASK_PLUGIN_ROOT, 'pom.xml'),
  };
}
