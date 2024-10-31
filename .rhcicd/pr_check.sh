#!/bin/bash

set -exv

# Clowder config
export APP_NAME="policies"
export COMPONENT_NAME="policies-engine"
export IMAGE="quay.io/cloudservices/policies-engine"
export DEPLOY_TIMEOUT="600"

# Temporary deploy template fix of policies-ui-backend [RHINENG-6805]
EXTRA_DEPLOY_ARGS="--set-template-ref policies-ui-backend=6a9c59d80231a36ba6e87af5e8be28158bbe2d64
                   --set-image-tag quay.io/cloudservices/policies-ui-backend=6a9c59d"

# IQE plugin config
export IQE_PLUGINS="policies"
export IQE_MARKER_EXPRESSION="policies_api_smoke"
export IQE_FILTER_EXPRESSION=""
export IQE_CJI_TIMEOUT="30m"

# Bonfire init
CICD_SCRIPT_URL=https://raw.githubusercontent.com/RedHatInsights/cicd-tools/main/bootstrap.sh
curl -sL $CICD_SCRIPT_URL > .cicd_bootstrap.sh && source .cicd_bootstrap.sh

# Build the image and push to Quay
export DOCKERFILE=src/main/docker/Dockerfile-build.jvm
source $CICD_ROOT/build.sh

# Deploy on ephemeral
export COMPONENTS_W_RESOURCES="policies-engine"
source $CICD_ROOT/deploy_ephemeral_env.sh

# Run smoke tests with ClowdJobInvocation
source $CICD_ROOT/cji_smoke_test.sh

# Until test results produce a junit XML file, create a dummy result file so Jenkins will pass
mkdir -p $WORKSPACE/artifacts
cat << EOF > ${WORKSPACE}/artifacts/junit-dummy.xml
<testsuite tests="1">
    <testcase classname="dummy" name="dummytest"/>
</testsuite>
EOF
