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

import type * as Monaco from 'monaco-editor';

/** Name of the minimalist dark theme inheriting from `vs-dark`. */
export const RESHAPR_DARK_THEME = 'reshapr-dark';

/** Palette colour used for the editor surface (kept in sync with `app.css`). */
const BACKGROUND_CSS_VAR = '--card';
/** Fallback hex if the CSS variable cannot be resolved (Tailwind slate-900). */
const BACKGROUND_FALLBACK = '#0f172a';

/**
 * Resolve a CSS custom property to a `#rrggbb` hex string. The app palette uses
 * `oklch(...)` values which Monaco cannot parse, so we normalise them through a
 * canvas 2D context (supports CSS Color 4 in modern browsers).
 */
function cssVarToHex(varName: string, fallback: string): string {
	if (typeof document === 'undefined') return fallback;

	const raw = getComputedStyle(document.documentElement).getPropertyValue(varName).trim();
	if (!raw) return fallback;

	const ctx = document.createElement('canvas').getContext('2d');
	if (!ctx) return fallback;

	ctx.fillStyle = fallback;
	ctx.fillStyle = raw;
	const normalised = ctx.fillStyle;
	return typeof normalised === 'string' && normalised.startsWith('#') ? normalised : fallback;
}

/**
 * Define (or refresh) a minimalist dark theme based on `vs-dark`, overriding only
 * the surface backgrounds with a colour from the application dark palette.
 */
export function defineReshaprDarkTheme(monaco: typeof Monaco): void {
	const background = cssVarToHex(BACKGROUND_CSS_VAR, BACKGROUND_FALLBACK);

	monaco.editor.defineTheme(RESHAPR_DARK_THEME, {
		base: 'vs-dark',
		inherit: true,
		rules: [],
		colors: {
			'editor.background': background,
			'editorGutter.background': background,
			'minimap.background': background,
			'editorWidget.background': background,
			'editorStickyScroll.background': background
		}
	});
}

