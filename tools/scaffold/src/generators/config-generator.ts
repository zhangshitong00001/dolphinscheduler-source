/**
 * Configuration file generator.
 * Handles merge-insert operations into shared configuration files like
 * Status.java, route modules, locale files, and pom.xml.
 */

import fs from 'fs-extra';
import path from 'path';
import type { ScaffoldContext } from '../types/index.js';

/**
 * Generates the content for merging status codes into Status.java.
 */
export function generateStatusMergeContent(ctx: ScaffoldContext): string {
  const { naming } = ctx;
  return [
    '',
    `        // SCAFFOLD:INSERT status-codes-${naming.lower} -- ${naming.kebab} status codes`,
    `        CREATE_${naming.snake}_ERROR(10001, "create ${naming.kebab} error", "创建${naming.kebab}错误"),`,
    `        QUERY_${naming.snake}_LIST_ERROR(10002, "query ${naming.kebab} list error", "查询${naming.kebab}列表错误"),`,
    `        UPDATE_${naming.snake}_ERROR(10003, "update ${naming.kebab} error", "更新${naming.kebab}错误"),`,
    `        DELETE_${naming.snake}_ERROR(10004, "delete ${naming.kebab} error", "删除${naming.kebab}错误"),`,
    `        VERIFY_${naming.snake}_ERROR(10005, "verify ${naming.kebab} error", "验证${naming.kebab}错误"),`,
    `        ${naming.snake}_NOT_EXIST(10006, "${naming.kebab} not exists", "${naming.kebab}不存在"),`,
    `        ${naming.snake}_ALREADY_EXISTS(10007, "${naming.kebab} already exists", "${naming.kebab}已存在"),`,
    `        // SCAFFOLD:END status-codes-${naming.lower}`,
  ].join('\n');
}

/**
 * Generates the content for merging permission constants.
 */
export function generatePermissionMergeContent(ctx: ScaffoldContext): string {
  const { naming, permissionPrefix } = ctx;
  return [
    '',
    `        // SCAFFOLD:INSERT permissions-${naming.lower} -- ${naming.kebab} permissions`,
    `        public static final String ${naming.snake}_CREATE = "${permissionPrefix}:create";`,
    `        public static final String ${naming.snake}_UPDATE = "${permissionPrefix}:update";`,
    `        public static final String ${naming.snake}_DELETE = "${permissionPrefix}:delete";`,
    `        public static final String ${naming.snake}_VIEW = "${permissionPrefix}:view";`,
    `        // SCAFFOLD:END permissions-${naming.lower}`,
  ].join('\n');
}

/**
 * Generates the route entry fragment for merging into a route module file.
 */
export function generateRouteMergeContent(ctx: ScaffoldContext): string {
  const { naming, domain, routeTitle, auth } = ctx;
  const authStr = auth.length > 0 ? auth.map(a => `'${a}'`).join(', ') : '';
  // Content starts with no leading indent — mergeInsertIntoFile adds indentation
  return [
    `{`,
    `  path: '/${domain}/${naming.kebab}-manage',`,
    `  name: '${domain}-${naming.kebab}-manage',`,
    `  component: components['${naming.kebab}-manage'],`,
    `  meta: {`,
    `    title: '${routeTitle || naming.pascal}',`,
    `    activeMenu: '${domain}',`,
    `    showSide: false,`,
    `    auth: [${authStr}]`,
    `  }`,
    `},`,
  ].join('\n');
}

/**
 * Generates locale entries (en_US and zh_CN) for merging.
 */
export function generateLocaleMergeContent(ctx: ScaffoldContext): { enUS: string; zhCN: string } {
  const { naming, localePrefix } = ctx;
  const prefix = localePrefix || `${naming.kebab}`;

  const enUS = [
    `  ${prefix}: {`,
    `    create: 'Create ${naming.pascal}',`,
    `    edit: 'Edit ${naming.pascal}',`,
    `    delete: 'Delete ${naming.pascal}',`,
    `    name: '${naming.pascal} Name',`,
    `    create_success: 'Created successfully',`,
    `    update_success: 'Updated successfully',`,
    `    delete_success: 'Deleted successfully',`,
    `  },`,
  ].join('\n');

  const zhCN = [
    `  ${prefix}: {`,
    `    create: '创建${naming.pascal}',`,
    `    edit: '编辑${naming.pascal}',`,
    `    delete: '删除${naming.pascal}',`,
    `    name: '${naming.pascal}名称',`,
    `    create_success: '创建成功',`,
    `    update_success: '更新成功',`,
    `    delete_success: '删除成功',`,
    `  },`,
  ].join('\n');

  return { enUS, zhCN };
}

/**
 * Generates the pom.xml module entry for merging into parent pom.
 */
export function generatePomModuleEntry(ctx: ScaffoldContext): string {
  const { naming } = ctx;
  return [
    `        <!-- SCAFFOLD:INSERT module-${naming.lower} -->`,
    `        <module>dolphinscheduler-task-${naming.lower}</module>`,
    `        <!-- SCAFFOLD:END module-${naming.lower} -->`,
  ].join('\n');
}
