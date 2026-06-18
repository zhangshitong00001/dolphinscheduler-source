/**
 * File utilities: ASL license header generation, safe file write, and merge-insert
 * operations for shared config files.
 */

import fs from 'fs-extra';
import path from 'path';

/** Apache Software License 2.0 header used in all project source files. */
export const ASL_HEADER_JAVA = `/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
`;

export const ASL_HEADER_TS = `/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
`;

/**
 * SCAFFOLD:INSERT anchor pattern.
 * Insert/update regions in shared files are marked with comments like:
 *   // SCAFFOLD:INSERT <key> -- <description>
 * The generator looks for these anchors and inserts/replaces content between them.
 */
const SCAFFOLD_ANCHOR_START = /\/\/\s*SCAFFOLD:INSERT\s+(\S+)\s*--\s*(.*)/;
const SCAFFOLD_ANCHOR_END = /\/\/\s*SCAFFOLD:END\s+(\S+)/;

/**
 * Safely writes a file to disk. If the file already exists and overwrite is false,
 * logs a warning and skips. Creates parent directories as needed.
 *
 * @returns true if the file was written, false if skipped
 */
export async function safeWriteFile(
  filePath: string,
  content: string,
  overwrite: boolean = false,
): Promise<boolean> {
  const exists = await fs.pathExists(filePath);
  if (exists && !overwrite) {
    console.warn(`  [SKIP] ${filePath} (already exists, use --overwrite to replace)`);
    return false;
  }

  await fs.ensureDir(path.dirname(filePath));
  await fs.writeFile(filePath, content, 'utf-8');
  console.log(`  [CREATE] ${filePath}`);
  return true;
}

/**
 * Merges content into an existing file at a SCAFFOLD:INSERT anchor point.
 * If the anchor doesn't exist, appends the content with newly created anchors.
 * If the anchor exists, replaces the content between the start and end anchors.
 *
 * @param filePath - Path to the existing file
 * @param anchorKey - Unique key for this scaffold region (e.g., "status-codes", "routes")
 * @param description - Human-readable description for the anchor comment
 * @param content - The content to insert
 * @returns true if merged, false if file doesn't exist
 */
export async function mergeInsertIntoFile(
  filePath: string,
  anchorKey: string,
  description: string,
  content: string,
): Promise<boolean> {
  if (!(await fs.pathExists(filePath))) {
    console.warn(`  [WARN] Cannot merge into ${filePath}: file not found`);
    return false;
  }

  const existing = await fs.readFile(filePath, 'utf-8');
  const lines = existing.split('\n');

  const startAnchor = `// SCAFFOLD:INSERT ${anchorKey} -- ${description}`;
  const endAnchor = `// SCAFFOLD:END ${anchorKey}`;

  // Check if anchor already exists
  const startIndex = lines.findIndex(line => line.includes(`SCAFFOLD:INSERT ${anchorKey}`));
  const endIndex = lines.findIndex(line => line.includes(`SCAFFOLD:END ${anchorKey}`));

  let newContent: string;
  if (startIndex >= 0 && endIndex >= 0) {
    // Replace existing scaffolded content
    const before = lines.slice(0, startIndex + 1);
    const after = lines.slice(endIndex);
    newContent = [...before, ...content.split('\n'), ...after].join('\n');
    console.log(`  [UPDATE] ${filePath} (replaced scaffold region "${anchorKey}")`);
  } else {
    // Append new scaffolded content at the end of the file
    const indent = '    '; // 4-space indent for Java enum entries
    const indentedContent = content
      .split('\n')
      .map(line => indent + line)
      .join('\n');
    newContent = existing.trimEnd() + '\n\n' + indent + startAnchor + '\n' + indentedContent + '\n' + indent + endAnchor + '\n';
    console.log(`  [MERGE] ${filePath} (added scaffold region "${anchorKey}")`);
  }

  await fs.writeFile(filePath, newContent, 'utf-8');
  return true;
}

/**
 * Prepends the ASL header to content if not already present.
 */
export function withAslHeader(content: string, language: 'java' | 'typescript'): string {
  const header = language === 'java' ? ASL_HEADER_JAVA : ASL_HEADER_TS;
  if (content.includes('Licensed to the Apache Software Foundation')) {
    return content;
  }
  return header + '\n' + content;
}

/**
 * Resolves a file path relative to the project root.
 */
export function projectPath(...segments: string[]): string {
  // tools/scaffold/src/utils -> project root
  const scaffoldDir = path.resolve(import.meta.dirname, '../../..');
  return path.resolve(scaffoldDir, ...segments);
}
