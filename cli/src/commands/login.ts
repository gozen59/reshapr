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
import http from 'http';
import url from 'url';
import open from 'open';
import getPort, {portNumbers} from 'get-port';

import { Command } from "commander";
import inquirer from 'inquirer';
import { Logger } from "../utils/logger.js";
import { ConfigUtil } from '../utils/config.js';
import { CLI_LABEL } from '../constants.js';

export const loginCommand = new Command('login')
  .description(`Login to ${CLI_LABEL}`)
  .option('-u, --username <username>', `Your ${CLI_LABEL} username`)
  .option('-p, --password <password>', `Your ${CLI_LABEL} password`)
  .option('-t, --token <token>', `Your ${CLI_LABEL} authentication token`)
  .option('-o, --org <org>', `Your ${CLI_LABEL} organization name`)
  .option('-s, --server <server>', `Your ${CLI_LABEL} Control Plane URL`, 'https://app.resphar.io')
  .option('-k, --insecure', 'Skip SSL certificate validation')
  .action(async (options) => {
    // First validate server URL and fetch server configuration.
    const configResponse = await fetch(`${options.server}/api/config`, {
      method: 'GET'
    }).catch(err => {
      Logger.error('Failed to connect to the server. Check URL.');
      process.exit(1);
    });

    const configData = await configResponse.json().catch(err => {
      Logger.error('Failed to parse server configuration: ' + err.message);
      process.exit(1);
    });

    if (configData.mode === 'on-premises') {
      await handleOnPremisesLogin(options);
      //await handleSaaSLogin(options);
    } else if (configData.mode === 'saas') {
      await handleSaaSLogin(options);
    }
  });

  async function handleOnPremisesLogin(options: any) {
    // Handle on-premises login logic here if needed.
    if (!options.username) {
      const username = await inquirer.prompt({
        type: 'input',
        name: 'username',
        message: `Enter your ${CLI_LABEL} username:`,
        validate: (input) => {
          if (!input) {
            return 'Username is required';
          }
          return true;
        }
      });
      options.username = username.username;
    }
    if (!options.password) {
      const password = await inquirer.prompt({
        type: 'password',
        name: 'password',
        message: `Enter your ${CLI_LABEL} password:`,
        validate: (input) => {
          if (!input) {
            return 'Password is required';
          }
          return true;
        }
      });
      options.password = password.password;
    }
    // Here you call a login function to authenticate the user.
    Logger.info(`Logging in to ${CLI_LABEL} at ${options.server}...`);
    const response = await fetch(`${options.server}/auth/login/reshapr`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username: options.username,
        password: options.password
      })
    });

    if (!response.ok) {
      Logger.error('Login failed: ' + response.statusText);
      process.exit(1);
    }
    response.text().then(data => {
      Logger.success('Login successful!');
      Logger.info(`Welcome, ${options.username}!`);
      // Here you would typically save the authentication token or session.
      let config = {
        username: options.username,
        server: options.server,
        insecure: options.insecure,
        token: data // Assuming the response contains a token.
      };
      ConfigUtil.writeConfig(config);
    }).catch(err => {
      Logger.error('Error parsing response: ' + err.message);
      process.exit(1);
    });
  }

  async function handleSaaSLogin(options: any) {
    // Prepare a token for reception.
    let token = null;

    // Start starting a lightweight web server to handle OAuth2 login.
    const server = http.createServer((req, res) => {
      // Handle Authentication callback here. Parse the URL.
      const parsedUrl = url.parse(req.url || '', true);

      // Get query parts of the URL.
      const query = parsedUrl.query;

      if (query.token && query.token.length > 0) {
        token = query.token as string;

        Logger.success('Login successful!');

        const tokenPayload = token.split('.')[1];
        const decodedPayload = Buffer.from(tokenPayload, 'base64').toString('utf8');
        const username = JSON.parse(decodedPayload).sub || 'unknown';

        Logger.info(`Welcome, ${username}!`);
        // Here you would typically save the authentication token or session.
        let config = {
          username: username,
          server: options.server,
          insecure: options.insecure,
          token: token
        };
        ConfigUtil.writeConfig(config);

        res.writeHead(200, { 'Content-Type': 'text/plain' });
        res.end('Login successful! You can close this window now.');
      } else {
        Logger.error('Login failed: No token received.');
        res.writeHead(400, { 'Content-Type': 'text/plain' });
        res.end(`Login failed: No token received. Please login on ${options.server} before trying again.`);
      }

      process.exit(0);
    });
    server.on('error', (err: any) => {
      Logger.error('Failed to start server for OAuth2 login: ' + err.message);
      process.exit(1);
    });


    const localPort = await getPort({port: portNumbers(5556, 5599)});

    server.listen(localPort, () => {
      Logger.info(`Listening for OAuth2 callback on http://localhost:${localPort}`);
    });

    // Opens the URL in the default browser.
    await open(`${options.server}/auth/login/saas?redirect_uri=http://localhost:${localPort}`, {
      wait: true
    });

    server.close(() => {
      Logger.info('Server closed after handling OAuth2 callback.');
    });
  }