#!/bin/bash
version=`java -jar JARs/nielm_sagewebserver.jar| awk '{x=$NF; gsub("[.]","_",x); print x} ' `
if [ ! "$version" ] ; then exit 1; fi
echo building webserver_$version.zip
read -p "Press return to continue" inp
zip -r webserver_$version.zip JARs/* webserver/*
read -p "Press return to upload  webserver_$version.zip to SF" inp
ncftpput  upload.sourceforge.net incoming webserver_$version.zip 
cygstart "http://sourceforge.net/project/admin/newrelease.php?package_id=153357&group_id=108108"
