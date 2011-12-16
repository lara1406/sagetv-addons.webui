
package net.sf.sageplugins.webserver;


import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Acme.Serve.Serve;

import net.sf.sageplugins.sageutils.SageApi;
import net.sf.sageplugins.sageutils.Translate;
import net.sf.sageplugins.sagexmlinfo.SageXmlWriter;

/**
 * Core sage servlet with helper functions
 */
public abstract class SageServlet extends HttpServlet {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static double SAGE_MAJOR_VERSION;
    static {
        try {
            // version=SageTV V6.2.1.141
            String versionString=SageApi.GetProperty("version", "");
            versionString=versionString.replaceAll(".*V([1-9]+)\\.([0-9]+).*","$1.$2");
            SAGE_MAJOR_VERSION=Double.parseDouble(versionString);
        }catch (Exception e) {
        	Acme.Serve.Serve.extLog("Error getting SAGE_MAJOR_VERSION : "+e.toString());
            SAGE_MAJOR_VERSION=6.1;
        }
    }
    
    public void log(String arg0) {
        if ( !arg0.equals("init") && !arg0.equals("destroy"))
            super.log(arg0);
    }
    static String[][] ENABLE_DISABLE_OPTS=new String[][]{{"true","Enabled"},{"false","Disabled"}};
    static String[][] FILTER_OPTS=new String[][]{
        {"##AsSageTV##","Same as SageTV"},
        {"##BLANK##","All"},
        {"IsFavorite|IsManualRecord","Manual Records & Favorites"},
        {"IsManualRecord","Manual Records"},
        {"IsFavorite","Favorites"},
    };
    
    static boolean init=false;
    protected static String charset=null;    
    protected SageServlet(){
        if ( ! init) {
            init=true;
            try { 
                charset=SageApi.GetProperty("nielm/webserver/charset","UTF-8");
            } catch (InvocationTargetException e){
                Serve.extLog("Error getting charset:"+ e.toString());
                Serve.extLog("caused:"+ e.getCause().toString());
                e.printStackTrace(System.out);
                charset="UTF-8";
            }
        }
        
    }
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        req.setCharacterEncoding(charset);
        try {
            doServletGet(req,resp);
        } catch (Throwable e) {
            log("Exception while processing servlet",e);
            System.out.println(e.toString());
            e.printStackTrace();
            
            if (!resp.isCommitted()){
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("text/html");
            }
            PrintWriter out = resp.getWriter();
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
    
    protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1)
    throws ServletException, IOException {
        doGet(arg0, arg1);
    }
    
    
    protected abstract void doServletGet(HttpServletRequest req, HttpServletResponse resp)
    throws Exception;
    
    protected void htmlHeaders(HttpServletResponse resp){
        resp.setContentType("text/html; charset="+charset);
        resp.setBufferSize(8192);
    }
    protected void noCacheHeaders(HttpServletResponse resp){
        resp.setHeader("Cache-Control","no-cache"); //HTTP 1.1
        resp.setHeader("Pragma","no-cache"); //HTTP 1.0
        resp.setDateHeader ("Expires", 0); //prevents caching at the proxy server
    }
    protected void xhtmlHeaders(PrintWriter out) throws IOException{
        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\" dir=\"ltr\">");
    }
    protected void jsCssImport(HttpServletRequest req, PrintWriter out) throws IOException{
        out.println(" <link rel=\"stylesheet\"  type=\"text/css\" media=\"all\" href=\"sage_all.css\"/>");
        out.println(" <link rel=\"stylesheet\"  type=\"text/css\" media=\"print\" href=\"sage_print.css\"/>");
        out.println(" <link rel=\"stylesheet\"  type=\"text/css\" media=\"handheld\" href=\"sage_handheld.css\"/>");
        String customcss=GetOption(req, "custom_css", "").trim();
        if ( customcss.length()>0)
        	out.println(" <link rel=\"stylesheet\"  type=\"text/css\" media=\"all\" href=\""+customcss+"\"/>");

        out.println(" <link rel=\"Shortcut Icon\" href=\"" + req.getContextPath() + "/favicon.ico\" type=\"image/x-icon\"/>");
        out.println(" <script type=\"text/javascript\" src=\"sage.js\"></script>");
    }
    
    static protected void printMenu(PrintWriter out){
        out.println(
                "<!-- start menu bar -->\r\n" +
                "<script language=\"JavaScript\" type=\"text/javascript\">\r\n" +
                "<!--//\r\n" +
                "var MENU_ITEMS = null;\r\n" +
                "//-->\r\n" +
                "</script>\r\n" +
                "<script language=\"JavaScript\" type=\"text/javascript\" src=\"menu_core.js\"></script>\r\n" +
                "<script language=\"JavaScript\" type=\"text/javascript\" src=\"menu_items.js\"></script>\r\n" +
                "<script language=\"JavaScript\" type=\"text/javascript\" src=\"menu_style.js\"></script>\r\n" +
                "<script language=\"JavaScript\" type=\"text/javascript\">\r\n" +
                "<!--//\r\n" +
                "if ( MENU_ITEMS==null ) { \r\n" +
                "   alert(\"Error in menu_items.js - check syntax\");\r\n" +
                "} else {\r\n"+
                "  doMenu(MENU_ITEMS, MENU_POS);\r\n" +
                "}\r\n" +
                "//-->\r\n" +
        "</script>");
    }

    /**
     * @deprecated use printMenu(PrintWriter out)
     */
    protected void printMenu(HttpServletRequest req, PrintWriter out) {
        printMenu(out);
    }
    static protected void printFooter(HttpServletRequest req, PrintWriter out){
        out.println("<hr/>");
        out.println("<p>Page generated at: "+ DateFormat.getDateTimeInstance().format(new Date()));
        out.print("<a href=\""+req.getRequestURI());
        if ( req.getQueryString()!=null)
            out.print("?"+req.getQueryString().replaceAll("&","&amp;"));
        out.println("\">[Refresh]</a>");
        out.println(
                "       <br/>Sage Webserver version "+Version.VERSION+"\r\n" +
                "       <br/><a href=\"http://validator.w3.org/check?uri=referer\"><img  src=\"valid-xhtml10.gif\"  alt=\"Valid XHTML 1.0!\" height=\"31\" width=\"88\" /></a>\r\n"+
                "       <a href=\"http://jigsaw.w3.org/css-validator/\"><img  src=\"valid-css.gif\"  alt=\"Valid CSS2!\" height=\"31\" width=\"88\" /></a>\r\n"+
                "</p>");
    }
    static protected void printTitle(PrintWriter out, String screenname) {
        out.println("<div id=\"title\">");
        out.println("<h1><a href=\"index.html\" title=\"home\"><img id=\"logoimg\" src=\"sagelogo.gif\" alt=\"SageTV logo\" title=\"Home Screen\" border=\"0\"/></a>"+screenname+"</h1>");
        out.println("</div>");
        
    }
    static protected void printTitleWithXml(PrintWriter out, String screenname,HttpServletRequest req) {
        out.println("<div id=\"title\">"+
                    "<h1><a href=\"index.html\" title=\"home\"><img id=\"logoimg\" src=\"sagelogo.gif\" alt=\"SageTV logo\" title=\"Home Screen\" border=\"0\"/></a>"+screenname+"\r\n"+
                    "<a href=\""+GetXmlUrl(req)+"\" title=\"Return page in XML\"><img src=\"xml_button.png\" alt=\"[XML]\"/></a>\r\n" +
                    "</h1></div>");
    }
    protected Object checkManualRecordConflicts(long startmillis, long stopmillis, Object channel)
    throws Exception{
        Object rv=null;
        
        Object inputs = SageApi.Api("GetConfiguredCaptureDeviceInputs");
        //for each input
        for (int inputnum=0;inputnum<SageApi.Size(inputs);inputnum++) {
            Object input=SageApi.GetElement(inputs,inputnum);
            Object lineup=SageApi.Api("GetLineupForCaptureDeviceInput",new Object[]{input});
            if ( SageApi.booleanApi("IsChannelViewableOnLineup",new Object[]{channel, lineup}) ){
                Object capdev=SageApi.Api("GetCaptureDeviceForInput",new Object[]{input});
                Object overlaps = SageApi.Api("GetScheduledRecordingsForDeviceForTime",new Object[]{
                        capdev,new Long(startmillis),new Long(stopmillis)});
                overlaps = SageApi.Api("FilterByBoolMethod",new Object[]{overlaps,"IsManualRecord", Boolean.TRUE});
                if ( SageApi.Size(overlaps)==0)
                    return null;
                rv=SageApi.Api("DataUnion",new Object[]{rv,overlaps});
            }
        }
        //System.out.println("returning "+SageApi.Size(rv)+" overlaps");
        return rv;
    }
    
    public static String GetCookieValue(Cookie[] cookies,
            String cookieName,
            String defaultValue) {
        if (cookies == null) {
            return defaultValue;
        }
        for(int i=0; i<cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookieName.equals(cookie.getName())){
                String value="";
                try {
                    value=URLDecoder.decode(cookie.getValue(),charset);
                } catch ( UnsupportedEncodingException e) {}
                if ( value.equals("##BLANK##"))
                    value="";
                return(value);
            }
        }
        return(defaultValue);
    }
    
    static public String GetOption(HttpServletRequest req,String opt, String defarg)
    {
        Cookie[] cookies=req.getCookies();
        return GetCookieValue(cookies,opt,defarg);
    }
    /***
     * 
     * @param req
     * @param optionset
     * @param optionname
     * @param prop
     * @param defarg -- default value (includes ##AsSageTV##)
     * @param fallbackdef -- default value for GetProperties
     * @return
     */
    public String GetOptionOrProfile(HttpServletRequest req,
            String[][] optionset,
            String optionname, 
            String propertyname, 
            String defarg,
            String Sagedefarg)
    throws Exception {
        Cookie[] cookies=req.getCookies();
        Cookie cookie=null;
        if (cookies != null) {
            for(int i=0; i<cookies.length; i++) {
                if (cookies[i].getName().equals(optionname)) {
                    cookie=cookies[i];
                    break;
                }
            }
        }
        String value=null;
        if ( cookie != null) {
            value=URLDecoder.decode(cookie.getValue(),charset);
            if ( value.equals("##AsSageTV##") )
                value=null;
        }
        
        if ( value == null && propertyname!=null){
            try {
                value=SageApi.GetProperty(propertyname,Sagedefarg);
                // translate "" -> ##BLANK## as cookies cannot be blank
                if ( value != null && value.length()==0)
                    value="##BLANK##";
            } catch (InvocationTargetException e){}
        }
        
        if ( value != null) {
            // got a value -- verify it
            for ( int i=0; i<optionset.length;i++){
                if ( optionset[i][0].equals(value)) {
                    if ( value.equals("##BLANK##"))
                        return "";
                    else
                        return value;
                }
            }
            // if got to here, then invalid value found
            log("invalid value: '"+value+"' found for option: "+optionname+" using defaults");
            value=null;
        }
        
        // if got to here, no value or invalid value used
        if ( defarg.equals("##AsSageTV##") && Sagedefarg != null )
            return Sagedefarg;
        else
            return defarg;
    }
    
    public void PrintOptionsDropdown(HttpServletRequest req,PrintWriter out,String opt,String defval,String[][]options)
    throws Exception {
        PrintOptionsDropdown(req,out,opt,null,defval,options);
    }
    public void PrintOptionsDropdown(HttpServletRequest req,PrintWriter out,String opt, String prop, String defval, String[][]options)
    throws Exception {
        String currval=GetOption(req,opt,defval);
        out.println("  <select name='"+opt+"' onchange='SetCookie(\""+opt+"\",this.options[this.selectedIndex].value);window.location.reload(true);'>");
        printOptionsList(out,options,currval);
        out.println("  </select>");
    }
    public static void printOptionsList(PrintWriter out,String[][] options, String currval){
        if ( currval != null && currval.length()==0)
            currval="##BLANK##";
        
        for ( int i=0; i<options.length;i++){
            out.print("    <option value='"+options[i][0]+"'");
            if ( currval!=null && options[i][0].equals(currval))
                out.print(" selected=\"selected\"");
            out.println(">"+Translate.encode(options[i][1])+"</option>");
        }
    }
    
    // handle Gzip encoding of output streams
    // equivalent to Apache mod_gzip, except the servlet decides when to do it.
    // Make sure all exceptions are caught and handled before OS is closed
    protected OutputStream getGzippedOutputStream(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        try {
            if ( ! SageApi.GetBooleanProperty("nielm/webserver/enable_gzip_encoding",true))
                return resp.getOutputStream();
        }catch ( InvocationTargetException e){
            return resp.getOutputStream();
        }
        String encoding = req.getHeader("Accept-Encoding");
        if ( encoding != null && encoding.toLowerCase().indexOf("gzip") > -1) {
            resp.setHeader("Content-Encoding", "gzip");
            return new GZIPOutputStream(resp.getOutputStream());
        } else {
            return resp.getOutputStream();
        }
    }
    // handle Gzip encoding of output writer
    // equivalent to Apache mod_gzip, except the servlet decides when to do it.
    // Make sure all exceptions are caught and handled before OS is closed
    protected PrintWriter getGzippedWriter(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        String encoding = req.getCharacterEncoding();
        if (encoding != null)
            return new PrintWriter(new OutputStreamWriter(getGzippedOutputStream(req,resp), encoding));
        else
            return new PrintWriter(getGzippedOutputStream(req,resp));
    }
    protected String[] GetUIContextNames(){
        // support backward compatibility for Sage 2.2
        String[] UiContexts=new String[] {};
        try {
            // 4.1
            UiContexts=(String[])SageApi.Api("GetUIContextNames");
        } catch (InvocationTargetException e){
            // 4.1 API failed, try 2.2 API
            try {
                Object[] widgets=(Object[])SageApi.Api("GetAllWidgets");
                if ( SageApi.Size(widgets) > 0){
                    UiContexts=new String[]{"SAGETV_PROCESS_LOCAL_UI"};
                }
            } catch ( InvocationTargetException e2){ 
                // 2.2 API failed - no UI contexts
            }
        }
        return UiContexts;
    }
    protected void SendXmlResult(HttpServletRequest req, HttpServletResponse resp, Object shows, String filename) throws IOException{
        resp.setContentType("text/xml; charset="+charset);
        noCacheHeaders(resp);
        if ( filename !=null)
            resp.setHeader("Content-Disposition","attachment; filename="+filename);
        resp.setBufferSize(8192);
        OutputStream outs=getGzippedOutputStream(req,resp);
        try {
            SageXmlWriter xmlWriter=new SageXmlWriter();
            xmlWriter.add(shows);
            try {
                xmlWriter.write(outs,charset);
            } catch ( SocketException e) {}
            outs.close();
        } catch (Throwable e) {
            log("Exception while processing servlet "+this.getClass().getName() ,e);
            System.out.println(e.toString());
            e.printStackTrace();
            if (!resp.isCommitted()){
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("text/html");
                PrintWriter out = new PrintWriter(outs);
                out.println();
                out.println();
                out.println("<body><pre>");
                out.println("Exception while processing servlet:\r\n"+e.toString());
                e.printStackTrace(out);
                out.println("</pre>");
                out.close();
                outs.close();
            }
        }
    }
    static protected String GetXmlUrl(HttpServletRequest req) {
        StringBuffer xmlurl=new StringBuffer();
        xmlurl.append(req.getContextPath() + req.getServletPath());
        xmlurl.append('?');
        if ( req.getQueryString() != null) {
            xmlurl.append(CleanSearchUrl(req));
            xmlurl.append("&");
        }
        xmlurl.append("xml=yes");
        return xmlurl.toString();
    }
    static protected String GetRssUrl(HttpServletRequest req, String servlet) {
        StringBuffer rssurl=new StringBuffer();
        rssurl.append(getPublicPath(req) + "/Rss/" + servlet);
        if ( req.getQueryString() != null){
            rssurl.append('?');
            rssurl.append(CleanSearchUrl(req));
        }
        return rssurl.toString();
    }
    static protected String getPublicPath(HttpServletRequest req) {
        StringBuffer publicurl=new StringBuffer();

        if ((req.getContextPath() == null) || (req.getContextPath().trim().length() == 0)) {
            // nielm's Acme web server
            publicurl.append("/sagepublic");
        } else {
            // Jetty web server
            publicurl.append(req.getContextPath() + "/public");
        }
        
        return publicurl.toString();
    }
    static private String CleanSearchUrl(HttpServletRequest req) {
        Enumeration<?> paramNames = req.getParameterNames();
        StringBuilder sb = new StringBuilder();

        while (paramNames.hasMoreElements())
        {
            String name = (String) paramNames.nextElement();
            String[] values = req.getParameterValues(name);
            for (String value : values)
            {
                if ((value != null) &&
                    (!value.toLowerCase().equals("any")) &&
                    (!value.toLowerCase().equals("**any**")) &&
                    (!value.toLowerCase().equals("none")))
                {
                    sb.append(name + "=" + value + "&");
                }
            }
        }

        String url = sb.toString();
        if (url.endsWith("&"))
        {
            url = url.substring(0, url.length() - "&".length());
        }
        return url;
    }
}
