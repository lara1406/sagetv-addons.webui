<@ webserver/templates/header.gsp @>
<%
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
 import org.apache.commons.lang.StringEscapeUtils
%>
<script>
<!--
	\$(document).ready(function() {
		\$('#name').change(function() {
			var url = "ur.groovy?a=qry&name=" + encodeURIComponent(\$('#name').val());
			\$.ajax({
				url: url,
				dataType: 'json',
				success: function() {
					var html = '<option value="">-- Select --</option>';
					for(var i = 0; i < arguments[0].length; ++i) {
						html += '<option value="' + \$('<div/>').text(arguments[0][i]).html() + '">' + \$('<div/>').text(arguments[0][i]).html() + '</option>';
					}
					\$('#key').html(html);
				},
				error: function() {
					alert(arguments[2]);
				},
			});
		});
	});
-->
</script>
<form method="POST" action="ur.groovy">
	<input type="hidden" name="a" value="edit" />
	<div>
		<b>Store:</b> <select name="name" id="name">
			<option value="">-- Select --</option>
		<% for(String s : request.getAttribute('stores')) { %>
			<option value="${StringEscapeUtils.escapeHtml(s)}">${StringEscapeUtils.escapeHtml(s)}</option>
		<% } %>
		</select>
	</div>
	<div>
		<b>Key:</b> <select id="key" name="key" id="key"></select>
	</div>
	<div><input type="submit" name="submit" value="Select" /></div>
</form>
<@ webserver/templates/footer.gsp @>

