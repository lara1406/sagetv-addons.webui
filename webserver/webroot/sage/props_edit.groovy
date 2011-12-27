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

def target
def props
def method
switch(params['type']) {
	case 'mf':
		target = MediaFileAPI.GetMediaFileForID(params['id'].toInteger())
		props = MediaFileAPI.GetMediaFileMetadataProperties(target)
		method = MediaFileAPI.&SetMediaFileMetadata
		break
	case 'fav':
		target = FavoriteAPI.GetFavoriteForID(params['id'].toInteger())
		props = FavoriteAPI.GetFavoriteProperties(target)
		method = FavoriteAPI.&SetFavoriteProperty
		break
	default:
		response.sendError(400, "Invalid object type! [${params['type']}]")
		return
}
switch(params['a']) {
	case 'update':
		props.each { k, v ->
			if(params[k] || params["null_$k"])
				method(target, k, !params["null_$k"] ? params[k] : null)
		}
		def i = 0
		while(params["name$i"]) {
			method(target, params["name$i"], !params["null$i"] ? params["val$i"] : null)
			++i
		}
		response.sendRedirect("props_edit.groovy?type=${params['type']}&id=${params['id']}")
		break
	default:
		if(target) {
			request.setAttribute('props', props)
			request.getRequestDispatcher('props_edit.gsp').forward(request, response)
		} else
			response.sendError(400, "Invalid object id! [${params['id']}]")
}
