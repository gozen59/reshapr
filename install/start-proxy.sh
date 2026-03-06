#!/bin/bash

docker run -it --rm -p 7777:7777 -e RESHAPR_CTRL_HOST=host.docker.internal \
  --add-host=host.docker.internal:host-gateway \
  quay.io/reshapr/reshapr-proxy:nightly