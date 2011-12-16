package net.sf.sageplugins.webserver;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;
import net.sf.sageplugins.sageutils.SystemMessageApi;

public class SystemMessagesServlet extends SageServlet {


    /**
	 * 
	 */
	private static final long serialVersionUID = -4584455130711765086L;

	protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
	throws Exception {

        Object systemMessagesList = null;
        int systemMessagesListSize = 0;

        if (SystemMessageApi.isSupported()) {
            systemMessagesList = SageApi.Api("GetSystemMessages");
            systemMessagesList = SageApi.Api("Sort", new Object[]{systemMessagesList, Boolean.TRUE, "GetSystemMessageTime"});
            systemMessagesListSize = SageApi.Size(systemMessagesList);
        }

        String xml=req.getParameter("xml");
        if ( xml != null){
            if ( xml.equalsIgnoreCase("yes")) {
                // output XML
                SendXmlResult(req,resp,systemMessagesList,"system_messages.xml");
                return;
            }
        }

        // output HTML
		htmlHeaders(resp);
		noCacheHeaders(resp);
		PrintWriter out = getGzippedWriter(req,resp);
		// must catch and report all errors within Gzipped Writer
		try {
			xhtmlHeaders(out);
			out.println("<head>");
			jsCssImport(req, out);
            String rssurl = GetRssUrl(req, "SystemMessages");
			out.println("<title>System Messages</title>");
			if (SystemMessageApi.isSupported())
			{
			    out.println("<link rel=\"alternate\" type=\"application/rss+xml\""
			            +" href=\""+rssurl.toString()+"\""
			            +" title=\"Sage System Message RSS feed\"/>");
			}
			out.println("</head>");
			out.println("<body>");

            out.println("<div id=\"title\">");
            out.println("   <h1><a href=\"index.html\" title=\"home\"><img id=\"logoimg\" src=\"sagelogo.gif\" alt=\"SageTV logo\" title=\"Home Screen\" border=\"0\"/></a>System Messages");
            if (SystemMessageApi.isSupported())
            {
                out.println("      <a href=\""+GetXmlUrl(req)+"\" title=\"Return page in XML\"><img src=\"xml_button.png\" alt=\"[XML]\"/></a>");
                out.println("      <a href=\""+rssurl.toString()+"\" title=\"RSS feed for this page\"><img src=\"rss_button.png\" alt=\"[RSS]\"/></a>");
            }
            out.println("   </h1></div>");

			out.println("<div id=\"content\">");

			if ( !SystemMessageApi.isSupported()){
				out.println("Requires Sage 6.5.17 or higher...");
			} else {

				out.println("   <form name=\"AlertsForm\" method=\"get\" action=\"SystemMessagesCommand\">");
				out.println("      <input type=\"hidden\" name=\"returnto\" value=\""+req.getRequestURI()+(req.getQueryString()==null?"":"?"+req.getQueryString())+"\"/>");
		        
				if ( systemMessagesListSize == 0){
					out.println("      <p align=\"center\">There are no system messages at this time.</p>");
				} else {

					out.println("      <div class=\"alerts\" id=\"alertsList\">");
					out.println("         <div class=\"exphideall\">");
                    out.println("            <a href=\"javascript:expandAllMessageDetails(" + systemMessagesListSize + ")\">[Expand all]</a>");
                    out.println("            <a href=\"javascript:hideAllMessageDetails(" + systemMessagesListSize + ")\">[Collapse all]</a>");
					out.println("            <a href=\"javascript:checkAllSystemMessages(true)\">[Select all]</a>");
					out.println("            <a href=\"javascript:checkAllSystemMessages(false)\">[Unselect all]</a>");
					out.println("         </div>");

                    for (int i = 1; i <= SageApi.Size(systemMessagesList); i++) {
						Object currentSystemMessage = SageApi.GetElement(systemMessagesList, i - 1);
						int level = SageApi.IntApi("GetSystemMessageLevel", new Object[]{currentSystemMessage});
                        String messageString = SageApi.StringApi("GetSystemMessageString", new Object[]{currentSystemMessage});
                        int typeCode = SageApi.IntApi("GetSystemMessageTypeCode", new Object[]{currentSystemMessage});
                        String typeName = SageApi.StringApi("GetSystemMessageTypeName", new Object[]{currentSystemMessage});
                        int count = SageApi.IntApi("GetSystemMessageRepeatCount", new Object[]{currentSystemMessage});
                        count = Math.max(1, count);
                        Long time = (Long) SageApi.Api("GetSystemMessageTime", new Object[]{currentSystemMessage}); 
                        Long endTime = (Long) SageApi.Api("GetSystemMessageEndTime", new Object[]{currentSystemMessage});
                        
                        String dateString = (String) SageApi.Api("PrintDateLong", time);
                        String timeString = (String) SageApi.Api("PrintTime", time);

                        String endDateString = (String) SageApi.Api("PrintDateLong", endTime);
                        String endTimeString = (String) SageApi.Api("PrintTime", endTime);
                        
                        String messageKey = SystemMessageApi.getKey(currentSystemMessage);

                        String iconTooltip = SystemMessageApi.ALERT_LEVEL_TEXT_PREFIX[level] + " - Alert Level " + (level);
                        out.println("         <a name=\"" + messageKey + "\"></a>");
                        out.println("         <table class=\"alertcell\" width=\"100%\">");
                        out.println("         <tr><td class=\"alertcellborder\">");
                        out.println("            <table width=\"100%\">");
                        out.println("               <tr>");
                        out.println("                  <td class='checkbox'><nobr><input type='checkbox' name=\"SystemMessageApi\" value=\"" + messageKey + "\"/> " + i + ") </nobr></td>");
                        out.println("                  <td class=\"sysalertmarkercell\">");
                        out.println("                     <img src=\"MarkerSysAlert" + level + ".png\" alt=\"" + iconTooltip + "\" title=\"" + iconTooltip + "\" width=\"36\" height=\"36\"/>");
                        out.println("                  </td>");
                        out.println("                  <td class=\"titlecell\" onclick=\"showDetail('message" + i + "detail')\">");
                        out.println("                     <div>");
                        out.println("                        <a href=\"javascript:NullFunc()\">" + typeName + "</a>");
                        out.println("                     </div>");
                        out.println("                  </td>");
                        out.println("                  <td class=\"countcell\">");
                        out.println("                     <div>");
                        out.println("                        (x" + count + ")");
                        out.println("                     </div>");
                        out.println("                  </td>");
                        out.println("                  <td class=\"datecell\">");
                        out.println("                     <div>");
                        out.println("                        " + dateString + "<br/>" + timeString);
                        out.println("                     </div>");
                        out.println("                  </td>");
                        out.println("               </tr>");
                        out.println("            </table>");
                        out.println("            <div id=\"message" + i + "detail\" class=\"messagedetail\">");
                        out.println("               <table>");
                        out.println("                  <tr>");
                        out.println("                     <td class=\"leftpadding\"></td>");
                        out.println("                     <td class=\"content\">");
                        out.println("                        <p>" + SystemMessageApi.ALERT_LEVEL_TEXT_PREFIX[level] + " Message Details</p>");
                        out.println("                        <p>" + messageString + "</p>");
                        if (count > 1)
                        {
                            out.println("                        <p>" + count + " Messages. Last Occurrence: " + endDateString + " " + endTimeString + "</p>");
                        }
                        out.println("                     </td>");
                        out.println("                     <td class=\"rightpadding\"></td>");
                        out.println("                  </tr>");
                        out.println("               </table>");
                        out.println("            </div>");
                        out.println("         </td></tr>");
                        out.println("         </table>");//alertcell
					}

					out.println("         <div class=\"exphideall\">");
                    out.println("            <a href=\"javascript:expandAllMessageDetails(" + systemMessagesListSize + ")\">[Expand all]</a>");
                    out.println("            <a href=\"javascript:hideAllMessageDetails(" + systemMessagesListSize + ")\">[Collapse all]</a>");
					out.println("            <a href=\"javascript:checkAllSystemMessages(true)\">[Select all]</a>");
			        out.println("            <a href=\"javascript:checkAllSystemMessages(false)\">[Unselect all]</a>");
					out.println("         </div>");
                    out.println("      </div>");//alertslist

                    Integer alertLevel = (Integer) SageApi.Api("GetSystemAlertLevel");
                    if (alertLevel.intValue() > 0) {
                        out.println("      <input type=\"submit\" name=\"ResetSystemAlertLevel\" value=\"Reset Alert Level\"/>");
                    }
					out.println("      <input type=\"submit\" name=\"DeleteSystemMessage\" value=\"Delete Selected Messages\"/>");
					out.println("      <input type=\"submit\" name=\"DeleteAllSystemMessages\" value=\"Delete All Messages\"/>");
					out.println("   </form>");
				}
			}		    
			printFooter(req,out);
			out.println("</div>");//content
			printMenu(out);
			out.println("</body></html>");
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
