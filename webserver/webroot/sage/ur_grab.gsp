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
 import org.apache.commons.lang.StringEscapeUtils
%>
<form method="POST" action="ur.groovy">
	<input type="hidden" name="a" value="edit" />
	<div>
		<b>Store:</b> <select name="name">
		<% for(String s : request.getAttribute('stores')) { %>
			<option value="${StringEscapeUtils.escapeHtml(s)}">${StringEscapeUtils.escapeHtml(s)}</option>
		<% } %>
		</select>
	</div>
	<div>
		<b>Key:</b> <input type="text" name="key" />
	</div>
	<div><input type="submit" name="submit" value="Select" /></div>
</form>
<@ webserver/templates/footer.gsp @>
