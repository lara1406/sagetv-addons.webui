
package net.sf.sageplugins.webserver;


import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;
import net.sf.sageplugins.sageutils.Translate;

/**
 * @author Owner
 *
 * 
 *
 */
public class RecordingScheduleServlet extends SageServlet {

	/**
     * 
     */
    private static final long serialVersionUID = 7941476880940090903L;
    /* (non-Javadoc)
	 * @see net.sf.sageplugins.webserver.SageServlet#doServletGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {

        String encodername=req.getParameter("encoder");
        Object encoder=null;
        Object encoders=SageApi.Api("GetActiveCaptureDevices");
        Object filelist;
            
        if (encodername==null || encodername.equals("all")){
            encodername=null;
            filelist=SageApi.Api("GetScheduledRecordings");
        } else {
            for(int i=0; i<SageApi.Size(encoders); i++)
                if ( encodername.equals(SageApi.GetElement(encoders,i).toString())) {
                    encoder=SageApi.GetElement(encoders,i);
                    break;
                }
            if ( encoder==null) {
                filelist=SageApi.Api("GetScheduledRecordings");
            } else {
                filelist=SageApi.Api("GetScheduledRecordingsForDevice",new Object[]{encoder});
            }
        }
        /*
         FROM STV: 
         Schedule = GetScheduledRecordings()
         IF Size(GetProperty("recording_schedule_filter", "")) > 0
         Schedule = FilterByBoolMethod(Schedule, GetProperty("recording_schedule_filter", ""), true)
         */
        
        String filter=GetOptionOrProfile(
                req,
                FILTER_OPTS,
                "recording_schedule_filter",
                "recording_schedule_filter",
                "##AsSageTV##",
                "");
        if ( filter!=null && filter.length()>0) {
            try {
                filelist=SageApi.Api("FilterByBoolMethod",new Object[]{filelist, filter, Boolean.TRUE});
            } catch (InvocationTargetException e) {
                log("filtering on "+filter+" failed",e);
            }
        }
		
        String xml=req.getParameter("xml");
        if ( xml != null){
            if ( xml.equalsIgnoreCase("yes")) {
                // output XML
                SendXmlResult(req,resp,filelist,"recording_schedule.xml");
                return;
            }
        }
		
		htmlHeaders(resp);
		noCacheHeaders(resp);
		PrintWriter out = getGzippedWriter(req,resp);
        // must catch and report all errors within Gzipped Writer
		try {
		    xhtmlHeaders(out);
		    out.println("<head>");
		    jsCssImport(req, out);
		    printMenu(out);
            String rssurl = GetRssUrl(req, "RecordingSchedule");
		    out.println("<title>Recording Schedule</title>");
		    out.println("<link rel=\"alternate\" type=\"application/rss+xml\""
		            +" href=\""+rssurl.toString()+"\""
		            +" title=\"Sage Recording Schedule RSS feed\"/>");
            
		    out.println("</head>");
		    out.println("<body>");
            out.println("<div id=\"menuContainer\"></div><div id=\"title\">"+
                    "<h1><a href=\"index.html\" title=\"home\"><img id=\"logoimg\" src=\"sagelogo.gif\" alt=\"SageTV logo\" title=\"Home Screen\" border=\"0\"/></a>Recording Schedule\r\n"+
                    "<a href=\""+GetXmlUrl(req)+"\" title=\"Return page in XML\"><img src=\"xml_button.png\" alt=\"[XML]\"/></a>\r\n" +
                    "<a href=\""+rssurl.toString()+"\" title=\"RSS feed for this page\"><img src=\"rss_button.png\" alt=\"[RSS]\"/></a>\r\n" +
                    "</h1></div>");
		    
		    out.println("<div id=\"content\">");
	        if ( encodername!=null && encoder==null) {
		            out.println("<h3>Error: unknown encoder: "+encodername+"</h3>");
            }		    
		    
		    out.println("<form action=\""+req.getRequestURI()+"\" method=\"get\">");
		    out.println(" For encoder: ");
		    out.println("  <select name=\"encoder\" onchange=\"this.form.submit()\">");
		    out.print("  <option value=\"all\"");
		    if ( encodername==null)
		        out.print(" selected=\"selected\"");
		    out.println(">all</option>");
		    
		    for(int i=0; i<SageApi.Size(encoders); i++){
		        Object thisenc=SageApi.GetElement(encoders,i);
		        out.print("  <option value=\""+thisenc.toString()+"\"");
		        if ( thisenc==encoder)
		            out.print(" selected=\"selected\"");
		        out.println(">"+Translate.encode(thisenc.toString())+"</option>");
		    }
		    out.println("    </select>\r\n" +
                    "      <input type=\"submit\" value=\"Refresh\"/>\r\n" +
                    "</form>");
		    out.println("<form name=\"AiringsForm\" method=\"get\" action=\"AiringCommand\">");
		    out.print("<input type=\"hidden\" name=\"returnto\" value=\""+req.getRequestURI()+"?"+req.getQueryString()+"\"/>");
		      
		  
            
		    // get Rec sched
            boolean showFileSize=false;
            Object allConflicts=SageApi.Api("GetAiringsThatWontBeRecorded",new Object[]{Boolean.FALSE});
            Object unresolvedConflicts=SageApi.Api("GetAiringsThatWontBeRecorded",new Object[]{Boolean.TRUE});


            AiringList.PrintPagedAiringList(this, 
                                              out, 
                                              filelist, 
                                              req, 
                                              true,
                                              true, 
                                              showFileSize,
                                              filelist,
                                              allConflicts,
                                              unresolvedConflicts,
                                              0);
            
		    out.println("Action on selected: <select name=\"command\">\r\n");
		    printOptionsList(out,AiringCommandServlet.AIRING_ACTIONS_OPT,null);
		    out.println("</select><input type=\"submit\" value=\"Set\"/>");
		    out.println("</form>\r\n");
		    
		    out.println("<div id=\"options\" class=\"options\">\r\n" +
		            "<h2><a name=\"options\">Sage Recording Schedule Display Options:</a></h2>\r\n" +
		            "<form method='get' action='SetOptions'>\r\n" +
		            "<input type=\"hidden\" name=\"returnto\" value=\"RecordingSchedule\"/>\r\n" +
		    "<dl><dt>Filter&nbsp;by:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"recording_schedule_filter","recording_schedule_filter","##AsSageTV##",FILTER_OPTS);
		    out.println("</dd><dt>Channel&nbsp;Logos:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"UseChannelLogos","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Show&nbsp;Markers:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"ShowMarkers","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Show&nbsp;Ratings:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"ShowRatings","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Include&nbsp;EpisodeID:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"ShowEpisodeID","false",ENABLE_DISABLE_OPTS);
            out.println("</dd><dt>Results&nbsp;Per&nbsp;Page</dt><dd>");
            PrintOptionsDropdown(req,out,"pagelen",Integer.toString(AiringList.DEF_NUM_ITEMS),AiringList.NUM_ITEMS_PER_PAGE_OPTS);
		    out.println("</dd></dl><noscript><input type=\"submit\" value=\"SetOptions\"/></noscript></form>");
		    out.println("</div>\r\n" +// options
		    "<script type=\"text/javascript\">hideOptions();</script>"); 
		    
            printFooter(req,out);
            out.println("</div>");//content
		    printMenu(req,out);
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
