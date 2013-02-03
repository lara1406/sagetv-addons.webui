
package net.sf.sageplugins.webserver;

import java.io.File;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sageplugins.sageutils.SageApi;
import net.sf.sageplugins.sageutils.Translate;

/**
 * @author Niel Markwick
 *
 * 
 *
 */
public class PlaylistGeneratorServlet extends SageServlet {
    private class PlaylistItemInfo {
        public String path;// path or URL
        public long length; // seconds or -1
        public String title; // or numm
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 6646801436519156488L;

    /**
     * 
     */
    public PlaylistGeneratorServlet() {
        super();
    }

    static final int PL_WVX=1;
    static final int PL_M3U=2;
    static final int PL_PLS=3;

    static final int FN_PATH=1;
    static final int FN_URL=2;
    
    /* (non-Javadoc)
     * @see net.sf.sageplugins.webserver.SageServlet#doServletGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {

       
        // request can be
        //
        // Auto-generate a playlist for MediaFiles/URLs
        // Playlist?Command=Generate&pltype=a&fntype=b&MediaFileId=x1&MediaFileId=x2
        // Playlist?Command=Generate&pltype=a&Url=z1&Url=z2&Url=z3
        //
        // Export a Playlist
        // Playlist?Command=Export&pltype=a&fntype=b&PlaylistName=y1
        //
        // List all Playlists
        // Playlist?Command=List

        // Where t = m3u|wvx|pls
        
        // b=filepath (generated playlist will contain local paths to filenames)
        // b=url      (generated playlist will contain urls to MediaFileServlet)
        
        // x#=mediafile ID
        // y1=PlaylistName
        // z#=any URL
        int pltype=0;
        String s=req.getParameter("pltype");
        if ( s!=null && s.equalsIgnoreCase("m3u") )
            pltype=PL_M3U;
        else if ( s!=null && s.equalsIgnoreCase("wvx") )
            pltype=PL_WVX;
        else if ( s!=null && s.equalsIgnoreCase("pls") )
            pltype=PL_PLS;
        
        int fntype=0;
        s=req.getParameter("fntype");
        if ( s!=null && s.equals("url") )
            fntype=FN_URL;
        else if ( s!=null && s.equals("filepath") )
            fntype=FN_PATH;
        
        try { 
	        String command=req.getParameter("Command");
	        if (command != null && command.equalsIgnoreCase("list")) {
			    htmlHeaders(resp);
	    		noCacheHeaders(resp);
	    		PrintWriter out = getGzippedWriter(req,resp);
                // must catch and report all errors within Gzipped Writer
	    		try {
	    		    xhtmlHeaders(out);
	    		    out.println("<head>");
	    		    jsCssImport(req, out);
	    		    out.println("<title>Playlist Manager</title></head>");
	    		    out.println("<body>");
	    		    printTitle(out,"Playlists", SageServlet.isTitleBroken(req));
	    		    out.println("<div id=\"content\">");
	    		    out.println("<table border=\"1\">");
	    		    out.println("<tr><td>Name</td><td>Content</td><td colspan=\"2\" align=\"center\">Export</td></tr>");
	    		    out.println("<tr><td>&nbsp;</td><td align=\"center\">Streaming</td><td align=\"center\">Files</td></tr>");
	    		    Object playlists=SageApi.Api("GetPlaylists");
	    		    int numplaylists=SageApi.Size(playlists);
	    		    for (int i=0;i<numplaylists;i++ ){
	    		        Object playlist=SageApi.GetElement(playlists,i);
	    		        String name=SageApi.StringApi("GetName",new Object[]{playlist});
	    		        out.println("<tr><td>"
	    		                + Translate.encode(name)
	    		                + "</td>");
                        // analyse playlist content
                        
	    		        Object[] items=(Object[])SageApi.Api("GetPlaylistItems",playlist);
                        if(items != null){
                            int segmentCount = items.length;
                            int songCount = 0;
                            int albumCount = 0;
                            int playlistCount = 0;
                            int TVCount = 0;
                            int DVDCount = 0;
                            int videoCount = 0;
                            int unknown=0;
                            for (int itemNum = 0; itemNum < items.length; itemNum++) {
                                Object playlistItem = items[itemNum];
                                String type=SageApi.StringApi("GetPlaylistItemTypeAt",new Object[]{playlist, new Integer(itemNum)});
                                if ( type.equalsIgnoreCase("Airing")){
                                    if ( SageApi.booleanApi("IsMusicFile", new Object[]{playlistItem})) {
                                        songCount++;
                                    } else if ( SageApi.booleanApi("IsTVFile", new Object[]{playlistItem})) {
                                        TVCount++;
                                    } else if ( SageApi.booleanApi("IsDVD", new Object[]{playlistItem})) {
                                        DVDCount++;
                                    } else if ( SageApi.booleanApi("IsVideoFile", new Object[]{playlistItem})) {
                                        videoCount++;
                                    } else {
                                        unknown++;
                                    }
                                } else if ( type.equalsIgnoreCase("Playlist")){
                                    playlistCount++;
                                } else if ( type.equalsIgnoreCase("Album")){
                                    albumCount++;
                                } else {
                                    unknown++;
                                }
                            }
                            out.println("<td>\n"
                                            + segmentCount+" segments<br/>");
                            if ( songCount>0)
                                out.println(songCount+" songs<br/>");
                            if ( albumCount>0)
                                out.println(albumCount+" albums<br/>");
                            if ( playlistCount>0)
                                out.println(playlistCount+" playlists<br/>");
                            if ( TVCount>0)
                                out.println(TVCount+" TV shows<br/>");
                            if ( DVDCount>0)
                                out.println(DVDCount+" DVDs<br/>");
                            if ( videoCount>0)
                                out.println(videoCount+" videos<br/>");
                            if ( unknown>0)
                                out.println(unknown+" unknown/missing files<br/>");
                            out.println("</td>");
                        }
                        
                        
	    		        out.println("<td>"+
	    		                "<a href=\"PlaylistGenerator?Command=Export&pltype=wvx&fntype=url&PlaylistName="+URLEncoder.encode(name,charset)+"\">[wvx]</a> "+
	    		                "<a href=\"PlaylistGenerator?Command=Export&pltype=m3u&fntype=url&PlaylistName="+URLEncoder.encode(name,charset)+"\">[m3u]</a> "+
	    		                "<a href=\"PlaylistGenerator?Command=Export&pltype=pls&fntype=url&PlaylistName="+URLEncoder.encode(name,charset)+"\">[pls]</a> "+
	    		        "</td>");
	    		        out.println("<td>"+
	    		                "<a href=\"PlaylistGenerator?Command=Export&pltype=wvx&fntype=filepath&PlaylistName="+URLEncoder.encode(name,charset)+"\">[wvx]</a> "+
	    		                "<a href=\"PlaylistGenerator?Command=Export&pltype=m3u&fntype=filepath&PlaylistName="+URLEncoder.encode(name,charset)+"\">[m3u]</a> "+
	    		                "<a href=\"PlaylistGenerator?Command=Export&pltype=pls&fntype=filepath&PlaylistName="+URLEncoder.encode(name,charset)+"\">[pls]</a> "+
	    		        "</td>");
	    		        out.println("</tr>");
	    		    }
	    		    out.println("</table>");
                    out.println("<p>Unknown/missing files can be files that were deleted, removed from the library, or re-imported.<br/>" +
                                "Attempting to play the playlist in Sage may automatically re-link these files</p>");
	    		    out.println("</div>");
	    		    printMenu(req,out);
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
				return;
			} else if (command != null &&  command.equalsIgnoreCase("generate")){
			    if (pltype==0){
			        throw new IllegalArgumentException("pltype not specified");
			    }
                if (fntype==0){
                    throw new IllegalArgumentException("fntype not specified");
                }
			    
                LinkedList<PlaylistItemInfo> playListItems=new LinkedList<PlaylistItemInfo>();
                String[] mfIds=req.getParameterValues("MediaFileId");
                if ( mfIds!=null ){
                    for ( int i=0;i<mfIds.length;i++){
                        Object mf=SageApi.Api("GetMediaFileForID",new Integer(mfIds[i]));
                        ExportMediaFile(req,playListItems,mf,fntype);
                    }
                }
                String[] urls=req.getParameterValues("Url");
                if  ( urls != null ) {
                    for ( int i=0;i<urls.length;i++){
                        PlaylistItemInfo item=new PlaylistItemInfo();
                        item.path=urls[i];
                        item.length=0;
                        playListItems.addLast(item);
                    }
                }
                WritePlaylist(req,resp,playListItems,pltype,"playlist");
                
			} else if ( command != null && command.equalsIgnoreCase("export")){
			    if (pltype==0){
			        throw new IllegalArgumentException("pltype not specified");
			    }
			    if (fntype==0){
			        throw new IllegalArgumentException("fntype not specified");
			    }
                String name=req.getParameter("PlaylistName");
                if ( name==null || name.length()==0 )
                    throw new IllegalArgumentException("PlaylistName not specified");
                name=URLDecoder.decode(name,charset);
                
			    Object playlists=SageApi.Api("GetPlaylists");
                playlists=SageApi.Api("FilterByMethod",new Object[]{playlists, "GetName", name, Boolean.TRUE});
                if ( SageApi.Size((playlists))==0 ){
                    throw new IllegalArgumentException("PlaylistName: \""+name+"\" not known");
                }
			    Object playlist=SageApi.GetElement(playlists,0);
                
                LinkedList<PlaylistItemInfo> PlayListItems=new LinkedList<PlaylistItemInfo>();
                ExportPlaylist(req,PlayListItems,playlist,fntype,0);
                
                WritePlaylist(req,resp,PlayListItems,pltype,name);
                
                
			} else {
		        htmlHeaders(resp);
				noCacheHeaders(resp);
				PrintWriter out = resp.getWriter();
				xhtmlHeaders(out);
				out.println("<head>");
				jsCssImport(req, out);
				out.println("<title>Playlist Manager</title></head>");
				out.println("<body>");
			    printTitle(out,"Error", SageServlet.isTitleBroken(req));
			    out.println("<div id=\"content\">");
				out.println("<h3>Invalid command passed</h3>");
				out.println("<pre>Request can be:\r\n"+
				            "\r\n"+
				            "\r\n"+
				            "Auto-generate a playlist for MediaFiles/URLs\r\n"+
				            "     Playlist?Command=Generate&pltype=a&fntype=b&MediaFileId=w1&MediaFileId=w2\r\n"+
				            "     Playlist?Command=Generate&pltype=a&Url=z1&Url=z2&Url=z3\r\n"+
				            "\r\n"+
				            "Export a Playlist\r\n"+
				            "     Playlist?Command=Export&pltype=a&fntype=b&PlaylistName=y\r\n"+
				            "\r\n"+
				            "List all Playlists\r\n"+
				            "     Playlist?Command=List\r\n"+
				            "\r\n"+
				            "Where:" +
				            "     t = m3u|wvx|pls\r\n"+
				            "     b=filepath (generated playlist will contain local paths to filenames)\r\n"+
				            "     b=url      (generated playlist will contain urls to MediaFileServlet)\r\n"+
				            "     w#=mediafile ID\r\n"+
				            "     x=PlaylistID\r\n"+
				            "     y=PlaylistName\r\n"+
				            "     z#=any URL</pre>");
                printFooter(req,out);
                out.println("</div>");//content
				printMenu(req,out);
				out.println("</body></html>");
				out.close();
				return;
			}
		} catch ( IllegalArgumentException e) {
		    htmlHeaders(resp);
    		noCacheHeaders(resp);
    		PrintWriter out = resp.getWriter();
    		xhtmlHeaders(out);
    		out.println("<head>");
    		jsCssImport(req, out);
    		out.println("<title>Playlist Manager</title></head>");
    		out.println("<body>");
    		printTitle(out,"Error", SageServlet.isTitleBroken(req));
		    out.println("<div id=\"content\">");
			out.println("<h3>"+e.getMessage()+"</h3>");
			out.println("<pre>"+e.toString());
			e.printStackTrace(out);
			out.println("</pre></div>");
			printMenu(req,out);
			out.println("</body></html>");
			out.close();
			return;
		}
    }
    private void ExportPlaylist(HttpServletRequest req, LinkedList<PlaylistItemInfo> playListItems, Object playlist,int fntype, int iterations) throws Exception {
        if ( playlist == null || !SageApi.booleanApi("IsPlaylistObject",new Object[]{playlist})) {
            log("ExportAlbum: Not an  Playlist "+playlist);
            return;
        }
        
        Object items=SageApi.Api("GetPlaylistItems",playlist);
        int numitems=SageApi.Size(items);
        for ( int i=0;i<numitems;i++) {
            Object item=SageApi.GetElement(items,i);
            String itemType=SageApi.StringApi("GetPlaylistItemTypeAt",new Object[] {playlist, new Integer(i)});
            if ( itemType.equalsIgnoreCase("Playlist")){
                iterations++;
                // avoid endless loop
                if ( iterations > 100) {
                    throw new IllegalArgumentException("Cannot export playlist: refers to itself"); 
                }
                ExportPlaylist(req,playListItems,item,fntype, iterations);
            } else if ( itemType.equalsIgnoreCase( "Album" )){
                ExportAlbum(req,playListItems,item,fntype);
            } else if ( itemType.equalsIgnoreCase( "Airing")){
                ExportAiring(req,playListItems,item,fntype);
            } else {
                log("Unknown playlist item type at "+i+": "+itemType);
            }
        }
        
    }
    private void ExportAlbum(HttpServletRequest req, LinkedList<PlaylistItemInfo> playListItems, Object album, int fntype) throws Exception {
        if ( album == null || !SageApi.booleanApi("IsAlbumObject",new Object[]{album})){
            log("ExportAlbum: Not an  Album "+album);
            return;
        }
        Object[] tracks=(Object[])SageApi.Api("GetAlbumTracks",album);
        for ( int i=0;i<tracks.length;i++)
            ExportAiring(req,playListItems,tracks[i],fntype);
        
    }
    private void ExportAiring(HttpServletRequest req, LinkedList<PlaylistItemInfo> playListItems, Object airing, int fntype) throws Exception {
        
        if ( airing == null ) {
            log("ExportAiring: Not a  Airing "+airing);
            return;
    
        } else if ( SageApi.booleanApi("IsMediaFileObject",new Object[]{airing}))
            ExportMediaFile(req,playListItems,airing,fntype);
        else if ( SageApi.booleanApi("IsAiringObject",new Object[]{airing})) {
            // try to get mediafile
            Object mf=SageApi.Api("GetMediaFileForAiring",airing);
            if ( mf==null) {
                log("ExportAiring: No mediafile for airing: "+airing);
                return;
            }
            ExportMediaFile(req,playListItems,mf,fntype);
        } else {
            log("ExportAiring: Unknown object type passed"+airing);
        }
    }
    private void ExportMediaFile(HttpServletRequest req, LinkedList<PlaylistItemInfo> playListItems, Object mediafile, int fntype) throws Exception {
        if ( mediafile == null || !SageApi.booleanApi("IsMediaFileObject",new Object[]{mediafile})) {
            log("ExportMediaFile: Not a  mediafile "+mediafile);
            return;
        }
        String transcodeOpts =req.getParameter("TranscodeOpts");  
        if (transcodeOpts != null)
        {
            // Android's browser escapes the TranscodeOpts before submitting the URL and causes getParameter to only return mode=vlc
            // replace & and = with special sequences to work around the issue
            transcodeOpts = transcodeOpts.replace("_eq_", "=").replace("_amp_", "&");
            transcodeOpts = java.net.URLDecoder.decode(transcodeOpts, charset);
        }
        
        boolean isdvd=SageApi.booleanApi("IsDVD",new Object[]{mediafile});    
        Object segments=SageApi.Api("GetSegmentFiles",mediafile);
        int numsegments=SageApi.Size(segments);
        File files[]=(File[])SageApi.Api("GetSegmentFiles",mediafile);
        for ( int seg=0;seg<numsegments;seg++){
            Integer segment=new Integer(seg);
            PlaylistItemInfo item=new PlaylistItemInfo();
            item.length=((Long)SageApi.Api("GetDurationForSegment",new Object[]{mediafile,segment})).longValue()/1000;
            if ( numsegments > 1 )
                item.title=SageApi.StringApi("PrintAiringShort",new Object[]{mediafile})+" - part "+seg;
            else 
                item.title=SageApi.StringApi("PrintAiringShort",new Object[]{mediafile});
            if ( fntype==FN_PATH) {
                item.path=files[seg].getAbsolutePath();
                if ( isdvd )
                    // dvd's need path to IFO file.
                    item.path=new File(files[seg],"VIDEO_TS.IFO").getAbsolutePath();
                else
                    item.path=files[seg].getAbsolutePath();
            } else if ( fntype==FN_URL ) {
                int mediaFileID=SageApi.IntApi("GetMediaFileID",new Object[]{mediafile});
                if (transcodeOpts!=null) {
                    if ( isdvd) {
                        item.path=req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort() + getPublicPath(req) + "/MediaFileTranscode?MediaFileId="+mediaFileID+
                        "&"+transcodeOpts;
                    }
                    else
                    {
                        item.path=req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort() + getPublicPath(req) + "/MediaFileTranscode?MediaFileId="+mediaFileID+"&Segment="+seg+
                            "&"+transcodeOpts;
                    }
                } else {
                    item.path=req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort() + getPublicPath(req) + "/MediaFile?MediaFileId="+mediaFileID+"&Segment="+seg;
                }
            }
            playListItems.addLast(item);
        } 
    }
    void WritePlaylist(HttpServletRequest req, HttpServletResponse resp, LinkedList<PlaylistItemInfo> PlayListItems, int pltype, String playlistName) throws Exception {
        String filename;
        if ( pltype==PL_M3U) {
            filename=playlistName+".m3u";
        }
        else if ( pltype==PL_WVX){
            filename=playlistName+".wvx";
        }
        else if ( pltype==PL_PLS){
            filename=playlistName+".pls";
        } else {
            throw new Exception("invalid playlist format "+pltype);
        }
       
        resp.setContentType(getServletContext().getMimeType( filename));
        resp.addHeader("Content-Disposition","attachment; filename=" +filename);
        resp.setBufferSize(8192);
        PrintWriter out = getGzippedWriter(req,resp);
        // must catch and report all errors within Gzipped Writer
        try {
            
            // write headers
            if ( pltype==PL_M3U) {
                out.println("#EXTM3U");
            } else if ( pltype==PL_WVX) {
                out.println("<asx Version = \"3.0\">");
                out.println("<title>Downloaded Playlist</title>");
            }  else if ( pltype==PL_PLS) {
                out.println("[playlist]");
            }
            
            // write items
            int itemnum=0;
            for ( Iterator<PlaylistItemInfo> i =PlayListItems.iterator();
            i.hasNext();) {
                PlaylistItemInfo item=(PlaylistItemInfo)i.next();
                if ( item != null ){
                    // write item
                    if ( pltype==PL_M3U) {
                        // #EXTINF:duration,name
                        // path
                        if ( item.title!=null )
                            out.println("#EXTINF:"+item.length+","+item.title);
                        else
                            out.println("#EXTINF:"+item.length+",");
                        out.println(item.path);
                    }                    
                    else if ( pltype==PL_WVX) {
                        out.println("<entry>");
                        if ( item.title!=null )
                            out.println("<title>"+item.title.replace('<', '(').replace('>', ')')+"</Title>");
                        out.println("<ref href=\""+item.path+"\"/>");
                        out.println("</entry>");
                    }
                    else if ( pltype==PL_PLS) {
                        out.println("File"+(itemnum+1)+"="+item.path);
                        if ( item.title==null )
                            out.println("Title"+(itemnum+1)+"=");
                        else
                            out.println("Title"+(itemnum+1)+"="+item.title);
                        out.println("Length"+itemnum+"="+item.length);
                    }
                    itemnum=itemnum+1;
                }
            }
            
            // write footers
            if ( pltype==PL_M3U)
                ;
            else if ( pltype==PL_WVX){
                out.println("</Asx>");
            } else if ( pltype==PL_PLS) {
                out.println("NumberOfEntries="+itemnum);
                out.println("Version=2");
            }
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
