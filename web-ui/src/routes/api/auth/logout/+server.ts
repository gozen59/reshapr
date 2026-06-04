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

import { json, redirect } from '@sveltejs/kit';
import { buildOidcLogoutUrl, clearSessionCookie, getCtrlUrl, getPublicUrl } from '$lib/server/auth.js';
import type { RequestHandler } from './$types.js';

async function isOidcEnabled(): Promise<boolean> {
  try {
    const res = await fetch(`${getCtrlUrl()}/api/config/`);
    if (!res.ok) return false;
    const config = await res.json();
    return config.oidcEnabled === true;
  } catch {
    return false;
  }
}

function oidcLogoutRedirectUrl(): string {
  return buildOidcLogoutUrl(`${getPublicUrl()}/login`);
}

/**
 * GET /api/auth/logout
 *
 * Clears the httpOnly session cookie and redirects to /login.
 * When OIDC is enabled, redirects to the IdP to terminate the SSO session.
 */
export const GET: RequestHandler = async ({ cookies }) => {
  clearSessionCookie(cookies);

  if (await isOidcEnabled()) {
    redirect(302, oidcLogoutRedirectUrl());
  }

  redirect(302, '/login');
};

/**
 * POST /api/auth/logout
 *
 * Clears the httpOnly session cookie.
 * Returns a redirectUrl when OIDC logout is required.
 */
export const POST: RequestHandler = async ({ cookies }) => {
  clearSessionCookie(cookies);

  if (await isOidcEnabled()) {
    return json({ ok: true, redirectUrl: oidcLogoutRedirectUrl() });
  }

  return json({ ok: true });
};
