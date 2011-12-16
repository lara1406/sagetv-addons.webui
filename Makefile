

JARS=../sageutils/bin/JARs/nielm_sageutls.jar \
     ../sagexmlinfo/bin/JARs/nielm_sagexmlinfo.jar \
     bin/JARs/nielm_sagewebserver.jar

VERSION=$(shell awk '/VERSION=/ { sub("[^\"]*\"","");sub("\".*",""); gsub("[.]","_") ; print } {} ' net/sf/sageplugins/webserver/Version.java)




all: bin/SageWebserverSetup_${VERSION}.exe bin/webserver_${VERSION}.zip




clean:
	rm -f 	bin/SageWebserverSetup.exe \
	 	bin/SageWebserverSetup_*.exe \
		bin/webserver_*.zip \
		bin/webserver_*_tmp.zip




bin/SageWebserverSetup_${VERSION}.exe: ${JARS}
	cd nsis && /cygdrive/c/Program\ Files/NSIS/makensis.exe SetupWebserver.nsi
	mv bin/SageWebserverSetup.exe bin/SageWebserverSetup_${VERSION}.exe




bin/webserver_${VERSION}.zip: ${JARS}
	zip -r bin/webserver_${VERSION}_tmp.zip \
		bin/JARs/nielm_sagewebserver.jar \
		bin/JARs/servlet.jar \
		webserver
	cd ../sageutils/bin && zip ../../webserver/bin/webserver_${VERSION}_tmp.zip \
		JARs/nielm_sageutls.jar
	cd ../sagexmlinfo/bin && zip ../../webserver/bin/webserver_${VERSION}_tmp.zip \
		JARs/nielm_sagexmlinfo.jar 
	cd ../sagexmlinfo && zip ../webserver/bin/webserver_${VERSION}_tmp.zip \
		sageshowinfo.dtd
	mv bin/webserver_${VERSION}_tmp.zip bin/webserver_${VERSION}.zip



deliver: bin/webserver_${VERSION}.zip bin/SageWebserverSetup_${VERSION}.exe
	ncftpput upload.sourceforge.net incoming \
		bin/webserver_${VERSION}.zip \
		bin/SageWebserverSetup_${VERSION}.exe
	cygstart 'http://sourceforge.net/project/admin/newrelease.php?package_id=153357&group_id=108108'
	cygstart 'http://tools.assembla.com/sageplugins/wiki/WebServer'
	cygstart 'http://forums.sagetv.com/forums/showthread.php?t=8426'
