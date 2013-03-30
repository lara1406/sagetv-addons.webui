
package net.sf.sageplugins.webserver;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;

/**
 * @author Owner
 *
 * 
 *
 */
public class ManualRecordServlet extends SageServlet {

	/**
     * 
     */
    private static final long serialVersionUID = -8098259501492380032L;
    Vector<String> commands=new Vector<String>();
	public ManualRecordServlet() {
		commands.add("Record"); //needs UI context
		commands.add("CancelRecord");
		commands.add("SetRecPad"); //needs UI context
		commands.add("SetRecQual"); //needs UI context
	}
	
	protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {


		Airing airing=null;
		try {
			airing=new Airing(req);
		} catch ( Exception e) {
			htmlHeaders(resp);
			noCacheHeaders(resp);
			PrintWriter out = resp.getWriter();
			xhtmlHeaders(out);
			out.println("<head>");
			jsCssImport(req, out);
			out.println("<title>Manual Record</title></head>");
			out.println("<body>");
			printTitle(out,"Error", SageServlet.isTitleBroken(req));
			out.println("<div id=\"content\">");
			out.println("<h3>Unknown Airing/MediaFile ID passed</h3>");
			out.println("</div>");
			printMenu(req,out);
			out.println("</body></html>");
			out.close();
			return;
		}
		String command=req.getParameter("command");
		if ( command == null || command.length()==0) {
			htmlHeaders(resp);
			noCacheHeaders(resp);
			PrintWriter out = resp.getWriter();
			xhtmlHeaders(out);
			out.println("<head>");
			jsCssImport(req, out);
			out.println("<title>Manual Record</title></head>");
			out.println("<body>");
		    printTitle(out,"Error", SageServlet.isTitleBroken(req));
		    out.println("<div id=\"content\">");
			out.println("<h3>No command passed</h3>");
			out.println("</div>");
			printMenu(req,out);
			out.println("</body></html>");
			out.close();
			return;
		}
		
		if ( commands.indexOf(command)<0){
			htmlHeaders(resp);
			noCacheHeaders(resp);
			PrintWriter out = resp.getWriter();
			xhtmlHeaders(out);
			out.println("<head>");
			jsCssImport(req, out);
			out.println("<title>Manual Record</title></head>");
			out.println("<body>");
			printTitle(out,"Error", SageServlet.isTitleBroken(req));
			out.println("<div id=\"content\">");
			out.println("<h3>Invalid command passed</h3>");
			out.println("</div>");
			printMenu(req,out);
			out.println("</body></html>");
			out.close();
			return;
		}
		
		Long schedstart=null;
		Long schedend=null;
		String airingTitle=airing.getTitle();
		Long airstart=((Long)SageApi.Api("GetAiringStartTime",new Object[]{airing.sageAiring}));
		Long airend=((Long)SageApi.Api("GetAiringEndTime",new Object[]{airing.sageAiring}));
		
		if ( ! command.equals("CancelRecord")){
			int defstartpad=SageApi.GetIntProperty("nielm/default_start_pad",0);
			int defendpad=SageApi.GetIntProperty("nielm/default_stop_pad",0);
			try {
				if (req.getParameter("startpad")!=null ){
					int startpad=Integer.parseInt(req.getParameter("startpad"));
					if ( req.getParameter("StartPadOffsetType").equalsIgnoreCase("earlier"))
						startpad=-startpad;
					schedstart=new Long(airstart.longValue()+startpad*60*1000);
				}else {
					schedstart=new Long(airstart.longValue()-defstartpad);
				}
				if (req.getParameter("endpad")!=null ){
					int endpad=Integer.parseInt(req.getParameter("endpad"));
					if ( req.getParameter("EndPadOffsetType").equalsIgnoreCase("earlier"))
						endpad=-endpad;
					schedend=new Long(airend.longValue()+endpad*60*1000);
				} else {
					schedend=new Long(airend.longValue()+defendpad);
				}
			} catch (Exception e) {
				htmlHeaders(resp);
				noCacheHeaders(resp);
				PrintWriter out = resp.getWriter();
				xhtmlHeaders(out);
				out.println("<head>");
				jsCssImport(req, out);
				out.println("<title>Manual Record</title></head>");
				out.println("<body>");
			    printTitle(out,"Error", SageServlet.isTitleBroken(req));
				out.println("<div id=\"content\">");
				out.println("<h3>Invalid padding arguments passed</h3>");
				out.println("</div>");
				printMenu(req,out);
				out.println("</body></html>");
				out.close();
				return;
			}
		} 
		Object conflicts=null;
		if ( command.equals("Record") ){
			Object channel=SageApi.Api("GetChannel",new Object[]{airing.sageAiring});
			conflicts=checkManualRecordConflicts(schedstart.longValue(), schedend.longValue(), channel);
			if ( SageApi.IntApi("FindElementIndex",new Object[]{conflicts,airing.sageAiring}) >= 0)
				conflicts=SageApi.Api("RemoveElement",new Object[]{conflicts,airing.sageAiring});
			//System.out.println("Got  "+SageApi.Size(conflicts)+" overlaps excluding self, removing padding");
			if (SageApi.Size(conflicts)>0 ){
				schedstart=airstart;
				schedend=airend;
			}
			conflicts=checkManualRecordConflicts(schedstart.longValue(), schedend.longValue(), channel);
			if ( SageApi.IntApi("FindElementIndex",new Object[]{conflicts,airing.sageAiring}) >= 0)
				conflicts=SageApi.Api("RemoveElement",new Object[]{conflicts,airing.sageAiring});			
		}
		else if ( command.equals("SetRecPad")){
			Object channel=SageApi.Api("GetChannel",new Object[]{airing.sageAiring});
			conflicts=checkManualRecordConflicts(schedstart.longValue(), schedend.longValue(), channel);
			//System.out.println("Got  "+SageApi.Size(conflicts)+" overlaps, removing self");
			if ( SageApi.IntApi("FindElementIndex",new Object[]{conflicts,airing.sageAiring}) >= 0)
				conflicts=SageApi.Api("RemoveElement",new Object[]{conflicts,airing.sageAiring});
			//System.out.println("Got  "+SageApi.Size(conflicts)+" overlaps excluding self");
		}
		if (SageApi.Size(conflicts)>0 ){
			
			// we cannot record this because of conflicts... show them...
			htmlHeaders(resp);
			noCacheHeaders(resp);
			PrintWriter out = resp.getWriter();
			xhtmlHeaders(out);
			out.println("<head>");
			jsCssImport(req, out);
			out.println("<title>Manual Record</title></head>");
			out.println("<body>");
		    printTitle(out,"Error", SageServlet.isTitleBroken(req));
			out.println("<div id=\"content\">");
			out.println("<p>Cannot record this episode of <a href=\"DetailedInfo?"+airing.getIdArg()+"\">"+airingTitle+"</a> because it would conflict with the following manual recording(s)</p>");
			out.println("<p>You must cancel one of the following recordings and re-set this manual recording</p>");
			out.println("<div class=\"airings\">");
            boolean usechannellogos=GetOption(req,"UseChannelLogos","true").equalsIgnoreCase("true");
            boolean showMarkers=GetOption(req,"ShowMarkers","true").equalsIgnoreCase("true");
            boolean showRatings=GetOption(req,"ShowRatings","true").equalsIgnoreCase("true");
            boolean showEpisodeID=GetOption(req,"ShowEpisodeID","false").equalsIgnoreCase("true");
            boolean showFileSize=GetOption(req,"ShowFileSize","true").equalsIgnoreCase("true");
            Object allConflicts=SageApi.Api("GetAiringsThatWontBeRecorded",new Object[]{Boolean.FALSE});
            Object unresolvedConflicts=SageApi.Api("GetAiringsThatWontBeRecorded",new Object[]{Boolean.TRUE});
            for ( int i = 0; i<SageApi.Size(conflicts);i++){
				Airing cfairing=new Airing(SageApi.GetElement(conflicts,i));
				cfairing.printAiringTableCell(req,out,false,
                        usechannellogos,
                        showMarkers,
                        showRatings,
                        showEpisodeID,
                        showFileSize,
                        null,
                        allConflicts,
                        unresolvedConflicts);
			}
			out.println("</div>");


            printFooter(req,out);
            out.println("</div>");//content
			printMenu(req,out);
			out.println("</body></html>");
			out.close();
			return;				
		} 
		// no conflicts: record it!
        String[] uicontexts=GetUIContextNames();
        
		if ( command.equals("Record") ){
            if ( uicontexts == null || uicontexts.length==0)
                SageApi.Api(command,new Object[]{airing.sageAiring});
            else  
                SageApi.ApiUI(uicontexts[0],command,new Object[]{airing.sageAiring});
			Thread.sleep(100);
			// if setting of record is successful...
			if ( SageApi.booleanApi("IsManualRecord",new Object[]{airing.sageAiring}))
                if ( uicontexts == null || uicontexts.length==0)
                    SageApi.Api("SetRecordingTimes",new Object[]{airing.sageAiring,schedstart,schedend});
                else
                    SageApi.ApiUI(uicontexts[0],"SetRecordingTimes",new Object[]{airing.sageAiring,schedstart,schedend});
                   
			
		} else if ( command.equals("SetRecPad")){
			if ( SageApi.booleanApi("IsManualRecord",new Object[]{airing.sageAiring}))
                if ( uicontexts == null || uicontexts.length==0)
                    SageApi.Api("SetRecordingTimes",new Object[]{airing.sageAiring,schedstart,schedend});
                else
                    SageApi.ApiUI(uicontexts[0],"SetRecordingTimes",new Object[]{airing.sageAiring,schedstart,schedend});

		} else if ( command.equals("SetRecQual") ) {
			String quality=req.getParameter("quality");
			if ( quality != null && quality.length()>0){
				Object qualities=SageApi.Api("GetRecordingQualities");
				int i=0;
				for ( i=0;i<SageApi.Size(qualities);i++){
					if ( SageApi.GetElement(qualities,i).toString().equals(quality))
						break;
				}
				if ( i<SageApi.Size(qualities)) {
					Object qual=SageApi.GetElement(qualities,i);
					SageApi.Api("SetRecordingQuality",new Object[]{airing.sageAiring, qual});
				} else if ( quality.equals("Default")) {
					SageApi.Api("SetRecordingQuality",new Object[]{airing.sageAiring, quality});
				} else {
					htmlHeaders(resp);
					noCacheHeaders(resp);
					PrintWriter out = resp.getWriter();
					xhtmlHeaders(out);
					out.println("<head>");
					jsCssImport(req, out);
					out.println("<title>Manual Record</title></head>");
					out.println("<body>");
				    printTitle(out,"Error", SageServlet.isTitleBroken(req));
					out.println("<div id=\"content\">");
					out.println("<h3>Invalid recording quality passed</h3>");
					out.println("</div>");
					printMenu(req,out);
					out.println("</body></html>");
					out.close();
					return;
				}
			}
		} else {
			SageApi.Api(command,new Object[]{airing.sageAiring});
		}
		
        // wait a little for the command to take effect before returning
        Thread.sleep(1000);

        String returnto=req.getParameter("returnto");
        if ( req.getParameter("RetImage") != null) {
            resp.setContentType("image/png");
            noCacheHeaders(resp);
            OutputStream os=resp.getOutputStream();
            BufferedImage img=new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
            javax.imageio.ImageIO.write(img,"png",os);
            os.close();
        } else if  (returnto!= null){
			resp.sendRedirect(returnto);
			return;
		} else {
		    // return HTML page
			htmlHeaders(resp);
			noCacheHeaders(resp);
			PrintWriter out = resp.getWriter();
			xhtmlHeaders(out);
			out.println("<head>");
			jsCssImport(req, out);
			out.println("<title>Manual Record</title></head>");
			out.println("<body>");
			printTitle(out,"", SageServlet.isTitleBroken(req));
			out.println("<div id=\"content\">");
			out.print("Applied command: "+command+" on "+airingTitle);
			String ep=airing.getEpisode();
			if ( ep != null && ep.length()>0)
				out.print(" - "+ep);		
			out.println("</div>");
			printMenu(req,out);
			out.println("</body></html>");
			out.close();
        }
		return;

	}

}
