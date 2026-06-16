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
	import type { Snippet } from 'svelte';

	let {
		title,
		subtitle,
		actions
	}: {
		/** Main page title rendered as the single <h1> of the page. */
		title: string;
		/** Optional supporting text. Pass a string, or a snippet for rich content (links, code…). */
		subtitle?: string | Snippet;
		/** Optional right-aligned actions (buttons, toggles…). */
		actions?: Snippet;
	} = $props();
</script>

<div class="mb-6 flex flex-wrap items-start justify-between gap-4 border-b pb-4">
	<div class="min-w-0">
		<h1 class="text-2xl font-bold tracking-tight text-foreground">{title}</h1>
		{#if subtitle}
			<p class="text-muted-foreground mt-1 text-sm">
				{#if typeof subtitle === 'string'}
					{subtitle}
				{:else}
					{@render subtitle()}
				{/if}
			</p>
		{/if}
	</div>
	{#if actions}
		<div class="flex shrink-0 flex-wrap items-center gap-2">
			{@render actions()}
		</div>
	{/if}
</div>
