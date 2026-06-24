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

import type { ReshaprArtifactKind } from './types.js';

export type AttachArtifactClient = {
	attachArtifactFile: (file: File) => Promise<unknown>;
};

/** Build a YAML file for POST `/api/v1/artifacts/attach`. */
export function yamlToAttachFile(content: string, kind: ReshaprArtifactKind): File {
	const filename = `${kind}.yaml`;
	return new File([content], filename, { type: 'application/x-yaml' });
}

/** Save custom artifact YAML via attach (create or replace-by-type). */
export async function saveCustomArtifact(
	client: AttachArtifactClient,
	content: string,
	kind: ReshaprArtifactKind
): Promise<unknown> {
	return client.attachArtifactFile(yamlToAttachFile(content, kind));
}

export function formatAttachSuccess(out: unknown): string {
	if (out && typeof out === 'object') {
		const o = out as Record<string, unknown>;
		const name = typeof o.name === 'string' ? o.name : undefined;
		const type = typeof o.type === 'string' ? o.type : undefined;
		const id = typeof o.id === 'string' ? o.id : undefined;
		const bits = ['Artifact saved'];
		if (type) bits.push(`type ${type}`);
		if (name) bits.push(`« ${name} »`);
		if (id) bits.push(`id ${id}`);
		return bits.join(' — ');
	}
	return `Artifact saved: ${JSON.stringify(out)}`;
}
