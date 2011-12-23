/*
*      Copyright 2011 Battams, Derek
*
*       Licensed under the Apache License, Version 2.0 (the "License");
*       you may not use this file except in compliance with the License.
*       You may obtain a copy of the License at
*
*          http://www.apache.org/licenses/LICENSE-2.0
*
*       Unless required by applicable law or agreed to in writing, software
*       distributed under the License is distributed on an "AS IS" BASIS,
*       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*       See the License for the specific language governing permissions and
*       limitations under the License.
*/
package net.sf.sageplugins.webserver;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import sagex.SageAPI;

public class Version {
	static private final String APP_JAR  = (!SageAPI.isRemote() ? "JARs/" : "build/jar/") + "nielm_sagewebserver.jar";
	static private final String VER_ATTR = "Implementation-Version";
	static private final Version INSTANCE = new Version();
	static public final String VERSION = INSTANCE.toString();
	
	private String ver;
	
	private Version() {
		File f = new File(APP_JAR);
		try {
			JarFile jar = new JarFile(f);
			ver = jar.getManifest().getMainAttributes().getValue(VER_ATTR);
		} catch (IOException e) {
			ver = "Unknown";
		}
	}
	
	@Override
	public String toString() { return ver; }
}
