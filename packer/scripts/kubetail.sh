#!/bin/bash

set -e
set -x

wget https://raw.githubusercontent.com/johanhaleby/kubetail/master/kubetail
chmod 755 kubetail
mkdir --parents ${HOME}/bin
mv kubetail ${HOME}/bin
