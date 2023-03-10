#!/bin/sh

set -e

SERVICE_NAME=ap-backend

rm config/application-local.yml 2> /dev/null || true

aws ssm get-parameters-by-path --with-decryption --path "/dev/$SERVICE_NAME/ecs_service/" \
    | jq ".Parameters[] | (.Name | match(\"/dev/$SERVICE_NAME/ecs_service/(.*)__.*\").captures[0].string) + \"=\" + .Value" \
    | sed 's/.$//; s/^.//' \
    | tr '"' '\"' \
    > .env.dev

if [ ! -s .env.dev ]; then echo "No SSM parameters fetched, check AWS profile"; exit 1; fi

start_marker="environment_variables_nonsensitive = {"
end_marker="}"

awk "/$start_marker/{flag=1; next} /$end_marker/{flag=0} flag" deploy_fargate/staging/dev/terragrunt.hcl | \
    while read -r line; do
        [[ $line =~ ([^ ]*)[[:blank:]]*=[[:blank:]]*(.*) ]]
        line=${BASH_REMATCH[1]}=${BASH_REMATCH[2]}

        if [[ $line =~ ([^ ]*)[[:blank:]]*=[[:blank:]]*jsonencode\((.*)\) ]]
        then
            result=$(echo "${BASH_REMATCH[1]}=${BASH_REMATCH[2]}" | sed 's/"/\\"/g')
            echo $result >> .env.dev
        else
            if [[ $line =~ (.*)=\"(.*)\"$ ]]
            then
                line="${BASH_REMATCH[1]}=${BASH_REMATCH[2]}"
            fi
            echo $line >> .env.dev
        fi
    done

if [ ! -s .env.overrides ]; then cp .env.overrides.example .env.overrides; echo "Note: you can override environment variables in .env.overrides"; fi
sort -u -t '=' -k 1,1 .env.overrides .env.dev \
    > config/application-local.properties

rm .env.dev
