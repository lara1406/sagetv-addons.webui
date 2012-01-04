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
def result = request.getAttribute('result')
if(result != null) {
	if(result.class == Boolean) {
		if(!result)
			out << '<p style="color: red;">ERROR: Operation failed!</p>'
		else
			out << '<p style="color: green;">OK: Operation succeeded!</p>\n'
	} else {
		if(result == 'RESTART')
			out << '<p style="color: yellow;">WARN: Operation succeeded; SageTV restart required to complete.</p>'
		else if(result == 'OK')
			out << '<p style="color: green;">OK: Operation succeeded!</p>'
		else
			out << "<p style=\"color: red;\">$result</p>"
	}
}
%>
<h2>Install Plugin</h2>
<div>
	<form  method="POST" action="pm_edit.groovy" onsubmit="return confirm('Proceed with installation?');">
		<input type="hidden" name="a" value="install" />
		<select name="id">
			<% 
				PluginAPI.GetAllAvailablePlugins().sort{ PluginAPI.GetPluginName(it) }.each { 
					if(PluginAPI.IsPluginCompatible(it) && !PluginAPI.IsPluginInstalled(it)) {
			%>
						<option value="${PluginAPI.GetPluginIdentifier(it)}">${PluginAPI.GetPluginName(it)}</option>
			<%
					}
				}
			%>
		</select>
		<input type="submit" name="submit" value="Install" />
	</form>
</div>

<h2>Uninstall Plugin</h2>
<div>
        <form action="pm_edit.groovy"  onsubmit="return confirm('Proceed with uninstall?');">
		<input type="hidden" name="a" value="uninstall" />
                <select name="id">
                        <%
                        PluginAPI.GetInstalledPlugins().sort{ PluginAPI.GetPluginName(it) }.each {
                        %>
                            <option value="${PluginAPI.GetPluginIdentifier(it)}">${PluginAPI.GetPluginName(it)}</option>
                        <%
			}
			%>
                </select>
                <input type="submit" name="submit" value="Uninstall" />
        </form>
</div>

<h2>Upgrade Plugin</h2>
<div>
        <form action="pm_edit.groovy" onsubmit="return confirm('Proceed with upgrade?');">
		<input type="hidden" name="a" value="upgrade" />
                <select name="id">
                        <%
                        PluginAPI.GetInstalledPlugins().sort{ PluginAPI.GetPluginName(it) }.each {
				if(!PluginAPI.IsPluginInstalledSameVersion(PluginAPI.GetAvailablePluginForID(PluginAPI.GetPluginIdentifier(it)))) {
			%>
                            		<option value="${PluginAPI.GetPluginIdentifier(it)}">${PluginAPI.GetPluginName(it)}</option>
                        <%
                       		}
			}
                        %>
                </select>
                <input type="submit" name="submit" value="Upgrade" />
        </form>
</div>

<h2>Enable Plugin</h2>
<p><b>Note:</b> Not all plugins can be enabled; only ones that can are listed here.</p>
<div>
        <form action="pm_edit.groovy"  onsubmit="return confirm('Proceed with enable?');">
		<input type="hidden" name="a" value="enable" />
                <select name="id">
                        <%
                                PluginAPI.GetInstalledPlugins().sort{ PluginAPI.GetPluginName(it) }.each {
                                        if(PluginAPI.CanPluginBeDisabled(it) && !PluginAPI.IsPluginEnabled(it)) {
                        %>
                                                <option value="${PluginAPI.GetPluginIdentifier(it)}">${PluginAPI.GetPluginName(it)}</option>
                        <%
                                        }
                                }
                        %>
                </select>
                <input type="submit" name="submit" value="Enable" />
        </form>
</div>

<h2>Disable Plugin</h2>
<p><b>Note:</b> Not all plugins can be disabled; only ones that can are listed here.</p>
<div>
        <form action="pm_edit.groovy" onsubmit="return confirm('Proceed with disable?');">
		<input type="hidden" name="a" value="disable" />
                <select name="id">
                        <%
                                PluginAPI.GetInstalledPlugins().sort{ PluginAPI.GetPluginName(it) }.each {
                                        if(PluginAPI.CanPluginBeDisabled(it) && PluginAPI.IsPluginEnabled(it)) {
                        %>
                                                <option value="${PluginAPI.GetPluginIdentifier(it)}">${PluginAPI.GetPluginName(it)}</option>
                        <%
                                        }
                                }
                        %>
                </select>
                <input type="submit" name="submit" value="Disable" />
        </form>
</div>

<h2>Configure Plugin</h2>
<p><b>Note:</b> Disabled plugins cannot be configured and will not be listed here.</p>
<div>
	<form method="GET" action="pm_edit.groovy">
		<input type="hidden" name="a" value="grab" />
		<select name="id">
			<% 
				PluginAPI.GetInstalledPlugins().sort{ PluginAPI.GetPluginName(it) }.each { 
					if(PluginAPI.IsPluginConfigurable(it) && PluginAPI.IsPluginEnabled(it)) {
			%>
						<option value="${PluginAPI.GetPluginIdentifier(it)}">${PluginAPI.GetPluginName(it)}</option>
			<%
					}
				}
			%>
		</select>
		<input type="submit" name="submit" value="Configure" />
	</form>
</div>
<@ webserver/templates/footer.gsp @>
