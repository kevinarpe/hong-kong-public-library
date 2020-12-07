#!/bin/bash --login

# Ex: /home/kca/saveme/git/hong-kong-public-library
PROJECT_DIR_PATH="$(readlink -f "$(dirname "$(dirname "$(dirname "$0")")")")"
source "$PROJECT_DIR_PATH/scripts/bashlib" "$PROJECT_DIR_PATH/log" "$@"

main()
{
    bashlib_echo_and_run_cmd \
        "$PROJECT_DIR_PATH"/scripts/await_internet_connection.bash

    bashlib_echo_and_run_cmd \
        "$PROJECT_DIR_PATH"/scripts/renew.bash \
            --chrome-headless \
            --hkpl-web-username "???" \
            --hkpl-web-password "???" \
            --smtp-host "smtp.gmail.com" \
            --smtp-port 587 \
            --email-address "???@gmail.com" \
            --smtp-username "???@gmail.com" \
            --smtp-password "???"
}

main "$@"
