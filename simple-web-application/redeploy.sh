#!/usr/bin/env bash

set -e -x

APP=simple-web-app

mvn clean package -DskipTests=true
oc start-build ${APP} --from-dir=. --follow
