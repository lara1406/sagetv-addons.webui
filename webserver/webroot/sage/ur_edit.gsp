<@ webserver/templates/header.gsp @>
<%
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
	import org.apache.commons.lang.StringEscapeUtils
	def record = request.getAttribute('data')
%>
<h1>${params['name']} :: ${params['key']}</h1>
<form method="POST" action="ur.groovy">
	<input type="hidden" name="name" value="${StringEscapeUtils.escapeHtml(params['name'])}" />
	<input type="hidden" name="key" value="${StringEscapeUtils.escapeHtml(params['key'])}" />
	<input type="hidden" name="a" value="update" />
	<div>
		<table border="0">
			<th>
				<td>Name</td>
				<td>Value</td>
				<td>Delete?</td>
			</th>
			<% for(String k : UserRecordAPI.GetUserRecordNames(record)) { %>
				<tr>
					<td><b>$k</b></td>
					<td><input type="text" name="$k" value="${StringEscapeUtils.escapeHtml(UserRecordAPI.GetUserRecordData(record, k))}" /></td>
					<td><input type="checkbox" name="null_${StringEscapeUtils.escapeHtml(k)}" /></td>
				</tr>
			<% } %>
			<tr><td colspan="3"><input type="submit" name="submit" value="Update" /></td></tr>
		</table>
	</div>
</form>
<@ webserver/templates/footer.gsp @>
