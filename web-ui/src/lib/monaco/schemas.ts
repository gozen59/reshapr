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

/** Glob matched against the Monaco model URI (see `artifactModelUri`). */
export function schemaFileMatch(schemaPath: string): string {
	const fileName = schemaPath.replace(/^\/schemas\//, '');
	return `inmemory://reshapr/schemas/${fileName}/**`;
}

/** Stable URI namespace so monaco-yaml can bind the right JSON Schema per kind. */
export function artifactModelUri(schemaPath: string): string {
	const fileName = schemaPath.replace(/^\/schemas\//, '');
	return `inmemory://reshapr/schemas/${fileName}/${crypto.randomUUID()}.yaml`;
}

export async function loadMonacoYamlSchemas(): Promise<SchemasSettings[]> {
	return Promise.all(
		EDITABLE_KINDS.map(async (def) => {
			const response = await fetch(def.schemaPath);
			if (!response.ok) {
				throw new Error(`Failed to load schema ${def.schemaPath}: ${response.status}`);
			}
			const schema = (await response.json()) as Record<string, unknown>;
			return {
				uri: typeof schema.$id === 'string' ? schema.$id : def.schemaPath,
				fileMatch: [schemaFileMatch(def.schemaPath)],
				schema
			};
		})
	);
}
