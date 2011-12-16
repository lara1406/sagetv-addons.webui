package net.sf.sageplugins.webserver;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.SocketException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;
import Acme.Serve.FileServlet;

public class ResizedImageServlet extends SageServlet {



    /**
     * 
     */
    private static final long serialVersionUID = 516902300215401847L;

    protected void doServletGet(HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        try {
            Airing airing=new Airing(req);
            if ( airing.idType!=Airing.ID_TYPE_MEDIAFILE)
                throw new IllegalArgumentException("no MediaFileID passed");
            
            if ( ! SageApi.booleanApi("IsPictureFile", new Object[]{airing.sageAiring}))
                throw new IllegalArgumentException("not a picture file");
            
            String widthStr=req.getParameter("width");
            if ( widthStr==null || widthStr.trim().length()==0)
                throw new IllegalArgumentException("no width specified");
            int width;
            try {
                width=Integer.parseInt(widthStr);
            } catch (NumberFormatException e){
                throw new IllegalArgumentException("invalid width specified -- "+e.toString());
            }
            
            File files[]=(File[])SageApi.Api("GetSegmentFiles",new Object[]{airing.sageAiring});
            File file=files[0];
            if ( !file.exists()) {
                throw new IllegalArgumentException("File does not exist: "+files[0]);
            }
            if ( ! file.canRead()) {
                throw new IllegalArgumentException("File can not be read: "+files[0]);
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
            res.setContentType( getServletContext().getMimeType( "test.jpg" ) );
            if (file.length() < Integer.MAX_VALUE )
                res.setContentLength( (int) file.length() );
            else
                res.setHeader("Content-Length", Long.toString(file.length()));
            res.setDateHeader( "Last-modified", lastMod );

            if ( headOnly ) {
                res.flushBuffer();
            } else {
                byte[] jpgResult;
                try {
                    sage.media.image.RawImage img=sage.media.image.ImageLoader.loadScaledImageFromFile(
                        file.getAbsolutePath(),width,0,0,0);
                    // recalc height based on width and reload
                    int height=(int)((double)width/(double)img.getWidth()*(double)img.getHeight());
                    img=sage.media.image.ImageLoader.loadScaledImageFromFile(file.getAbsolutePath(), width,height,0,0);
                    jpgResult=sage.media.image.ImageLoader.compressImageToMemory(
                            img, "jpg");
                } catch (java.io.IOException e) {
                    throw new IllegalArgumentException("File can not be read: "+files[0] + " -- " +e.toString());
                }

                res.setHeader("Content-Length", Integer.toString(jpgResult.length));
                res.setHeader("Content-Disposition","attachment; filename="+file.getName().replaceAll("\\.[^.]*$", "")+"_preview.jpg");
                OutputStream os=res.getOutputStream();
                try {
                    os.write(jpgResult);
                } catch ( SocketException e) {
                    /* assume socket has been closed -- ignore */
                } finally {
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
