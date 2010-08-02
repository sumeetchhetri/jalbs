#!/bin/sh

BASEDIR=`dirname $0`
export CLASSPATH=${BASEDIR}/../lib/jalbs.jar:${BASEDIR}/../lib/xstream-1.3.1.jar
java -Ddebugflag=false com.cpfj.JBSLProcessor $*