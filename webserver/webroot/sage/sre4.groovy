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
import sagex.api.*
import com.google.code.sagetvaddons.sre.engine.DataStore
import com.google.code.sagetvaddons.sre.plugin.SrePlugin
import net.sf.sageplugins.webserver.Version
import com.google.code.livepvrdata4j.Client

def regex = ~/(?:.*: )*([A-Z].*) (?:at|vs\.?) ([A-Z].*)/

switch(params['a']) {
        case 'edit':
                request.setAttribute('pageTitle', 'SREv4 Override Editor')
                def airing = AiringAPI.GetAiringForID(params['id'].toInteger())
                request.setAttribute('airing', airing)
                if(AiringAPI.GetAiringTitle(airing).startsWith('College'))
                        request.setAttribute('global', 'true')
                def matcher = regex.matcher(ShowAPI.GetShowEpisode(airing))
                if(matcher.matches()) {
                        request.setAttribute('team1', matcher[0][1])
                        request.setAttribute('team2', matcher[0][2])
                }
                request.getRequestDispatcher('sre4_edit.gsp').forward(request, response)
                break
        case 'update':
                def ds = DataStore.getInstance()
				ds.newOverrideForObj(AiringAPI.GetAiringForID(params['id'].toInteger()), params['title'], "${params['team1_new']} vs. ${params['team2_new']}", true)
				if(params['global']) {
					def email = Configuration.GetServerProperty(SrePlugin.PROP_EMAIL, null)
					if(email) {
						def clnt = new Client("SageTV_WebUI/${Version.VERSION}", null)
						if(params['team1_old'] != params['team1_new'])
							clnt.addGlobalOverride(params['team1_old'], params['team1_new'], email)
						if(params['team2_old'] != params['team2_new'])
							clnt.addGlobalOverride(params['team2_old'], params['team2_new'], email)
					}
				}
				response.sendRedirect("DetailedInfo?AiringId=${params['id']}")
				break
		case 'delete':
				def ds = DataStore.getInstance()
				ds.deleteOverrideByObj(AiringAPI.GetAiringForID(params['id'].toInteger()))
				response.sendRedirect("DetailedInfo?AiringId=${params['id']}")
				break
		default:
				response.sendError(400, "Invalid action! [${params['a']}]")
}