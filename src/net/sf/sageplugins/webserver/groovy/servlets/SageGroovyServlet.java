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
package net.sf.sageplugins.webserver.groovy.servlets;

import groovy.servlet.GroovyServlet;
import groovy.util.ResourceException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public final class SageGroovyServlet extends GroovyServlet {

	static private final long serialVersionUID = 1L;
	
	// Majority of this code copied from Groovy 1.8.4 src; slight mods to support purpose
	@Override
	public URLConnection getResourceConnection(String name) throws ResourceException {        
		try {
			URLConnection c = super.getResourceConnection(name);
			return c;
		} catch(ResourceException e) {
	        while (name.startsWith(ServletHelpers.SCRIPT_DIR.getAbsolutePath())) name = name.substring(ServletHelpers.SCRIPT_DIR.getAbsolutePath().length());
	        name = name.replaceAll("\\\\", "/");

	        //remove the leading / as we are trying with a leading / now
	        if (name.startsWith("/")) name = name.substring(1);
	        
			File script = new File(ServletHelpers.SCRIPT_DIR, name);
			if(script.canRead()) {
				try {
					URL url = new URL("file", "", script.getAbsolutePath());
					return url.openConnection();
				} catch(IOException x) {
					throw new ResourceException("IOError", x);
				}
			} else
				throw new ResourceException(String.format("Script not found! [%s]", script.getAbsolutePath()));
		}
	}
}
