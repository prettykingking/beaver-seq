#!/bin/sh

# See original template
# https://github.com/gradle/gradle/blob/HEAD/platforms/jvm/plugins-application/src/main/resources/org/gradle/api/internal/plugins/unixStartScript.txt

BIN_DIR=\$(dirname \$(readlink -f "\$0"))
KTOR_HOME=\$(dirname "\$BIN_DIR")
APP_HOME='.'
CLASSPATH=\$APP_HOME/resources:$classpath
# Add default JVM options here. You can also use JAVA_OPTS and ${optsEnvironmentVar} to pass JVM options to this script.
DEFAULT_JVM_OPTS=$defaultJvmOpts
DEFAULT_JVM_OPTS="\${DEFAULT_JVM_OPTS%\\"}" # remove rightmost double quote
DEFAULT_JVM_OPTS="\${DEFAULT_JVM_OPTS#\\"}" # remove leftmost double quote
MAIN_CLASS=$mainClassName

# OS specific support (must be 'true' or 'false').
cygwin=false
darwin=false
case \$(uname) in
  Darwin*)
    darwin=true
  ;;
  CYGWIN*)
    cygwin=true
  ;;
esac

# -t fd True if file descriptor fd is open and refers to a terminal.
# 0 stdin, 1 stdout, 2 stderr
is_tty=false
if [ -t 1 ]; then
  is_tty=true
fi

. "\$BIN_DIR"/functions.sh
. "\$BIN_DIR"/findJava.sh
. "\$BIN_DIR"/setenv.sh
. "\$BIN_DIR"/main.sh
