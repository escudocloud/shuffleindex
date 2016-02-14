#!/bin/sh

rm -rf log

exec java -cp pir.jar:log4j.jar client.test.Main
