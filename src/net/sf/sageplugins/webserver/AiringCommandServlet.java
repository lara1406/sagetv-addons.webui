
package net.sf.sageplugins.webserver;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
public class AiringCommandServlet extends SageServlet {

	/**
     * 
     */
    private static final long serialVersionUID = -7571299791722363182L;
    
    static final String[][] AIRING_ACTIONS_OPT;
    static final String[][] MEDIAFILE_ACTIONS_OPT;
    
    static class CommandParams {
        CommandParams(boolean confirmationRequired,
                     boolean isMediaFileCommand,
                     boolean isAiringCommand,
                     String commandDesc){
            this.confirmationRequired=confirmationRequired;
            this.isMediaFileCommand=isMediaFileCommand;
            this.isAiringCommand=isAiringCommand;
            this.commandDesc=commandDesc;
        }
        boolean confirmationRequired;
        boolean isMediaFileCommand;
        boolean isAiringCommand;
        String commandDesc;
    }
    
    static HashMap<String, CommandParams> commands=new HashMap<String, CommandParams>();
    static {
        LinkedList<String[]> airingsCommands=new LinkedList<String[]>();
        LinkedList<String[]> mediafileCommands=new LinkedList<String[]>();
        commands.put("SetDontLike",new CommandParams(false,true,true,"Set Don't Like Flag"));
        airingsCommands.add(new String[]{"SetDontLike","Set Don't Like Flag"});
        mediafileCommands.add(airingsCommands.getLast());
        commands.put("SetWatched",new CommandParams(false,true,true, "Set Watched Flag"));
        airingsCommands.add(new String[]{"SetWatched","Set Watched Flag"});
        mediafileCommands.add(airingsCommands.getLast());
        commands.put("ClearDontLike",new CommandParams(false,true,true, "Clear Don't Like Flag"));
        airingsCommands.add(new String[]{"ClearDontLike","Clear Don't Like Flag"});
        mediafileCommands.add(airingsCommands.getLast());
        commands.put("ClearWatched",new CommandParams(false,true,true, "Clear Watched Flag"));
        airingsCommands.add(new String[]{"ClearWatched","Clear Watched Flag"});
        mediafileCommands.add(airingsCommands.getLast());
        commands.put("Record",new CommandParams(true,false,true,"Record"));
        airingsCommands.add(new String[]{"Record","Record"});
        commands.put("CancelRecord",new CommandParams(true,false,true,"Cancel Manual Recording"));
        airingsCommands.add(new String[]{"CancelRecord","Cancel Manual Recording"});
        commands.put("DeleteFile",new CommandParams(true,true,false, "Delete File"));
        mediafileCommands.add(new String[]{"DeleteFile","Delete File"});
        commands.put("RecordingError",new CommandParams(true,true,false, "Delete File - Wrong Recording"));
        mediafileCommands.add(new String[]{"RecordingError","Delete File - Wrong Recording"});
        
        commands.put("WatchNow",new CommandParams(false,true,true, "Watch Now"));
        
        commands.put("Archive",new CommandParams(false,true,false, "Set Archived Flag"));
        mediafileCommands.add(new String[]{"Archive","Set Archived Flag"});
        commands.put("Unarchive",new CommandParams(false,true,false, "Clear Archived Flag"));
        mediafileCommands.add(new String[]{"Unarchive","Clear Archived Flag"});
        commands.put("SetManRecStatus",new CommandParams(false,true,false, "Set Manual Record Status"));
        mediafileCommands.add(new String[]{"SetManRecStatus","Set Manual Record Status"});
        commands.put("RemoveManRecStatus",new CommandParams(false,true,false, "Clear Manual Record Status"));
        mediafileCommands.add(new String[]{"RemoveManRecStatus","Clear Manual Record Status"});
        
        if ( SAGE_MAJOR_VERSION>=5.1 ) {
        	commands.put("convert",new CommandParams(false,true,false, "Convert Media File"));
        	mediafileCommands.add(new String[]{"convert","Convert Media File"});
        }
        
        try {
            boolean useSageEncoder=SageApi.GetBooleanProperty("nielm/webserver/enableSageEncoderOpts",false);
            if ( useSageEncoder ){
                mediafileCommands.add(new String[]{"Encode","Encode with SageEncoder"});
                
            }
        } catch (InvocationTargetException e){}
        
        AIRING_ACTIONS_OPT=airingsCommands.toArray(new String[0][0]);
        MEDIAFILE_ACTIONS_OPT=mediafileCommands.toArray(new String[0][0]);
    }
	/**
	 * 
	 */
	public AiringCommandServlet() {
		
		
	}
	/* (non-Javadoc)
	 * @see net.sf.sageplugins.webserver.SageServlet#doServletGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		
		String command=req.getParameter("command");
		if ( command == null || command.length()==0) {
			htmlHeaders(resp);
			noCacheHeaders(resp);
			PrintWriter out = resp.getWriter();
			xhtmlHeaders(out);
			out.println("<head>");
			jsCssImport(req, out);
			out.println("<title>InternalCommand</title></head>");
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
		final CommandParams params = commands.get(command);
		if ( params==null){
			htmlHeaders(resp);
			noCacheHeaders(resp);
			PrintWriter out = resp.getWriter();
			xhtmlHeaders(out);
			out.println("<head>");
			jsCssImport(req, out);
			out.println("<title>InternalCommand</title></head>");
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
        
		Vector<Airing> airings=new Vector<Airing>();
		String currType="";
		String currId="";
		try {
			currType="AiringId";
			String[] airingIds=req.getParameterValues(currType);
			if ( airingIds!=null )
				for (int i=0;i<airingIds.length;i++)
				{	
					currId=airingIds[i];
					airings.add(new Airing(Airing.ID_TYPE_AIRING,Integer.parseInt(airingIds[i])));
				}
			currType="MediaFileId";
			airingIds=req.getParameterValues(currType);
			if ( airingIds!=null )
				for (int i=0;i<airingIds.length;i++)
				{
					currId=airingIds[i];
					airings.add(new Airing(Airing.ID_TYPE_MEDIAFILE,Integer.parseInt(airingIds[i])));
				}
            currType="FileName";
            airingIds=req.getParameterValues(currType);
            if ( airingIds!=null )
                for (int i=0;i<airingIds.length;i++)
                {
                    currId=airingIds[i];
                    airings.add(new Airing(currId));
                }
		} catch ( Exception e) {
			htmlHeaders(resp);
			noCacheHeaders(resp);
			PrintWriter out = resp.getWriter();
			xhtmlHeaders(out);
			out.println("<head>");
			jsCssImport(req, out);
			out.println("<title>InternalCommand</title></head>");
			out.println("<body>");
			printTitle(out,"Error", SageServlet.isTitleBroken(req));
			out.println("<div id=\"content\">");
			out.println("<h3>Unknown Airing/MediaFile ID passed</h3>");
			out.println("type:"+currType+" id:"+currId+" --"+e.toString());
			e.printStackTrace();
			out.println("</div>");
			printMenu(out);
			out.println("</body></html>");
			out.close();
			return;
		}
        String returnto=req.getParameter("returnto");
        
        if ( params.confirmationRequired && airings.size()>0){
            String confirmed=req.getParameter("confirm");
            if ( confirmed!=null && ! confirmed.equalsIgnoreCase("yes")) {
                // cancelled
                if  (returnto!= null){
                    //  wait a little for the command to take effect before redirecting
                    Thread.sleep(500);
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
                    out.println("<title>InternalCommand</title></head>");
                    out.println("<body>");
                    printTitle(out,"", SageServlet.isTitleBroken(req));
                    out.println("<div id=\"content\">");
                    out.print("Command cancelled");
                    out.println("</div>");
                    printMenu(req,out);
                    out.println("</body></html>");
                    out.close();
                    return;
                } 
             }
            if ( confirmed==null || ! confirmed.equalsIgnoreCase("yes")) {
                htmlHeaders(resp);
                noCacheHeaders(resp);
                PrintWriter out = getGzippedWriter(req,resp);
                // must catch and report all errors within Gzipped Writer
                try {
                    xhtmlHeaders(out);
                    out.println("<head>");
                    jsCssImport(req, out);
                    out.println("<title>Command confirmation required</title></head>");
                    out.println("<body>");
                    printTitle(out,"Confirmation required:", SageServlet.isTitleBroken(req));
                    out.println("<div id=\"content\">");
                    out.println("<p>Attempting to perfom command \""+params.commandDesc+"\" on the following "+airings.size()+" shows requires confirmation:</p>");
                    boolean showFileSize=GetOption(req,"ShowFileSize","true").equalsIgnoreCase("true");
                    AiringList.PrintPagedAiringList(this, out, airings, req, false, false, showFileSize, null, null, null, 0);
                    out.println("<p>Are you sure you want to do this?</p>\n" +
                            "<form method=\"get\" action=\"AiringCommand\">\n" +
                            "<input type=\"hidden\" name=\"command\" value=\""+command+"\"/>"); 
                    for ( java.util.Iterator<Airing> it = airings.iterator(); it.hasNext();){
                        Airing airing=it.next();
                        out.println("<input type=\"hidden\" name=\""
                                +(airing.idType==Airing.ID_TYPE_AIRING?"AiringId":"MediaFileId")
                                +"\" value=\""+airing.id+"\"/>");
                    }
                    if(req.getParameter("returnto")!=null)
                        out.println("<input type=\"hidden\" name=\"returnto\" value=\""+req.getParameter("returnto")+"\"/>");
                    out.println("<input type=\"radio\" name=\"confirm\" value=\"yes\"/>Perfom command \""+params.commandDesc+"\" on these "+airings.size()+" shows <br/>");
                    out.println("<input type=\"radio\" name=\"confirm\" value=\"no\" selected=\"selected\"/>Cancel action<br/>");
                    out.println("<input type=\"submit\"/>");
                    out.println("</form>");
                    out.println("</div>");
                    printMenu(req,out);
                    out.println("</body></html>");
                    out.close();
                    return;
                }
                catch (Throwable e) {
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

        // Special handling for Convert -- acts on all airings
        if ( command.equalsIgnoreCase("convert") && airings.size()>0) {
        	handleConversion(req,resp,airings);
        	return;
        }
        
		for ( Iterator<Airing> it = airings.iterator(); it.hasNext();){
			Airing airing=it.next();
			if (airing.idType==Airing.ID_TYPE_MEDIAFILE &&
						( command.equals("DeleteFile") 
						  || command.equals("RecordingError"))){
                Boolean result;
				if (command.equals("RecordingError") )
					result=(Boolean)SageApi.Api("DeleteFileWithoutPrejudice",new Object[]{airing.sageAiring});
				else
					result=(Boolean)SageApi.Api(command,new Object[]{airing.sageAiring});
                if ( result == null || result.booleanValue() == false){
                    System.out.println("Failed to delete file "+airing.getAiringShortDescr());
                }
			} else  if ( command.equals("WatchNow")){
                String context=req.getParameter("context");
                String[] UiContexts=GetUIContextNames();
                String[] connectedClients= (String[]) SageApi.Api("GetConnectedClients");

                List<String> contexts = new ArrayList<String>();
                contexts.addAll(Arrays.asList(UiContexts));
                if (SAGE_MAJOR_VERSION >= 7.0)
                {
                    contexts.addAll(Arrays.asList(connectedClients));
                }

                if ( context == null || ! contexts.contains(context) ) {
                    // invalid or no context -- error page
                    //  set headers before accessing the Writer
                    htmlHeaders(resp);
                    noCacheHeaders(resp);
                    PrintWriter out = resp.getWriter();
                    xhtmlHeaders(out);
                    out.println("<html>" +
                      "<head><title>SageTV Airing Command</title>");
                    jsCssImport(req, out);
                    out.println("</head><body>");
                    printTitle(out,"Error", SageServlet.isTitleBroken(req));
                    out.println("<div id=\"content\">");
                    if ( context == null )
                        out.println("<p>No UI Context Specified:<br/>");
                    if ( ! contexts.contains(context) )
                        out.println("<p>UI Context: \""+context+"\" is not active:<br/>");
                    out.println(" use: "+req.getRequestURI()+"?command=&lt;command&rt;&amp;context=&lt;context&rt;</p>");
                    out.println("</div></body></html>");
                    out.close();
                    return;
                } else {
					long now=System.currentTimeMillis();
					if ( airing.idType==Airing.ID_TYPE_MEDIAFILE 
						|| ( airing.idType==Airing.ID_TYPE_AIRING
							&& airing.getStartDate().getTime()<=now
							&& airing.getEndDate().getTime()>=now))  {
						SageApi.ApiUI(context,"Watch",airing.sageAiring);
						Thread.sleep(500);
						SageApi.ApiUI(context,"SageCommand","Home");
						Thread.sleep(500);
						SageApi.ApiUI(context,"SageCommand","TV");
					}
				}
            } else if ( command.equals("SetManRecStatus")){
                if (airing.idType==Airing.ID_TYPE_MEDIAFILE
                        && ! SageApi.booleanApi("IsManualRecord", new Object[] {airing.sageAiring})) {
                    SageApi.Api("Record",new Object[]{airing.sageAiring});
                }
            } else if ( command.equals("RemoveManRecStatus")){
                if (airing.idType==Airing.ID_TYPE_MEDIAFILE
                        && SageApi.booleanApi("IsManualRecord", new Object[] {airing.sageAiring})) {
                    SageApi.Api("CancelRecord",new Object[]{airing.sageAiring});
                }
            } else if ( command.equals("Archive")){
                if (airing.idType==Airing.ID_TYPE_MEDIAFILE) {
                    SageApi.Api("MoveFileToLibrary",new Object[]{airing.sageAiring});
                }
            } else if ( command.equals("Unarchive")){
                if (airing.idType==Airing.ID_TYPE_MEDIAFILE
                    && SageApi.booleanApi("IsTVFile", new Object[]{airing.sageAiring})) {
                    String[] uicontexts=GetUIContextNames();
                    if ( uicontexts == null || uicontexts.length==0) {
                        SageApi.Api("Record",new Object[]{airing.sageAiring});
                    } else {  
                        SageApi.ApiUI(uicontexts[0],"Record",new Object[]{airing.sageAiring});
                    }
                    SageApi.Api("MoveTVFileOutOfLibrary",new Object[]{airing.sageAiring});
                }
            } else if (command.equals("Encode")
                        && airing.idType==Airing.ID_TYPE_MEDIAFILE ){
                Object files=SageApi.Api("GetSegmentFiles",new Object[]{airing.sageAiring});
                
                if ( files!=null ){
                    Object[] filenames=(Object[])files;
    
                    for ( int cnt=0;cnt<filenames.length;cnt++) {
                        File dynFile = new File(filenames[cnt].toString() + ".wse");
                        dynFile.createNewFile();
                    }
                }
            } else if (command.equals("Record")) {
                // don't check for conflicts when multiple recordings are being set up at one time
                // it wouldn't hurt to call Record on an existing manual recording, but there's no reason to
                // do use default padding unless the selected item is already a manual recording
                
                boolean existingManualRecording = SageApi.booleanApi("IsManualRecord",new Object[]{airing.sageAiring});

                if (!existingManualRecording)
                {
                    Long airstart=((Long)SageApi.Api("GetAiringStartTime",new Object[]{airing.sageAiring}));
                    Long airend=((Long)SageApi.Api("GetAiringEndTime",new Object[]{airing.sageAiring}));
                    int defstartpad=SageApi.GetIntProperty("nielm/default_start_pad",0);
                    int defendpad=SageApi.GetIntProperty("nielm/default_stop_pad",0);
                    Long schedstart=new Long(airstart.longValue()-defstartpad);
                    Long schedend=new Long(airend.longValue()+defendpad);

                    String[] uicontexts=GetUIContextNames();
                    if (uicontexts == null || uicontexts.length==0)
                    {
                        SageApi.Api(command,new Object[]{airing.sageAiring});
                    }
                    else  
                    {
                        SageApi.ApiUI(uicontexts[0],command,new Object[]{airing.sageAiring});
                    }

                    Thread.sleep(100);
                    // if setting of record is successful...
                    if (SageApi.booleanApi("IsManualRecord",new Object[]{airing.sageAiring}))
                    {
                        if (uicontexts == null || uicontexts.length==0)
                        {
                            SageApi.Api("SetRecordingTimes",new Object[]{airing.sageAiring,schedstart,schedend});
                        }
                        else
                        {
                            SageApi.ApiUI(uicontexts[0],"SetRecordingTimes",new Object[]{airing.sageAiring,schedstart,schedend});
                        }
                    }
                }
            }else {
				Object result=SageApi.Api(command,new Object[]{airing.sageAiring});
				if ( result != null) {
					//System.out.println("command: "+command+" on "+airing.getTitle()+" rets "+result.toString());
                }
			}
		}
		
		if ( req.getParameter("RetImage") != null) {
			resp.setContentType(getServletContext().getMimeType(".png"));
		    noCacheHeaders(resp);
		    OutputStream os=resp.getOutputStream();
		    BufferedImage img=new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
		    javax.imageio.ImageIO.write(img,"png",os);
		    os.close();
		} else if  (returnto!= null){
            //  wait a little for the command to take effect before redirecting
            Thread.sleep(500);
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
			printMenu(out);
		    out.println("<title>InternalCommand</title></head>");
		    out.println("<body>");
		    printTitle(out,"", SageServlet.isTitleBroken(req));
		    out.println("<div id=\"content\">");
		    out.print("Applied command: "+command+" on "+Integer.toString(airings.size())+" airings");
		    out.println("</div>");
		    out.println("</body></html>");
		    out.close();
		} 
		return;

	}

	protected void handleConversion(HttpServletRequest req, HttpServletResponse resp, Vector<Airing> airings)
	throws Exception{
		
		try {
			// if transcoding mode specified, start conversion
			String transcodeMode=req.getParameter("transcodeMode");
			if ( transcodeMode == null || transcodeMode.trim().length()==0){
				// prompt for conversion parameters
				htmlHeaders(resp);
				noCacheHeaders(resp);
				PrintWriter out = resp.getWriter();
				xhtmlHeaders(out);
				out.println("<head>");
				jsCssImport(req, out);
				printMenu(out);
				if (airings.size() == 1) {
				    out.println("<title>Convert Media File</title></head>");
				} else {
                    out.println("<title>Convert Media Files</title></head>");
				}
				out.println("<body>");
                if (airings.size() == 1) {
                    printTitle(out,"Convert Media File", SageServlet.isTitleBroken(req));
                } else {
                    printTitle(out,"Convert Media Files", SageServlet.isTitleBroken(req));
                }
	            out.println("<div id=\"content\">");
	            boolean showFileSize=GetOption(req,"ShowFileSize","true").equalsIgnoreCase("true");
	            AiringList.PrintPagedAiringList(this, out, airings, req, false, false, showFileSize, null, null, null, 0);
				
				out.println("<h3>Conversion Options</h3>");
				out.println("<form method=\"get\" action=\"AiringCommand\">\n" +
							"<input type=\"hidden\" name=\"command\" value=\"convert\"/>"); 
		        for ( java.util.Iterator<Airing> it = airings.iterator(); it.hasNext();){
		            Airing airing=it.next();
		            out.println("<input type=\"hidden\" name=\""
		                    +(airing.idType==Airing.ID_TYPE_AIRING?"AiringId":"MediaFileId")
		                    +"\" value=\""+airing.id+"\"/>");
		        }
		        if(req.getParameter("returnto")!=null)
		            out.println("<input type=\"hidden\" name=\"returnto\" value=\""+req.getParameter("returnto")+"\"/>");
		        
		        out.println("<dl><dt>Transcode Format:</dt>");
		        
		        out.println("<dd><select name=\"transcodeMode\">");
		        String[] transcodeFormats=(String[])SageApi.Api("GetTranscodeFormats");
		        java.util.Arrays.sort(transcodeFormats);
		        
		        String transcodeGroup="";
		        String lastFormatName = SageApi.GetProperty("transcoder/last_format_name", null );
		        if ( lastFormatName!=null) 
		        	lastFormatName = SageApi.GetProperty("transcoder/last_format_quality/" + lastFormatName, null );
		        if ( lastFormatName==null) lastFormatName="";


		        out.println("<option value=\"\">Select...</option>");
		        
		        for (int i = 0; i < transcodeFormats.length; i++) {
					String formatName = transcodeFormats[i];
					String[] formatArr=formatName.split("-", 2);
					if ( !formatArr[0].equals(transcodeGroup)){
						if ( transcodeGroup.length()>0)
							out.println("</optgroup>");
						out.println("<optgroup label=\""+formatArr[0]+"\">");
						transcodeGroup=formatArr[0];
					}
					out.println("<option value=\""+formatName+"\" " +
							(formatName.equals(lastFormatName)?" selected=\"selected\"":"")+
							">"+formatName+"</option>");
				}
				if ( transcodeGroup.length()>0)
					out.println("</optgroup>");
				out.println("</select></dd><br/>");

				
				String replaceChoiceStr=SageApi.GetProperty("transcoder/last_replace_choice", "xKeepBoth");
				boolean replaceFile=false;
				if ( replaceChoiceStr!=null && replaceChoiceStr.equals("xKeepOnlyConversion"))
					replaceFile=true;
				
				out.println("<dt>Original File:</dt>");
				
				out.println("<dd><input type=\"radio\" name=\"replaceOriginal\" value=\"yes\" "
						+(replaceFile?"checked=\"checked\"":"")+
						"/> Delete Original File<br/>");
				out.println("<input id=\"noReplaceOriginal\" type=\"radio\" name=\"replaceOriginal\" value=\"no\" "
						+(!replaceFile?"checked=\"checked\"":"") +
						"/> Keep Original File ");
				out.println("</dd><br/>");
				
				out.println("<dt>Destination Folder:</dt>");
				String lastDestDir=SageApi.GetProperty("transcoder/last_dest_dir", null);
				out.println("<dd><input type=\"radio\" onchange=\"updateDestFolder()\" name=\"origDestDir\" value=\"yes\" "
						+(lastDestDir==null?"checked=\"checked\"":"")+
						"/> Original Folder<br/>");
				out.println("<input id=\"altdestfolder\" onchange=\"updateDestFolder()\" type=\"radio\" name=\"origDestDir\" value=\"no\" "
						+(lastDestDir!=null?"checked=\"checked\"":"") +
						"/> Alternate Destination Folder (must exist on server):<br/> ");
				out.print("<dl style=\"border: none\"><dt></dt><dd><input id=\"destfoldername\" name=\"destDir\" type=\"text\" disabled=\"true\" style=\"width: 30em;\" ");
				if ( lastDestDir!=null)
					out.print("value=\""+Translate.encode(lastDestDir)+"\"");
				else
					out.print("value=\"\"");
				out.println("/>");
				out.println("</dd></dl></dd><br/>");
				
				if ( airings.size()==1){
					out.println("<dt>Destination File (leave blank to use automatic naming):</dt>\n" +
							"<dd><input name=\"destFile\" type=\"text\" style=\"width: 30em;\" value=\"\"/></dd><br/>");
				}

				out.println("<script language=\"JavaScript\" type=\"text/javascript\">\n" +
						"function updateDestFolder()\n" +
						"{\n"+
						"   if ( ! document.getElementById('altdestfolder').checked ) {\n" +
						"      document.getElementById('destfoldername').disabled = true;\n" +
				        "   } else {\n"+
                        "      document.getElementById('destfoldername').disabled = false;\n" +
						"   }\n"+
			            "}\n"+
						"updateDestFolder();\n"+
						"</script>");
				
				out.println("<dt>Start at timestamp (secs):</dt><dd><input name=\"startTime\" type=\"text\" value=\"0\"/></dd>");
				out.println("<dt>Duration (secs):</dt><dd><input name=\"duration\" type=\"text\" value=\"0\"/> (use '0' to convert to end of file)</dd><br/>");

	            out.println("</dl>");
	            if (airings.size() == 1) {
	                out.println("<input type=\"submit\" value=\"Submit Transcode Job\"/>");
	            } else {
                    out.println("<input type=\"submit\" value=\"Submit Transcode Jobs\"/>");
	            }
		        out.println("</form>");
				out.println("</div>");//content
                printFooter(req,out);
				out.println("</body></html>");
				out.close();

			} else {
				// get required parameters:
				// 		replaceOriginal t/f
				// original dir
				// if ( ! original dir)
				//	   destdir
				//
				//  start
				//	duration
				//  
				//  original file
				//	if ( ! original file)
				//	   destfilename	
				String tmp;
				boolean replaceOriginal=false;
				File destFile=null;
				File destDir=null;
				long startTime=-1;
				long duration=-1;
				
				tmp=req.getParameter("replaceOriginal");
				if ( tmp==null || ( ! tmp.equals("yes") && ! tmp.equals("no")))
					throw new IllegalArgumentException ("replaceOriginal is not valid");
				replaceOriginal=tmp.equals("yes");
				
				tmp=req.getParameter("origDestDir");
				if ( tmp==null || ( ! tmp.equals("yes") && ! tmp.equals("no")))
					throw new IllegalArgumentException ("origDestDir is not valid");
				if (tmp.equals("no")){
					tmp=req.getParameter("destDir");
					if ( tmp==null || tmp.trim().length()==0)
						throw new IllegalArgumentException ("Destination Directory has not been specified");
				
					destDir=new File(tmp);
					if ( ! destDir.isAbsolute())
						throw new IllegalArgumentException ("Destination Directory "+tmp+" is not a full path");
					if ( ! destDir.exists())
						throw new IllegalArgumentException ("Destination Directory "+tmp+" does not exist");
					if ( ! destDir.isDirectory())
						throw new IllegalArgumentException ("Destination Directory "+tmp+" is not a directory");
					if ( ! destDir.canWrite())
						throw new IllegalArgumentException ("Destination Directory "+tmp+" is not writeable");
					destFile=destDir.getAbsoluteFile();
				}
				if ( airings.size()==1){
					Airing airing=airings.firstElement();
					tmp=req.getParameter("destFile");
					if ( tmp==null)
						throw new IllegalArgumentException ("Destination Filename has not been specified");
					tmp=tmp.trim();
					if ( tmp.length()>0){
						// filename specified
						if ( destFile==null){
							// no directory specified yet
							destFile=((File)SageApi.Api("GetFileForSegment",new Object[]{airing.sageAiring,new Integer(0)})).getParentFile();
						} 
						destFile=new File(destFile,tmp);
					} else {
						// no filename specified - use path
						
					}
				}
				
				tmp=req.getParameter("startTime");
				if ( tmp != null) {
					try {
						startTime=Long.parseLong(tmp);
						if ( startTime <0 )
							throw new IllegalArgumentException("invalid startTime value "+startTime);
					} catch (NumberFormatException e){
						throw new IllegalArgumentException("invalid startTime value "+tmp,e);
					}
					tmp=req.getParameter("duration");
					if ( tmp != null) {
						try {
							duration=Long.parseLong(tmp);
							if ( duration <0 )
								throw new IllegalArgumentException("invalid duration value "+duration);
						} catch (NumberFormatException e){
							throw new IllegalArgumentException("invalid duration value "+tmp,e);
						}
					}
				}
				
				String[] formats=(String[])SageApi.Api("GetTranscodeFormats");
				if ( ! java.util.Arrays.asList(formats).contains(transcodeMode))
					throw new IllegalArgumentException("unknown transcode mode: "+transcodeMode);

				
				htmlHeaders(resp);
				noCacheHeaders(resp);
				if ( req.getParameter("returnto")!=null)
					resp.setHeader( "Refresh", "10; URL="+req.getParameter("returnto") );
				PrintWriter out = resp.getWriter();
				xhtmlHeaders(out);
				out.println("<head>");
				jsCssImport(req, out);
				printMenu(out);
				out.println("<title>Convert Media File(s)</title></head>");
				out.println("<body>");
			    printTitle(out,"Converting Media File(s)", SageServlet.isTitleBroken(req));
				out.println("<h3>Converting Media file(s):</h3>");
	            out.println("<div id=\"content\">");
				
				for (Iterator<Airing> iter = airings.iterator(); iter.hasNext();) {
					Airing airing = iter.next();
					if ( SageApi.booleanApi("IsMediaFileObject", new Object[]{airing.sageAiring})
						 && SageApi.booleanApi("CanFileBeTranscoded", new Object[]{airing.sageAiring}))
					{
						Integer jobID;
						if ( startTime==-1 || duration ==-1)
							jobID=(Integer)SageApi.Api("AddTranscodeJob", new Object[]{airing.sageAiring, transcodeMode, destFile, new Boolean(replaceOriginal)});
						else 
							jobID=(Integer)SageApi.Api("AddTranscodeJob", new Object[]{airing.sageAiring, transcodeMode, destFile, new Boolean(replaceOriginal), new Long(startTime), new Long(duration)});
						out.print("<p>Added transcoding job ID "+jobID+" for "+airing.getAiringShortDescr());
						if ( destFile != null)
							out.print(" to new file "+destFile.getAbsolutePath());
						out.println("</p>");
					}
				}
				
				out.println("<p><a href=\"TranscodeJobs\">View Video Conversion Queue</a></p>");

				// set properties with defaults.
				SageApi.Api("SetProperty",new Object[]{"transcoder/last_replace_choice", (replaceOriginal?"xKeepOnlyConversion":"xKeepBoth")});
				if (destDir == null) {
					SageApi.Api("SetProperty",new Object[]{"transcoder/last_dest_dir", null});
				} else {
					SageApi.Api("SetProperty",new Object[]{"transcoder/last_dest_dir", destDir.getAbsolutePath()});
				}
				String[] formatArr=transcodeMode.split("-", 2);
				SageApi.Api("SetProperty",new Object[]{"transcoder/last_format_name", formatArr[0]});
				SageApi.Api("SetProperty",new Object[]{"transcoder/last_format_quality/"+formatArr[0], transcodeMode});
				
				out.println("</div>");//content
				out.println("</body></html>");
				out.close();
			}
		} catch (IllegalArgumentException e){
			htmlHeaders(resp);
			noCacheHeaders(resp);
			PrintWriter out = resp.getWriter();
			xhtmlHeaders(out);
			out.println("<head>");
			jsCssImport(req, out);
			out.println("<title>Convert Media File(s): Error</title></head>");
			out.println("<body>");
		    printTitle(out,"Convert Media File(s): Error", SageServlet.isTitleBroken(req));
			out.println("<h3>Failed to convert media file(s):</h3>");
            out.println("<div id=\"content\">");
            boolean showFileSize=GetOption(req,"ShowFileSize","true").equalsIgnoreCase("true");
            AiringList.PrintPagedAiringList(this, out, airings, req, false, false, showFileSize, null, null, null, 0);
			
			out.println("<pre>Error: "+e.toString()+"</pre>");
			out.println("</div>");
			printMenu(out);
			out.println("</body></html>");
			out.close();
			return;
		}
	}
}
