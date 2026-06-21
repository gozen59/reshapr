/*
 * Copyright The Reshapr Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { EDITABLE_KINDS } from '$lib/artifacts/kinds.js';
import type { SchemasSettings } from 'monaco-yaml';

import customToolsSchema from '../../../static/schemas/CustomTools-v1alpha1-schema.json';
import promptsSchema from '../../../static/schemas/Prompts-v1alpha1-schema.json';
import resourcesSchema from '../../../static/schemas/Resources-v1alpha1-schema.json';
import toolsOutputFiltersSchema from '../../../static/schemas/ToolsOutputFilters-v1alpha1-schema.json';

const SCHEMA_BY_PATH: Record<string, Record<string, unknown>> = {
	'/schemas/Prompts-v1alpha1-schema.json': promptsSchema,
	'/schemas/CustomTools-v1alpha1-schema.json': customToolsSchema,
	'/schemas/Resources-v1alpha1-schema.json': resourcesSchema,
	'/schemas/ToolsOutputFilters-v1alpha1-schema.json': toolsOutputFiltersSchema
};

function schemaSlug(schemaPath: string): string {
	return schemaPath.replace(/^\/schemas\//, '').replace(/-schema\.json$/, '');
}

/** Glob matched against the Monaco model URI (see `artifactModelUri`). */
export function schemaFileMatch(schemaPath: string): string {
	return `/reshapr-artifact/${schemaSlug(schemaPath)}/**`;
}

/** Stable URI namespace so monaco-yaml can bind the right JSON Schema per kind. */
export function artifactModelUri(schemaPath: string): string {
	return `file:///reshapr-artifact/${schemaSlug(schemaPath)}/${crypto.randomUUID()}.yaml`;
}

/** Bundled JSON Schemas — no runtime fetch (works in dev, Docker, offline). */
export function buildMonacoYamlSchemas(): SchemasSettings[] {
	return EDITABLE_KINDS.map((def) => {
		const schema = SCHEMA_BY_PATH[def.schemaPath];
		if (!schema) {
			throw new Error(`Missing bundled schema for ${def.schemaPath}`);
		}
		return {
			uri: typeof schema.$id === 'string' ? schema.$id : def.schemaPath,
			fileMatch: [schemaFileMatch(def.schemaPath)],
			schema
		};
	});
}
