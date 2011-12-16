package net.sf.sageplugins.webserver;

import net.sf.sageplugins.sageutils.SageApi;

import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.SocketException;

/**
 * Servlet used to transcode media files.  Similar to the MediaFileServlet class
 */
public class MediaFileTranscodeServlet extends SageServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -6376686701500403065L;

    public MediaFileTranscodeServlet() {
        super();
    }

    public void init(ServletConfig arg0) throws ServletException {
        super.init(arg0);
    }

    protected void doServletGet(HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        TranscodeInputStream is;
        try {
            log("MediaFileTranscodeServlet.doServletGet(): Retrieveing Airing Info");
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
            boolean isdvd=SageApi.booleanApi("IsDVD",new Object[]{airing.sageAiring});    
            
            if ( !file.exists()) {
                throw new IllegalArgumentException("File does not exist: "+files[filenum]);
            }
            if ( isdvd ) {
                if ( !file.isDirectory())
                    throw new IllegalArgumentException("Not a DVD directory: "+files[filenum]);
            }else if ( ! file.canRead()) {
                throw new IllegalArgumentException("File can not be read: "+files[filenum]);
            } 

            //read transcode parameters
            log("doServletGet(): Retrieveing transcoding parameters");
            
            boolean headOnly;
            if ( req.getMethod().equalsIgnoreCase( "get" ) )
                headOnly = false;
            else if ( ! req.getMethod().equalsIgnoreCase( "head" ) )
                headOnly = true;
            else {
                res.sendError( HttpServletResponse.SC_NOT_IMPLEMENTED );
                return;
            }
            String mode = req.getParameter("mode");
            if ( mode != null && mode.equalsIgnoreCase("vlc")) {
                String videoCodec = req.getParameter("vc");
                String videoBitrate = req.getParameter("vb");
                String audioCodec = req.getParameter("ac");
                String audioBitrate = req.getParameter("ab");
                String scale = req.getParameter("scale");
                String mux = req.getParameter("mux");
                
                String filename;
                if ( isdvd ){
                    filename="dvdsimple://";
                    if ( file.getName().equalsIgnoreCase("VIDEO_TS"))
                        filename=filename+file.getParentFile().getAbsolutePath();
                    else
                        filename=filename+file.getAbsolutePath();
                    String dvdTitle=req.getParameter("title");
                    if ( dvdTitle!=null )
                        filename=filename+"@"+dvdTitle;
                }
                else filename= file.getAbsolutePath();
                String deint = req.getParameter("deint");
                boolean do_deint=false;
                if ( deint != null &&( deint.trim().equals("1") ||
                                       deint.trim().equalsIgnoreCase("on") ))
                    do_deint=true;
                else    
                    do_deint=false;
                
                log("doServletGet(): File to play: " + filename);
    
                
                res.setStatus( HttpServletResponse.SC_OK );
                log("doServletGet(): Starting the transcode process.");
                is = VlcTranscodeMgr.getInstance().startTranscodeProcess(filename, videoCodec, Integer.parseInt(videoBitrate),
                                                        audioCodec, Integer.parseInt(audioBitrate), scale,do_deint,mux);
            } 
            // TODO add SageTVTranscoder as a transcoding mode.
            else {
                throw new java.lang.IllegalArgumentException("invalid transcoding mode "+mode); 
            }
            if (is == null) {
                log("doServletGet(): Unable to find transcoded stream.");
                throw new IOException("Unable to find transcoded stream.");
            }
            res.setContentType(getServletContext().getMimeType(is.getFileExt()));

            if ( headOnly ) {
                res.flushBuffer();
            } else {
                int lastDot=file.getName().lastIndexOf('.');
                if ( lastDot>0){
                    res.setHeader("Content-Disposition","attachment; filename="+file.getName().substring(0, lastDot-1)+is.getFileExt());
                }
                OutputStream os=res.getOutputStream();

                try {
                    // copy stream stopping at EOF 
                    log("doServletGet(): Copying transcode stream to outputstream");
                    byte[] buf=new byte[4096];
                    
                    int bytesRead;
                    while (-1 != (bytesRead = is.read(buf, 0, buf.length))) {
                        //copy bytes into os
                        os.write(buf, 0, bytesRead);
                    }
                    log("doServletGet(): End of file has been reached..... Done");
                } catch ( SocketException e) {
                    log("doServletGet(): Stopping transcode due: "+e);
                    /* assume socket has been closed -- ignore */
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ex) {
                            //do nothing
                        }
                    }
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException ex) {
                            //do nothing
                        }
                    }
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
            out.println("Exception while processing MediaFileTranscode Servlet:\r\n"+e.toString());
            e.printStackTrace(out);
            out.println("</pre>");
            out.close();
            os.close();
            log("Exception while processing servlet",e);
        }
    }

}
