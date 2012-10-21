/*
*      Copyright 2011-2012 Battams, Derek
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
import org.json.JSONArray

final def UR_KEY_REGEX = ~/^UserRecord\[store=.*, key=(.*), props=.*$/

request.setAttribute('pageTitle', 'UserRecord Editor')
switch(params['a']) {
    case 'edit':
        request.setAttribute('data', UserRecordAPI.GetUserRecord(params['name'], params['key']))
        request.getRequestDispatcher('ur_edit.gsp').forward(request, response)
        break
    case 'update':
        def record = UserRecordAPI.GetUserRecord(params['name'], params['key'])
        for(def k : UserRecordAPI.GetUserRecordNames(record))
            if(params[k] || params["null_$k"])
                UserRecordAPI.SetUserRecordData(record, k, !params["null_$k"] ? params[k] : null)
        response.sendRedirect("ur.groovy?a=edit&name=${params['name']}&key=${params['key']}")
        break
    case 'qry':
		def vals = []
		UserRecordAPI.GetAllUserRecords(params['name']).each {
        	if(it.toString() ==~ UR_KEY_REGEX)
                	vals << UR_KEY_REGEX.matcher(it.toString())[0][1]
		}
		out << new JSONArray(vals)
		break
    case 'select':
    default:
        request.setAttribute('stores', UserRecordAPI.GetAllUserStores())
        request.getRequestDispatcher('ur_grab.gsp').forward(request, response)
}

