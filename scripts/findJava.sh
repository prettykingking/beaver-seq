#!/bin/sh

# Make sure prerequisite environment variables are set
if [ -z "$JAVA_HOME" ]; then
  if $darwin; then
    if [ -x '/usr/libexec/java_home' ]; then
      JAVA_HOME=$(/usr/libexec/java_home)
    fi
  else
    JAVA_PATH=$(command -v java 2>/dev/null)
    if [ "x$JAVA_PATH" != "x" ]; then
     JAVA_PATH=$(dirname "$JAVA_PATH" 2>/dev/null)
     JAVA_HOME=$(dirname "$JAVA_PATH" 2>/dev/null)
    fi
  fi

  if [ -n "$JRE_HOME" ]; then
    JAVA_HOME="$JRE_HOME"
  fi

  if [ -z "$JAVA_HOME" ]; then
    error 'Neither the JAVA_HOME nor the JRE_HOME environment variable is defined'
    error 'At least one of these environment variable is needed to run this program'
    quit 1
  fi
fi

# If we're running under jdb, we need a full jdk.
if [ "$1" = "debug" ]; then
  if [ ! -x "${JAVA_HOME}/bin/jdb" ] || [ ! -x "${JAVA_HOME}/bin/javac" ]; then
    error 'The JAVA_HOME environment variable is not defined correctly'
    error 'This environment variable is needed to run this program'
    error 'NB: JAVA_HOME should point to a JDK not a JRE'
    quit 1
  fi
fi

# Set standard commands for invoking Java, if not already set.
if [ -z "$RUN_JAVA" ]; then
  RUN_JAVA="${JAVA_HOME}/bin/java"
fi

if [ -z "$RUN_JDB" ]; then
  RUN_JDB="${RUN_JDB}/bin/jdb"
fi
