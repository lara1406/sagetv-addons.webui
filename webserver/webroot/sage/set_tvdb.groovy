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
import javax.servlet.http.HttpServletResponse

import net.sf.sageplugins.webserver.mc2xmlepg.Mc2XmlEpgUtils
import sagex.api.*

if((!params['mf'] && !params['a']) || !params['tvdb']) {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, 'MediaFile or Airing and a tvdb id must be specified!')
		return
}

def src
if(params['mf'])
		src = MediaFileAPI.GetMediaFileForID(params['mf'].toInteger())
else
		src = AiringAPI.GetAiringForID(params['a'].toInteger())

if(!src) {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, 'Object id is invalid!')
		return
}
Mc2XmlEpgUtils.setTvdbId(src, params['tvdb'].toInteger())
response.sendRedirect("DetailedInfo?AiringId=${AiringAPI.GetAiringID(src)}")