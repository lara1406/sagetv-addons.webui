
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
public class RecordingsServlet extends SageServlet {

	/**
     * 
     */
    private static final long serialVersionUID = 1844174102981825745L;
    static String[][] GROUPING_OPTS=new String[][]{
			{"##AsSageTV##","Same as SageTV"},
			{"None","No Grouping"},
			{"GetAiringTitle","Title"},
			{"GetAiringChannelName","Channel"},
			{"GetShowCategory","Category"},
	};
	static String[][] SORTING_OPTS=new String[][]{
			{"##AsSageTV##","Same as SageTV"},
			{"Intelligent","Intelligent"},
			{"GetAiringStartTime","Date"},
            {"GetOriginalAiringDate","Original Airing Date"},
			{"GetAiringTitle","Title"},
			{"GetAiringChannelName","Channel Name"},
			{"GetShowCategory","Category"},
	};

	static String[][] SORTING_DIR=new String[][]{
			{"##AsSageTV##","Same as SageTV"},
			{"false","Forward"},
			{"true","Reverse"},
	};
	
	/* (non-Javadoc)
	 * @see net.sf.sageplugins.webserver.SageServlet#doServletGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
        
	    Object filelist=SageApi.Api("GetMediaFiles");
	    boolean include_achived=GetOptionOrProfile(
	            req,
	            ENABLE_DISABLE_OPTS,
	            "sage_recordings_includes_archived_files",
	            "nielm/webserver/sage_recordings_includes_archived_files",
	            "true",
	    "true").equalsIgnoreCase("true");
	    if ( ! include_achived )
	        filelist=SageApi.Api("FilterByBoolMethod",new Object[]{filelist, "IsLibraryFile", Boolean.FALSE});
	    filelist=SageApi.Api("FilterByBoolMethod",new Object[]{filelist, "IsCompleteRecording|IsManualRecord", Boolean.TRUE});
	    Object RecordedFiles=SageApi.Api("FilterByBoolMethod",new Object[]{filelist, "IsTVFile", Boolean.TRUE});
	    Object CurrentlyRecordingFiles=SageApi.Api("GetCurrentlyRecordingMediaFiles",null);
	    boolean reverse=GetOptionOrProfile(
	            req,
	            ENABLE_DISABLE_OPTS,
	            "sagetv_recordings_sort_reverse",
	            "sagetv_recordings_sort_reverse",
	            "false",
	    "false").equalsIgnoreCase("true");
	    Boolean reverseObj=new Boolean(reverse);
	    String sortmode=GetOptionOrProfile(
	            req,
	            SORTING_OPTS,
	            "sagetv_recordings_sort",
	            "sagetv_recordings_sort",
	            "##AsSageTV##",
	            "Intelligent"
	    );
	    if ( sortmode.equals("Intelligent")){
	        RecordedFiles=SageApi.Api("Sort",new Object[]{RecordedFiles, reverseObj, "Intelligent"});
	        if ( reverse ) {
	            filelist = SageApi.Api("DataUnion",new Object[]{RecordedFiles, CurrentlyRecordingFiles});
	        } else {
	            filelist = SageApi.Api("DataUnion",new Object[]{CurrentlyRecordingFiles,RecordedFiles});
	        }
	    } else {
	        RecordedFiles = SageApi.Api("DataUnion",new Object[]{CurrentlyRecordingFiles,RecordedFiles});
	        try {
	            filelist = SageApi.Api("Sort",new Object[]{RecordedFiles, reverseObj,sortmode});
	        } catch (InvocationTargetException e) {
	            log("sorting on "+sortmode+" failed",e);
	        }	
	    }
	    String filter=GetOptionOrProfile(
	            req,
	            FILTER_OPTS,
	            "sagetv_recordings_filter",
	            "sagetv_recordings_filter",
	            "##AsSageTV##",
	    "");
	    if ( filter.length()>0){
	        try {
	            filelist=SageApi.Api("FilterByBoolMethod",new Object[]{filelist, filter, Boolean.TRUE});
	        } catch (InvocationTargetException e) {
	            log("Filtering on "+filter+" failed",e);
	        }
	    }
	    String grouping = GetOptionOrProfile(
	            req,
	            GROUPING_OPTS,
	            "sagetv_recordings_grouping",
	            "sagetv_recordings_grouping",
	            "##AsSageTV##",
	    "GetAiringTitle");

        String xml=req.getParameter("xml");
        if ( xml != null && xml.equalsIgnoreCase("yes")) {
            // output XML
            SendXmlResult(req,resp,filelist,"recordings.xml");
            return;
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
            out.println("<title>Sage Recordings</title>");
            out.println("</head>");
            out.println("<body onload=\"hideAll()\">");
            printTitleWithXml(out,"Sage Recordings",req);
            
         
            out.println("<div id=\"content\">\r\n");
            out.println("<form name=\"AiringsForm\" method=\"get\" action=\"MediaFileCommand\">");
            out.print("<input type=\"hidden\" name=\"returnto\" value=\""+req.getRequestURI()+(req.getQueryString()==null?"":"?"+req.getQueryString())+"\"/>");            

		    boolean showFileSize=GetOption(req,"ShowFileSize","true").equalsIgnoreCase("true");

            AiringList.PrintPagedGroupedAiringList(this, 
                                              out, 
                                              filelist, 
                                              req,
                                              grouping,
                                              true,
                                              true,
                                              showFileSize,
                                              null,
                                              null,
                                              null,
                                              0);
            out.println("\r\nAction on selected: <select name=\"command\">\r\n");
            printOptionsList(out, AiringCommandServlet.MEDIAFILE_ACTIONS_OPT,null);
            out.println("</select><input type=\"submit\" value=\"Execute\"/>\r\n");
            out.println("</form>\r\n");

		    
		    out.println("<div id=\"options\" class=\"options\">\r\n" +
		            "<h2><a name=\"options\">Sage Recordings Display Options:</a></h2>\r\n" +
		            "<form method='get' action='SetOptions'>\r\n" +
		            "<input type=\"hidden\" name=\"returnto\" value=\"Recordings\"/>\r\n"+
		    "<dl><dt>Sort&nbsp;by:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"sagetv_recordings_sort","sagetv_recordings_sort","##AsSageTV##",SORTING_OPTS);
		    out.println("&nbsp;Direction:&nbsp;");
		    PrintOptionsDropdown(req,out,"sagetv_recordings_sort_reverse","sagetv_recordings_sort_reverse","##AsSageTV##",SORTING_DIR);
		    out.println("</dd><dt>Group&nbsp;by:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"sagetv_recordings_grouping","sagetv_recordings_grouping","##AsSageTV##",GROUPING_OPTS);
		    out.println("</dd><dt>Filter:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"sagetv_recordings_filter","sagetv_recordings_filter","##AsSageTV##",FILTER_OPTS);
		    out.println("</dd><dt>Channel&nbsp;Logos:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"UseChannelLogos","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Include Archived Files:</dt><dd>");
		    PrintOptionsDropdown(req,out,"sage_recordings_includes_archived_files","nielm/webserver/sage_recordings_includes_archived_files","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Show&nbsp;Markers:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"ShowMarkers","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Show&nbsp;Ratings:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"ShowRatings","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Include&nbsp;EpisodeID:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"ShowEpisodeID","false",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Include&nbsp;File&nbsp;Size:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"ShowFileSize","true",ENABLE_DISABLE_OPTS);
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

