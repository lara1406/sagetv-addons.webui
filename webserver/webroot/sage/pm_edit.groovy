/*
*      Copyright 2012-2013 Battams, Derek
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

def action = request.getAttribute('a') ?: params['a']
def id     = request.getAttribute('id') ?: params['id']
switch(action) {
	case 'grab':
		def p = PluginAPI.GetAvailablePluginForID(id)
		request.setAttribute('id', PluginAPI.GetPluginIdentifier(p))
		request.setAttribute('opts', getOpts(p))
		request.setAttribute('pageTitle', PluginAPI.GetPluginName(p))
		request.getRequestDispatcher('pm_conf.gsp').forward(request, response)
		break
	case 'update':
		def p = PluginAPI.GetAvailablePluginForID(id)
		def errs = [:]
		PluginAPI.GetPluginConfigSettings(p).each {
			def type = PluginAPI.GetPluginConfigType(p, it)	
			def result
			if(type == 'Multichoice')
				result = PluginAPI.SetPluginConfigValues(p, it, request.getParameterValues(it))			
			else if(type != 'Button' || params[it] == PluginAPI.GetPluginConfigValue(p, it))
				result = PluginAPI.SetPluginConfigValue(p, it, getValue(type, params[it]))
			if(result) errs[it] = result
		}
		if(errs.keySet().size() > 0)
			request.setAttribute('errors', errs)
		request.setAttribute('id', id)
		request.setAttribute('a', 'grab')
		request.getRequestDispatcher('pm_edit.groovy').forward(request, response)
		break
	case 'install':
		def p = PluginAPI.GetAvailablePluginForID(params['id'])
		request.setAttribute('result', PluginAPI.InstallPlugin(p))
		break
	case 'uninstall':
		def p = PluginAPI.GetAvailablePluginForID(params['id'])
		request.setAttribute('result', PluginAPI.UninstallPlugin(p))
		break
	case 'upgrade':
		def p = PluginAPI.GetAvailablePluginForID(params['id'])
		request.setAttribute('result', PluginAPI.InstallPlugin(p))
		break
	case 'enable':
		def p = PluginAPI.GetAvailablePluginForID(params['id'])
		request.setAttribute('result', PluginAPI.EnablePlugin(p))
		break
	case 'disable':
		def p = PluginAPI.GetAvailablePluginForID(params['id'])
		request.setAttribute('result', PluginAPI.DisablePlugin(p))
		break
	default:
		request.setAttribute('result', "ERROR: Invalid action! [$action]")
}
if(request.getAttribute('result') != null)
    request.getRequestDispatcher('pm.gsp').forward(request, response)

def getValue(def type, String val) {
	switch(type) {
			case 'Boolean':
				return val ? 'true' : 'false' 
			case 'Directory':
			case 'File':
			case 'Integer':
			case 'Text':
			case 'Choice':
			case 'Password':
			default:
				return val ? val : ''
	}
}

def getOpts(def plugin) {
	def map = [:]
	PluginAPI.GetPluginConfigSettings(plugin).each {
		def vals = [:]
		vals['id'] = it
		vals['help'] = PluginAPI.GetPluginConfigHelpText(plugin, it)
		vals['label'] = PluginAPI.GetPluginConfigLabel(plugin, it)
		def type = PluginAPI.GetPluginConfigType(plugin, it)
		switch(type) {
			case 'Boolean':
				vals['html'] = "<input type=\"checkbox\" name=\"$it\" " + (PluginAPI.GetPluginConfigValue(plugin, it).toBoolean() ? 'checked="checked"' : '') + ' />'
				break
			case 'Directory':
			case 'File':
			case 'Integer':
			case 'Text':
				vals['html'] = "<input type=\"text\" name=\"$it\" value=\"${PluginAPI.GetPluginConfigValue(plugin, it)}\" />"
				break
			case 'Password':
				vals['html'] = "<input type=\"password\" name=\"$it\" value=\"${PluginAPI.GetPluginConfigValue(plugin, it)}\" />"
				break
			case 'Multichoice':
			case 'Choice':
				vals['html'] = new StringBuilder("<select name=\"$it\"" + (type == 'Multichoice' ? 'multiple="multiple"' : '') + '>')
				def curVal = PluginAPI.GetPluginConfigValue(plugin, it)
				PluginAPI.GetPluginConfigOptions(plugin, it).each {
					def selTxt = curVal == it ? 'selected="selected"' : ''
					vals['html'].append("<option value=\"$it\" $selTxt>$it</option>\n")
				}
				vals['html'].append('</select>\n')
				vals['html'] = vals['html'].toString()
				break
			default: // It must be a button
				vals['html'] = "<input type=\"submit\" name=\"$it\" value=\"${PluginAPI.GetPluginConfigValue(plugin, it)}\" />\n"
		}
		map[it] = vals
	}
	return map
}
