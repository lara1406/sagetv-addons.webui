
package net.sf.sageplugins.webserver;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;

/**
 *
 * @author Owner
 *
 * 
 *
 */
public class IRSuggestionsServlet extends SageServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -8532813573695468297L;
    static String[][] GROUPING_OPTS=new String[][]{
			{"None","No Grouping"},
			{"GetAiringTitle","Title"},
			{"GetAiringChannelName","Channel"},
			{"GetShowCategory","Category"},
	};
	static String[][] SORTING_OPTS=new String[][]{
            {"Intelligent","By Suggestion Strength ('Intelligent')"},
			{"GetAiringStartTime","By Date"},
			{"GetAiringTitle","By Title"},
			{"GetAiringChannelName","By Channel Name"},
			{"GetShowCategory","By Category"},
            {"GetShowEpisode","By Episode Name"},
	};

	static String[][] SORTING_DIR=new String[][]{
			{"false","Forward"},
			{"true","Reverse"},
	};
    static final String[][] AIRING_ACTIONS_OPT=new String[][]{
        {"SetDontLike","Set Don't Like"},
        {"ClearDontLike","Clear Don't Like"},
        {"SetWatched","Set Watched"},
        {"ClearWatched","Clear Watched"},
    };	
	/* (non-Javadoc)
	 * @see net.sf.sageplugins.webserver.SageServlet#doServletGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		
        // get Intelligent Suggestions in a similar style to Sage Recordings
        Object filelist=SageApi.Api("GetSuggestedIntelligentRecordings");

        String xml=req.getParameter("xml");
        if ( xml != null && xml.equalsIgnoreCase("yes")) {
            // output XML
            SendXmlResult(req,resp,filelist,"intelligent_suggestions.xml");
            return;
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
		    out.println("<title>Sage Intelligent Suggestions</title>");
            String rssurl=GetRssUrl(req, "IRSuggestions");
            out.println("<link rel=\"alternate\" type=\"application/rss+xml\""
                    +" href=\""+rssurl+"\""
                    +" title=\"Sage Intelligent Suggestions RSS feed\"/>");
		    out.println("</head>");
		    out.println("<body>");
		    
            out.println(String.format("<div id=\"menuContainer\"></div>%s<div id=\"title\">", SageServlet.isTitleBroken(req) ? "<br/>" : "") +
                    "<h1><a href=\"index.html\" title=\"home\"><img id=\"logoimg\" src=\"sagelogo.gif\" alt=\"SageTV logo\" title=\"Home Screen\" border=\"0\"/></a>Sage Intelligent Suggestions\r\n"+
                    "<a href=\""+GetXmlUrl(req)+"\" title=\"Return page in XML\"><img src=\"xml_button.png\" alt=\"[XML]\"/></a>\r\n" +
                    "<a href=\""+rssurl+"\" title=\"RSS feed for this page\"><img src=\"rss_button.png\" alt=\"[RSS]\"/></a>\r\n" +
                    "</h1></div>");
            		    
		    out.println("<div id=\"content\">\r\n"+
		            "<form name=\"AiringsForm\" method=\"get\" action=\"AiringCommand\">\r\n"+
		            "<input type=\"hidden\" name=\"returnto\" value=\""+req.getRequestURI()+"\"/>\r\n"
		            );
		    
		    // get Intelligent Suggestions in a similar style to Sage Recordings
		    Object RecSchedList=SageApi.Api("GetScheduledRecordings");
            Object allConflicts=SageApi.Api("GetAiringsThatWontBeRecorded",new Object[]{Boolean.FALSE});
            Object unresolvedConflicts=SageApi.Api("GetAiringsThatWontBeRecorded",new Object[]{Boolean.TRUE});
            
		    boolean reverse1=GetOption(
		            req,
		            "irsuggestions_sort1_reverse",
		            "false").equalsIgnoreCase("true");
		    String sortmode1=GetOption(
		            req,
		            "irsuggestions_sort1",
		            "Intelligent"
		    );
		    boolean reverse2=GetOption(
		            req,
		            "irsuggestions_sort2_reverse",
		            "false").equalsIgnoreCase("true");
		    String sortmode2=GetOption(
		            req,
		            "irsuggestions_sort2",
		            "GetAiringStartTime"
		    );
		    try {
		        filelist = SageApi.Api("Sort",new Object[]{filelist, new Boolean(reverse2),sortmode2});
		    } catch (InvocationTargetException e) {
		        log("sorting on "+sortmode2+" failed",e);
		        out.println("<script  type=\"text/javascript\">DeleteOptionsCookie(\"irsuggestions_sort2\");</script>");	
		    }
		    try {
		        filelist = SageApi.Api("Sort",new Object[]{filelist, new Boolean(reverse1),sortmode1});
		    } catch (InvocationTargetException e) {
		        log("sorting on "+sortmode1+" failed",e);
		        out.println("<script  type=\"text/javascript\">DeleteOptionsCookie(\"irsuggestions_sort1\");</script>");    
		    }
		    String grouping = GetOption(
		            req,
		            "irsuggestions_grouping",
		    "GetShowCategory");
		    boolean showFileSize=false;

            AiringList.PrintPagedGroupedAiringList(this, 
                                              out, 
                                              filelist, 
                                              req, 
                                              grouping, 
                                              true,
                                              true, 
                                              showFileSize,
                                              RecSchedList,
                                              allConflicts,
                                              unresolvedConflicts,
                                              0);
		    out.println("Action on selected: <select name=\"command\">\r\n");
		    printOptionsList(out,AIRING_ACTIONS_OPT,null);
		    out.println("</select><input type=\"submit\" value=\"Set\"/>");
		    out.println("</form>");
		    
		    
		    
		    out.println("<div id=\"options\" class=\"options\">\r\n" +
		            "<h2><a name=\"options\">Sage Intelligent Suggestions Display Options:</a></h2>\r\n" +
		            "<form method='get' action='SetOptions'>\r\n" +
		            "<input type=\"hidden\" name=\"returnto\" value=\""+req.getRequestURI()+"\"/>\r\n"+
		    "<dl><dt>Primary&nbsp;Sort&nbsp;by:&nbsp;</dt><dd>");
		    
		    PrintOptionsDropdown(req,out,"irsuggestions_sort1","Intelligent",SORTING_OPTS);
		    out.println("&nbsp;Direction:&nbsp;");
		    PrintOptionsDropdown(req,out,"irsuggestions_sort1_reverse","false",SORTING_DIR);
		    
		    out.println("</dd><dt>Secondary&nbsp;Sort&nbsp;by:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"irsuggestions_sort2","GetAiringStartTime",SORTING_OPTS);
		    out.println("&nbsp;Direction:&nbsp;");
		    PrintOptionsDropdown(req,out,"irsuggestions_sort2_reverse","false",SORTING_DIR);
		    
		    out.println("</dd><dt>Group&nbsp;by:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"irsuggestions_grouping","GetShowCategory",GROUPING_OPTS);
		    
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

