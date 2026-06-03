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
import { Command } from "commander";
import { Logger } from "../utils/logger.js";
import { ConfigUtil } from "../utils/config.js";

export const switchOrgCommand = new Command('switch-org')
  .description(`Switch to a different organization`)
  .argument('<target-org>', 'Name of the organization to switch to')
  .action(async (targetOrg: string) => {
    // Retrieve config and keep it as it will be updated.
    const config = ConfigUtil.config;
    if (config.org === targetOrg) {
      Logger.warn(`You are already in the '${targetOrg}' organization.`);
      return;
    }

    Logger.info(`Switching to organization '${targetOrg}'...`);
    const response = await fetch(`${config.server}/auth/switchOrganization/${encodeURIComponent(targetOrg)}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${config.token}`,
      }
    });

    if (!response.ok) {
      if (response.status === 401) {
        Logger.error('Authentication failed. Please login again.');
      } else if (response.status === 403) {
        Logger.error(`You are not a member of the '${targetOrg}' organization.`);
      } else {
        Logger.error(`Failed to switch organization: ${response.status} ${response.statusText}`);
      }
      process.exit(1);
    }

    response.text().then(data => {
      Logger.success('Login successful!');
      config.token = data;
      ConfigUtil.writeConfig(config);

      Logger.success(`Switched to organization '${targetOrg}'.`);
    }).catch(err => {
      Logger.error('Error parsing response: ' + err.message);
      process.exit(1);
    });
  });
