/*
 * Created on Mar 7, 2011
 *
 */
package net.sf.sageplugins.webserver;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;

/**
 * @author Owner
 *
 */
public class ExtenderDetailsServlet extends SageServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -1282179705230630195L;

    public ExtenderDetailsServlet() {
	}

	/* (non-Javadoc)
	 * @see net.sf.sageplugins.webserver.SageServlet#doServletGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {

	    String pageTitle = "";
	    String context = req.getParameter("context");

		htmlHeaders(resp);
		noCacheHeaders(resp);
		PrintWriter out = getGzippedWriter(req,resp);
        // must catch and report all errors within Gzipped Writer
		try {
		    xhtmlHeaders(out);
		    out.println("<head>");
		    jsCssImport(req, out);

		    Boolean isClient = (Boolean) SageApi.ApiUI(context, "IsClient");
		    if (isClient) {
		        pageTitle = "Client Details";
		    } else {
                pageTitle = "Extender Details";
		    }
            out.println("<title>" + pageTitle + "</title>");
		    out.println("</head>");
		    out.println("<body>");
            printTitle(out,pageTitle, SageServlet.isTitleBroken(req));
		    out.println("<div id=\"content\">");
		    out.println("<div id=\"airdetailedinfo\">");
		    
		    out.println("    <form method=\"post\" action=\"ExtenderCommand\">");
		    
		    out.println("        <dl>");
		    		    
		    // Properties for ui contexts

            // Context id
            out.println("        <dt>Id:</dt>");
            out.println("        <dd>");
            out.println("            " + context);
            out.println("        </dd>");

            // Name
            out.println("        <dt>Name:</dt>");
            out.println("        <dd>");
            out.println("            <input type=\"hidden\" name=\"context\" value=\"" + context + "\"/>");
            out.println("            <input type=\"text\" name=\"name\" value=\"" + net.sf.sageplugins.webserver.UiContextProperties.getProperty(context, "name") + "\"/>");
            out.println("            <input type=\"hidden\" name=\"confirm\" value=\"yes\"/>");
            out.println("            <input type=\"hidden\" name=\"returnto\" value=\"ExtenderDetails?" + req.getQueryString() + "\"/>");
            out.println("            <button type=\"submit\" name=\"command\" value=\"rename\">Rename</button>");
            out.println("            ");
            out.println("        </dd>");

            // Type
            String uiType = (String) SageApi.ApiUI(context, "GetRemoteUIType");
            out.println("        <dt>Type:</dt>");
            out.println("        <dd>");
            out.println("            " + uiType);
            out.println("        </dd>");

            // Platform
            // No API for GetOS that takes context as a parameter
//            String platform = (String) SageApi.ApiUI(context, "GetOS");
//            out.println("        <dt>Platform:</dt>");
//            out.println("        <dd>");
//            out.println("            " + platform);
//            out.println("        </dd>");

            // Version
            String uiVersion = (String) SageApi.ApiUI(context, "GetRemoteClientVersion");
            out.println("        <dt>Version:</dt>");
            out.println("        <dd>");
            out.println("            " + uiVersion);
            out.println("        </dd>");

            // Full screen
            Boolean isFullScreen = (Boolean) SageApi.ApiUI(context, "IsFullScreen");
            out.println("        <dt>Full Screen:</dt>");
            out.println("        <dd>");
            out.println("            " + (isFullScreen ? "Yes" : "No"));
            out.println("        </dd>");

            // Window Size
            Integer width = (Integer) SageApi.ApiUI(context, "GetFullUIWidth");
            Integer height = (Integer) SageApi.ApiUI(context, "GetFullUIHeight");
            out.println("        <dt>Window Size:</dt>");
            out.println("        <dd>");
            out.println("            " + width + "x" + height);
            out.println("        </dd>");

            // Resolution
            String uiResolution = (String) SageApi.ApiUI(context, "GetDisplayResolution");
            out.println("        <dt>Display Resolution:</dt>");
            out.println("        <dd>");
            out.println("            " + uiResolution);
            out.println("        </dd>");

            // Display Resolution Options
            String[] uiResolutionOptions = (String[]) SageApi.ApiUI(context, "GetDisplayResolutionOptions");
            if ((uiResolutionOptions != null) && (uiResolutionOptions.length > 0))
            {
                out.println("        <dt>Display Resolution Options:</dt>");
                out.println("        <dd>");
                for (String uiResolutionOption : uiResolutionOptions)
                {
                    out.println("            <p>" + uiResolutionOption + "</p>");
                }
                out.println("        </dd>");
            }

            // Available Update
//            String availableUpdate = (String) SageApi.ApiUI(context, "GetAvailableUpdate");
//            out.println("        <dt>Available Update:</dt>");
//            out.println("        <dd>");
//            out.println("            " + availableUpdate);
//            out.println("        </dd>");

            // Asleep
            Boolean isAsleep = (Boolean) SageApi.ApiUI(context, "IsAsleep");
            out.println("        <dt>Asleep:</dt>");
            out.println("        <dd>");
            out.println("            " + (isAsleep ? "Yes" : "No"));
            out.println("        </dd>");

            // End of properties, close the form element
            out.println("    </form>");
		    
		    out.println("</div>");//airdetailedinfo	
		    
		    out.println("<div id=\"commands\">");
		    out.println("    <ul>");
		    
	        out.println("        <li>");
	        out.println("            (No Commands)");        
	        out.println("        </li>");
		    
		    out.println("    </ul>");
		    out.println("</div>");//commands
		    
            printFooter(req,out);
            out.println("</div>");//content
		    printMenu(req,out);
		    out.println("</body>");
		    out.println("</html>");
		    out.close();
        } catch (Throwable e) {
            if (!resp.isCommitted()){
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("text/html");
            }
            out.println();
            out.println();
            out.println("<body><pre>");
            out.println("Exception while processing servlet:\r\n"+e.toString());
            e.printStackTrace(out);
            out.println("</pre>");
            out.close();
            log("Exception while processing servlet",e);
        }
	}
}
