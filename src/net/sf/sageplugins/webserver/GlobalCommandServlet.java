
package net.sf.sageplugins.webserver;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;

/**
 * @author Niel
 *
 * 
 *
 */
public class GlobalCommandServlet extends SageServlet {

	/**
     * 
     */
    private static final long serialVersionUID = 1319984023974983853L;

    /* (non-Javadoc)
	 * @see net.sf.sageplugins.webserver.SageServlet#doServletGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		
		String command = req.getParameter("command");
		if ( command != null )
			command=command.trim();
		
		htmlHeaders(resp);
		noCacheHeaders(resp);
		resp.setHeader( "Refresh", "5; URL=Home" );
		PrintWriter out = resp.getWriter();
		xhtmlHeaders(out);
		if ( command.equalsIgnoreCase("ForceEpgUpdate")){
			out.println("<html>" +
			"<head><title>Force EPG Update</title>");
			jsCssImport(req, out);
			out.println("</head><body>");
			printTitle(out,"Force EPG Update");
			out.println("<div id=\"content\">");
			Object AllInputs = SageApi.Api("GetConfiguredCaptureDeviceInputs");
			if ( SageApi.Size(AllInputs)> 0){
				Map<?, ?> LineupMap = (Map<?, ?>)SageApi.Api("GroupByMethod",
						new Object[]{AllInputs, "GetLineupForCaptureDeviceInput"});
				Set<?> Lineups=LineupMap.keySet();
				Iterator<?> it=Lineups.iterator();
				while(it.hasNext()){
					Object Lineup=it.next();
					out.println("<p>Forcing update on lineup: "+Lineup.toString()+"</p>");
					Object AllChannels = SageApi.Api("GetChannelsOnLineup",Lineup);
					Object Channel=SageApi.GetElement(AllChannels,0);
					Object ChannelNumbers = SageApi.Api("GetChannelNumbersForLineup",new Object[]{Channel, Lineup});
					Object Number=SageApi.GetElement(ChannelNumbers,0);
					boolean viewable=SageApi.booleanApi("IsChannelViewableOnNumberOnLineup",new Object[]{Channel, Number, Lineup});
					// out.println("<p>Toggling viewable for channel number: "+Number.toString()+"</p>");
					SageApi.Api("SetChannelViewabilityForChannelNumberOnLineup",new Object[]{Channel, Number, Lineup, new Boolean(!viewable)});
					SageApi.Api("SetChannelViewabilityForChannelNumberOnLineup",new Object[]{Channel, Number, Lineup, new Boolean(viewable)});
				}
				// the following probably commits the changes and triggers an EPG update
				SageApi.Api("RemoveUnusedLineups");
			} else {
				out.println("<p>No configured capture devices</p>");
			}
			out.println("<p>Returning to <a href=\"Home\">System Info page</a></p>");
		} else if ( command.equals("UpdateMediaLibrary")) {
			resp.setHeader( "Refresh", "5; URL=Home" );
			out.println("<html>" +
			"<head><title>Refresh Media Library</title>");
			jsCssImport(req, out);
			out.println("</head><body>");
			printTitle(out,"Refresh Media Library");
			out.println("<div id=\"content\">");
			SageApi.Api("RunLibraryImportScan",Boolean.FALSE);
			out.println("<p>Sage is now rescanning all media import directories for new/removed content</p>");
			out.println("<p>Returning to <a href=\"Home\">System Info page</a></p>");
		} else if (command.equals("CancelTranscodeJobs")){
			if (req.getParameter("clearall")!=null){
				SageApi.Api("ClearTranscodedJobs");
			}else {
				// clear selected
				String[] jobIds=req.getParameterValues("JobId");
				if ( jobIds!=null){
					for (int i = 0; i < jobIds.length; i++) {
						SageApi.Api("CancelTranscodeJob",new Integer(jobIds[i]));
					}
				}
			}
			
			String returnto=req.getParameter("returnto");
	        if  (returnto!= null){
	            //  wait a little for the command to take effect before redirecting
	            Thread.sleep(500);
	            resp.sendRedirect(returnto);
	        } else {
	        	resp.setHeader( "Refresh", "5; URL=Home" );
				out.println("<html>" +
				"<head><title>Cancel Transcode Jobs</title>");
				jsCssImport(req, out);
				out.println("</head><body>");
				printTitle(out,"Cancel Transcode Jobs");
				out.println("<div id=\"content\">");
				out.println("<p>Selected transcode jobs have been cancelled</p>");
				out.println("<p>Returning to <a href=\"Home\">System Info page</a></p>");
	        }
		}
		
		out.println("</div>"); //content
		printMenu(out);
		out.println("</body></html>");
		out.close();
	}
}
