# Build stage: use Node.js to build the SvelteKit app
# Build context: monorepo root (see .github/workflows/build-containers.yml)
FROM node:22-alpine AS build

WORKDIR /app/web-ui

# Install dependencies first (layer caching)
COPY web-ui/package.json web-ui/package-lock.json* ./
RUN npm ci

# Control-plane JSON Schemas — single source of truth (not committed under web-ui)
COPY control-plane/src/main/resources/schemas /schemas-source
ENV SCHEMA_SOURCE=/schemas-source

# Copy web-ui sources and build (prebuild runs copy-schemas)
COPY web-ui .
RUN npm run build

# Prune dev dependencies for a leaner production image
RUN npm prune --omit=dev \
    && rm -rf /app/web-ui/node_modules/.vite \
    && rm -rf /app/web-ui/node_modules/@hugeicons \
    && rm -rf /app/web-ui/node_modules/@rolldown

# -----------------------------------------------------------
# Production stage: Red Hat UBI9 minimal
# -----------------------------------------------------------
FROM registry.access.redhat.com/ubi9/ubi-minimal:9.8-1780378819

# Install Node.js 22 via microdnf
RUN echo -e "[nodejs]\nname=nodejs\nstream=22\nprofiles=\nstate=enabled" > /etc/dnf/modules.d/nodejs.module \
    && microdnf install --setopt install_weak_deps=0 nodejs -y \
    && microdnf clean all \
    && rm /var/lib/rpm/rpmdb.sqlite \
    && rm -rf /mnt/rootfs/var/cache/* /mnt/rootfs/var/log/dnf* /mnt/rootfs/var/log/yum.*

WORKDIR /app

# Copy the built application and production node_modules
COPY --from=build /app/web-ui/build ./build
COPY --from=build /app/web-ui/node_modules ./node_modules
COPY --from=build /app/web-ui/package.json ./

# SvelteKit adapter-node configuration
ENV NODE_ENV=production
ENV PORT=3333
ENV HOST=0.0.0.0

EXPOSE 3333

# Run the SvelteKit server
CMD ["node", "build"]
