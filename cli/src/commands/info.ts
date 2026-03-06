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
import { CLI_LABEL } from '../constants.js';

export const infoCommand = new Command('info')
  .description(`Display information about current context and the ${CLI_LABEL} Server`)
  .action(async () => {
    Logger.info('User Information');
    console.log(`  User        : ${ConfigUtil.config.username}`);
    console.log(`  Organization: ${ConfigUtil.config.org}`);
    console.log(`  Server      : ${ConfigUtil.config.server}`);

    const response = await fetch(`${ConfigUtil.config.server}/api/config`, {
      method: 'GET'
    });

    if (!response.ok) {
      Logger.error('Failed to fetch server information: ' + response.statusText);
      process.exit(1);
    }
    response.json().then(async data => {
      Logger.info('Server Information');
      console.log(`  Version     : ${data.version}`);
      console.log(`  Build time  : ${data.buildTimestamp}`);
      console.log(`  Mode        : ${data.mode}`);
      console.log(`  Internal IDP: ${data.internalIDPUrl}`);
      if (data.authenticationConfig && data.authenticationConfig.enabled) {
        console.log(`  OAuth2 IDP : ${data.authenticationConfig.url}/${data.authenticationConfig.realm}`);
      }
    }).catch(err => {
      Logger.error('Failed to parse server information: ' + err.message);
      process.exit(1);
    });
  });