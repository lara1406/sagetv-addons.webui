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
package net.sf.sageplugins.webserver.mc2xmlepg;

import java.io.PrintWriter;

import sagex.api.AiringAPI;
import sagex.api.SeriesInfoAPI;
import sagex.api.ShowAPI;
import sagex.api.UserRecordAPI;

public class Mc2XmlEpgUtils {
	static private final String STORE_NAME = "mc2xmlepg";
	static private final String STORE_KEY  = "tvdb";

	static public void writeInputForm(PrintWriter w, Object airing) {
		String extId = ShowAPI.GetShowExternalID(airing);
		if(!extId.startsWith("EP")) return;
		int tvdbId = getTvdbId(airing);
		w.println(String.format("<p>TVDB ID: <form method=\"POST\" action=\"groovy/set_tvdb.groovy\" enctype=\"application/x-www-form-urlencoded\"><input type=\"hidden\" name=\"a\" value=\"%d\"/><input type=\"text\" size=\"2\" value=\"%d\" name=\"tvdb\"/><input type=\"submit\" name=\"submit\" value=\"Edit\"/></form></p>", AiringAPI.GetAiringID(airing), tvdbId));
	}
	
	static public int getTvdbId(Object airing) {
		String extId = ShowAPI.GetShowExternalID(airing);
		if(!extId.startsWith("EP")) return 0;
		int tvdbId = 0;
		Object info = ShowAPI.GetShowSeriesInfo(airing);
		if(info == null) {
			Object record = UserRecordAPI.GetUserRecord(STORE_NAME, STORE_KEY);
			String seriesId = Integer.toString(Integer.parseInt(extId.substring(2, extId.length() - 4)));
			String val = UserRecordAPI.GetUserRecordData(record, seriesId);
			if(val != null && val.length() > 0)
				tvdbId = Integer.parseInt(val);
		} else {
			String val = SeriesInfoAPI.GetSeriesInfoProperty(info, STORE_KEY);
			if(val != null && val.length() > 0)
				tvdbId = Integer.parseInt(val);
		}
		return tvdbId;
	}
	
	static public void setTvdbId(Object airing, int id) {
		String extId = ShowAPI.GetShowExternalID(airing);
		if(!extId.startsWith("EP")) return;
		Object info = ShowAPI.GetShowSeriesInfo(airing);
		if(info == null) {
			Object record = UserRecordAPI.AddUserRecord(STORE_NAME, STORE_KEY);
			String seriesId = Integer.toString(Integer.parseInt(extId.substring(2, extId.length() - 4)));
			UserRecordAPI.SetUserRecordData(record, seriesId, Integer.toString(id));
		} else
			SeriesInfoAPI.SetSeriesInfoProperty(info, STORE_KEY, Integer.toString(id));
	}

	private Mc2XmlEpgUtils() {}
}
