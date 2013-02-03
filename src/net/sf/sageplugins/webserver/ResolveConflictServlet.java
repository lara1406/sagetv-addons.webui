
package net.sf.sageplugins.webserver;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;

/**
 * @author Owner
 *
 * 
 *
 */
public class ResolveConflictServlet extends SageServlet {

	/**
     * 
     */
    private static final long serialVersionUID = 970734516787359160L;

    void paramError(HttpServletRequest req,HttpServletResponse resp, String err)
	throws Exception {
		htmlHeaders(resp);
		noCacheHeaders(resp);
		PrintWriter out = resp.getWriter();
		xhtmlHeaders(out);
		out.println("<head>");
		jsCssImport(req, out);
		out.println("<title>Resolving Conflicts</title></head>");
		out.println("<body>");
	    printTitle(out,"Error", SageServlet.isTitleBroken(req));
	    out.println("<div id=\"content\">");
		out.println("<h3>"+err+"</h3>");
		out.println("</div>");
		printMenu(req,out);
		out.println("</body></html>");
		out.close();
		return;
	}
	protected Object getManualRecordConflicts(long startmillis, long stopmillis, Object channel)
	throws Exception{
		Object rv=null;
		
		Object inputs = SageApi.Api("GetConfiguredCaptureDeviceInputs");
		//for each input
		for (int inputnum=0;inputnum<SageApi.Size(inputs);inputnum++) {
			Object input=SageApi.GetElement(inputs,inputnum);
			//System.out.println("looking at input"+input.toString());
			Object lineup=SageApi.Api("GetLineupForCaptureDeviceInput",new Object[]{input});
			//System.out.println("looking at lineup "+lineup.toString());
			if ( SageApi.booleanApi("IsChannelViewableOnLineup",new Object[]{channel, lineup}) ){
				//System.out.println("looking at channel"+channel.toString());
				Object capdev=SageApi.Api("GetCaptureDeviceForInput",new Object[]{input});
				Object overlaps = SageApi.Api("GetScheduledRecordingsForDeviceForTime",new Object[]{
										capdev,new Long(startmillis),new Long(stopmillis)});
				//System.out.println("found "+SageApi.Size(overlaps)+" scheduled");
				overlaps = SageApi.Api("FilterByBoolMethod",new Object[]{overlaps,"IsManualRecord", Boolean.TRUE});
				//System.out.println("found "+SageApi.Size(overlaps)+" manrecs scheduled");
				if ( SageApi.Size(overlaps)>0)
					rv=SageApi.Api("DataUnion",new Object[]{rv,overlaps});
			}
		}
		//System.out.println("returning "+SageApi.Size(rv)+" overlaps");
		return rv;
	}
	
	protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		PrintWriter out=null;
		String returnto=req.getParameter("returnto");
		String command=req.getParameter("command");
		
		
		if ( command != null ) {
			if ( command.equals("ConfirmManRecOverride")){
				Airing missedAiring=null;
				String missedAiringId=null;
				try {
					missedAiringId=req.getParameter("missedAiringId");
					missedAiring=new Airing(Airing.ID_TYPE_AIRING,Integer.parseInt(missedAiringId));
				} catch (Exception e){
					paramError(req,resp,"invalid missedAiringId: "+missedAiringId);
					return;
				}
				Object channel=SageApi.Api("GetChannel",new Object[]{missedAiring.sageAiring});
				Object manrecs=getManualRecordConflicts(
						missedAiring.getStartDate().getTime(),
						missedAiring.getEndDate().getTime(),
						channel);
				//System.out.println("Got conflicting manuals: "+Integer.toString(SageApi.Size(manrecs))+
				//        manrecs.toString());

				for ( int i =0; i<SageApi.Size(manrecs); i ++){
					Object cfair=SageApi.GetElement(manrecs,i);
					if ( cfair != missedAiring.sageAiring)
					    SageApi.Api("ConfirmManualRecordOverFavoritePriority",
							new Object[]{cfair, missedAiring.sageAiring});
				}
			} else if ( command.equals("FavoriteOverride")){
				String newpriofaveid=null;
				Object newpriofave=null;
				String oldfaveid=null;
				Object oldfave=null;
				try {
					newpriofaveid=req.getParameter("newpriofaveid");
					oldfaveid=req.getParameter("oldfaveid");
					newpriofave=SageApi.Api("GetFavoriteForID",new Object[]{newpriofaveid});
					oldfave=SageApi.Api("GetFavoriteForID",new Object[]{oldfaveid});
				} catch (Exception e){
					paramError(req,resp,"invalid favorite IDs: "+newpriofaveid+", "+oldfaveid);
					return;
				}
				try {
					SageApi.Api("CreateFavoritePriority",new Object[]{newpriofave,oldfave});
				}catch (Exception e){
					paramError(req,resp,"failed to modify favorite priorities for IDs: "+newpriofaveid+", "+oldfaveid);
					return;
				}
			} else {
				paramError(req,resp,"Invalid command");
				return;
			}
		} else {
			paramError(req,resp,"No command passed");
			return;
		}
		
		
		if  (returnto== null){
			htmlHeaders(resp);
			noCacheHeaders(resp);
			out = resp.getWriter();
			xhtmlHeaders(out);
			out.println("<head>");
			jsCssImport(req, out);
			out.println("<title>Resolved Conflicts</title></head>");
			out.println("<body>");
		    printTitle(out,"", SageServlet.isTitleBroken(req));
			out.println("<div id=\"content\">");
			if ( command.equals("ConfirmManRecOverride")){
				out.println("Confirmed manual record has priority over favorite");
			} else if ( command.equals("FavoriteOverride")){
				out.println("Altered favorite priorities");
			}
			out.println("</div>");
			printMenu(req,out);
			out.println("</body></html>");
			out.close();
		} else {
			//	wait a little for the command to take effect before redirecting
			Thread.sleep(500);
			resp.sendRedirect(returnto);
		} 	
	}
}
