#! /bin/sh

echo
echo
echo "   #       #"
echo "   #       #"
echo "   #"
echo "   #    #  #   #    #  #          #   ###    # ###"
echo "   #   #   #   #    #   #   ##   ##  #   #   ###  #"
echo "   #  #    #   #    #   #   ##   #       #   #    #"
echo "   ####    #   #    #   #  ## #  #    ####   #    #"
echo "   #  #    #   #    #    # #  # #    #   #   #    #"
echo "   #  ##   #   #    #    # #  # #   #    #   #    #"
echo "   #   #   #   #    #    ##    ##   #    #   #    #"
echo "   #    #  #   ######     #    #     #####   #    #"
echo
echo "                                   www.kiuwan.com"
echo

# --------------------
# OSX specific support
# --------------------
darwin=false;
case "`uname`" in
  Darwin*)
    darwin=true
    if [ -z "$JAVA_HOME" ] ; then
      if [ -f "/usr/libexec/java_home" ] ; then
        JAVA_HOME=$(/usr/libexec/java_home)
      else 
        JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Home
      fi
    fi
    ;;
esac

# ------------------------------
# Infer agent home (KIUWAN_HOME)
# ------------------------------
if [ -z "$KIUWAN_HOME" -o ! -d "$KIUWAN_HOME" ] ; then
  ## resolve links - $0 may be a link to agent home
  PRG="$0"
  progname=`basename "$0"`

  # need this for relative symlinks
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
    else
    PRG=`dirname "$PRG"`"/$link"
    fi
  done

  KIUWAN_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  KIUWAN_HOME=`cd "$KIUWAN_HOME" && pwd`
fi

# ---------------------
# Infer java executable
# ---------------------
if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    # IBM's JDK on AIX uses strange locations for the executables
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      JAVACMD="$JAVA_HOME/jre/sh/java"
    elif [ -x "$JAVA_HOME/jre/bin/java" ] ; then
      JAVACMD="$JAVA_HOME/jre/bin/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
      JAVACMD=java
    fi
  fi
fi

# ---------------------
# Check java executable
# ---------------------
if [ ! -x "$JAVACMD" ] ; then
  echo "Java executable not found. Kiuwan Local Analyzer needs JDK 1.8 or higher to run."
  echo
  echo "You may download Java from http://www.oracle.com/technetwork/java/javase/downloads"
  echo
  exit 1
fi

# ------------------------
# Set launch configuration
# ------------------------
JAVA_CP="$KIUWAN_HOME/lib/*:$KIUWAN_HOME/conf/:$KIUWAN_HOME/lib.custom/*"
JAVA_JKQA_HOME="-DJKQA_HOME=$KIUWAN_HOME"
JAVA_AGENT_HOME="-DAGENT_HOME=$KIUWAN_HOME"
JAVA_TASK_LOG="-DTASK.LOG=$KIUWAN_HOME/temp/agent.log"
JAVA_JAVA_CMD="-DJAVACMD=$JAVACMD"
JAVA_OPTS="-showversion"

# ----------------------
# Create upgrade command
# ----------------------
upgrade() {
  "${JAVACMD}" "${JAVA_AGENT_HOME}" "${JAVA_JAVA_CMD}" "${JAVA_OPTS}" -jar "$KIUWAN_HOME/lib/upgrade.jar"
}

# -----------------
# Automatic upgrade
# -----------------
case "$@" in
  *"--clean"*)
    echo "Upgrade bypassed (--clean argument detected)"
    ;;
  *)
    upgrade                                                                                                                                                   
    RET_CODE=$?
    if [ ! $RET_CODE = 0 ] ; then
      echo
      echo "Error: Local analyzer upgrade failed. Check Internet connection from your computer."
      echo "If under a web proxy, check proxy configuration."
      echo
      echo "Alternatively, you may perform a manual analyzer upgrade if necessary,"
      echo "following the instructions in kiuwan website."
      echo
      echo "Check the Kiuwan Local Analyzer troubleshooting guide for instructions on fixing this problem:"
      echo "https://www.kiuwan.com/docs/display/K5/Troubleshooting"
      echo
      if [ "$1" = "allowpause" ] ; then
        echo "Press any key to continue..."
        read _
      fi
    fi
    if [ -f "$KIUWAN_HOME/lib/upgrade.jar.new" ] ; then
      mv -f "$KIUWAN_HOME/lib/upgrade.jar.new" "$KIUWAN_HOME/lib/upgrade.jar"
    fi
    ;;
esac

# ------------------
# Create run command
# ------------------
run() {
  "${JAVACMD}" "${JAVA_AGENT_HOME}" "${JAVA_JKQA_HOME}" "$JAVA_TASK_LOG" "${JAVA_OPTS}" -cp "${JAVA_CP}" com.optimyth.qaking.agent.analyzer.ConsoleLauncher "$@"
}

# ---
# Run
# ---
echo "Launching...."
run "$@"
RET_CODE=$?
if [ ! $RET_CODE = 0 ]  && ! [ $RET_CODE -ge 10 -a $RET_CODE -le 50 ] ; then
  echo
  echo "Check the Kiuwan Local Analyzer troubleshooting guide for instructions on fixing this problem:"
  echo "https://www.kiuwan.com/docs/display/K5/Troubleshooting"
  echo
  if [ "$1" = "allowpause" ] ; then
    echo "Press any key to continue..."
    read _
  fi
fi
exit $RET_CODE
