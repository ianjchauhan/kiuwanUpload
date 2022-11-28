#! /bin/sh
SCRIPT=$(readlink "$0")
CURRDIR=$(dirname "$SCRIPT")
"$CURRDIR/agent.sh" allowpause --userInput
