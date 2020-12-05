#!/bin/bash --login

# Ex: /home/kca/saveme/git/hong-kong-public-library
PROJECT_DIR_PATH="$(readlink -f "$(dirname "$(dirname "$(dirname "$0")")")")"
source "$PROJECT_DIR_PATH/scripts/crontab/crontab.bash_library" "$PROJECT_DIR_PATH/log" "$@"

main()
{
    crontab_echo_and_run_cmd \
        "$PROJECT_DIR_PATH"/scripts/crontab/wait-for-internet-connection.bash

    crontab_echo_and_run_cmd \
        "$PROJECT_DIR_PATH"/scripts/crontab/renew.bash \
            --chrome-headless --hkpl-web-username "???" --hkpl-web-password "???" \
            --smtp-host "smtp.gmail.com" --smtp-port 587 --email-address "???@gmail.com" \
            --smtp-username "???@gmail.com" --smtp-password "???"
}

main "$@"
