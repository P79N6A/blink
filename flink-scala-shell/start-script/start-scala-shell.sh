#!/bin/bash
################################################################################
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
# limitations under the License.
################################################################################

# from scala-lang 2.10.4

# restore stty settings (echo in particular)
function restoreSttySettings() {
  if [[ -n $SCALA_RUNNER_DEBUG ]]; then
    echo "restoring stty:"
    echo "$saved_stty"
  fi
  stty $saved_stty
  saved_stty=""
}

function onExit() {
  [[ "$saved_stty" != "" ]] && restoreSttySettings
  exit $scala_exit_status
}


# to reenable echo if we are interrupted before completing.
trap onExit INT
# save terminal settings
saved_stty=$(stty -g 2>/dev/null)
# clear on error so we don't later try to restore them
if [[ ! $? ]]; then
  saved_stty=""
fi


bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin"/config.sh

FLINK_CLASSPATH=`constructFlinkClassPath`

# Append flink-table jar into class path
opt=`dirname "$0"`
opt=`cd "$opt";cd ../opt;pwd`
FLINK_TABLE_LIB_PATH=$opt/`ls $opt|grep flink-table_*`
FLINK_CLASSPATH=$FLINK_CLASSPATH:$FLINK_TABLE_LIB_PATH


# https://issues.scala-lang.org/browse/SI-6502, cant load external jars interactively
# in scala shell since 2.10, has to be done at startup
# checks arguments for additional classpath and adds it to the "standard classpath"

EXTERNAL_LIB_FOUND=false
for ((i=1;i<=$#;i++))
do
    if [[  ${!i} = "-a" || ${!i} = "--addclasspath" ]]
    then
	EXTERNAL_LIB_FOUND=true
	
        #adding to classpath
        k=$((i+1))
        j=$((k+1))
        echo " "
        echo "Additional classpath:${!k}"
        echo " "
        EXT_CLASSPATH="${!k}"
        FLINK_CLASSPATH="$FLINK_CLASSPATH:${!k}"
        set -- "${@:1:$((i-1))}" "${@:j}"
    fi
done

log_setting=""

if [[ $1 = "yarn" ]]
then
FLINK_CLASSPATH=$FLINK_CLASSPATH:$HADOOP_CLASSPATH:$HADOOP_CONF_DIR:$YARN_CONF_DIR
log=$FLINK_LOG_DIR/flink-$FLINK_IDENT_STRING-scala-shell-yarn-$HOSTNAME.log
log_setting="-Dlog.file="$log" -Dlog4j.configuration=file:"$FLINK_CONF_DIR"/log4j-yarn-session.properties -Dlogback.configurationFile=file:"$FLINK_CONF_DIR"/logback-yarn.xml"
fi

if ${EXTERNAL_LIB_FOUND}
then
    java -Dscala.color -cp "$FLINK_CLASSPATH" $log_setting org.apache.flink.api.scala.FlinkShell $@ --addclasspath "$EXT_CLASSPATH"
else
    java -Dscala.color -cp "$FLINK_CLASSPATH" $log_setting org.apache.flink.api.scala.FlinkShell $@
fi

#restore echo
onExit
