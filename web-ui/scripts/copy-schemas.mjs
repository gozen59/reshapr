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

import { cpSync, existsSync, mkdirSync, readdirSync } from 'node:fs';
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
/** Importable by Vite — not under static/ (public). */
const schemaDest = join(webUiRoot, 'src', 'lib', 'schemas');

mkdirSync(schemaDest, { recursive: true });

if (!existsSync(schemaSource)) {
	const existing = readdirSync(schemaDest).filter((name) => name.endsWith('.json'));
	if (existing.length === 0) {
		throw new Error(
			`Schema source missing at ${schemaSource} and no committed schemas in ${schemaDest}`
		);
	}
	console.warn(
		`Schema source not found (${schemaSource}); using ${existing.length} committed schema(s) in ${schemaDest}`
	);
	process.exit(0);
}

const schemaFiles = readdirSync(schemaSource).filter((name) => name.endsWith('.json'));
for (const file of schemaFiles) {
	cpSync(join(schemaSource, file), join(schemaDest, file));
}

console.log(`Copied ${schemaFiles.length} schema(s) to ${schemaDest}`);
