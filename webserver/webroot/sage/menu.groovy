import groovy.xml.StreamingMarkupBuilder
import sagex.api.*

String renderFormattedXml( String xml ){
	def stringWriter = new StringWriter()
	def node = new XmlParser().parseText( xml )
	new XmlNodePrinter( new PrintWriter( stringWriter ) ).print( node )
	stringWriter.toString()
}

void injectSource(File src, def xml) {
	def ids = []
	def menus = []
	src.readLines().each {
		if(it.startsWith('!!'))
			ids << it.substring(2)
		else {
			def menu = null
			if(!menus.size() || menus.size() != ids.size()) {
				menu = []
				menus << menu
			} else
				menu = menus[ids.size() - 1]
			menu << it
		}
	}
	for(def i = 0; i < ids.size(); ++i) {
		def parent = xml.breadthFirst().find { it.@id == ids[i] }
		if(parent) {
			def data = menus[i].join('\n')
			parent.appendNode { mkp.yieldUnescaped data }
		}
	}
}

response.contentType = 'text/plain'
def xml = new XmlSlurper().parse(new File('jetty/webapps/nielm_sagewebserver/webapp/ddmenu.txt'))
PluginAPI.GetInstalledPlugins().each {
	def src = new File(PluginAPI.GetPluginResourcePath(it), 'web.menu')
	if(src.canRead() && src.parentFile.absoluteFile != new File('.').absoluteFile)
		injectSource(src, xml)
}
def outputBuilder = new StreamingMarkupBuilder()
String result = outputBuilder.bind { mkp.yield xml }
println renderFormattedXml(result)
