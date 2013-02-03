/*
 * Created on Nov 2, 2004
 *
 * 
 *
 */
package net.sf.sageplugins.webserver;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;

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
public class TimedRecordServlet extends SageServlet {
	
	/**
     * 
     */
    private static final long serialVersionUID = -1373652794841647748L;

    void printOptionNumberRange(PrintWriter out,int start,int stop,int interval,int selected){
		for(int i=start;i<=stop;i+=interval){
			out.print("    <option value=\""+
						Integer.toString(i)+"\"");
			if ( i<=selected && (i+interval)>selected )
				out.print(" selected=\"selected\"");
			out.println(">"+
						Integer.toString(i)+"</option>");
		}
	}
	void printMonthRange(PrintWriter out,int selected){
		DateFormatSymbols dsyms=new DateFormatSymbols();
		String[] months=dsyms.getMonths();
		for(int i=0;i<=11;i++){
			out.print("    <option value=\""+
					Integer.toString(i)+"\"");
		if ( i==selected )
			out.print(" selected=\"selected\"");
		out.println(">"+months[i]+"</option>");
		}
	}
	
	protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {

		htmlHeaders(resp);
		noCacheHeaders(resp);
		PrintWriter out = resp.getWriter();
		xhtmlHeaders(out);
		out.println("<head>");
		jsCssImport(req, out);
		out.println("<title>Create Timed Recording</title>");
	    out.println("</head>");
	    out.println("<body>");
	    printTitle(out,"Create Timed Recording", SageServlet.isTitleBroken(req));
	    out.println("<div id=\"content\">\r\n");
	    		
		if ( req.getParameterMap()==null || req.getParameterMap().isEmpty()) {
		    GregorianCalendar cal=new GregorianCalendar();
		    
			// just output the form
			out.println("<form method='get' action='"+req.getRequestURI()+"'>\r\n" +
		    			"<table border=\"0\">\r\n" +
		    			"  <tr>\r\n" +
		    			"    <td>Channel:</td>\r\n" +
		    			"    <td>");
		    // channel selector
		    out.println("      <select name=\"StationId\">");
		    Object channels=SageApi.Api("GetAllChannels");
		    channels=SageApi.Api("FilterByBoolMethod",new Object[]{channels, "IsChannelViewable", Boolean.TRUE});
		    channels=SageApi.Api("Sort",new Object[]{channels,Boolean.FALSE,"ChannelNumber"});
		    for (int i =0; i<SageApi.Size(channels);i++){
		    	Object channel=SageApi.GetElement(channels,i);
		    	Object stationId=SageApi.Api("GetStationID",new Object[]{channel});
		    	Object channelnum=SageApi.Api("GetChannelNumber",new Object[]{channel});
		    	out.println("        <option value=\""+
		    				stationId.toString()+
							"\">"+
							channelnum.toString()+
							" - "+
							Translate.encode((String)SageApi.Api("GetChannelName",new Object[]{channel}))+
							"</option>");
		    }
			out.println("      </select>\r\n" +
					    "    </td>\r\n" +
					    "  </tr>\r\n" +
					    "  <tr>\r\n" +
					    "    <td>Start Date</td>\r\n"+
						"    <td>\r\n"+
						"     <select name='StartDay'>");
			printOptionNumberRange(out,1,31,1,cal.get(Calendar.DAY_OF_MONTH));
			out.println("     </select>\r\n" +
					    "     <select name='StartMonth'>");
			printMonthRange(out,cal.get(Calendar.MONTH));
			out.println("     </select>\r\n" +
		    			"     <select name='StartYear'>");
			printOptionNumberRange(out,cal.get(Calendar.YEAR),cal.get(Calendar.YEAR)+5,1,-1);
			out.println("     </select>\r\n" +
	    				"    </td>\r\n" +
						"  </tr>\r\n" +
					    "  <tr>\r\n" +
					    "    <td>Start Time</td>\r\n"+
						"    <td>\r\n"+
						"     <select name='StartHr'>");
			printOptionNumberRange(out,0,23,1,cal.get(Calendar.HOUR_OF_DAY));
			out.println("     </select>\r\n" +
						"     <select name='StartMins'>");
			printOptionNumberRange(out,0,59,5,cal.get(Calendar.MINUTE));
			out.println("      </select>\r\n" +
					    "    </td>\r\n" +
					    "  </tr>\r\n" +
					    "  <tr>\r\n" +
					    "    <td>Stop Date</td>\r\n"+
						"    <td>\r\n"+
						"     <select name='StopDay'>");
			printOptionNumberRange(out,1,31,1,cal.get(Calendar.DAY_OF_MONTH));
			out.println("     </select>\r\n" +
					    "     <select name='StopMonth'>");
			printMonthRange(out,cal.get(Calendar.MONTH));
			out.println("     </select>\r\n" +
		    			"     <select name='StopYear'>");
			printOptionNumberRange(out,cal.get(Calendar.YEAR),cal.get(Calendar.YEAR)+5,1,-1);
			out.println("     </select>\r\n" +
	    				"    </td>\r\n" +
						"  </tr>\r\n" +
					    "  <tr>\r\n" +
					    "    <td>Stop Time</td>\r\n"+
						"    <td>\r\n"+
						"    <select name='StopHr'>");
			printOptionNumberRange(out,0,23,1,cal.get(Calendar.HOUR_OF_DAY));
			out.println("     </select>\r\n" +
						"     <select name='StopMins'>");
			printOptionNumberRange(out,0,59,5,cal.get(Calendar.MINUTE));
			out.println("     </select>\r\n" +
	    				"    </td>\r\n" +
						"  </tr>\r\n" +
						"</table>\r\n" +
						"     <input type='submit' value=\"Set Recording\"/>\r\n"+
						"</form>\r\n");
			
			out.println("</div>");
			printMenu(req,out);
			out.println("</body></html>");
			out.close();
			return;
		} 
		// look for all arguments
		boolean argsOk=true;
		Object channel=null;
		String p=null;
		if ( (p=req.getParameter("StationId"))!=null){
			channel=SageApi.Api("GetChannelForStationID",new Object[]{p});
			if ( channel == null ){
				out.println("<p>Error: unknown Station ID: "+p+"</p>");
				argsOk=false;
			}
		}else {
			out.println("<p>Error: Missing StationId argument</p>");
			argsOk=false;
		}
		
		GregorianCalendar start=null;
		try {
			start=new GregorianCalendar(
				Utils.getIntParam(req,"StartYear","Start Year",Integer.MIN_VALUE,Integer.MAX_VALUE),
				Utils.getIntParam(req,"StartMonth","Start Month",0,11),
				Utils.getIntParam(req,"StartDay","Start Day",1,31),
				Utils.getIntParam(req,"StartHr","Start hour",0,23),
				Utils.getIntParam(req,"StartMins","Start minute",0,59));
		}catch (Exception e) {
			out.println("<p>Error: Invalid Starting Date: "+e.getMessage()+"</p>");
			argsOk=false;
		}
		GregorianCalendar stop=null;
		try {
			stop=new GregorianCalendar(
				Utils.getIntParam(req,"StopYear","Stop Year",Integer.MIN_VALUE,Integer.MAX_VALUE),
				Utils.getIntParam(req,"StopMonth","Stop Month",0,11),
				Utils.getIntParam(req,"StopDay","Stop Day",1,31),
				Utils.getIntParam(req,"StopHr","Stop hour",0,23),
				Utils.getIntParam(req,"StopMins","Stop minute",0,59));
		}catch (Exception e) {
			out.println("<p>Error: Invalid finishing Date/time: "+e.getMessage()+"</p>");
			argsOk=false;
		}
		if ( start.getTimeInMillis()>stop.getTimeInMillis()){
			out.println("<p>Error: recording stops before it starts!</p>");
			argsOk=false;
		}
		
		if ( argsOk ) {
			Object conflicts=checkManualRecordConflicts(start.getTimeInMillis(), stop.getTimeInMillis(), channel);
			if (SageApi.Size(conflicts)>0 ){
				
				// we cannot record this because of conflicts... show them...
				out.println("<p>Cannot this timed recording because it would coflict with the following manual recording(s)</p>");
				out.println("<p>You must cancel one of the following recordings and re-set this timed recording</p>");
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
			} else {
                String[] uicontexts=GetUIContextNames();
                if ( uicontexts == null || uicontexts.length==0)
    				SageApi.Api("CreateTimedRecording",new Object[]{
    						channel, 
    						new Long(start.getTimeInMillis()), 
    						new Long(stop.getTimeInMillis()),
    						"Once"});
                else
                    SageApi.ApiUI(uicontexts[0],
                            "CreateTimedRecording",new Object[]{
                            channel, 
                            new Long(start.getTimeInMillis()), 
                            new Long(stop.getTimeInMillis()),
                            "Once"});
				Object channelnum=SageApi.Api("GetChannelNumber",new Object[]{channel});
				String chname=Translate.encode((String)SageApi.Api("GetChannelName",new Object[]{channel}));
				DateFormat fmt=DateFormat.getDateTimeInstance();
				out.println("<p>Probably set up a timed recording from "+fmt.format(start.getTime())+" to "+fmt.format(stop.getTime())+" on channel "+
						channelnum.toString()+" - "+chname+"</p>");
				out.println("<p>(it's worth checking the <a href='RecordingSchedule'>Recording Schedule</a> to make sure!)</p>");
			}
		}
			
        printFooter(req,out);
        out.println("</div>");//content
		printMenu(req,out);
		out.println("</body></html>");
		out.close();
		return;
	}

}
