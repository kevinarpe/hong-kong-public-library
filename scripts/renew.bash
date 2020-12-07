#!/bin/bash --login

# Ex: /home/kca/saveme/git/hong-kong-public-library
PROJECT_DIR_PATH="$(readlink -f "$(dirname "$(dirname "$0")")")"
source "$PROJECT_DIR_PATH/scripts/bashlib" "$PROJECT_DIR_PATH/log" "$@"

JAR_FILE_PATH="$PROJECT_DIR_PATH/target/hong-kong-public-library-1.0.0-SNAPSHOT.jar"

main()
{
    # Command line args stored in "$@"
    check_args "$@"
#
#    bashlib_echo_and_run_cmd \
#        export JAVA_HOME='/home/kca/saveme/jdk-11'
#
#    bashlib_echo_and_run_cmd \
#        ls -l "$JAVA_HOME/bin/java"
#
#    bashlib_echo_and_run_cmd \
#        export PATH="$JAVA_HOME/bin:$PATH"

    bashlib_echo_and_run_cmd \
        which java

    bashlib_echo_and_run_cmd \
        java -version

    bashlib_echo_and_run_cmd \
        cd "$PROJECT_DIR_PATH"

    # Class-Path:
    # 1) $PROJECT_DIR_PATH: Always include this dir, as project may use class resources.  Order is not important.
    # 2) $CLASSES_DIR_PATH: Must appear before $JAR_FILE_PATH b/c IntelliJ will build class files, but not JARs.
    # 3) $JAR_FILE_PATH: Must appear after $CLASSES_DIR_PATH b/c MANIFEST file has Class-Path for dependent JARs.

    bashlib_echo_and_run_cmd \
        java -classpath "$PROJECT_DIR_PATH:$JAR_FILE_PATH" com.github.kevinarpe.hkpl.main.HkplRenewalMain "$@"
}

check_args()
{
    if [ 0 = $# ]
    then
        printf -- '%s\n' "$0"
        printf -- '\n'
        printf -- 'Example args: --chrome-headless --hkpl-web-username "23838017432123" --hkpl-web-password "password123" --smtp-host "smtp.gmail.com" --smtp-port 587 --email-address "kevinarpe@gmail.com" --smtp-username "kevinarpe@gmail.com" --smtp-password "password123"\n'
        printf -- '\n'
        exit 1
    fi
}

main "$@"
