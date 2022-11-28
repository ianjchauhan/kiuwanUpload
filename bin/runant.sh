# -----------------------------------------
# OS specific support (MacOS a.k.a. Darwin)
# -----------------------------------------
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

# ------------------------
# Set launch configuration
# ------------------------
JAVA_CP="$KIUWAN_HOME/lib/ant-launcher.jar:$KIUWAN_HOME/lib/ant.jar"
JAVA_JKQA_HOME="-DJKQA_HOME=$KIUWAN_HOME"
JAVA_AGENT_HOME="-DAGENT_HOME=$KIUWAN_HOME"
JAVA_TASK_LOG="-DTASK.LOG=$KIUWAN_HOME/temp/agent.log"
JAVA_OPTS="-showversion"

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

$JAVACMD -cp $JAVA_CP $JAVA_JKQA_HOME $JAVA_AGENT_HOME $JAVA_TASK_LOG $JAVA_OPTS org.apache.tools.ant.launch.Launcher "$@"

RET_CODE=$?
if [ ! $RET_CODE = "0" ] ; then
  echo
  echo "Error executing runant.sh script"
  echo
fi

exit $RET_CODE
