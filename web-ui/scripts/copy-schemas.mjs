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

import { cpSync, existsSync, mkdirSync, readdirSync, unlinkSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const webUiRoot = join(dirname(fileURLToPath(import.meta.url)), '..');
const schemaSource = join(
	webUiRoot,
	'..',
	'control-plane',
	'src',
	'main',
	'resources',
	'schemas'
);
/** Generated at build time — not committed (see web-ui/.gitignore). */
const schemaDest = join(webUiRoot, 'src', 'lib', 'schemas');

mkdirSync(schemaDest, { recursive: true });

function listDestSchemas() {
	return readdirSync(schemaDest).filter((name) => name.endsWith('.json'));
}

if (!existsSync(schemaSource)) {
	const existing = listDestSchemas();
	if (existing.length === 0) {
		throw new Error(
			`Schema source missing at ${schemaSource} and no schemas in ${schemaDest}. ` +
				'Run from the monorepo, or pre-copy schemas (CI does this before docker build).'
		);
	}
	console.warn(
		`Schema source not found (${schemaSource}); using ${existing.length} schema(s) already in ${schemaDest}`
	);
	process.exit(0);
}

const schemaFiles = readdirSync(schemaSource).filter((name) => name.endsWith('.json'));
if (schemaFiles.length === 0) {
	throw new Error(`No JSON schema files found in ${schemaSource}`);
}

for (const name of listDestSchemas()) {
	unlinkSync(join(schemaDest, name));
}

for (const file of schemaFiles) {
	cpSync(join(schemaSource, file), join(schemaDest, file));
}

console.log(`Copied ${schemaFiles.length} schema(s) from ${schemaSource} to ${schemaDest}`);
