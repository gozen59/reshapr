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
 * Compute the SHA-256 hash of an email address as required by the
 * (modern) Gravatar API. The email is trimmed and lowercased before hashing.
 *
 * Returns `null` when the Web Crypto API is unavailable (e.g. during SSR
 * or in a non-secure context).
 */
export async function gravatarHash(email: string): Promise<string | null> {
  const normalized = email.trim().toLowerCase();
  if (!normalized || typeof crypto === 'undefined' || !crypto.subtle) {
    return null;
  }
  const data = new TextEncoder().encode(normalized);
  const digest = await crypto.subtle.digest('SHA-256', data);
  return Array.from(new Uint8Array(digest))
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('');
}

/**
 * Build a Gravatar image URL for the given email.
 *
 * The `d=404` parameter makes Gravatar return an HTTP 404 when the email has
 * no associated avatar, which lets the caller detect the absence of an avatar
 * (via the `<img>` `onerror` event) and fall back to a local placeholder.
 *
 * @param email the user's email address
 * @param size  the requested image size in pixels (default 64)
 * @returns the Gravatar URL, or `null` when a hash cannot be computed
 */
export async function gravatarUrl(email: string, size = 64): Promise<string | null> {
  const hash = await gravatarHash(email);
  if (!hash) {
    return null;
  }
  return `https://www.gravatar.com/avatar/${hash}?s=${size}&d=404`;
}

