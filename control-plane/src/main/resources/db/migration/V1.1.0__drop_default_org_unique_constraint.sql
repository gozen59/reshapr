-- Remove the unique constraint on default_organization_id in users table to allow multiple users to have the same default organization.
alter table if exists users
   drop constraint if exists UK9y2qaac3upin34bciv2av7e35,
   drop constraint if exists users_default_organization_id_key;

-- Drop the index associated with the unique constraint
drop index if exists users_default_organization_id_key;
