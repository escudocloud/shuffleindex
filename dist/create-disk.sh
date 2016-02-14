#!/bin/sh

rm -f disk/disk_conf.xml
rm -f disk/disk?
ln -s disk_conf_large.xml disk/disk_conf.xml

exec java -cp pir.jar base.commandline.CreateCustomBptree 1
