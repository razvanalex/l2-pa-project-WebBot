#!/bin/bash

NAME=""
MESSAGE=""
STAGE_NUM=0
FILE="Team_Log"

function help {
    echo -e "Usage: ./addlog.sh [OPTIONS]"
    echo -e "More precisely: ./addlog.sh -n @NAME@ -m 'MESSAGE'"
    echo -e "            or: ./addlog.sh -nS STAGE_NUM"
    echo -e "Add log to $FILE file."
    echo -e ""
    echo -e "Available options:"
    # --name NAME
    echo -e "  -n, --name \t\t Set the name of the contribuitor. This option is"
    echo -e "\t\t\t mandatory. The name has to be between @ (e.g. @Your Name@)."
    echo
    # --message MESSAGE
    echo -e "  -m, --message \t Set the message or the log. In other words, describe"
    echo -e "\t\t\t new things you did. This option is mandatory. The message"
    echo -e "\t\t\t should be marked using quotes, but is not necessary."
    echo -e "\t\t\t Note: All characters after -m (or --message)"
    echo -e "\t\t\t will be considered as the body of log, whether or not"
    echo -e "\t\t\t there is a quote character."
    echo
    # --newStage STAGE_NUM
    echo -e "  -nS, --newStage \t Add a new stage in the log file (e.g. Stage 1)."
}

function addLog {
    if [[ "$STAGE_NUM" != "" ]]; then
        printf "\n#================================================\n" >> $FILE
        printf "#\tEtapa %s\n" "$STAGE_NUM" >> $FILE
        printf "#================================================\n\n" >> $FILE
    fi

    if [[ "$NAME" != "" ]] && [[ "$MESSAGE" != "" ]]; then
        DATE=`date '+%d-%m-%Y %H:%M:%S'`
        printf "%s by %s:\n" "$DATE" "$NAME" >> $FILE
        printf "%s" "$MESSAGE" >> $FILE
        printf "\n-------------------------------------------------\n" >> $FILE
    fi
}

function getMessage {
    fileName=$1
    patterns=$2

    sed -Ei "s/${patterns[1]}/\4/g" $fileName &> /dev/null
    cat $fileName
}

function interpretParams {
    declare -a patterns=(
        ".*(-)?-n(ame)? +@([A-Za-z -]+)@.*"
        "(.*(-)?-m(essage)? +)(.*)"
        ".*(-)?-n(ew)?S(tage)? +([0-9])"
    )

    string="$@"
    NAME=$(echo "$string" | sed -En "s/${patterns[0]}/\3/p" 2> /dev/null)
    printf "%s" "$string" > tmp_addLog
    MESSAGE=$(getMessage "tmp_addLog" $patterns)
    rm -fr tmp_addLog
    STAGE_NUM=$(echo "$string" | sed -En "s/${patterns[2]}/\4/p" 2> /dev/null)

    # write log to file
    addLog
}

function checkArgs {
    declare -a patterns=(
        "^.*(-)?-n(ame)? +@[A-Za-z -]+@.*$"
        "^.*(-)?-m(essage)? +.*$"
        "^.*(-)?-n(ew)?S(tage)? +[0-9]$"
    )

    type=0
    string="$@"

    if [[ $string =~ ${patterns[0]} ]]; then
        type=$type+1
    fi

    if [[ $string =~ ${patterns[1]} ]]; then
        type=$type+1
    fi

    if [[ $type -eq 2 ]]; then      # first type of command
        return 1
    elif [[ $type -ne 0 ]]; then    # there is no type of command
        return 0
    fi

    if [[ $string =~ ${patterns[2]} ]]; then
        type=3;
    fi

    # type two of command
    if [[ $type -eq 3 ]] && [[ $# -eq 2 ]]; then    # first type of command
        return 3
    else    # there is no type of command
        return 0
    fi
}

function main {
    if [[ $# -eq 0 ]]; then
        echo "Cannot execute without any mandatory command."
        echo
        help
    fi

    checkArgs "$@"
    result=$?

    if [[ $result -ne 0 ]]; then
        interpretParams "$@"
    fi
}

main "$@"
