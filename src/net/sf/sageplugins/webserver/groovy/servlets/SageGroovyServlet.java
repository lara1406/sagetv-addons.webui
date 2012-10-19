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
import groovy.util.GroovyScriptEngine;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

public final class SageGroovyServlet extends GroovyServlet {

	static private final long serialVersionUID = 1L;
	
	@Override
	protected GroovyScriptEngine createGroovyScriptEngine() {
		File warRoot = new File(getServletContext().getRealPath("/"));
		String[] roots = new String[] {
			warRoot.getAbsolutePath(),
			new File(warRoot, "/WEB-INF/groovy").getAbsolutePath(),
			new File("webserver/groovy").getAbsolutePath()
		};
		try {
			return new GroovyScriptEngine(roots);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected String getScriptUri(HttpServletRequest req) {
		return req.getServletPath().substring(1);
	}

	@Override
	protected File getScriptUriAsFile(HttpServletRequest req) {
		File warRoot = new File(getServletContext().getRealPath("/"));
		String scriptUri = getScriptUri(req);
		for(File root : new File[] { warRoot, new File(warRoot, "/WEB-INF/groovy"), new File("webserver/groovy") }) {
			File src = new File(root, scriptUri);
			if(src.exists())
				return src;
		}
		return null;
	}
}

