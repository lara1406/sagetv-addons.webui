package net.sf.sageplugins.webserver;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;
import net.sf.sageplugins.sageutils.Translate;

public class TranscodeJobsServlet extends SageServlet {


	/**
	 * 
	 */
	private static final long serialVersionUID = -4584455130711765086L;

	protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
	throws Exception {
		// output HTML
		htmlHeaders(resp);
		noCacheHeaders(resp);
		PrintWriter out = getGzippedWriter(req,resp);
		// must catch and report all errors within Gzipped Writer
		try {
			xhtmlHeaders(out);
			out.println("<head>");
			jsCssImport(req, out);
			out.println("<title>Video Conversions</title>");
			out.println("</head>");
			out.println("<body>");
			printTitle(out,"Video Conversions");


			out.println("<div id=\"content\">\r\n");

			if ( SAGE_MAJOR_VERSION<5.1){
				out.println("Requires Sage 5.1 or higher...");
			} else {

				out.println("<form name=\"AiringsForm\" method=\"get\" action=\"GlobalCommand\">");
				out.print("<input type=\"hidden\" name=\"returnto\" value=\""+req.getRequestURI()+(req.getQueryString()==null?"":"?"+req.getQueryString())+"\"/>");
				out.print("<input type=\"hidden\" name=\"command\" value=\"CancelTranscodeJobs\"/>");

				// clean up old transcode jobs...
				Integer[] TranscodeJobList = (Integer[])SageApi.Api("GetTranscodeJobs");
				for (int i = 0; i < TranscodeJobList.length; i++) {
					Integer jobId = TranscodeJobList[i];

					Object JobSrc = SageApi.Api("GetTranscodeJobSourceFile",jobId );
					if ( JobSrc ==null ){
						SageApi.Api("CancelTranscodeJob", jobId );
					} else {
						Object CurFile = SageApi.Api("GetFileForSegment", new Object[]{JobSrc,new Integer(0)});
						if ( CurFile==null)
							SageApi.Api("CancelTranscodeJob", jobId );
					}
				}	     
				TranscodeJobList = (Integer[])SageApi.Api("GetTranscodeJobs");

				if ( SageApi.Size(TranscodeJobList)==0){
					out.println("There are no video conversion jobs in the queue");
				} else {

					Object JobsDone = SageApi.Api("FilterByMethod", new Object[]{TranscodeJobList, "GetTranscodeJobStatus", "COMPLETED", Boolean.TRUE} );
					JobsDone = SageApi.Api("Sort",new Object[]{ JobsDone, Boolean.FALSE, null });
					Object JobsWorking = SageApi.Api("FilterByMethod", new Object[]{TranscodeJobList, "GetTranscodeJobStatus", "TRANSCODING", Boolean.TRUE} );
					JobsWorking = SageApi.Api("Sort",new Object[]{ JobsWorking, Boolean.FALSE, null });
					Object JobsWaiting = SageApi.Api("FilterByMethod", new Object[]{TranscodeJobList, "GetTranscodeJobStatus", "WAITING TO START", Boolean.TRUE});
					JobsWaiting = SageApi.Api("Sort",new Object[]{ JobsWaiting, Boolean.FALSE, null });
					Object JobsFailed = SageApi.Api("FilterByMethod", new Object[]{TranscodeJobList, "GetTranscodeJobStatus", "FAILED", Boolean.TRUE} );
					JobsFailed = SageApi.Api("Sort",new Object[]{ JobsFailed,Boolean.FALSE, null });
					Vector<?> AllJobs = (Vector<?>)SageApi.Api("DataUnion", new Object[]{ JobsWorking, JobsWaiting, JobsFailed, JobsDone });


					out.println("<div class=\"airings\" id=\"airingsList\">");
					out.println("<div class=\"exphideall\">");
					out.println("<a href=\"javascript:checkAll(true)\">[Select all]</a>\r\n" +
					"<a href=\"javascript:checkAll(false)\">[Unselect all]</a>");
					out.println("</div>");
					for (Iterator<?> iter = AllJobs.iterator(); iter.hasNext();) {
						Integer thisJob = (Integer) iter.next();
						Object src=SageApi.Api("GetTranscodeJobSourceFile",thisJob);
						System.out.println("src=("+src.getClass()+") - "+src);
						if ( ! SageApi.booleanApi("IsMediaFileObject", new Object[]{src})) {
							src=SageApi.Api("GetMediaFileAiring",src);
							System.out.println("src=("+src.getClass()+") - "+src);
						}
						if ( src != null ) {
							Airing airing=new Airing(src);
							out.println("   <div class=\"epgcell\"><table width=\"100%\" height=\"2.5em\" class=\"epgcellborder showother show_other\"><tr>");
							out.print("     <td class='checkbox'><input type='checkbox' name=\"JobId\" value=\""+thisJob+"\"/></td>");

							out.println("      <td class=\"titlecell\">");
							out.println("<div>");
							String infoLink="<a href=\"DetailedInfo?"+airing.getIdArg()+"\">";

							String title=airing.getTitle().trim();
							if ( title.length()==0)
								title="\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0"; // 10 non-breaking spaces
							out.println(infoLink);
							out.println(Translate.encode(title));
							String ep=airing.getEpisode();
							if (  ep != null && ep.trim().length()!=0)
								out.println("-- "+Translate.encode(ep));
							// transcoding details

							Long startTime=(Long)SageApi.Api("GetTranscodeJobClipStart",thisJob);
							Long duration=(Long)SageApi.Api("GetTranscodeJobClipDuration",thisJob);
							String quality=SageApi.StringApi("GetTranscodeJobFormat", new Object[]{thisJob});
							if ( startTime != null && duration!=null) {
								if ( duration.longValue() >0) {
									// from x to y
									String startText=(String)SageApi.Api("DurFormat",new Object[]{"%h:%rm:%rs",new Long(startTime.longValue())});
									String endText=(String)SageApi.Api("DurFormat",new Object[]{"%h:%rm:%rs",new Long((duration.longValue()+startTime.longValue()))});
									out.println("<br/>From "+startText+" to "+endText+" @ "+quality);
								} else {
									// to end
									if ( startTime.longValue()>0) {
										// from x to end
										String startText=(String)SageApi.Api("DurFormat",new Object[]{"%h:%rm:%rs",new Long(startTime.longValue()*1000)});
										out.println("<br/>From "+startText+" to end @ "+quality);
									} else
										out.println("<br/>Entire file @ "+quality);
								}
							} else {
								out.println("<br/>Entire file @ "+quality);
							}

							out.println("      </a></div></td>");
                            String status=(String)SageApi.Api("GetTranscodeJobStatus",thisJob);
                            
                            out.println("      <td class=\"datecell\"><div>");
                            boolean replaceOrig=!SageApi.booleanApi("GetTranscodeJobShouldKeepOriginal",new Object[]{thisJob});
                            if ( status.equals("COMPLETED")){
                                if  (replaceOrig){
                                    out.println("Original replaced");
                                } else {
                                    Object destFile=SageApi.Api("GetTranscodeJobDestFile",thisJob);
                                    if (destFile!=null ) {
                                        destFile=SageApi.Api("GetMediaFileForFilePath",destFile);
                                        if ( destFile!=null){
                                            Airing convAiring=new Airing(destFile);
                                            out.println("<a title=\"Show detailed info for original file\" href=\"DetailedInfo?"+airing.getIdArg()+"\">[Info-Original]</a><br/>");
                                            out.println("<a title=\"Show detailed info for converted file\" href=\"DetailedInfo?"+convAiring.getIdArg()+"\">[Info-Converted]</a>");
                                        }
                                    }
                                    if ( destFile==null){
                                        out.println("Dest file removed");
                                    }
                                }
                            } else if ( status.equals("TRANSCODING") || status.equals("WAITING TO START") ){
                                if  (replaceOrig){
                                    out.println("Replacing Original");
                                } else {
                                    out.println("Creating New File");
                                }
                            }
                            out.println("      </div></td>");
							out.println("      <td class=\"channelcell\"><div>");
							if ( status.equals("COMPLETED")){
								out.println("Converted");
							} else if ( status.equals("TRANSCODING")){
								Float PercentDone = (Float)SageApi.Api("GetTranscodeJobCompletePercent",thisJob);
								out.println("Converting: "+(int)(PercentDone.floatValue()*100.0)+"%");
							} else if ( status.equals("WAITING TO START")){
								out.println("Waiting...");
							} else if ( status.equals("FAILED")){
								out.println("Failed");
							} else {
								out.println("Unknown status: "+status);
							}

							out.println("</div></td>");

							out.println("   </tr></table></div>");//epgcell
						}
					}

					out.println("   </div>");//airingslist

					out.println("<div class=\"exphideall\">");
					out.println("<a href=\"javascript:checkAll(true)\">[Select all]</a>\r\n" +
					"<a href=\"javascript:checkAll(false)\">[Unselect all]</a>");
					out.println("</div>");

					out.println("<input type=\"submit\" name=\"cancelselected\" value=\"Cancel Selected Jobs\"/> " +
					"<input type=\"submit\" name=\"clearall\" value=\"Clear All Completed\"/>\r\n");
					out.println("</form>\r\n");

				}
			}		    
			printFooter(req,out);
			out.println("</div>");//content
			printMenu(out);
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
