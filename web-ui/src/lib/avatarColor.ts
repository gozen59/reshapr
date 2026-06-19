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

/**
 * Avatar color palette derived from the reShapr brand color (a teal at hue
 * ~173°) and a set of harmonized complementary hues spread around the color
 * wheel. All entries share a consistent lightness/chroma so the squares feel
 * like a single coordinated palette, with white text for good contrast.
 */
const AVATAR_PALETTE = [
  'oklch(0.58 0.11 173)', // teal — reShapr primary
  'oklch(0.60 0.12 150)', // emerald
  'oklch(0.60 0.12 197)', // cyan
  'oklch(0.58 0.13 240)', // blue
  'oklch(0.56 0.14 270)', // indigo
  'oklch(0.56 0.15 300)', // violet
  'oklch(0.58 0.16 330)', // magenta
  'oklch(0.60 0.15 25)', //  rose
  'oklch(0.64 0.15 55)', //  orange
  'oklch(0.66 0.13 95)' //   amber-olive
] as const;

/** Simple, stable string hash (FNV-1a-ish) used to pick a palette entry. */
function hashString(value: string): number {
  let hash = 2166136261;
  for (let i = 0; i < value.length; i++) {
    hash ^= value.charCodeAt(i);
    hash = Math.imul(hash, 16777619);
  }
  return hash >>> 0;
}

/**
 * Deterministically pick a background color (CSS `oklch(...)` string) from the
 * reShapr-derived palette based on the given key (e.g. a service name).
 */
export function avatarColor(key: string): string {
  const normalized = (key ?? '').trim().toLowerCase();
  if (!normalized) return AVATAR_PALETTE[0];
  return AVATAR_PALETTE[hashString(normalized) % AVATAR_PALETTE.length];
}

/**
 * Compute up to two uppercase initials from a name. Falls back to '?' when the
 * name is empty.
 */
export function avatarInitials(name: string): string {
  const trimmed = (name ?? '').trim();
  if (!trimmed) return '?';
  return trimmed.substring(0, 2).toUpperCase();
}

