#! /bin/sh
if [ "$(uname -s)" = 'Darwin' ]; then
SCRIPT=$(readlink "$0")
else
SCRIPT=$(readlink -f "$0")
fi
CURRDIR=$(dirname "$SCRIPT")
"$CURRDIR/../bin/agent.sh" --development
