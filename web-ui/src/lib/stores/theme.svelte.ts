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

export type ThemePreference = 'light' | 'dark' | 'system';

/** localStorage key — keep in sync with the inline anti-FOUC script in app.html. */
const STORAGE_KEY = 'reshapr-theme';

/** Reactive theme store using Svelte 5 runes. */
function createThemeStore() {
  let preference = $state<ThemePreference>('system');
  // The actually-applied theme (resolves 'system' to light/dark).
  let resolved = $state<'light' | 'dark'>('light');

  function systemPrefersDark(): boolean {
    return typeof window !== 'undefined'
      && window.matchMedia('(prefers-color-scheme: dark)').matches;
  }

  function apply() {
    resolved = preference === 'system'
      ? (systemPrefersDark() ? 'dark' : 'light')
      : preference;
    document.documentElement.classList.toggle('dark', resolved === 'dark');
  }

  return {
    get preference() { return preference; },
    get resolved() { return resolved; },

    /** Read the persisted preference and start listening to system changes. */
    init() {
      const stored = localStorage.getItem(STORAGE_KEY) as ThemePreference | null;
      if (stored === 'light' || stored === 'dark' || stored === 'system') {
        preference = stored;
      }
      apply();

      // Re-apply when the OS theme changes (only matters in 'system' mode).
      window
        .matchMedia('(prefers-color-scheme: dark)')
        .addEventListener('change', () => {
          if (preference === 'system') apply();
        });
    },

    /** Set an explicit preference, persist it and apply it immediately. */
    set(value: ThemePreference) {
      preference = value;
      localStorage.setItem(STORAGE_KEY, value);
      apply();
    },

    /** Cycle light → dark → system → light. */
    cycle() {
      const order: ThemePreference[] = ['light', 'dark', 'system'];
      const next = order[(order.indexOf(preference) + 1) % order.length];
      this.set(next);
    }
  };
}

export const theme = createThemeStore();

