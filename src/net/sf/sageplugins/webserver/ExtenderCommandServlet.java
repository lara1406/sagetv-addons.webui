package net.sf.sageplugins.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;


public class ExtenderCommandServlet extends SageServlet {

    static public boolean isExtender(String uicontext) {
        try {
            return ((Boolean)SageApi.ApiUI(uicontext, "IsRemoteUI")).booleanValue()
            && !((Boolean)SageApi.ApiUI(uicontext, "IsDesktopUI")).booleanValue();
        } catch (InvocationTargetException e){
            System.out.println(e);
            e.printStackTrace();
        }
        return false;
    }

    static public boolean isPlaceshifter(String uicontext) {
        try {
            return ((Boolean)SageApi.ApiUI(uicontext, "IsRemoteUI")).booleanValue()
            && ((Boolean)SageApi.ApiUI(uicontext, "IsDesktopUI")).booleanValue();
        } catch (InvocationTargetException e){
            System.out.println(e);
            e.printStackTrace();
        }
        return false;
    }
    
	static public boolean isMvp(String uicontext){
	    try {
	        return isExtender(uicontext) &&
		        SageApi.ApiUI(uicontext, "GetAudioOutputOptions")==null;
        } catch (InvocationTargetException e){
            System.out.println(e);
            e.printStackTrace();
        }
        return false;
	}

    static public boolean isHDExtender(String uicontext){
        try {
            return isExtender(uicontext) &&
                SageApi.ApiUI(uicontext, "GetAudioOutputOptions")!=null;
        } catch (InvocationTargetException e){
            System.out.println(e);
            e.printStackTrace();
        }
        return false;
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
	            printMenu(out);
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if ( args.length!=2){
			System.out.println("Usage: MvpPowerServlet [IPaddr|UIContextMACAddress] [poweroff|reboot]");
			return;
		}
		Object ret;
		if ( args[1].equalsIgnoreCase("poweroff"))
			ret=mvpPowerOff(args[0]);
		else if ( args[1].equalsIgnoreCase("reboot"))
			ret=mvpReboot(args[0]);
		else {
			System.out.println("Usage: MvpPowerServlet [IPaddr|UIContextMACAddress] [poweroff|reboot]");
			return;
		}
		if (ret instanceof String) {
			String error = (String) ret;
			System.out.println("Failed: "+error);
		} else {
			System.out.println("Success");
		}
		

	}
	public static Object mvpReboot(String uiContext){
		return mvpCommand(uiContext, "killall miniclient;sleep 1;reboot");
	}
	
	public static Object mvpPowerOff(String uiContext){
		return mvpCommand(uiContext, "killall miniclient");
	}

	public static Object stxPowerOff(String uiContext){
        return mvpCommand(uiContext, "poweroff");
    }
    public static Object stxReboot(String uiContext){
        return mvpCommand(uiContext, "reboot");
    }
	
	private static Object mvpCommand(String uiContext,String command){
		// check Mac or IP
		if ( uiContext.matches("(\\d{1,3}\\.){3}\\d{1,3}")){
			try {
				InetAddress ipaddr=InetAddress.getByName(uiContext);
				return mvpCommand(ipaddr,command);
			} catch (java.net.UnknownHostException e){}
		}
		// assume Mac
		uiContext=uiContext.toLowerCase().replaceAll("[^a-f0-9]", "");
		if ( uiContext.length()!=12){
			return "Invalid Mac address: cleaned should be 12 chars: "+uiContext;
		}
		final Process p;
		try {
			// Windows arp output 
			// c:\ arp -a
			//
			// Interface: 192.168.0.3 --- 0x9
			//  Internet Address      Physical Address      Type
			//  192.168.0.1           00-0f-b5-af-0d-48     dynamic
			//  192.168.0.50          00-11-2f-ea-21-02     dynamic
			
			// Linux arp output:
			// $arp -an
			// ? (192.168.0.1)           00:0f:b5:af:0d:48
			// ? (192.168.0.50)          00:11:2f:ea:21:02
			
			if ( true || SageApi.booleanApi("IsWindowsOs", null)) {
				p=Runtime.getRuntime().exec("arp -a");
			} else {
				p=Runtime.getRuntime().exec("arp -an");
			}
			
			try {
				Thread errGobbler=new Thread() {
					public void run() {
						BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
						String line;
						try {
							while((line=in.readLine())!=null){
								System.out.println("Arp: stderr: "+line);
							}
							in.close();	
						} catch (IOException e){}
					}
				};
				errGobbler.start();
				String line;
				InetAddress addr=null;
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				try {
					Pattern pat=Pattern.compile("^.*?(([0-9]{1,3}.){3}[0-9]{1,3}).+?(([0-9a-f]{2}[:\\-.]){5}[0-9a-f]{2}).*$",Pattern.CASE_INSENSITIVE);
					while((line=in.readLine())!=null){
						System.out.println("Arp: stdout: "+line);
						java.util.regex.Matcher m=pat.matcher(line);
						if ( m.matches()){
							if ( m.group(3).toLowerCase().replaceAll("[^a-f0-9]", "")
									.equals(uiContext))
							{
								try {
									addr=InetAddress.getByName(m.group(1));
								} catch (java.net.UnknownHostException e){
									System.out.println("arp "+e);
								}
							}
						}
					}
					in.close();
					if ( addr!=null)
						return mvpCommand(addr,command);
				} catch (IOException e){}
			}
			finally {
				try {
					System.out.println("arp exited with "+p.exitValue());
				}catch (IllegalThreadStateException e){
					System.out.println("Terminating ARP");
					p.destroy();
					p.getOutputStream().close(); // close stdin
				}
			}
			return "Could not determine IP address for UI Context"; 
			
		}
		catch (Exception e) {
			System.out.println("Getting IP for MAC " +e);
		}
		return "Could not determine IP address for UI Context - Arp failed";
		
	}
	static Object mvpCommand(InetAddress ipaddr, String command){
		System.out.println("powering off extender at "+ipaddr);
		
		PrintWriter out =null;
		BufferedReader in =null;
		java.net.Socket sock =null;
		try {
			sock = new java.net.Socket(ipaddr,23);
			out = new PrintWriter(sock.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out.println("root");
            out.println();
            out.println(command);
            out.println("exit");
            String line;
            while ((line=in.readLine())!=null){
            	System.out.println("Extender: "+line);
            }
		} catch (Exception e){
			e.printStackTrace();
			return "Failed to connect to extender at "+ipaddr.getHostAddress()+" -- "+e;
			
		} finally {
			try {
				if ( out!=null)
					out.close();
				if ( in != null)
					in.close();
				if ( sock != null)
					sock.close();
			} catch (Exception e){}
		}
		
		return Boolean.TRUE;
	}

}
