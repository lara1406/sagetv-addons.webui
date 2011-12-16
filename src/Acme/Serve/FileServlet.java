// FileServlet - servlet similar to a standard httpd
//
// Copyright (C)1996,1998 by Jef Poskanzer <jef@acme.com>. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// Visit the ACME Labs Java page for up-to-date versions of this and other
// fine Java utilities: http://www.acme.com/java/
// 
// All enhancements Copyright (C)1998-2004 by Dmitriy Rogatkin
// http://tjws.sourceforge.net

package Acme.Serve;

import java.io.*;
import java.util.*;
import java.text.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.net.SocketException;
import java.net.URLEncoder;

/// Servlet similar to a standard httpd.
// <P>
// Implements the "GET" and "HEAD" methods for files and directories.
// Handles index.html, index.htm, default.htm, default.html.
// Redirects directory URLs that lack a trailing /.
// Handles If-Modified-Since.
// <P>
// <A HREF="/resources/classes/Acme/Serve/FileServlet.java">Fetch the software.</A><BR>
// <A HREF="/resources/classes/Acme.tar.Z">Fetch the entire Acme package.</A>
// <P>
// @see Acme.Serve.Serve
@SuppressWarnings("unchecked")
public class FileServlet extends HttpServlet {
	
   /**
     * 
     */
    private static final long serialVersionUID = 8208738034880116752L;
    // We keep a single throttle table for all instances of the servlet.
	// Normally there is only one instance; the exception is subclasses.
	static Acme.WildcardDictionary throttleTab = null;
	static final String[] DEFAULTINDEXPAGES = {"index.html", "index.htm", "default.htm", "default.html"};
	static final DecimalFormat lengthftm = new DecimalFormat("#");
	protected String charSet = "UTF-8";//"iso-8859-1";
	
	private static final boolean logenabled =
		//		true;
		false;
	
	/// Constructor.
	public FileServlet()
	{
	}
	
	/// Constructor with throttling.
	// @param throttles filename containing throttle settings
	// @param charset used for displaying directory page
	// @see ThrottledOutputStream
	public FileServlet( String throttles, String charset ) throws IOException
	{
		this();
		if (charset != null)
			this.charSet = charset;
		readThrottles( throttles );
	}
	
	private void readThrottles( String throttles ) throws IOException
	{
		Acme.WildcardDictionary newThrottleTab =
			ThrottledOutputStream.parseThrottleFile( throttles );
		if ( throttleTab == null )
			throttleTab = newThrottleTab;
		else {
			// Merge the new one into the old one.
			Enumeration keys = newThrottleTab.keys();
			Enumeration elements = newThrottleTab.elements();
			while ( keys.hasMoreElements() ) {
				Object key = keys.nextElement();
				Object element = elements.nextElement();
				throttleTab.put( key, element );
			}
		}
	}
	
	/// Returns a string containing information about the author, version, and
	// copyright of the servlet.
	public String getServletInfo()
	{
		return "servlet similar to a standard httpd";
	}
	
	
	/// Services a single request from the client.
	// @param req the servlet request
	// @param req the servlet response
	// @exception ServletException when an exception has occurred
	public void service( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException
	{
		boolean headOnly;
		if ( req.getMethod().equalsIgnoreCase( "get" ) )
			headOnly = false;
		else if ( ! req.getMethod().equalsIgnoreCase( "head" ) )
			headOnly = true;
		else {
			res.sendError( HttpServletResponse.SC_NOT_IMPLEMENTED );
			return;
		}
		
		String path = req.getPathInfo();
		if (path != null && path.length() > 2) {
			path = path.replace( '\\', '/');
			if ( path.indexOf( "/../" ) >= 0 || path.endsWith( "/.." ) ) {
				res.sendError( HttpServletResponse.SC_FORBIDDEN );
				return;
			}
		}
		
		dispatchPathname( req, res, headOnly, path );
	}
	
	
	private void dispatchPathname( HttpServletRequest req, HttpServletResponse res, boolean headOnly, String path ) throws IOException
	{
		String filename = req.getPathTranslated()!=null?req.getPathTranslated().replace( '/', File.separatorChar ):"";
		if ( filename.length() > 0 && filename.charAt( filename.length() - 1 ) == File.separatorChar )
			filename = filename.substring( 0, filename.length() - 1 );
		File file = new File( filename );
		path = new String(path.getBytes("ISO-8859-1"), "UTF-8");
		log("showing >>"+filename+"<< for path >>"+path+"<<");
		if ( file.exists() ) {
			if ( ! file.isDirectory() )
				serveFile( req, res, headOnly, path, file );
			else {
				log("showing dir "+file);
				if ( path.charAt( path.length() - 1 ) != '/' )
					redirectDirectory( req, res, path, file );
				else 					
					showIdexFile(req, res, headOnly, path, filename);
			}
		} else {
			for (int i=0; i<DEFAULTINDEXPAGES.length; i++) {
				if ( filename.endsWith( File.separator+DEFAULTINDEXPAGES[i] ) ) {
					showIdexFile(req, res, headOnly, path, file.getParent());
					return;
				}
			}
			res.sendError( HttpServletResponse.SC_NOT_FOUND );
		}
	}
	
	private void showIdexFile(HttpServletRequest req, HttpServletResponse res,
			boolean headOnly, String path, String parent) throws IOException {
		log("showing index in directory "+parent);
		for (int i=0; i<DEFAULTINDEXPAGES.length; i++) {
			File indexFile = new File( parent, DEFAULTINDEXPAGES[i] );
			if ( indexFile.exists() ) {
				serveFile(req, res, headOnly, path, indexFile );
				return;
			}
		}
		// index not found
		serveDirectory(req, res, headOnly, path, new File(parent) );
	}
	
	private void serveFile( HttpServletRequest req, HttpServletResponse res, boolean headOnly, String path, File file ) throws IOException
	{
		
		if ( ! file.canRead()) {
			res.sendError( HttpServletResponse.SC_FORBIDDEN );
			return;
		}
		// test for Win32 devices such as "/path/con" or "/path/lptx" which
		// java reports as valid readable files!
		try {
			// On Win32 this fails if its a device
			file.getCanonicalPath();
		}
		catch (Exception e){ 
			res.sendError( HttpServletResponse.SC_NOT_FOUND );
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
	        long stop=file.length()-1;
	        String range=req.getHeader("Range");
		    if ( range != null && range.trim().startsWith("bytes=")){
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
		            if ( start > stop || start<0 || stop>=file.length()){
	                   res.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
	                    return;
	                }
			        res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		        }
		    } 
		    RandomAccessFile rfile=new RandomAccessFile(file,"r");
		    if ( start>0){
		        rfile.seek(start);
		    }
		    res.setHeader("Content-Range","bytes "+Long.toString(start)+"-"+Long.toString(stop)+"/"+Long.toString(file.length()));
		    //System.out.println("Content-Range bytes "+Long.toString(start)+"-"+Long.toString(stop)+"/"+Long.toString(file.length()));
		    res.setHeader("Content-Length", Long.toString(stop-start+1));
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
		        // copy stream stopping at 'stop' bytes.
		        long curlen=start;
		        byte[] buf=new byte[4096];
		        while ( curlen <= stop) {
		            long len=stop-curlen+1;
		            if ( len > 4096 )
		                len=4096;
		            len=rfile.read(buf,0,(int)len);
		            if ( len >0 ){
		                os.write(buf,0,(int)len);
		                curlen+=len;
		            }else {
		                // end of file -- stop
		                curlen=stop+1;
		            }   
		        }
		    } finally {
		        rfile.close();
		        os.close();
		    }
		}
	}
	
	/// Check If-Modified-Since header
	// returns True if header is older  than lastModified --
	// and file needs to be sent
	static public boolean CheckIfModifiedSince(HttpServletRequest req, long lastModified)
	throws IllegalArgumentException
	{
		long ifModSince=req.getDateHeader("If-Modified-Since");
		if ( ifModSince != -1) {
		    // headers are to nearest second, timestamps are 
		    // often of a higher resolution...
		    lastModified=(lastModified/1000)*1000;
		    if ( ifModSince >= lastModified ) {
		        return false;
		    }
		}
		return true;
	}
	
	/// Copy a file from in to out.
	// Sub-classes can override this in order to do filtering of some sort.
	public void copyStream( InputStream in, OutputStream out ) throws IOException
	{
	    try {
	        Acme.Utils.copyStream( in, out );
	    }catch ( SocketException e ) { /* assume socket has been closed -- ignore */}
	}
	
	
	private void serveDirectory( HttpServletRequest req, HttpServletResponse res, boolean headOnly, String path, File file ) throws IOException
	{
		log( "indexing " + file );
		if ( ! file.canRead() ) {
			res.sendError( HttpServletResponse.SC_FORBIDDEN );
			return;
		}
		res.setStatus( HttpServletResponse.SC_OK );
		res.setContentType( "text/html");
		// TODO: set charset for out stream
		OutputStream out = res.getOutputStream();
		if ( ! headOnly ) {
			PrintStream p = new PrintStream( new BufferedOutputStream( out ), false, charSet ); // 1.4
			p.println( "<HTML><HEAD>" );
			p.println( "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset="+charSet+"\">" );
			p.println( "<TITLE>Index of " + path + "</TITLE>" );
			p.println( "</HEAD><BODY BGCOLOR=\"#F1D0F2\">" );
			p.println( "<H2>Index of " + path + "</H2>" );
			p.println( "<PRE>" );
			p.println( "mode         bytes  last-changed  name" );
			p.println( "<HR/>" );
			String[] names = file.list();
			Acme.Utils.sortStrings( names );
			for ( int i = 0; i < names.length; ++i ) {
				File aFile = new File( file, names[i] );
				String aFileType;
				if ( aFile.isDirectory() )
					aFileType = "d";
				else if ( aFile.isFile() )
					aFileType = "-";
				else
					aFileType = "?";
				String aFileRead = ( aFile.canRead() ? "r" : "-" );
				String aFileWrite = ( aFile.canWrite() ? "w" : "-" );
				String aFileExe = "-";
				String aFileSize = lengthftm.format( aFile.length() );
				while (aFileSize.length() < 12)
					aFileSize = " "+aFileSize;
				String aFileDate =
					Acme.Utils.lsDateStr( new Date( aFile.lastModified() ) );
				while (aFileDate.length() < 14)
					aFileDate += " ";
				String aFileDirsuf = ( aFile.isDirectory() ? "/" : "" );
				String aFileSuf = ( aFile.isDirectory() ? "/" : "" );
				p.println(
						aFileType + aFileRead + aFileWrite + aFileExe +
						"  " + aFileSize + "  " + aFileDate + "  " +
						"<A HREF=\"" + URLEncoder.encode(names[i], charSet) /* 1.4  */+ aFileDirsuf + "\">" +
						names[i] + aFileSuf + "</A>" );
			}
			p.println( "</PRE>" );
			p.println( "<HR/>" );
			Serve.Identification.writeAddress( p );
			p.println( "</BODY></HTML>" );
			p.flush();
		}
		out.close();
	}
	
	
	private void redirectDirectory( HttpServletRequest req, HttpServletResponse res, String path, File file ) throws IOException
	{
		log( "redirecting " + path );
		res.sendRedirect( path + "/" );
	}
	
	public void log(String msg)
	{
		if (logenabled)
		super.log(msg);
	}
}
