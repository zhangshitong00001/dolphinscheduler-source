/**
 * Core type definitions for the scaffolding harness.
 */

/** Describes a single field/column in the entity being scaffolded. */
export interface FieldDefinition {
  /** Field name in camelCase (e.g., "alertName", "createdTime") */
  name: string;
  /** Java type (e.g., "String", "Integer", "Long", "Date") */
  type: string;
  /** Whether this field is required (adds @NotEmpty validation) */
  required?: boolean;
  /** Whether this field is searchable (adds search input in UI) */
  searchable?: boolean;
  /** Human-readable description for Javadoc/Swagger */
  description?: string;
}

/** Naming variants computed from the entity PascalCase name. */
export interface NamingVariants {
  pascal: string;
  camel: string;
  kebab: string;
  snake: string;
  lower: string;
}

/** The central context object passed to all templates during rendering. */
export interface ScaffoldContext {
  /** PascalCase entity name (e.g., "AlertConfig") */
  entityName: string;
  /** All naming variants */
  naming: NamingVariants;
  /** Feature domain (e.g., "security", "project", "resource") */
  domain: string;
  /** REST base URL (e.g., "/alert-configs") */
  baseUrl: string;
  /** Swagger API tag (e.g., "ALERT_CONFIG_TAG") */
  apiTag: string;
  /** i18n locale prefix (e.g., "security.alert_config") */
  localePrefix: string;
  /** Permission key prefix (e.g., "security:alert-config") */
  permissionPrefix: string;
  /** Field definitions for the entity */
  fields: FieldDefinition[];
  /** Path to the reference file used for pattern inference */
  referencePath?: string;
  /** Extracted structural patterns from the reference file */
  extractedPattern?: ExtractedPattern;
  /** Base Java package (e.g., "org.apache.dolphinscheduler") */
  javaPackage: string;
  /** Java package for API classes */
  apiPackage: string;
  /** Java package for DAO classes */
  daoPackage: string;
  /** Parent route name for Vue router */
  parentRoute: string;
  /** Route meta title */
  routeTitle: string;
  /** Auth roles for route guard */
  auth: string[];
  /** Whether to overwrite existing files */
  overwrite: boolean;
  /** Whether this is a dry run (no file writes) */
  dryRun: boolean;
}

/** Represents a Java import statement. */
export interface ImportInfo {
  /** Fully qualified class or package */
  qualifiedName: string;
  /** Whether this is a static import */
  isStatic: boolean;
  /** Category: 'jdk', 'framework', 'project', 'static' */
  category: 'jdk' | 'framework' | 'project' | 'static';
}

/** Represents a Java annotation. */
export interface AnnotationInfo {
  /** Annotation name (e.g., "GetMapping", "ApiOperation") */
  name: string;
  /** Annotation parameters as key-value pairs */
  parameters: Record<string, string>;
  /** Raw annotation text for use when parameters cannot be parsed */
  raw: string;
}

/** Represents a method signature in Java or TypeScript. */
export interface MethodInfo {
  /** Method name */
  name: string;
  /** Return type */
  returnType: string;
  /** Method-level annotations */
  annotations: AnnotationInfo[];
  /** Parameter definitions */
  parameters: ParameterInfo[];
  /** Visibility modifier */
  visibility: 'public' | 'protected' | 'private';
  /** Whether this method is a CRUD operation */
  crudType?: 'list' | 'get' | 'create' | 'update' | 'delete' | 'query' | 'other';
}

/** Represents a method parameter. */
export interface ParameterInfo {
  /** Parameter name */
  name: string;
  /** Parameter type */
  type: string;
  /** Parameter-level annotations */
  annotations: AnnotationInfo[];
}

/** Full pattern extracted from a reference file. */
export interface ExtractedPattern {
  /** Source file path */
  sourcePath: string;
  /** Source file type */
  sourceType: 'java-controller' | 'java-service' | 'java-service-impl' | 'java-mapper' |
    'vue-view' | 'vue-store' | 'vue-service' | 'typescript';
  /** Package declaration (Java only) */
  packageName?: string;
  /** Class-level imports */
  imports: ImportInfo[];
  /** Class-level annotations */
  classAnnotations: AnnotationInfo[];
  /** Class declaration info */
  className: string;
  /** What the class extends */
  extendsClass?: string;
  /** What interfaces the class implements */
  implementsInterfaces: string[];
  /** Method signatures */
  methods: MethodInfo[];
  /** Field declarations */
  fields: ParameterInfo[];
}
