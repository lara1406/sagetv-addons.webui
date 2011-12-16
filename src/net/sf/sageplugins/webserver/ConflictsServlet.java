
package net.sf.sageplugins.webserver;

import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

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
public class ConflictsServlet extends SageServlet {

	/**
     * 
     */
    private static final long serialVersionUID = -5845982254897873358L;

    protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		
        String xml=req.getParameter("xml");
        if ( xml != null){
            if ( xml.equalsIgnoreCase("yes")) {
                // output XML
                Object missedairings=SageApi.Api("GetAiringsThatWontBeRecorded",new Object[]{Boolean.FALSE});
                SendXmlResult(req,resp,missedairings,"recording_conflicts.xml");
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
    		out.println("<title>Recording Conflicts</title>");
            String rssurl=GetRssUrl(req, "Conflicts");
            out.println("<link rel=\"alternate\" type=\"application/rss+xml\""
                    +" href=\""+rssurl+"\""
                    +" title=\"Sage Recording Conflicts RSS feed\"/>");
            
    		out.println("</head>");
    		out.println("<body>");
            out.println("<div id=\"title\">"+
                    "<h1><a href=\"index.html\" title=\"home\"><img id=\"logoimg\" src=\"sagelogo.gif\" alt=\"SageTV logo\" title=\"Home Screen\" border=\"0\"/></a>Recording Conflicts\r\n"+
                    "<a href=\""+GetXmlUrl(req)+"\" title=\"Return page in XML\"><img src=\"xml_button.png\" alt=\"[XML]\"/></a>\r\n" +
                    "<a href=\""+rssurl+"\" title=\"RSS feed for this page\"><img src=\"rss_button.png\" alt=\"[RSS]\"/></a>\r\n" +
                    "</h1></div>");
    		out.println("<div id=\"content\">");

            Object UnresolvedConflicts=SageApi.Api("GetAiringsThatWontBeRecorded",new Object[]{Boolean.TRUE});
            Object missedairings=SageApi.Api("GetAiringsThatWontBeRecorded",new Object[]{Boolean.FALSE});

            if (SageApi.Size(missedairings) == 0)
            {
                out.println("<p>There are no conflicts to report at this time.</p>");
            }
            else
            {
                out.println("<p>The following airings will not be recorded due to conflicts with other recordings:</p>");

        		missedairings=SageApi.Api("Sort",new Object[]{missedairings,Boolean.FALSE,"GetAiringTitle"});
        		missedairings=SageApi.Api("Sort",new Object[]{missedairings,Boolean.FALSE,"GetAiringStartTime"});
    		
        		for ( int i=0; i<SageApi.Size(missedairings); i ++){
        			Object MissedAiring=SageApi.GetElement(missedairings,i);
        			Airing airing=new Airing(MissedAiring);
                    out.println("<div class=\"conflict\">");
        			String descr=(String)SageApi.Api("PrintAiringShort",new Object[]{MissedAiring});
        			Object MissedFavorite=SageApi.Api("GetFavoriteForAiring",new Object[]{MissedAiring});
                    int faveid=0;
        			String favedesc=null;
                    if (MissedFavorite != null)
                    {
                        faveid=SageApi.IntApi("GetFavoriteID",new Object[]{MissedFavorite});
                        favedesc=(String)SageApi.Api("GetFavoriteDescription",new Object[]{MissedFavorite});
                    }
        			boolean isUnresolved=false;
        			out.print("<h3><a name=\"AiringId"+airing.id+"\">");
        			if ( SageApi.Size(SageApi.Api("DataIntersection",new Object[]{UnresolvedConflicts, MissedAiring}))>0){
        				isUnresolved=true;
        				out.println("<img src=\"conflicticon.gif\" title=\"Unresolved Conflict\" alt=\"Unresolved Conflict\"/>");
        			} else {
                        out.println("<img src=\"resolvedconflicticon.gif\" title=\"Resolved Conflict\" alt=\"Resolved Conflict\"/>");
                    }

        			out.println("</a><a href=\"DetailedInfo?"+airing.getIdArg()+"\" title=\"Show detailed info\">"+Translate.encode(descr)+"</a>");
        			if (MissedFavorite != null)
        			{
        			    out.println("<br/><a href=\"FavoriteDetails?FavoriteId="+faveid+"\" title=\"Show Favorite Details\">from favourite: "+Translate.encode(favedesc)+"</a></h3>");
        			}
    
    	
        			// find conflicts
        			Object channel=SageApi.Api("GetChannel",new Object[]{MissedAiring});
        			Object starttime=SageApi.Api("GetScheduleStartTime",new Object[]{MissedAiring});
        			Object endtime=SageApi.Api("GetScheduleEndTime",new Object[]{MissedAiring});
    			
        			Vector<Object> ConflictingManuals=new Vector<Object>();
        			Vector<Object> ConflictingSingleFavourites=new Vector<Object>();
        			Vector<Object> ConflictingFavorites=new Vector<Object>();
    			
        			Object inputs=SageApi.Api("GetConfiguredCaptureDeviceInputs");
        			for ( int inpnum=0;inpnum<SageApi.Size(inputs); inpnum++){
        				Object input=SageApi.GetElement(inputs,inpnum);
        				Object lineup=SageApi.Api("GetLineupForCaptureDeviceInput",new Object[]{input});
        				if ( SageApi.booleanApi("IsChannelViewableOnLineup",new Object[]{channel, lineup})){
        					Object capdev=SageApi.Api("GetCaptureDeviceForInput",new Object[]{input});
        					//System.out.println("got capdev"+capdev.toString());
        					Object overlaps=SageApi.Api("GetScheduledRecordingsForDeviceForTime",new Object[]{
        							capdev, starttime, endtime});
        					//System.out.println("got "+SageApi.Size(overlaps)+" overlpas");
        					for ( int ovlapnum=0;ovlapnum<SageApi.Size(overlaps); ovlapnum++){
        						Object ConflictingRecord=SageApi.GetElement(overlaps,ovlapnum);
        						if (((Boolean)SageApi.Api("IsManualRecord",new Object[]{ConflictingRecord})).booleanValue()){
        							ConflictingManuals.add(ConflictingRecord);
        						} else {
        							ConflictingSingleFavourites.add(ConflictingRecord);
        							Object OtherFavorite = SageApi.Api("GetFavoriteForAiring",new Object[]{ConflictingRecord});
        							long OtherFavouriteTime=((Long)SageApi.Api("GetScheduleStartTime",new Object[]{ConflictingRecord})).longValue();
        							Date now=new Date();
        							if ( OtherFavorite != null 
        								&& ! OtherFavorite.equals(MissedFavorite)
        								&& OtherFavouriteTime>now.getTime() )
        								ConflictingFavorites.add(OtherFavorite);
        						}
        					}	
        				}
        			}
        			out.println("<div class=\"resolution\"><ul>");
    
        			if (isUnresolved && 
        				ConflictingManuals.size()>0
        				&& ConflictingSingleFavourites.size() ==0 
        				&& ConflictingFavorites.size() ==0 ) {
        			out.println("<li>" +
                            "<form method=\"post\" action=\"ResolveConflict\">" +
                            "<input type=\"hidden\" name=\"command\" value=\"ConfirmManRecOverride\"/>" +
                            "<input type=\"hidden\" name=\"returnto\" value=\"Conflicts\"/>" +
                            "<input type=\"hidden\" name=\"missedAiringId\" value=\""+airing.id+"\"/>" +
                            "<input type=\"submit\" value=\"Ignore this conflict\"/></form>" +
                            "</li>");
        			}
        			// conflicting manuals
        			for ( Iterator<Object> it=ConflictingManuals.iterator(); it.hasNext();){
        				Object conflict=it.next();
        				Airing cfair=new Airing(conflict);
    				
        				out.println("<li>"+
        				        "<form method=\"post\" action=\"ManualRecord\">" +
        				        "<input type=\"hidden\" name=\"command\" value=\"CancelRecord\"/>" +
        				        "<input type=\"hidden\" name=\"returnto\" value=\"Conflicts\"/>" +
        				        "<input type=\"hidden\" name=\"AiringId\" value=\""+cfair.id+"\"/>" +
        				        "<input type=\"submit\" value=\"Cancel the manual recording\"/>"+
        				        " of <a title=\"View detailed info\" href=\"DetailedInfo?"+cfair.getIdArg()+"\">"+Translate.encode(cfair.getAiringShortDescr())+"</a>" +
        				        "</form></li>");
        			}
        			if (MissedFavorite != null) {
        		    	for ( Iterator<Object> it=ConflictingSingleFavourites.iterator(); it.hasNext();){
        		    		Object conflict=it.next();
        	    			Airing cfair=new Airing(conflict);
    
            				out.println("<li>"+
                                        "<form method=\"post\" action=\"ManualRecord\">" +
                                        "<input type=\"hidden\" name=\"command\" value=\"Record\"/>" +
                                        "<input type=\"hidden\" name=\"returnto\" value=\"Conflicts\"/>" +
                                        "<input type=\"hidden\" name=\"AiringId\" value=\""+airing.id+"\"/>" +
                                        "<input type=\"submit\" value=\"Override the single favourite recording\"/>" +
                                        " of <a href=\"DetailedInfo?"+cfair.getIdArg()+"\">"+Translate.encode(cfair.getAiringShortDescr())+"</a>" +
        			    	            "</form></li>");
        		    	}
        	    		for ( Iterator<Object> it=ConflictingFavorites.iterator(); it.hasNext();){
            				Object cffave=it.next();
            				int cffaveid=SageApi.IntApi("GetFavoriteID",new Object[]{cffave});
            				String cffavedesc=SageApi.StringApi("GetFavoriteDescription",new Object[]{cffave});
    
        		    		out.println("<li>" +
                                "<form method=\"post\" action=\"ResolveConflict\">" +
                                "<input type=\"hidden\" name=\"command\" value=\"FavoriteOverride\"/>" +
                                "<input type=\"hidden\" name=\"returnto\" value=\"Conflicts\"/>" +
                                "<input type=\"hidden\" name=\"newpriofaveid\" value=\""+faveid+"\"/>" +
                                "<input type=\"hidden\" name=\"oldfaveid\" value=\""+cffaveid+"\"/>" +
                                "<input type=\"submit\" value=\"ALWAYS have\"/>" +
                                " <a href=\"FavoriteDetails?FavoriteId="+faveid+"\" title=\"Show Favorite Details\">favorite " + Translate.encode(favedesc) + "</a>" +
                                " override " +
                                "<a href=\"FavoriteDetails?FavoriteId="+cffaveid+"\" title=\"Show Favorite Details\">favorite " + Translate.encode(cffavedesc)+"</a>" +
                                "</form></li>");
                            out.println("<li>" +
                                    "<form method=\"post\" action=\"ResolveConflict\">" +
                                    "<input type=\"hidden\" name=\"command\" value=\"FavoriteOverride\"/>" +
                                    "<input type=\"hidden\" name=\"returnto\" value=\"Conflicts\"/>" +
                                    "<input type=\"hidden\" name=\"newpriofaveid\" value=\""+cffaveid+"\"/>" +
                                    "<input type=\"hidden\" name=\"oldfaveid\" value=\""+faveid+"\"/>" +
                                    "<input type=\"submit\" value=\"ALWAYS have\"/>" +
                                    " <a href=\"FavoriteDetails?FavoriteId="+cffaveid+"\" title=\"Show Favorite Details\">favorite " + Translate.encode(cffavedesc) + "</a>" +
                                    " override " +
                                    "<a href=\"FavoriteDetails?FavoriteId="+faveid+"\" title=\"Show Favorite Details\">favorite " + Translate.encode(favedesc)+"</a>" +
                                    "</form></li>");
        			    }
        			}
                    if (((Boolean)SageApi.Api("IsManualRecord",new Object[]{MissedAiring})).booleanValue()){
                        out.println("<li>"+
                                "<form method=\"post\" action=\"ManualRecord\">" +
                                "<input type=\"hidden\" name=\"command\" value=\"CancelRecord\"/>" +
                                "<input type=\"hidden\" name=\"returnto\" value=\"Conflicts\"/>" +
                                "<input type=\"hidden\" name=\"AiringId\" value=\""+airing.id+"\"/>" +
                                "<input type=\"submit\" value=\"Cancel the manual recording\"/>"+
                                " of <a title=\"View detailed info\" href=\"DetailedInfo?"+airing.getIdArg()+"\">"+Translate.encode(airing.getAiringShortDescr())+"</a>" +
                                "</form></li>");
                    }

        			out.println("</ul>");
        			out.println("</div>"); // resolution
        			out.println("</div>");//conflict
        		}
            }
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
