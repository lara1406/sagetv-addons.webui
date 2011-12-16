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
package net.sf.sageplugins.webserver.utils;

import sagex.api.PluginAPI;

public class PluginUtils {

	static public boolean isServerPluginInstalled(String id) {
		if(id == null) return false;
		for(Object p : PluginAPI.GetInstalledPlugins())
			if(PluginAPI.GetPluginIdentifier(p).toLowerCase().equals(id.toLowerCase()))
				return true;
		return false;
	}
	
	private PluginUtils() {}
}
