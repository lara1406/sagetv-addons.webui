
package net.sf.sageplugins.webserver;


import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;

import Acme.Utils;
import Acme.Serve.FileServlet;

/**
 * @author Niel
 *
 * 
 *
 */
public class ChannelLogoServlet extends SageServlet {

	/**
     * 
     */
    private static final long serialVersionUID = -5245919447450812680L;
    /**
	 * 
	 */
	public ChannelLogoServlet() {
		super();
	}
	/* (non-Javadoc)
	 * @see net.sf.sageplugins.webserver.SageServlet#doServletGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		try {
            String channelLogoTypeStr = req.getParameter("type");
            String channelLogoIndexStr = req.getParameter("index");
            String channelLogoFallbackStr = req.getParameter("fallback");
            String channelLogoType = "Small";
            Integer channelLogoIndex = 1;
            Boolean channelLogoFallback = true;

            if ((channelLogoTypeStr != null) &&
                ((channelLogoTypeStr.equals("Small")) ||
                 (channelLogoTypeStr.equals("Med")) ||
                 (channelLogoTypeStr.equals("Large"))))
            {
                channelLogoType = channelLogoTypeStr;
            }

            if ((channelLogoIndexStr != null) &&
                ((channelLogoIndexStr.equals("0")) ||
                 (channelLogoIndexStr.equals("1")) ||
                 (channelLogoIndexStr.equals("2"))))
            {
                channelLogoIndex = Integer.valueOf(channelLogoIndexStr);
            }

            if ((channelLogoFallbackStr != null) &&
                (!channelLogoFallbackStr.equals("true")))
            {
                channelLogoFallback = false;
            }

            String ChannelID=req.getParameter("ChannelID");
			if ( ChannelID==null || ChannelID.length()==0)
				throw new Exception ("no ChannelID");
			Object Channel=SageApi.Api("GetChannelForStationID",ChannelID);
			if (Channel==null)
				throw new Exception ("invalid ChannelID: "+ChannelID);
			Object Logo=SageApi.Api("GetChannelLogo",Channel);
			if ( Logo == null )
				throw new Exception ("No logo for ChannelID: "+ChannelID);
			
			// so look in channel logos directory for channel name.ext
			String dirname=System.getProperty("user.dir")+File.separator+"ChannelLogos"+File.separator;
			String channelname=SageApi.StringApi("GetChannelName",new Object[]{Channel});
			File logofile=new File(dirname+channelname+".jpg");
			if ( !logofile.exists() ){
			    logofile=new File(dirname+channelname+".gif");
			}
			if ( !logofile.exists() ) {
			    logofile=new File(dirname+channelname+".png");
			}
			
			boolean headOnly=false;
			if (logofile.exists())
			{
    			long lastMod = logofile.lastModified();
    			try {
    			    if ( ! FileServlet.CheckIfModifiedSince(req,lastMod)){
    			        resp.setStatus( HttpServletResponse.SC_NOT_MODIFIED );
    			        headOnly=true;
    			    }
    			} catch ( IllegalArgumentException e) {
    			    log(e.toString());
    			}
    			resp.setContentType(getServletContext().getMimeType(logofile.getPath()));
    			resp.setDateHeader( "Last-modified", lastMod );
    			resp.setBufferSize(8192);
			}
			else
			{
	            Logo=SageApi.Api("GetChannelLogo",new Object[] {Channel, channelLogoType, channelLogoIndex, channelLogoFallback});
	            BufferedImage image=(BufferedImage)SageApi.Api("GetImageAsBufferedImage",Logo);
	            if ( image==null) 
	                throw new Exception("GetImageAsBufferedImage returned null");
	            // got a BufferedImage, write it out as PNG
	            // cache for at least 10 mins 
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
	                OutputStream os=resp.getOutputStream();
	                javax.imageio.ImageIO.write(image,"png",os);
	                os.close();
	            }
			}
			// set expiry date to now+1 week
			long expiry=System.currentTimeMillis()+(1000*60*60*24*7);
			resp.setDateHeader ("Expires", expiry);
			if ( ! headOnly ) {
			    FileInputStream ins=new FileInputStream(logofile);
			    OutputStream os=resp.getOutputStream();
			    try {
			        Utils.copyStream(ins,os);
			    } finally {
			        ins.close();
			        os.close();
			    }
			}
			
		} catch (Throwable e) {
			if (!resp.isCommitted()){
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				resp.setContentType("text/html");
			}
			OutputStream os = resp.getOutputStream();
			PrintWriter out=new PrintWriter(os);
			out.println();
			out.println();
			out.println("<body><pre>");
			out.println("Exception while processing servlet:\r\n"+e.toString());
			e.printStackTrace(out);
			out.println("</pre>");
			out.close();
			os.close();
			log("Exception while processing servlet",e);
		}
	}

}
