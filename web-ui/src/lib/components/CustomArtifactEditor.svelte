<!--
  ~ Copyright The Reshapr Authors.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<script lang="ts">
	import { goto } from '$app/navigation';
	import { apiClient, ApiError } from '$lib/api/client.js';
	import {
		artifactTypeLabel,
		extractKindFromYaml,
		getKindDefinition,
		saveCustomArtifact,
		type ArtifactType,
		type EditorMode,
		type ReshaprArtifactKind
	} from '$lib/artifacts/index.js';
	import ApiErrorAlert from '$lib/components/ApiErrorAlert.svelte';
	import YamlMonacoEditor from '$lib/components/YamlMonacoEditor.svelte';
	import { Badge } from '$lib/components/ui/badge/index.js';
	import { Button } from '$lib/components/ui/button/index.js';
	import * as Card from '$lib/components/ui/card/index.js';
	import type * as Monaco from 'monaco-editor';

	const MONACO_ERROR_SEVERITY = 8;

	let {
		mode,
		kind,
		initialContent,
		listHref,
		artifactName = undefined,
		artifactType = undefined
	}: {
		mode: EditorMode;
		kind: ReshaprArtifactKind;
		initialContent: string;
		listHref: string;
		artifactName?: string;
		artifactType?: ArtifactType;
	} = $props();

	let content = $state('');
	let contentSeed = $state('');
	let validationMarkers = $state<Monaco.editor.IMarker[]>([]);
	let saveError = $state<string | null>(null);
	let saving = $state(false);

	const kindDef = $derived(getKindDefinition(kind));
	const editable = $derived(mode === 'create' || mode === 'edit');
	const schemaUri = $derived(editable ? kindDef?.schemaPath : undefined);
	const schemaErrors = $derived(
		validationMarkers.filter((marker) => marker.severity === MONACO_ERROR_SEVERITY)
	);
	const canSave = $derived(
		editable && !saving && content.trim().length > 0 && schemaErrors.length === 0
	);

	$effect(() => {
		if (initialContent === contentSeed) return;
		contentSeed = initialContent;
		content = initialContent;
	});

	async function onSave() {
		if (!canSave) return;
		saveError = null;

		const extractedKind = extractKindFromYaml(content);
		if (extractedKind && extractedKind !== kind) {
			saveError = `YAML kind must be "${kind}", got "${extractedKind}".`;
			return;
		}
		if (!extractedKind) {
			saveError = `Could not read kind: from YAML. Expected kind: ${kind}.`;
			return;
		}

		saving = true;
		try {
			await saveCustomArtifact(apiClient(), content, kind);
			await goto(listHref);
		} catch (e) {
			saveError = e instanceof ApiError ? e.message : String(e);
		} finally {
			saving = false;
		}
	}
</script>

<Card.Root class="mb-4">
	<Card.Header>
		<Card.Title>
			{#if mode === 'create'}
				{kindDef?.label ?? kind}
			{:else}
				{artifactName ?? 'Artifact'}
			{/if}
		</Card.Title>
		<Card.Description>
			{#if mode === 'create'}
				New custom artifact — edit the YAML below and save to attach it to this service.
				Use <kbd class="bg-muted rounded px-1 font-mono text-xs">Ctrl+Space</kbd> for schema suggestions.
			{:else if artifactType}
				Type: {artifactTypeLabel(artifactType)}
				{#if editable}
					<Badge variant="secondary" class="ml-2">Editable</Badge>
					— JSON Schema validation and completion. Saving replaces the artifact of this type.
					Use <kbd class="bg-muted rounded px-1 font-mono text-xs">Ctrl+Space</kbd> for suggestions.
				{:else}
					<Badge variant="outline" class="ml-2">Read-only</Badge>
				{/if}
			{/if}
		</Card.Description>
	</Card.Header>
</Card.Root>

{#if saveError}
	<div class="mb-4">
		<ApiErrorAlert message={saveError} />
	</div>
{/if}

<YamlMonacoEditor
	value={content}
	readOnly={!editable}
	{schemaUri}
	height="min(70vh, 32rem)"
	onChange={(value) => {
		content = value;
	}}
	onValidationChange={(markers) => {
		validationMarkers = markers;
	}}
/>

{#if editable}
	<div class="mt-4 flex flex-wrap items-center gap-2">
		<Button disabled={!canSave} onclick={() => void onSave()}>
			{saving ? 'Saving…' : 'Save'}
		</Button>
		<Button variant="outline" href={listHref} disabled={saving}>Cancel</Button>
		{#if schemaErrors.length > 0}
			<p class="text-destructive text-sm">Fix schema errors before saving.</p>
		{/if}
	</div>
{/if}
