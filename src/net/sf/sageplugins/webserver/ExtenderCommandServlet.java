package net.sf.sageplugins.webserver;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;

import com.google.code.sagetvaddons.groovy.api.GlobalHelpers;


public class ExtenderCommandServlet extends SageServlet {

    static public boolean isExtender(String uicontext) {
    	return GlobalHelpers.isMacAddrExtender(uicontext);
    }

    static public boolean isPlaceshifter(String uicontext) {
    	return GlobalHelpers.isMacAddrPlaceshifter(uicontext);
    }
    
	static public boolean isMvp(String uicontext){
		return GlobalHelpers.isMacAddrMediaMVP(uicontext);
	}

    static public boolean isHDExtender(String uicontext){
    	return GlobalHelpers.isMacAddrExtender(uicontext) && !GlobalHelpers.isMacAddrMediaMVP(uicontext);
    }
	/**
	 * 
	 */
	private static final long serialVersionUID = -4783073806357375492L;
	protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {

		
		
		try {
			String command=req.getParameter("command");
			String uicontext=req.getParameter("context");
			if ( command == null || command.length()==0) {
				throw new Exception ("No command passed");
			} 
			if ( uicontext==null || uicontext.length()==0) {
				throw new Exception ("No uicontext passed");
			}

			if ( req.getParameter("confirm")==null ){
				// confirmation required
	            htmlHeaders(resp);
	            noCacheHeaders(resp);
	            PrintWriter out = resp.getWriter();
	            xhtmlHeaders(out);
	            out.println("<head>");
	            jsCssImport(req, out);
	            printMenu(out);
	            out.println("<title>Command confirmation required</title></head>");
	            out.println("<body>");
	            printTitle(out,"Confirmation required:");
	            out.println("<div id=\"content\">");
	            out.println("<p>Are you sure you want to "+command+" the "+
	            		(isMvp(uicontext)?"MVP":(isHDExtender(uicontext)?"HD Extender":"Placeshifter"))+
	            		" at "+UiContextProperties.getProperty(uicontext, "name") +"?</p>");
	            		
	            out.println("<form method=\"get\" action=\"ExtenderCommand\">\n"+
	                    "<input type=\"hidden\" name=\"command\" value=\""+command+"\"/>\n"+
	            		"<input type=\"hidden\" name=\"context\" value=\""+uicontext+"\"/>");
	            if(req.getParameter("returnto")!=null)
	                out.println("<input type=\"hidden\" name=\"returnto\" value=\""+req.getParameter("returnto")+"\"/>");
	            
	            out.println("<input type=\"submit\" name=\"confirm\" value=\"yes\"/>");
	            out.println("<input type=\"submit\" name=\"confirm\" value=\"no\"/>");
	            out.println("</form>");
	            out.println("</div>");
	            out.println("</body></html>");
	            out.close();
	            return;
			}
			
			
			
			if (  req.getParameter("confirm").equalsIgnoreCase("yes")) {
				Object ret = null;
				if ( command.equalsIgnoreCase("poweroff")) {
				    if ( isMvp(uicontext))
				        ret = mvpPowerOff(uicontext);
				    else if ( isHDExtender(uicontext))
				        ret = stxPowerOff(uicontext);
				    else 
				        ret = "Not an MVP or STX extender";
				} else if ( command.equalsIgnoreCase("reboot") ) {
                    if ( isMvp(uicontext))
                        ret = mvpReboot(uicontext);
				    else if ( isHDExtender(uicontext))
				        ret = stxReboot(uicontext);
				    else 
				        ret = "Not an MVP or STX extender";
				} else if ( command.equalsIgnoreCase("exit") && isPlaceshifter(uicontext)) {
					SageApi.ApiUI(uicontext, "Exit");
					ret=Boolean.TRUE;
                } else if ( command.equalsIgnoreCase("rename") ) {
                    String contextName = req.getParameter("name");
                    contextName = (contextName == null) ? "" : contextName;
                    UiContextProperties.setProperty(uicontext, "name", contextName);
				} else {
					throw new Exception ("Invalid command passed");
			    }								
				
				if (ret instanceof String) {
					String error = (String) ret;
					throw new Exception (error);
				}
				String returnto=req.getParameter("returnto");
		        if  (returnto !=null && returnto.length()>0) {
		            //  wait a little for the command to take effect before redirecting
		            Thread.sleep(500);
		            resp.sendRedirect(returnto);
		            return;
		        } else {
				    // return HTML page
				    htmlHeaders(resp);
				    noCacheHeaders(resp);
				    PrintWriter out = resp.getWriter();
				    xhtmlHeaders(out);
				    out.println("<head>");
				    jsCssImport(req, out);
				    out.println("<title>Mvp Command</title></head>");
				    out.println("<body>");
				    printTitle(out,"");
				    out.println("<div id=\"content\">");
				    out.print("Applied command: "+command+" on "+uicontext);
				    out.println("</div>");
				    printMenu(req,out);
				    out.println("</body></html>");
				    out.close();
		        } 
			} else {
				String returnto=req.getParameter("returnto");
				if  (returnto !=null && returnto.length()>0) {
					//  wait a little for the command to take effect before redirecting
					Thread.sleep(500);
					resp.sendRedirect(returnto);
					return;
				} else {
					resp.sendRedirect("Home");
					return;
				}
			}
			return;
		} catch (Exception e) {
			htmlHeaders(resp);
			noCacheHeaders(resp);
			PrintWriter out = resp.getWriter();
			xhtmlHeaders(out);
			out.println("<head>");
			jsCssImport(req, out);
			out.println("<title>MVP Command</title></head>");
			out.println("<body>");
		    printTitle(out,"Error");
		    out.println("<div id=\"content\">");
			out.println("<h3>Failed to send command to MVP:</h3>");
			out.println("<pre>"+e.toString()+"</pre>");
			out.println("</div>");
			printMenu(req,out);
			out.println("</body></html>");
			out.close();
			return;
		}
	}

	public static Object mvpReboot(String uiContext){
		GlobalHelpers.rebootExtenderMacAddr(uiContext);
		return true;
	}
	
	public static Object mvpPowerOff(String uiContext){
		try {
			GlobalHelpers.powerOffExtenderMacAddr(uiContext);
			return true;
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public static Object stxPowerOff(String uiContext){
        return mvpPowerOff(uiContext);
    }
	
    public static Object stxReboot(String uiContext){
        return mvpReboot(uiContext);
    }	
}
