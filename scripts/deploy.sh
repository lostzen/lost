#!/usr/bin/env bash
#
# triggers circle build with parameters to trigger a release
#

if [ -z ${CIRCLE_TOKEN} ]
  then
    echo "[ERROR] CIRCLE_TOKEN environment variable is not set."
    exit 1
fi

if [ $# -ne 2 ]; then
    echo "[ERROR] release and new version numbers not specified. Exiting."
    exit 1
fi

trigger_build_url=https://circleci.com/api/v1/project/mapzen/lost/tree/master?circle-token=${CIRCLE_TOKEN}

post_data=$(cat <<EOF
{
  "build_parameters": {
    "RELEASE_VERSION_NUMBER": "$1",
    "NEW_VERSION_NUMBER": "$2"
  }
}
EOF)

curl \
--header "Accept: application/json" \
--header "Content-Type: application/json" \
--data "${post_data}" \
--request POST ${trigger_build_url}

echo
