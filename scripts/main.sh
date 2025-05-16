#!/bin/sh

# This script should not be called directly, use beaver-seq.sh instead

usage() {
  cat <<EOF | xargs -0 printf '%b'

$(colorize 'NAME' 0 1)
  $0

$(colorize 'USAGE' 0 1)
  $0 [OPTIONS] <COMMAND> <OPTION...> <ARG...>

$(colorize 'COMMANDS' 0 1)
  start         Start the server in background.
  stop          Stop the server.
  run           Run the server in the current terminal.
  debug         Run application in debug mode.

$(colorize 'OPTIONS' 0 1)
  --jvm-opts    JVM options
  -h, --help    Show usage

EOF
  exit 0
}

parse_args() {
  # default values of options
  # built-in variables
  options_count=0
  args_count=0
  args=''

  # define options
  jvm_opts=''

  while :; do
    case "${1-}" in
    -h | --help)
      usage
      ;;

    # parse options
    --jvm-opts)
      jvm_opts="${2-}"
      options_count=$((options_count + 1))
      shift
      ;;

    -?*)
      usage
      ;;

    *)
      if [ -z "$args" ]; then
         args="${1-}"
       else
         args="${args} ${1-}"
       fi

      if [ -z "${1-}" ]; then
        break
      fi
      ;;
    esac

    if [ -n "${1-}" ]; then
      shift
    fi
  done

  # init args
  command=''
  app_args=''

  # parse args
  for arg in $args; do
    if [ -n "$arg" ]; then
      args_count=$((args_count + 1))

      # define positional arguments here
      if [ "$args_count" -eq 1 ]; then
        command="$arg"
      else
        app_args="${app_args} $arg"
      fi
    fi
  done

  if [ $options_count -eq 0 ] && [ $args_count -eq 0 ]; then
    usage
  fi

  # validate args
  if [ -z "$command" ]; then
    error 'command is required'
    quit 1
  fi
}

LOGS_DIR="$APP_HOME/logs"
main() {
  parse_args "$@"

  # Force to application home for relative directory resolve
  work_dir=$(pwd -P)
  if [ "$KTOR_HOME" != "$work_dir" ]; then
    error "Please go application home($KTOR_HOME) to run it."
    quit 1
  fi
  if [ ! -d "$LOGS_DIR" ]; then
    mkdir $LOGS_DIR
    if [ $? -gt 0 ]; then
      error "Could not create $LOGS_DIR, make sure you have the writable permission in application home."
      quit 1
    fi
  fi

  if [ "$command" = "run" ] || [ "$command" = "start" ]; then
    if $hava_tty; then
      info "Using JAVA_HOME: $JAVA_HOME"
      info "Using JAVA_OPTS: $JAVA_OPTS"
      info "Using MAIN_CLASS: $MAIN_CLASS"
      info "Using CLASSPATH: $CLASSPATH"
      info "Passed APP_ARGS: $app_args"
      info "Passed JVM_OPTIONS: $jvm_opts"
    fi
  fi

  if [ "$command" = "run" ]; then
    _run 0
  elif [ "$command" = "start" ]; then
    _start
  elif [ "$command" = "stop" ]; then
    _stop
  fi
}

# SIGNALS
# HUP 1
# INT 2 CTRL-C
# QUIT 3 CTRL-\
# TERM 15
trap cleanup HUP INT QUIT TERM

cleanup() {
  info "Received signal"
}

KTOR_OUT="$LOGS_DIR/ktor.out"
_run() {
  if [ -w "$KTOR_OUT" ]; then
    # clear last application output
    cat /dev/null > $KTOR_OUT
    info "Clear last application output."
  fi

  suspend=''
  if [ "$1" -eq 1 ]; then
    suspend='&'
  fi

  eval nohup "\"$RUN_JAVA\"" \
    "$JAVA_OPTS" \
    "$jvm_opts" \
    -classpath "\"$CLASSPATH\"" \
    "$MAIN_CLASS" "$app_args" > $KTOR_OUT 2>&1 "$suspend"
}

KTOR_PID="$LOGS_DIR/ktor.pid"
_start() {
  _run 1
  pid=$!
  if [ -w "$KTOR_PID" ]; then
    cat /dev/null > $KTOR_PID
    info "Clear PID file."
  fi
  echo "$pid" > "$KTOR_PID"
  info "Ktor has started. Process PID is $pid"
}

_stop() {
  if [ ! -r "$KTOR_PID" ]; then
    error "PID file not found."
    quit 1
  fi
  if [ ! -s "$KTOR_PID" ]; then
    error "PID file is empty and has been ignored."
    quit 1
  fi

  pid=$(cat "$KTOR_PID")
  kill -0 $pid > /dev/null 2>&1
  if [ $? -gt 0 ]; then
    error "No matching process was found or the current user does not have permission to stop the process. Stop aborted."
    quit 1
  fi

  kill -s TERM $pid > /dev/null 2>&1
  counter=1
  timeout=10
  while [ $counter -le $timeout ]; do
    kill -0 $pid > /dev/null 2>&1
    if [ $? -gt 0 ]; then
      if [ -w "$KTOR_PID" ]; then
        cat /dev/null > "$KTOR_PID"
      else
        warn "The PID file could not be cleared."
      fi
      info "Ktor shutdown gracefully."
      break # process has already stopped.
    else
      if [ $counter -gt 1 ]; then
        info "waiting $counter seconds"
      else
        info "waiting $counter second"
      fi
      sleep 1
    fi

    if [ $counter -eq $timeout ]; then
      error "Ktor did not stop in time. "
      error "To aid diagnostics a thread dump has been written to standard out."
      kill -s QUIT $pid
      quit 1
    fi

    counter=$((counter + 1))
  done
}

main "$@"
