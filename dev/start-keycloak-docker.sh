docker run -it --rm -v $(pwd):/opt/keycloak/data/import -p 8888:8080 \
  -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:26.3.0 start-dev --hostname http://localhost:8888 --import-realm
