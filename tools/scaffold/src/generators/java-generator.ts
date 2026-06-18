/**
 * Java file generator.
 * Renders EJS templates with scaffold context to produce Java source files.
 */

import { renderTemplate } from '../utils/template-engine.js';
import { withAslHeader } from '../utils/file-utils.js';
import type { ScaffoldContext } from '../types/index.js';

/**
 * Generates a Java controller file from the controller template.
 */
export async function generateController(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('java/controller.ejs', ctx);
}

/**
 * Generates a Java service interface from the service-interface template.
 */
export async function generateServiceInterface(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('java/service-interface.ejs', ctx);
}

/**
 * Generates a Java service implementation from the service-impl template.
 */
export async function generateServiceImpl(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('java/service-impl.ejs', ctx);
}

/**
 * Generates a MyBatis Mapper interface from the mapper template.
 */
export async function generateMapper(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('java/mapper.ejs', ctx);
}

/**
 * Generates status code fragment for merging into Status.java.
 */
export async function generateStatusCodes(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('java/status-codes.ejs', ctx);
}

/**
 * Generates permission constant fragment for merging into ApiFuncIdentificationConstant.java.
 */
export async function generatePermissionConstants(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('java/permission-constants.ejs', ctx);
}

/**
 * Generates a task channel factory from template.
 */
export async function generateTaskChannelFactory(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('java/task-channel-factory.ejs', ctx);
}

/**
 * Generates a task channel from template.
 */
export async function generateTaskChannel(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('java/task-channel.ejs', ctx);
}

/**
 * Generates an AbstractTask subclass from template.
 */
export async function generateTask(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('java/task.ejs', ctx);
}

/**
 * Generates task Parameters POJO from template.
 */
export async function generateTaskParameters(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('java/task-parameters.ejs', ctx);
}

/**
 * Generates a task plugin pom.xml from template.
 */
export async function generateTaskPluginPom(ctx: ScaffoldContext): Promise<string> {
  return renderTemplate('java/task-plugin-pom.ejs', ctx);
}
