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
		buildDefaultArtifactTitle,
		extractKindFromYaml,
		getKindDefinition,
		saveCustomArtifact,
		type EditorMode,
		type ReshaprArtifactKind
	} from '$lib/artifacts/index.js';
	import ApiErrorAlert from '$lib/components/ApiErrorAlert.svelte';
	import YamlMonacoEditor from '$lib/components/YamlMonacoEditor.svelte';
	import { Button } from '$lib/components/ui/button/index.js';
	import { Input } from '$lib/components/ui/input/index.js';
	import PencilIcon from '@lucide/svelte/icons/pencil';
	import CheckIcon from '@lucide/svelte/icons/check';
	import type * as Monaco from 'monaco-editor';

	const MONACO_WARNING_SEVERITY = 4;

	let {
		mode,
		kind,
		initialContent,
		listHref,
		artifactName = undefined,
		existingNames = []
	}: {
		mode: EditorMode;
		kind: ReshaprArtifactKind;
		initialContent: string;
		listHref: string;
		artifactName?: string;
		existingNames?: string[];
	} = $props();

	let content = $state('');
	let contentSeed = $state('');
	let validationMarkers = $state<Monaco.editor.IMarker[]>([]);
	let saveError = $state<string | null>(null);
	let saving = $state(false);

	let title = $state('');
	let titleDirty = $state(false);
	let editingTitle = $state(false);
	let titleInputRef = $state<HTMLInputElement | null>(null);

	const kindDef = $derived(getKindDefinition(kind));
	const editable = $derived(mode === 'create' || mode === 'edit');
	const schemaUri = $derived(editable ? kindDef?.schemaPath : undefined);
	const schemaErrors = $derived(
		validationMarkers.filter((marker) => marker.severity >= MONACO_WARNING_SEVERITY)
	);
	const canSave = $derived(
		editable && !saving && content.trim().length > 0 && schemaErrors.length === 0
	);

	const defaultTitle = $derived(
		mode === 'create'
			? buildDefaultArtifactTitle(kind, existingNames)
			: (artifactName ?? 'Artifact')
	);

	// Keep the title in sync with the default until the user edits it manually.
	$effect(() => {
		if (!titleDirty) title = defaultTitle;
	});

	// Focus and select the input when entering title edit mode.
	$effect(() => {
		if (editingTitle && titleInputRef) {
			titleInputRef.focus();
			titleInputRef.select();
		}
	});

	$effect(() => {
		if (initialContent === contentSeed) return;
		contentSeed = initialContent;
		content = initialContent;
	});

	function startEditTitle() {
		editingTitle = true;
	}

	function commitTitle() {
		const trimmed = title.trim();
		if (!trimmed) {
			titleDirty = false;
			title = defaultTitle;
		} else {
			title = trimmed;
			titleDirty = true;
		}
		editingTitle = false;
	}

	function cancelTitle() {
		editingTitle = false;
	}

	function onTitleKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			event.preventDefault();
			commitTitle();
		} else if (event.key === 'Escape') {
			event.preventDefault();
			cancelTitle();
		}
	}

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
			await saveCustomArtifact(apiClient(), content, kind, title.trim() || undefined);
			await goto(listHref);
		} catch (e) {
			saveError = e instanceof ApiError ? e.message : String(e);
		} finally {
			saving = false;
		}
	}
</script>

<div class="mb-4 flex min-h-9 items-center gap-2">
	{#if mode === 'create' && editingTitle}
		<Input
			bind:ref={titleInputRef}
			bind:value={title}
			class="h-9 max-w-md text-lg font-semibold"
			aria-label="Artifact title"
			onkeydown={onTitleKeydown}
			onblur={commitTitle}
		/>
		<Button
			variant="ghost"
			size="icon"
			class="size-8"
			aria-label="Confirm title"
			onmousedown={(e) => e.preventDefault()}
			onclick={commitTitle}
		>
			<CheckIcon class="size-4" />
		</Button>
	{:else}
		<h2 class="text-lg font-semibold break-all">{title}</h2>
		{#if mode === 'create'}
			<Button
				variant="ghost"
				size="icon"
				class="size-8"
				aria-label="Edit title"
				onclick={startEditTitle}
			>
				<PencilIcon class="size-4" />
			</Button>
		{/if}
	{/if}
</div>

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
	</div>
{/if}
