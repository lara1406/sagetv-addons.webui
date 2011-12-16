package net.sf.sageplugins.webserver;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Acme.Serve.FileServlet;

import net.sf.sageplugins.sageutils.SageApi;

public class MediaFileThumbnailServlet extends SageServlet {
    
    /**
     * 
     */
    private static final long serialVersionUID = 5914576130809217889L;
    
    protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
    throws Exception {
        Airing airing=null;
        try {
            Object thumb=null;
            String albumname=req.getParameter("albumname");
            if ( albumname==null ) {
                airing=new Airing(req);                
                thumb=SageApi.Api("GetThumbnail",airing.sageAiring);
                if ( thumb == null)
                    throw new Exception("GetThumbnail(mfid="+airing.id+") returned null");
            } else {
                // album thumb
                String artist=req.getParameter("artist");
                if ( artist == null) artist="";
                String year=req.getParameter("year");
                if ( year == null) year="";
                String genre=req.getParameter("genre");
                if ( genre == null) genre="";
                Object Albums=SageApi.Api("GetAlbums");
                Albums=SageApi.Api("FilterByMethod",new Object[]{Albums,"GetAlbumName",albumname,Boolean.TRUE});
                Albums=SageApi.Api("FilterByMethod",new Object[]{Albums,"GetAlbumArtist",artist,Boolean.TRUE});
                Albums=SageApi.Api("FilterByMethod",new Object[]{Albums,"GetAlbumYear",year,Boolean.TRUE});
                Albums=SageApi.Api("FilterByMethod",new Object[]{Albums,"GetAlbumGenre",genre,Boolean.TRUE});
                Object Album=SageApi.GetElement(Albums,0);
                if ( Album == null )
                    throw new Exception("Cound not find album for: "+albumname+"/"+artist+"/"+year+"/"+genre);
                if ( SageApi.booleanApi("HasAlbumArt",new Object[]{Album}))
                    thumb=SageApi.Api("GetAlbumArt",new Object[]{Album});
                if ( thumb == null)
                    throw new Exception("GetAlbumArt on album: "+Album+" returned null");
            }
            BufferedImage image=(BufferedImage)SageApi.Api("GetImageAsBufferedImage",thumb);
            if ( image==null) 
                throw new Exception("GetImageAsBufferedImage returned null");
            // got a BufferedImage, write it out as JPEG
            // cache for at least 10 mins 
            boolean headOnly=false;
            long lastMod = System.currentTimeMillis()-(10*60*1000);
            try {
                if ( ! FileServlet.CheckIfModifiedSince(req,lastMod)){
                    resp.setStatus( HttpServletResponse.SC_NOT_MODIFIED );
                    headOnly=true;
                }
            } catch ( IllegalArgumentException e) {
                log(e.toString());
            }
            
            resp.setContentType("image/png");
            resp.setDateHeader( "Last-modified", lastMod );
            resp.setBufferSize(8192);
            // set expiry date to now+1 week
            long expiry=System.currentTimeMillis()+(1000*60*60*24*7);
            resp.setDateHeader ("Expires", expiry);
            if ( ! headOnly ) {
                if ( req.getParameter("small") != null
                     && ( image.getWidth() > 100 || image.getHeight() > 100 ) ) {
                    Image scaledimage=image.getScaledInstance(50,-1,Image.SCALE_SMOOTH);
                    image=new BufferedImage(scaledimage.getWidth(null),scaledimage.getHeight(null),BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d=image.createGraphics();
                    g2d.drawImage(scaledimage,0,0,null);
                    g2d.dispose();
                }
                OutputStream os=resp.getOutputStream();
                javax.imageio.ImageIO.write(image,"png",os);
                os.close();
            }
            
        } catch ( Exception e) {
            htmlHeaders(resp);
            noCacheHeaders(resp);
            PrintWriter out = resp.getWriter();
            xhtmlHeaders(out);
            out.println("<head>");
            jsCssImport(req, out);
            out.println("<title>"+"MediaFile Thumbnail Servlet"+"</title></head>"); 
            out.println("<body>");
            printTitle(out,"Error");
            out.println("<div id=\"content\">");
            out.println("<h3>"+"failed to get thumbnail for mediafile</h3><pre>");
            out.println(e);
            e.printStackTrace(out);
            out.println("</pre></div>");
            printMenu(req,out);
            out.println("</body></html>");
            out.close();
            return;
        }
        
        
        
    }
    
}

