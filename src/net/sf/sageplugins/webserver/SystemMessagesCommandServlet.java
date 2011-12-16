/*
 * Created on Jul 12, 2009
 *
 */
package net.sf.sageplugins.webserver;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;
import net.sf.sageplugins.sageutils.SystemMessageApi;

/**
 * @author Owner
 *
 */
public class SystemMessagesCommandServlet extends SageServlet {

	/**
     * 
     */
    private static final long serialVersionUID = 7208810244349974154L;

    /**
	 * 
	 */
	public SystemMessagesCommandServlet()
	{
	}

	/* (non-Javadoc)
	 * @see net.sf.sageplugins.webserver.SageServlet#doServletGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
			throws Exception
	{
	    try
	    {
	        if (req.getParameter("ResetSystemAlertLevel") != null)
	        {
	            SageApi.Api("ResetSystemAlertLevel");
	        }
	        else if (req.getParameter("DeleteSystemMessage") != null)
	        {
	            String[] messageParams = req.getParameterValues("SystemMessageApi");
	            Object systemMessages = SageApi.Api("GetSystemMessages");
	            
	            // messages do not have ids, but the combination of level, time and type code is unique
	            for (String messageParam : messageParams)
	            {
	                for (int i = 0; i < SageApi.Size(systemMessages); i++)
	                {
	                    Object currentSystemMessage = SageApi.GetElement(systemMessages, i);
                        String key =  SystemMessageApi.getKey(currentSystemMessage);
                        if (messageParam.equals(key))
                        {
                            SageApi.Api("DeleteSystemMessage", currentSystemMessage);
                        }
	                }
	            }
	        }
	        else if (req.getParameter("DeleteAllSystemMessages") != null)
	        {
                SageApi.Api("DeleteAllSystemMessages");
	        }
	        else
	        {
	            htmlHeaders(resp);
	            noCacheHeaders(resp);
	            PrintWriter out = resp.getWriter();
	            xhtmlHeaders(out);
	            out.println("<head>");
	            jsCssImport(req, out);
	            out.println("<title>InternalCommand</title></head>");
	            out.println("<body>");
	            printTitle(out,"Error");
	            out.println("<div id=\"content\">");
	            out.println("<h3>No command passed</h3>");
	            out.println("</div>");
	            printMenu(req,out);
	            out.println("</body></html>");
	            out.close();
	            return;
	        }
	    } catch ( Exception e) {
	        htmlHeaders(resp);
	        noCacheHeaders(resp);
	        PrintWriter out = resp.getWriter();
	        xhtmlHeaders(out);
	        out.println("<head>");
	        jsCssImport(req, out);
	        out.println("<title>SystemMessageCommand</title></head>");
	        out.println("<body>");
	        printTitle(out,"Error");
	        out.println("<div id=\"content\">");
	        out.println("<h3>Unable to perform action on SystemCommand</h3>");
	        e.printStackTrace();
	        out.println("</div>");
	        printMenu(req,out);
	        out.println("</body></html>");
	        out.close();
	        return;
	    }

        String returnto = req.getParameter("returnto");
        if (returnto != null)
		{
            //  wait a little for the command to take effect before redirecting
            Thread.sleep(100);
            resp.sendRedirect(returnto);
            return;
        }
		else
		{
		    // return HTML page
		    htmlHeaders(resp);
		    noCacheHeaders(resp);
		    PrintWriter out = resp.getWriter();
		    xhtmlHeaders(out);
		    out.println("<head>");
		    jsCssImport(req, out);
		    out.println("<title>InternalCommand</title></head>");
		    out.println("<body>");
		    printTitle(out,"");
		    out.println("<div id=\"content\">");
		    out.print("Applied command on system messages");
		    out.println("</div>");
		    printMenu(req,out);
		    out.println("</body></html>");
		    out.close();
		} 
		return;
	}
}
