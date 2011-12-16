
package net.sf.sageplugins.webserver;


import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;
/** 
 * @author Owner
 *
 * 
 *
 */
public class CommandServlet extends SageServlet {

	
	/**
     * 
     */
    private static final long serialVersionUID = -7002040121007411966L;

    protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
	throws Exception {
		String command = req.getParameter("command");
        String context = req.getParameter("context");
		String returnto = req.getParameter("returnto");
        
        String[] UiContexts=GetUIContextNames();
        String[] connectedClients= (String[]) SageApi.Api("GetConnectedClients");

        List<String> contexts = new ArrayList<String>();
        contexts.addAll(Arrays.asList(UiContexts));
        if (SAGE_MAJOR_VERSION >= 7.0) {
            contexts.addAll(Arrays.asList(connectedClients));
        }
        
		if (command==null || context == null || ! contexts.contains(context) ) {
			// error response
			//	set headers before accessing the Writer
			htmlHeaders(resp);
			noCacheHeaders(resp);
			PrintWriter out = resp.getWriter();
			xhtmlHeaders(out);
			out.println("<html>" +
			  "<head><title>SageTV Command</title>");
			jsCssImport(req, out);
			out.println("</head><body>");
			printTitle(out,"Error");
			out.println("<div id=\"content\">");
			if ( command==null )
			    out.println("<p>No Command Specified:<br/>");
            if ( context == null )
                out.println("<p>No UI Context Specified:<br/>");
            if ( ! contexts.contains(context) ) {
                out.println("<p>UI Context: \""+context+"\" is not active:<br/>");
                if ( context.equals("SAGETV_PROCESS_LOCAL_UI") ){
                    out.println("Webserver is running in SageTV Service - no remote control possible<br/>");
                } else {
                    out.println("Sage Extender is not connected<br/>");
                }
            }
            out.println(" use: "+req.getRequestURI()+"?command=&lt;command&gt;&amp;context=&lt;context&gt;</p>");
			out.println("</div></body></html>");
			out.close();
			return;
		} else {
	        SageApi.ApiUI(context,"SageCommand",command);
		}
		if( req.getParameter("RetImage") != null) {
			resp.setContentType("image/png");
		    noCacheHeaders(resp);
		    OutputStream os=resp.getOutputStream();
		    BufferedImage img=new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
		    javax.imageio.ImageIO.write(img,"png",os);
		    os.close();
		} else if (returnto!=null) {
            //  wait a little for the command to take effect before redirecting
            Thread.sleep(100);
            resp.sendRedirect(returnto);
        } else {
			//	set headers before accessing the Writer
			htmlHeaders(resp);
			noCacheHeaders(resp);
			PrintWriter out = resp.getWriter();
			xhtmlHeaders(out);
			out.println("<html>" +
			  "<head><title>SageTV Command</title>");
			jsCssImport(req, out);
			out.println("</head><body>");
			printTitle(out,"SageTV Command");
			out.println("<div id=\"content\">");
			out.println("Sent Sage Command: \""+command+"\" to UI with context \""+context+"");
			out.println("</div></body></html>");
			out.close();
		} 
	}
}
