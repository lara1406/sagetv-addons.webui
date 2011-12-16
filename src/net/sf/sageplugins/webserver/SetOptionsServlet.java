/*
 * Created on Nov 4, 2004
 *
 * 
 *
 */
package net.sf.sageplugins.webserver;

import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Owner
 *
 * 
 *
 */
public class SetOptionsServlet extends SageServlet {

	/**
     * 
     */
    private static final long serialVersionUID = -2899992728011249131L;

    protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		
		Map<?, ?> params=req.getParameterMap();
		Set<?> paramnames=params.keySet();
		for ( Iterator<?> i=paramnames.iterator();
		       i.hasNext();) {
			String paramname=URLDecoder.decode((String)i.next(),charset);
			if ( ! paramname.equals("returnto")) {
				String paramvalues[]=(String[])params.get(paramname);
				if ( paramvalues.length>=1 && paramvalues[0]!=null){
					String paramvalue=paramvalues[0];
					Cookie cookie=new Cookie(paramname,paramvalue);
					cookie.setMaxAge(60*60*24*365*10);
					resp.addCookie(cookie);
					//System.out.println("set cookie "+paramname+"="+paramvalue);
				}
			}
		}
		
		String returnto = req.getParameter("returnto");
		if (returnto==null) {
			//	set headers before accessing the Writer
			htmlHeaders(resp);
			noCacheHeaders(resp);
			PrintWriter out = resp.getWriter();
			xhtmlHeaders(out);
			out.println("<html>" +
			  "<head><title>SetOptions</title>");
			jsCssImport(req, out);
			out.println("</head><body>");
			printTitle(out,"SageTV Command");
			out.println("<div id=\"content\">");
			out.println("Options have been set");
			out.println("</div></body></html>");
			out.close();
		} else {
			resp.sendRedirect(returnto);
		}
	}
}
