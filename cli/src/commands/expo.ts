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
import { program } from "commander";
import { Logger } from "../utils/logger.js";
import { ConfigUtil } from "../utils/config.js";
import { ageFrom } from "../utils/age.js";
import { formatEndpoint } from "../utils/format.js";
import { Context } from "../utils/context.js";
import { CLI_LABEL } from '../constants.js';

export const expoCommand = program.command('expo')
  .description(`Manage expositions in ${CLI_LABEL}`);

/* List all expositions */
expoCommand.command('list')
  .description('List all expositions')
  .option('-a, --all', 'Display also inactive expositions')
  .option('-o, --output <format>', 'Output format (json, yaml)')
  .action(async (options) => {
    if (options.all) {
      // If --all option is provided, list all expositions (active and inactive)
      const response = await fetch(`${ConfigUtil.config.server}/api/v1/expositions`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${ConfigUtil.config.token}`
        }
      });

      if (!response.ok) {
        Logger.error('Fetching expositions failed: ' + response.statusText);
        process.exit(1);
      }

      const data = await response.json().catch(err => {
        Logger.error('Error parsing expositions response: ' + err.message);
      });
      
      if (data.length === 0) {
        Logger.info('No expositions found.');
      } else {
        Context.put('expositions', data);
        
        const longestName = longestServiceName(data) + 1; // +1 for padding
        const longestBackend = longestBackendEndpoint(data) + 1; // +1 for padding

        Logger.log(`${'ID'.padEnd(13, ' ')}  ${'SERVICE'.padEnd(longestName, ' ')} ${'BACKEND'.padEnd(longestBackend, ' ')} AGE`);
        data.forEach((expo: any) => {
          Logger.log(`${expo.id}  ${(expo.service.name + ':' +  expo.service.version).padEnd(longestName, ' ')} ${expo.configurationPlan.backendEndpoint.padEnd(longestBackend, ' ')} ${ageFrom(expo.createdOn)}`);
        });
      }

    } else {
      // Default is to list only active expositions
      const response = await fetch(`${ConfigUtil.config.server}/api/v1/expositions/active`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${ConfigUtil.config.token}`
        }
      });

      if (!response.ok) {
        Logger.error('Fetching expositions failed: ' + response.statusText);
        process.exit(1);
      }

      const data = await response.json().catch(err => {
        Logger.error('Error parsing expositions response: ' + err.message);
      });

      if (data.length === 0) {
        Logger.info('No active expositions found. Check inactive expositions with the --all option.');
      } else {
        Context.put('expositions', data);
        const longestName = longestServiceName(data) + 1; // +1 for padding
        const longestBackend = longestBackendEndpoint(data) + 1; // +1 for padding
        const longestEndpoints = longestGatewaysFQDNs(data) + 1; // +1 for padding

        Logger.log(`${'ID'.padEnd(13, ' ')}  ${'SERVICE'.padEnd(longestName, ' ')} ${'BACKEND'.padEnd(longestBackend, ' ')} ${'ENDPOINTS'.padEnd(longestEndpoints, ' ')} AGE`);
        data.forEach((expo: any) => {
          let allFqdns = uniqueFQDNs(expo.gateways);
          Logger.log(`${expo.id}  ${(expo.service.name + ':' +  expo.service.version).padEnd(longestName, ' ')} ${expo.configurationPlan.backendEndpoint.padEnd(longestBackend, ' ')} ${allFqdns.join(',').padEnd(longestEndpoints, ' ')} ${ageFrom(expo.createdOn)}`);
        });
      }
    }    
  });

function longestServiceName(expos: any[]) {
  return expos.reduce((max, expo) => {
    return Math.max(max, expo.service.name.length + expo.service.version.length + 1);
  }, 0);
}  

function longestBackendEndpoint(expos: any[]) {
  return expos.reduce((max, expo) => {
    return Math.max(max, expo.configurationPlan.backendEndpoint ? expo.configurationPlan.backendEndpoint.length : 0);
  }, 0);
}

function uniqueFQDNs(gateways: { fqdns: string[]; }[]): string[] {
  let allFqdns: string[] = [];
  gateways.forEach(gateway => {
    gateway.fqdns.filter(fqdn => !allFqdns.includes(fqdn)).forEach(fqdn => allFqdns.push(fqdn));
  });
  return allFqdns;
}

function longestGatewaysFQDNs(expos: any[]) {
  return expos.reduce((max, expo) => {
    return Math.max(max, uniqueFQDNs(expo.gateways).join(',').length);
  }, 0);
}

/** Get exposition by ID */
expoCommand.command('get <id>')
  .description('Get details of an exposition by ID')
  .option('-o, --output <format>', 'Output format (json, yaml)')
  .action(async (id) => {
    const response = await fetch(`${ConfigUtil.config.server}/api/v1/expositions/${id}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${ConfigUtil.config.token}`
      }
    });

    if (!response.ok) {
      Logger.error('Fetching exposition failed: ' + response.statusText);
      process.exit(1);
    }

    const exposition = await response.json();
    Context.put('exposition', exposition);
    await displayExpositionDetails(exposition);
  });

  /** Create a new exposition */
expoCommand.command('create')
  .description('Create a new exposition')
  .requiredOption('-c, --configuration <id>', 'Configuration Plan ID to use')
  .requiredOption('-g, --gateway-group <id>', 'Gateway Group ID to use')
  .option('-o, --output <format>', 'Output format (json, yaml)')
  .action(async (options) => {
    const response = await fetch(`${ConfigUtil.config.server}/api/v1/expositions`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${ConfigUtil.config.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        configurationPlanId: options.configuration,
        gatewayGroupId: options.gatewayGroup
      })
    });

    if (!response.ok) {
      Logger.error('Failed to create exposition: ' + response.statusText);
      if (response.status === 429) {
        Logger.error('Exposition creation quota exceeded. Check your quotas.');
      }
      process.exit(1);
    }

    const data = await response.json().catch(err => {
      Logger.error('Error parsing exposition creation response: ' + err.message);
      process.exit(1);
    });

    Context.put('exposition', data);
    Logger.success(`Exposition created successfully with ID: ${data.id}`);
    await displayExpositionDetails(data);
  });

/** Delete exposition by ID */
expoCommand.command('delete <id>')
  .description('Delete an exposition by ID')
  .action(async (id) => {
    const response = await fetch(`${ConfigUtil.config.server}/api/v1/expositions/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${ConfigUtil.config.token}`
      }
    });

    if (!response.ok) {
      Logger.error('Deleting exposition failed: ' + response.statusText);
      process.exit(1);
    }

    Logger.success(`Exposition ${id} deleted successfully.`);
  });

async function displayExpositionDetails(exposition: any) {
  Logger.info('Exposition details');
  Logger.log(`ID          : ${exposition.id}`);
  Logger.log(`Created on  : ${exposition.createdOn}`);
  Logger.log(`Organization: ${exposition.organizationId}`);
  Logger.bold('Service:');
  Logger.log(`  ID     : ${exposition.service.id}`);
  Logger.log(`  Name   : ${exposition.service.name}`);
  Logger.log(`  Version: ${exposition.service.version}`);
  Logger.log(`  Type   : ${exposition.service.type}`);
  Logger.bold('Configuration Plan');
  Logger.log(`  ID             : ${exposition.configurationPlan.id}`);
  Logger.log(`  Name           : ${exposition.configurationPlan.name}`);
  Logger.log(`  BackendEndpoint: ${exposition.configurationPlan.backendEndpoint}`);
  Logger.log(`  Included Ops.  : ${JSON.stringify(exposition.configurationPlan.includedOperations || [])}`);
  Logger.log(`  Excluded Ops.  : ${JSON.stringify(exposition.configurationPlan.excludedOperations || [])}`);
  Logger.bold('Gateway Group');
  Logger.log(`  ID    : ${exposition.gatewayGroup.id}`);
  Logger.log(`  Name  : ${exposition.gatewayGroup.name}`);
  Logger.log(`  Labels: ${JSON.stringify(exposition.gatewayGroup.labels)}`);

  // Now check if acctive and print active endpoints if any.
  const activeResponse = await fetch(`${ConfigUtil.config.server}/api/v1/expositions/active/${exposition.id}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${ConfigUtil.config.token}`
    }
  });

  if (activeResponse.status === 404) {
    Logger.warn(`No active exposition found for the Exposition ${exposition.id}. Maybe there's no running Gateway at the moment?`);
    process.exit(0);
  }

  const activeExpositions = await activeResponse.json();
  Context.put('gateways', activeExpositions.gateways);

  let allFqdns = uniqueFQDNs(activeExpositions.gateways);
  Context.put('endpoints', allFqdns.map(
    fqdn => formatEndpoint(fqdn, exposition.organizationId, exposition.service.name, exposition.service.version)
  ));
  
  Logger.bold('Gateway Endpoints');
  activeExpositions.gateways.forEach((gateway: { id: string; name: string; fqdns: string[]; }) => {
    Logger.log(`  - ID       : ${gateway.id}`);
    Logger.log(`    Name     : ${gateway.name}`);
    Logger.log(`    Endpoints: ${gateway.fqdns.map(
      (fqdn: string) => formatEndpoint(fqdn, exposition.organizationId, exposition.service.name, exposition.service.version))
    .join(', ')}`);
  });
}