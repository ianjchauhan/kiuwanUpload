#! /bin/sh
SCRIPT=$(readlink "$0")
CURRDIR=$(dirname "$SCRIPT")
"$CURRDIR/bin/agent.sh" allowpause --gui
