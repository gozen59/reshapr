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
	import { getContext } from 'svelte';
	import { page } from '$app/state';
	import { apiClient, ApiError } from '$lib/api/client.js';
	import {
		artifactTypeLabel,
		getKindDefinition,
		isEditableArtifactType,
		parseArtifactDetail,
		type ArtifactType,
		type ReshaprArtifactKind
	} from '$lib/artifacts/index.js';
	import ApiErrorAlert from '$lib/components/ApiErrorAlert.svelte';
	import { SERVICE_CONTEXT_KEY, type ServiceContextValue } from '$lib/serviceContext.js';
	import { Badge } from '$lib/components/ui/badge/index.js';
	import { Button } from '$lib/components/ui/button/index.js';
	import * as Card from '$lib/components/ui/card/index.js';

	const ctx = getContext<ServiceContextValue>(SERVICE_CONTEXT_KEY);

	const artifactId = $derived(page.params.artifactId ?? '');
	const isCreate = $derived(artifactId === 'new');
	const createKind = $derived(
		(page.url.searchParams.get('kind') as ReshaprArtifactKind | null) ?? 'Prompts'
	);
	const createKindDef = $derived(getKindDefinition(createKind));

	let error = $state<string | null>(null);
	let loading = $state(false);
	let artifactName = $state<string | null>(null);
	let artifactType = $state<ArtifactType | null>(null);

	const listHref = $derived(`/services/${ctx.id}/artifacts`);

	async function loadArtifact() {
		if (isCreate || !artifactId) return;
		loading = true;
		error = null;
		artifactName = null;
		artifactType = null;
		try {
			const raw = await apiClient().getArtifact(artifactId);
			const detail = parseArtifactDetail(raw);
			if (!detail) {
				error = 'Artifact not found or invalid response.';
				return;
			}
			artifactName = detail.name;
			artifactType = detail.type;
		} catch (e) {
			error = e instanceof ApiError ? e.message : String(e);
		} finally {
			loading = false;
		}
	}

	$effect(() => {
		if (ctx.id && !ctx.loading && !isCreate && artifactId) void loadArtifact();
	});
</script>

<div class="mb-4 flex flex-wrap items-center justify-between gap-4">
	<h3 class="text-lg font-semibold">
		{#if isCreate}
			New custom artifact
		{:else}
			Artifact detail
		{/if}
	</h3>
	<Button variant="outline" size="sm" href={listHref}>Back to list</Button>
</div>

{#if error}
	<ApiErrorAlert message={error} />
{/if}

<Card.Root>
	<Card.Header>
		<Card.Title>
			{#if isCreate}
				{createKindDef?.label ?? createKind}
			{:else if loading}
				Loading…
			{:else}
				{artifactName ?? artifactId}
			{/if}
		</Card.Title>
		<Card.Description>
			{#if isCreate}
				YAML editor and save flow ship in release 2–4 of this feature branch.
			{:else if artifactType}
				Type: {artifactTypeLabel(artifactType)}
				{#if !isEditableArtifactType(artifactType)}
					<Badge variant="outline" class="ml-2">Read-only</Badge>
				{/if}
			{:else}
				Placeholder until the Monaco editor lands (release 2+).
			{/if}
		</Card.Description>
	</Card.Header>
	<Card.Content class="text-muted-foreground text-sm">
		{#if isCreate}
			<p>
				Kind <code class="text-xs">{createKind}</code> selected. The unified editor will open here in
				<code class="text-xs">release4</code>.
			</p>
		{:else}
			<p>Artifact <code class="text-xs">{artifactId}</code> — view and edit UI coming in release 2+.</p>
		{/if}
	</Card.Content>
</Card.Root>
