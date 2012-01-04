<@ webserver/templates/header.gsp @>
<%
/*
*      Copyright 2012 Battams, Derek
*      
*      This is a modified version of the code; original code by nielm, et al.
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
def errs = request.getAttribute('errors')
if(errs && errs.keySet().size() > 0) {
%>
	<p style="color: red;">The following options failed to update:</p>
	<ul>
<%
	errs.each { k, v ->
%>
		<li>
			<b>${PluginAPI.GetPluginConfigLabel(PluginAPI.GetAvailablePluginForID(request.getAttribute('id')), k)}</b>
			<ul><li>$v</li></ul>
		</li>
<%
	}
%>
	</ul>
<%
}
%>
<form method="POST" action="pm_edit.groovy">
	<input type="hidden" name="id" value="${request.getAttribute('id')}" />
	<input type="hidden" name="a" value="update" />
	<table border="0">
		<% request.getAttribute('opts').each { k, v -> %>
			<tr>
				<td colspan="2"><b>${v['label']}</b></td>
			</tr>
			<tr>
				<td width="33%">${v['help']}</td>
				<td>${v['html']}</td>
			</tr>
		<% } %>
	</table>
	<input type="submit" name="submit" value="Update" />
	<input type="reset" name="reset" value="Reset" />
</form>
<@ webserver/templates/footer.gsp @>
