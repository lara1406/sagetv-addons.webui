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
		return isServerPluginInstalled(id, null);
	}
	
	static public boolean isServerPluginInstalled(String id, String minVerRegex) {
		if(id == null) return false;
		return isPluginInstalled(PluginAPI.GetInstalledPlugins(), id, minVerRegex);
	}
	
	static public boolean isClientPluginInstalled(String id) {
		return isClientPluginInstalled(id, null);
	}
	
	static public boolean isClientPluginInstalled(String id, String minVerRegex) {
		if(id == null) return false;
		return isPluginInstalled(PluginAPI.GetInstalledClientPlugins(), id, minVerRegex);
	}
	
	static private boolean isPluginInstalled(Object[] list, String id, String minVerRegex) {
		if(id == null) return false;
		for(Object p : list)
			if(PluginAPI.GetPluginIdentifier(p).toLowerCase().equals(id.toLowerCase()))
				return minVerRegex != null && minVerRegex.length() > 0 ? PluginAPI.GetPluginVersion(p).matches(minVerRegex) : true;
		return false;
	}
	
	private PluginUtils() {}
}
