/*
 * Created on Nov 6, 2005
 *
 */
package net.sf.sageplugins.webserver;

import net.sf.sageplugins.sageutils.SageApi;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Owner
 *
 */
public class FavoritesServlet extends SageServlet {

	/**
     * 
     */
    private static final long serialVersionUID = 8134419957310995429L;

    /* (non-Javadoc)
	 * @see net.sf.sageplugins.webserver.SageServlet#doServletGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {

        Object favoritesList = SageApi.Api("GetFavorites");
        favoritesList = SageApi.Api("Sort", new Object[]{favoritesList, Boolean.FALSE, "FavoritePriority"});
        
        String xml=req.getParameter("xml");
        if ( xml != null && xml.equalsIgnoreCase("yes")) {
            // output XML
            SendXmlResult(req,resp,favoritesList,"favorites.xml");
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
		    out.println("    <title>Sage Favorites</title>");
		    out.println("</head>");
		    out.println("<body>");
		    printTitleWithXml(out,"Sage Favorites",req);
		    
		    out.println("<div id=\"content\">");
		    
		    out.println("<div class=\"airings\">");
		    
		    out.println("    <div>New by: ");
		    out.println("    <a href=\"FavoriteDetails?AddTitle=\">[Title]</a>");
		    out.println("    <a href=\"FavoriteDetails?AddPerson=\">[Actor]</a>");
		    out.println("    <a href=\"FavoriteDetails?AddCategory=\">[Category]</a>");
		    out.println("    <a href=\"FavoriteDetails?AddKeyword=\">[Keyword]</a>");
		    out.println("    </div>");
		    
		    boolean showFullDescription=GetOption(req,"fav_show_full_desc","false").equalsIgnoreCase("true");
		    boolean showChannels=GetOption(req,"fav_show_channels","true").equalsIgnoreCase("true");
		    boolean usechannellogos=GetOption(req,"UseChannelLogos","true").equalsIgnoreCase("true");
		    boolean showRatings=GetOption(req,"ShowRatings","true").equalsIgnoreCase("true");
		    boolean showFirstRunsAndReRuns=GetOption(req,"fav_show_first_runs_and_reruns","true").equalsIgnoreCase("true");
		    boolean showAutoDelete=GetOption(req,"fav_show_auto_delete","true").equalsIgnoreCase("true");
		    boolean showKeepAtMost=GetOption(req,"fav_show_keep_at_most","true").equalsIgnoreCase("true");
		    boolean showPadding=GetOption(req,"fav_show_padding","true").equalsIgnoreCase("true");
		    boolean showQuality=GetOption(req,"fav_show_quality","true").equalsIgnoreCase("true");
		    boolean showTimeslot=GetOption(req,"fav_show_timeslot","true").equalsIgnoreCase("true");

		    if (favoritesList == null) {
		        out.println("    <p align=\"center\">No favorites exist.</p>");
		    } else {
		        
		        try {
		            Favorite previousFavorite = null;
		            Favorite currentFavorite = null;
		            Favorite nextFavorite = null;
		            for (int j = 0; j < SageApi.Size(favoritesList); j++) {
		                previousFavorite = currentFavorite; // use the one from the previous loop.  null the first time
		                currentFavorite = (j == 0 ? new Favorite(SageApi.GetElement(favoritesList,j)) : nextFavorite); // create new one the first time, otherwise use the 'next' one from the previous loop
		                nextFavorite = ((j >= SageApi.Size(favoritesList) - 1) ? null : new Favorite(SageApi.GetElement(favoritesList,j+1))); // null if at the end, otherwise create a new one
		                currentFavorite.printFavoriteTableCell(out, previousFavorite, currentFavorite, nextFavorite,false /* JR useSageEncoder JR */,
		                		showFullDescription,
		                		showFirstRunsAndReRuns,
		                		showAutoDelete,
		                		showKeepAtMost,
		                		showPadding,
		                		showQuality,
		                		showRatings,
		                		showTimeslot,
		                		showChannels,
		                		usechannellogos,
		                		j+1);
		            }
		        } catch (Exception e) {
		            e.printStackTrace(out);
		        }
		    }    
		    
		    out.println("    <div>New by: ");
		    out.println("    <a href=\"FavoriteDetails?AddTitle=\">[Title]</a>");
		    out.println("    <a href=\"FavoriteDetails?AddPerson=\">[Actor]</a>");
		    out.println("    <a href=\"FavoriteDetails?AddCategory=\">[Category]</a>");
		    out.println("    <a href=\"FavoriteDetails?AddKeyword=\">[Keyword]</a>");
		    out.println("    </div>");
		    
		    out.println("<br/><a onclick=\"javascript:showOptions()\" href=\"#options\">[Show Options]</a>");

		    out.println("</div>"); // airings
		    
		    out.println("<div id=\"options\" class=\"options\">\r\n" +
		            "<h2><a name=\"options\">Sage Favorites Display Options:</a></h2>\r\n" +
		            "<form method='get' action='SetOptions'>\r\n" +
		            "<input type=\"hidden\" name=\"returnto\" value=\"Favorites\"/>\r\n"+
		    		"<dl>");
		    out.println("<dt>Show&nbsp;Full&nbsp;Description:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"fav_show_full_desc","false",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Show&nbsp;First&nbsp;Runs&nbsp;and&nbsp;ReRuns:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"fav_show_first_runs_and_reruns","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Show&nbsp;Auto&nbsp;Delete:</dt><dd>");
		    PrintOptionsDropdown(req,out,"fav_show_auto_delete","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Show&nbsp;Keep&nbsp;At&nbsp;Most:</dt><dd>");
		    PrintOptionsDropdown(req,out,"fav_show_keep_at_most","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Show&nbsp;Padding:</dt><dd>");
		    PrintOptionsDropdown(req,out,"fav_show_padding","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Show&nbsp;Quality:</dt><dd>");
		    PrintOptionsDropdown(req,out,"fav_show_quality","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Show&nbsp;Ratings:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"ShowRatings","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Show&nbsp;Timeslot:</dt><dd>");
		    PrintOptionsDropdown(req,out,"fav_show_timeslot","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Show&nbsp;Channels:</dt><dd>");
		    PrintOptionsDropdown(req,out,"fav_show_channels","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd><dt>Channel&nbsp;Logos:&nbsp;</dt><dd>");
		    PrintOptionsDropdown(req,out,"UseChannelLogos","true",ENABLE_DISABLE_OPTS);
		    out.println("</dd></dl><noscript><input type=\"submit\" value=\"SetOptions\"/></noscript></form>");
		    out.println("</div>\r\n" +// options
		    "<script type=\"text/javascript\">hideOptions();</script>");

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

