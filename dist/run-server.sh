#!/bin/sh

rm -f server.pid

./jsvc -cp pir.jar:commons-daemon.jar -pidfile server.pid -nodetach server.main.DaemonServer
echo "Shutting down Server... "
./jsvc -cp pir.jar:commons-daemon.jar -pidfile server.pid -stop server.main.DaemonServer
echo "Server process terminated!"
