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
export function ageFrom(value: string): string {
  const d = new Date(value);
  const now = new Date();

  const seconds = Math.round(Math.abs((now.getTime() - d.getTime())/1000));
  const minutes = Math.round(Math.abs(seconds / 60));
  const hours = Math.round(Math.abs(minutes / 60));
  const days = Math.round(Math.abs(hours / 24));
  const months = Math.round(Math.abs(days/30.416));
  const years = Math.round(Math.abs(days/365));

  if (Number.isNaN(seconds)){
    return '';
  } else if (seconds <= 90) {
    return `${seconds}s`;
  } else if (minutes <= 90) {
    return `${minutes}m`;
  } else if (hours <= 23) {
    return `${hours}h`;
  } else if (days <= 29) {
    return `${days}d`;
  } else if (days <= 45) {
    return `${months}m`;
  } else if (days <= 545) {
    return '1y';
  } else { // (days > 545)
    return `${years}y`;
  }
}