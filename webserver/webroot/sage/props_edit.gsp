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

	def props = request.getAttribute('props')
	def url
	if(params['type'] == 'mf')
		url = "DetailedInfo?MediaFileId=${params['id']}"
	else
		url = "FavoriteDetails?FavoriteId=${params['id']}"
%>
<div id="airdetailedinfo">
<form method="POST" action="props_edit.groovy">
	<input type="hidden" name="type" value="${params['type']}" />
	<input type="hidden" name="id" value="${params['id']}" />
	<input type="hidden" name="a" value="update" />
	<table border="0">
		<tr>
			<th>Name</th>
			<th>Value</th>
			<th>Delete?</th>
		</tr>
	<%
		props.each { k, v ->
	%>
			<tr>
				<td><b>$k</b></td>
				<td><input type="text" name="$k" value="$v" /></td>
				<td><input type="checkbox" name="null_${k}" /></td>
			</tr>
	<%
		}
	%>
		<tr>
			<td><input type="text" name="name0" /></td>
			<td><input type="text" name="val0" /></td>
			<td><input type="checkbox" name="null0" /></td>
		</tr>

		<tr>
			<td colspan="3"><input type="submit" name="submit" value="Update" /></td>
		</tr>
	</table>
</form>
</div>
<div id="commands">
        <ul>
                <li><a href="$url">Return</a></li>
        </ul>
</div>
<@ webserver/templates/footer.gsp @>
