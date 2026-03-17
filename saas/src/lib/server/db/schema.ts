import { pgTable, uuid, varchar, boolean, timestamp, uniqueIndex } from 'drizzle-orm/pg-core';

export const users = pgTable('users', {
  id: uuid('id').primaryKey().defaultRandom(),
  email: varchar('email', { length: 255 }).notNull(),
  username: varchar('username', { length: 255 }).notNull(),
  provider: varchar('provider', { length: 50 }).notNull(),
  providerId: varchar('provider_id', { length: 255 }).notNull(),
  displayName: varchar('display_name', { length: 255 }),
  avatarUrl: varchar('avatar_url', { length: 500 }),
  organizationName: varchar('organization_name', { length: 255 }),
  createdAt: timestamp('created_at').defaultNow().notNull(),
  ctrlProvisioned: boolean('ctrl_provisioned').default(false).notNull()
}, (table) => [
  uniqueIndex('uq_provider_provider_id').on(table.provider, table.providerId)
]);

