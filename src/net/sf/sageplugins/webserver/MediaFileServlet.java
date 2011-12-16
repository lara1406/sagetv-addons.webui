
package net.sf.sageplugins.webserver;

import java.io.File;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;

import Acme.Serve.FileServlet;
import Acme.Serve.ThrottleItem;
import Acme.Serve.ThrottledOutputStream;

/**
 * @author Niel Markwick
 *
 * 
 *
 */
public class MediaFileServlet extends SageServlet {

	/**
     * 
     */
    private static final long serialVersionUID = -8306840008048777846L;
    static Acme.WildcardDictionary throttleTab = null;
    /**
     * 
     */
    public MediaFileServlet() {
        super();
        try {
            String root=SageApi.GetProperty("nielm/webserver/root","webserver");
            String throttles=SageApi.GetProperty("nielm/webserver/throttles_file","throttles.properties");
            if ( throttles != null && throttles.length()>0){
                File file=new File(throttles);
                if (file.isAbsolute() == false)
					file = new File(root, file.getName());
                if (file.isAbsolute() == false)
					file = new File(System.getProperty("user.dir", "."), file.getPath());
                //System.out.println("root:"+root+"  throttles:"+throttles+"  file:"+file.getPath());
            	Acme.WildcardDictionary newThrottleTab =
            		ThrottledOutputStream.parseThrottleFile( file.getPath() );
            	if ( throttleTab == null )
            		throttleTab = newThrottleTab;
            	else {
            		// Merge the new one into the old one.
            		Enumeration<?> keys = newThrottleTab.keys();
            		Enumeration<?> elements = newThrottleTab.elements();
            		while ( keys.hasMoreElements() ) {
            			Object key = keys.nextElement();
            			Object element = elements.nextElement();
            			throttleTab.put( key, element );
            		}
            	}
            }
        }catch (Throwable e) {
            Acme.Serve.Serve.extLog("Failed to load throttles:"+e.toString());
            e.printStackTrace();
        }
    }
    

    public void init(ServletConfig arg0) throws ServletException {
        super.init(arg0);
        
    }
    /* (non-Javadoc)
     * @see net.sf.sageplugins.webserver.SageServlet#doServletGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doServletGet(HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        
        try {
            Airing airing=new Airing(req);
            if ( airing.idType!=Airing.ID_TYPE_MEDIAFILE)
                throw new IllegalArgumentException("no MediaFileID passed");
            File files[]=(File[])SageApi.Api("GetSegmentFiles",new Object[]{airing.sageAiring});
            int filenum=0;
            if ( files.length > 1 ){
                String filenum_str=req.getParameter("Segment");
                if ( filenum_str == null )
                    throw new IllegalArgumentException("Segmented file: requires Segment Number");
                try { 
                    filenum=Integer.parseInt(filenum_str);
                    if ( filenum < 0 || filenum > files.length)
                        throw new NumberFormatException();
                }catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid Segment Number: "+filenum_str);
                }
            }
            File file=files[filenum];
            if ( !file.exists()) {
                throw new IllegalArgumentException("File does not exist: "+files[filenum]);
            }
            if ( ! file.canRead()) {
                throw new IllegalArgumentException("File can not be read: "+files[filenum]);
            }

            boolean headOnly=false;
    		if ( req.getMethod().equalsIgnoreCase( "get" ) )
    			headOnly = false;
    		else if ( ! req.getMethod().equalsIgnoreCase( "head" ) )
    			headOnly = true;
    		else {
    			res.sendError( HttpServletResponse.SC_NOT_IMPLEMENTED );
    			return;
    		}
    		res.setStatus( HttpServletResponse.SC_OK );
    		
            boolean isRecording=SageApi.booleanApi("IsFileCurrentlyRecording", new Object[]{airing.sageAiring});
            
    		// Handle If-Modified-Since.
    		long lastMod = file.lastModified();
    		try {
    		    if ( ! FileServlet.CheckIfModifiedSince(req,lastMod)){
    		        res.setStatus( HttpServletResponse.SC_NOT_MODIFIED );
    		        headOnly=true;
    		    }
    		} catch ( IllegalArgumentException e) {
    		    log(e.toString());
    		}
    		res.setContentType( getServletContext().getMimeType( file.getName() ) );
    		if (file.length() < Integer.MAX_VALUE )
    			res.setContentLength( (int) file.length() );
    		else
    			res.setHeader("Content-Length", Long.toString(file.length()));
    		res.setDateHeader( "Last-modified", lastMod );
            
    		
    		if ( headOnly ) {
    		    res.flushBuffer();
    		} else {
    		    res.setHeader("Accept-Ranges","bytes");
    		    			    
    		    // check range request
    		    long start=0;
    	        long fileLen=file.length();
                long stop=fileLen-1;
                //  if currently recording file, handle length specially:
                if ( isRecording && filenum == files.length-1 ){
                    // estimate filesize of scheduled recording
                    //
                    long startTime=((Long)SageApi.Api("GetStartForSegment",new Object[]{airing.sageAiring,new Integer(filenum)})).longValue();
                    long endTime=((Long)SageApi.Api("GetScheduleEndTime",airing.sageAiring)).longValue();
                    long currTime=System.currentTimeMillis();
                    long byterate=(file.length())/(currTime-startTime);
                    fileLen=((endTime-startTime)*105/100)*byterate;
                    stop=fileLen-1;
                    System.out.println("MediaFileServlet: Currently recording file: start="+startTime+" curr="+currTime+" end="+endTime+" est filesize="+fileLen+ " est byterate="+byterate);
                }
                
    	        String range=req.getHeader("Range");
    		    if ( range != null && range.trim().startsWith("bytes=")){
                    System.out.println("MediaFileServlet: Requested Range ="+range + " File length ="+fileLen);
    		        range=range.trim().substring(6);
    		        String ranges[]=range.split(",");
    		        if ( ranges.length > 0 ) {
    			        String startstop[]=ranges[0].split("-");
    			        if (startstop.length>0 && startstop[0].trim().length()>0){
    			            try {
    			                start=Long.parseLong(startstop[0].trim());
    			            } catch (NumberFormatException e) {
    			                log("invalid range specifyer (start)"+range,e);
    			            }
    			        }
    			        if ( startstop.length>1 && startstop[1].trim().length()>0){
    			            try {
    			                stop=Long.parseLong(startstop[1].trim());
    			            } catch (NumberFormatException e) {
    			                log("invalid range specifyer (stop)"+range,e);
    			            }
    			        }
                        
                        // if currently recording file, handle range request specially:
    		            if ( isRecording && filenum == files.length-1 ){
                            // check start point
                            if ( start>file.length())
                                start=file.length();
                        } 
                        
                        // check ranges
                        if ( start > stop || start<0 || stop>=fileLen){
                           res.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                            return;
                        }
                        res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
    		        }
    		    } 
                System.out.println("MediaFileServlet: Supplying range "+"bytes "+Long.toString(start)+"-"+Long.toString(stop)+"/"+Long.toString(fileLen));
    		    RandomAccessFile rfile=new RandomAccessFile(file,"r");
    		    if ( start>0 ){
                    rfile.seek(start);
    		    }
    		    res.setHeader("Content-Range","bytes "+Long.toString(start)+"-"+Long.toString(stop)+"/"+Long.toString(fileLen));
    		    res.setHeader("Content-Length", Long.toString(stop-start+1));
                res.setHeader("Content-Disposition","attachment; filename="+file.getName());
    		    OutputStream os=res.getOutputStream();
    			// Check throttle.
    			if ( throttleTab != null ) {
    			    // create pseudo path name
    				ThrottleItem throttleItem =
    					(ThrottleItem) throttleTab.get( req.getRequestURI()+"/"+file.getName().toLowerCase());
    				if ( throttleItem != null ) {
    					// !!! Need to account for multiple simultaneous fetches.
    					os = new ThrottledOutputStream(
    							os, throttleItem.getMaxBps() );
    					//System.out.println("Throttling "+file.getName().toLowerCase()+" to "+throttleItem.getMaxBps());
    				}
    			}
    		    try {
    		        // copy stream stopping at 'stop' bytes, or at EOF if stop==fileLen-1
    		        long curlen=start;
    		        byte[] buf=new byte[4096];
    		        while ( stop == fileLen-1 || curlen <= stop ) {
    		            int len=4096;
    		            len=rfile.read(buf,0,(int)len);
                        if ( len ==-1 ) {
                            // EOF reached -- check if file is still being recorded.
                            isRecording=SageApi.booleanApi("IsFileCurrentlyRecording", new Object[]{airing.sageAiring});
                            files=(File[])SageApi.Api("GetSegmentFiles",new Object[]{airing.sageAiring});
                            if ( isRecording && filenum == files.length-1 ){
                                // still recording this segment -- try reading again after a few 100ms
                                Thread.sleep(500);
                                len=4096;
                                len=rfile.read(buf,0,(int)len);
                            }
                        }
    		            if ( len >0 ){
                            os.write(buf,0,(int)len);
                            curlen+=len;
    		            }else {
                            System.out.println("MediaFileServlet: EOF met");
    		                // end of file -- stop
    		                break;
    		            }   
    		        }
                } catch ( SocketException e) {
                    /* assume socket has been closed -- ignore */
    		    } finally {
    		        rfile.close();
    		        os.close();
    		    }
    		}
        } catch (Throwable e){
			if (!res.isCommitted()){
				res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				res.setContentType("text/html");
			}
			OutputStream os = res.getOutputStream();
			PrintWriter out=new PrintWriter(os);
			out.println();
			out.println();
			out.println("<body><pre>");
			out.println("Exception while processing MediaFile Servlet:\r\n"+e.toString());
			e.printStackTrace(out);
			out.println("</pre>");
			out.close();
			os.close();
			log("Exception while processing servlet",e);
        }
    

    }

}
