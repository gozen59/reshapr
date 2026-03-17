/*
 * Copyright The Reshapr Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import * as os from 'node:os';
import * as fs from 'node:fs';
import * as path from 'node:path';

import { Command } from "commander";
import { Logger } from "../utils/logger.js";
import { runDockerCompose } from '../utils/containers.js';
import { CLI_NAME, CLI_LABEL } from '../constants.js';

const GITHUB_REPO = 'reshaprio/reshapr';
const GITHUB_RAW_BASE = `https://raw.githubusercontent.com/${GITHUB_REPO}`;
const GITHUB_API_BASE = `https://api.github.com/repos/${GITHUB_REPO}`;
const COMPOSE_FILE_NAME = 'docker-compose-all-in-one.yml';
const COMPOSE_REMOTE_PATH = `install/${COMPOSE_FILE_NAME}`;
const RESHAPR_DIR = path.join(os.homedir(), `.${CLI_NAME}`);
const RUN_STATE_FILE = path.join(RESHAPR_DIR, 'run-state.json');

export const runCommand = new Command('run')
  .description(`Start ${CLI_LABEL} locally using Docker Compose`)
  .option('-r, --release <release>', 'Release of the containers to run', 'latest')
  .action(async (options) => {
    let release: string = options.release;

    // Resolve 'latest' to the actual latest GitHub release tag.
    if (release === 'latest') {
      release = await resolveLatestRelease();
      Logger.info(`Resolved 'latest' to release '${release}'.`);
    }

    const composeFile = getComposeFilePath(release);

    // Download compose file if not already cached for this release.
    if (!fs.existsSync(composeFile)) {
      await downloadComposeFile(release, composeFile);
    } else {
      Logger.info(`Using cached compose file for release '${release}'.`);
    }

    Logger.info(`Starting ${CLI_LABEL} containers (release: ${release})...`);
    const exitCode = await runDockerCompose(['up', '-d'], composeFile);

    if (exitCode !== 0) {
      Logger.error(`docker compose exited with code ${exitCode}.`);
      process.exit(exitCode);
    }

    saveRunState(release, composeFile);
    Logger.success(`${CLI_LABEL} containers started successfully.`);
  });

function getComposeFilePath(release: string): string {
  return path.join(RESHAPR_DIR, `docker-compose-${release}.yml`);
}

async function resolveLatestRelease(): Promise<string> {
  const url = `${GITHUB_API_BASE}/releases/latest`;
  const response = await fetch(url, {
    headers: { 'Accept': 'application/vnd.github+json' },
  });

  if (!response.ok) {
    Logger.error(`Failed to fetch latest release from GitHub: ${response.status} ${response.statusText}`);
    process.exit(1);
  }

  const data = await response.json() as { tag_name: string };
  return data.tag_name;
}

function getGitHubRef(release: string): string {
  if (release === 'nightly') {
    return 'refs/heads/main';
  }
  return `refs/tags/${release}`;
}

async function downloadComposeFile(release: string, destPath: string): Promise<void> {
  const ref = getGitHubRef(release);
  const url = `${GITHUB_RAW_BASE}/${ref}/${COMPOSE_REMOTE_PATH}`;

  Logger.info(`Downloading compose file from ${url}...`);
  const response = await fetch(url);

  if (!response.ok) {
    Logger.error(`Failed to download compose file: ${response.status} ${response.statusText}`);
    Logger.error(`Make sure the release '${release}' exists on the GitHub repository.`);
    process.exit(1);
  }

  let content = await response.text();

  // Replace image tags with the requested release.
  content = content.replace(/(quay\.io\/reshapr\/[^:]+):[\w.-]+/g, `$1:${release}`);

  fs.mkdirSync(RESHAPR_DIR, { recursive: true });
  fs.writeFileSync(destPath, content, 'utf-8');
  Logger.success(`Compose file saved to ${destPath}`);
}

function saveRunState(release: string, composeFile: string): void {
  const state = { release, composeFile, startedAt: new Date().toISOString() };
  fs.writeFileSync(RUN_STATE_FILE, JSON.stringify(state, null, 2), 'utf-8');
}

export function readRunState(): { release: string; composeFile: string; startedAt: string } | null {
  if (!fs.existsSync(RUN_STATE_FILE)) {
    return null;
  }
  try {
    return JSON.parse(fs.readFileSync(RUN_STATE_FILE, 'utf-8'));
  } catch {
    return null;
  }
}

export function removeRunState(): void {
  if (fs.existsSync(RUN_STATE_FILE)) {
    fs.unlinkSync(RUN_STATE_FILE);
  }
}
