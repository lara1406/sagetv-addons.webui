package net.sf.sageplugins.webserver;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import net.sf.sageplugins.sageutils.SageApi;
import net.sf.sageplugins.sageutils.Translate;

/// utility class for displaying lists of Airings
public class AiringList {
    static final int DEF_NUM_ITEMS=100;
    
    static final String[][] NUM_ITEMS_PER_PAGE_OPTS=new String[][]{
        {"10","10"},
        {"25","25"},
        {"50","50"},
        {"100","100"},
        {"200","200"},
        {"500","500"},
        {"inf","All"},
    };
    
    static void PrintPageRange(PrintWriter out, HttpServletRequest req, int pagenum, int numpages){

        if ( numpages == 1)
            return;
        
        String URL_nopage;
        if ( req.getQueryString()!=null && req.getQueryString().trim().length()>0)
            URL_nopage=req.getRequestURI()+"?"+req.getQueryString().replaceAll("&page=[^&]*","");
        else 
            URL_nopage=req.getRequestURI()+"?";
        URL_nopage=Translate.encode(URL_nopage);
        
        //  print page range...
        out.println("<div class=\"pagenav\" align=\"right\"><table cellspacing=\"0\" class=\"pagerange\"><tr>");
        
        out.println("<td class=\"pageinfo\">Page "+pagenum+" of "+numpages+"</td>");
        // <<First and <Prev page indicators.
        if ( pagenum>3 )
            // first
            out.println("<td><a title=\"First Page\"href=\""+URL_nopage+"&amp;page=1\"><strong>&laquo;</strong> First</a></td>");
        if (pagenum >1 )
            // prev
            out.println("<td><a title=\"Previous Page\" href=\""+URL_nopage+"&amp;page="+(pagenum-1)+"\">&lt;</a></td>");

        // print 2 page numbers either side of current
        for ( int page=java.lang.Math.max(1, pagenum-2);
              page<=java.lang.Math.min(numpages, pagenum+2);
              page++) {
            if ( page==pagenum )
            out.println("<td class=\"currpage\">"+page+"</td>");
            else
                out.println("<td><a title=\"Page "+page+"\" href=\""+URL_nopage+"&amp;page="+(page)+"\">"+page+"</a></td>");
        }        
        // Next>
        if ( pagenum<numpages )
            out.println("<td><a title=\"Next Page\" href=\""+URL_nopage+"&amp;page="+(pagenum+1)+"\">&gt;</a></td>");
        //Last>> page indicators.
        if ( pagenum<(numpages-2) )
            // Last
            out.println("<td><a title=\"Last Page\"href=\""+URL_nopage+"&amp;page="+numpages+"\">Last<strong>&raquo;</strong></a></td>");
        out.println("</tr></table></div>");
    }

    static void PrintPagedAiringList(
            SageServlet servlet,
            PrintWriter out,
            Object airings,
            HttpServletRequest req,
            boolean withOptions,
            boolean withCheckbox,
            boolean showFileSize,
            Object RecSchedList,
            Object allConflicts,
            Object unresolvedConflicts,
            int numItemsPerPageOverride // overrides cookie value
            )throws Exception {
        Object[] airingsArray;
        if (airings instanceof Object[]){
            airingsArray=((Object[])airings);
        } else if (airings instanceof List){
            airingsArray=((List<?>)airings).toArray();
        } else {
            throw new IllegalArgumentException("airings is not an array or list");
        }
        int itemsperpage=numItemsPerPageOverride;
        if ( itemsperpage <=0) {
            // not specified, get itemsperpage from cookie
            itemsperpage=DEF_NUM_ITEMS;
            try {
                String itemsPerPageStr=SageServlet.GetOption(req, "pagelen", Integer.toString(DEF_NUM_ITEMS)).trim();
                if ( itemsPerPageStr.length()>0)
                    itemsperpage=Integer.parseInt(itemsPerPageStr);
            } catch ( NumberFormatException e) {}
            catch (NullPointerException e){}
        }

        // calc num pages
        int numpages=(airingsArray.length/itemsperpage)+1;
                
        // get pagenum
        int pagenum=1;
        String pagenumStr=req.getParameter("page");
        try {
            pagenum=Integer.parseInt(pagenumStr);
        }
        catch (NullPointerException e){} // ignore parameter not found
        catch (NumberFormatException e) {} // ignore invalid page num
        
        if ( pagenum > numpages )
            pagenum=numpages;

        out.println("<div class=\"airings\"  id=\"airingsList\">");
        if ( withCheckbox || withOptions ){
            out.println("<div class=\"exphideall\">");
            if ( withCheckbox ){
                out.println("<a href=\"javascript:checkAll(true)\">[Select all]</a>\r\n" +
                            "<a href=\"javascript:checkAll(false)\">[Unselect all]</a>");
            }
            if ( withOptions )
                out.println("<a onclick=\"javascript:showOptions()\" href=\"#options\">[Show Options]</a>\r\n");
            out.println("</div>");
        }
        boolean usechannellogos=SageServlet.GetOption(req,"UseChannelLogos","true").equalsIgnoreCase("true");
        boolean showMarkers=SageServlet.GetOption(req,"ShowMarkers","true").equalsIgnoreCase("true");
        boolean showRatings=SageServlet.GetOption(req,"ShowRatings","true").equalsIgnoreCase("true");
        boolean showEpisodeID=SageServlet.GetOption(req,"ShowEpisodeID","false").equalsIgnoreCase("true");

        PrintPageRange(out, req, pagenum, numpages);
        for (int i=(pagenum-1)*itemsperpage; 
                 ( i<(pagenum)*itemsperpage 
                 && i<airingsArray.length );
                 i++){
            Airing airing=new Airing(airingsArray[i]);
            airing.printAiringTableCell(req,out,withCheckbox,usechannellogos,showMarkers,showRatings,showEpisodeID,showFileSize,RecSchedList,allConflicts,unresolvedConflicts);
        }
        PrintPageRange(out, req, pagenum, numpages);
        if ( withCheckbox || withOptions ){
            out.println("<div class=\"exphideall\">");
            if ( withCheckbox ){
                out.println("<a href=\"javascript:checkAll(true)\">[Select all]</a>\r\n" +
                            "<a href=\"javascript:checkAll(false)\">[Unselect all]</a>");
            }
            if ( withOptions )
                out.println("<a onclick=\"javascript:showOptions()\" href=\"#options\">[Show Options]</a>\r\n");
            out.println("</div>");
        }
        out.println("</div>");
    }
    

    static void PrintPagedGroupedAiringList(
            SageServlet servlet,
            PrintWriter out,
            Object airings,
            HttpServletRequest req,
            String groupMethod,
            boolean withCheckbox,
            boolean withOptions,
            boolean showFileSize,
            Object RecSchedList,
            Object allConflicts,
            Object unresolvedConflicts,
            int numItemsPerPageOverride
    ) 
    throws Exception {
        Object[] airingsArray;
        if (airings instanceof Object[]){
            airingsArray=((Object[])airings);
        } else if (airings instanceof List){
            airingsArray=((List<?>)airings).toArray();
        } else {
            throw new IllegalArgumentException("airings is not an array or list");
        }
        
        if ( groupMethod == null 
                || groupMethod.trim().length()==0 
                || groupMethod.equalsIgnoreCase("None")) {
            PrintPagedAiringList(servlet, out, airings, req, withOptions, withCheckbox, showFileSize, RecSchedList, allConflicts, unresolvedConflicts, numItemsPerPageOverride);
            return;
        }
        int itemsperpage=numItemsPerPageOverride;
        if ( itemsperpage <=0) {
            // not specified, get itemsperpage from cookie
            itemsperpage=DEF_NUM_ITEMS;
            try {
                String itemsPerPageStr=SageServlet.GetOption(req, "pagelen", Integer.toString(DEF_NUM_ITEMS)).trim();
                if ( itemsPerPageStr.length()>0)
                	if (itemsPerPageStr.equalsIgnoreCase("inf"))
                		itemsperpage=Integer.MAX_VALUE;
                	else
                		itemsperpage=Integer.parseInt(itemsPerPageStr);
            } catch ( NumberFormatException e) {
            	Acme.Serve.Serve.extLog("invalide pagelen "+e.toString());
            }
            catch (NullPointerException e){}
        }

        // calc num pages
        int numpages=(airingsArray.length/itemsperpage)+1;
                
        // get pagenum
        int pagenum=1;
        String pagenumStr=req.getParameter("page");
        try {
            pagenum=Integer.parseInt(pagenumStr);
        }
        catch (NullPointerException e){} // ignore parameter not found
        catch (NumberFormatException e) {} // ignore invalid page num
        
        if ( pagenum > numpages )
            pagenum=numpages;

        // get an array subset for the items to be displayed
        if (numpages > 1) {
            int itemsOnPage=itemsperpage;
            if ( pagenum == numpages){
                itemsOnPage=airingsArray.length - ((numpages-1)*itemsperpage);
            }
            Object[] displayedItems=new Object[itemsOnPage];
            System.arraycopy(airingsArray, (pagenum-1)*itemsperpage, displayedItems, 0, itemsOnPage);
            airingsArray=displayedItems;
        }
            
        
        // do grouping
        java.util.Map<?,?> groupedAirings;
        try {
            groupedAirings = (Map<?, ?>)SageApi.Api("GroupByMethod", new Object[]{airingsArray,groupMethod});
        } catch (InvocationTargetException e) {
            servlet.log("Grouping on "+groupMethod+" failed: ",e);
            out.println("<script  type=\"text/javascript\">DeleteOptionsCookie(\"sagetv_recordings_grouping\");</script>");
            PrintPagedAiringList(servlet,out,airings,req,withOptions,withCheckbox,showFileSize,RecSchedList,allConflicts,unresolvedConflicts,numItemsPerPageOverride);
            return;
        }

        boolean usechannellogos=SageServlet.GetOption(req,"UseChannelLogos","true").equalsIgnoreCase("true");
        boolean showMarkers=SageServlet.GetOption(req,"ShowMarkers","true").equalsIgnoreCase("true");
        boolean showRatings=SageServlet.GetOption(req,"ShowRatings","true").equalsIgnoreCase("true");
        boolean showEpisodeID=SageServlet.GetOption(req,"ShowEpisodeID","false").equalsIgnoreCase("true");
        
        DecimalFormat fmt=new DecimalFormat("0.00");
        Set<?> keyset=groupedAirings.keySet();
        int groupnum=0;

        out.println("<div class=\"airings\" id=\"airingsList\">");
        out.println("<div class=\"exphideall\"><a href=\"javascript:expandAll("+keyset.size()+")\">[Expand all Folders]</a>\r\n"+
                    "<a href=\"javascript:hideAll("+keyset.size()+")\">[Hide all Folders]</a>\r\n");
        if ( withCheckbox ){
            out.println("<a href=\"javascript:checkAll(true)\">[Select all]</a>\r\n" +
                        "<a href=\"javascript:checkAll(false)\">[Unselect all]</a>");
        }
        if ( withOptions )
            out.println("<a onclick=\"javascript:showOptions()\" href=\"#options\">[Show Options]</a>\r\n");
        out.println("</div>");
        PrintPageRange(out, req, pagenum, numpages);
        for ( Iterator<?> i = keyset.iterator(); i.hasNext();){
            groupnum++;
            Object groupkey=i.next();
            Vector<?> group=(Vector<?>)(groupedAirings.get(groupkey));
            if ( group.size()==1) {
                Airing airing=new Airing(group.elementAt(0));
                airing.printAiringTableCell(req,out,withCheckbox,usechannellogos,showMarkers,showRatings,showEpisodeID,showFileSize,RecSchedList,allConflicts,unresolvedConflicts);
            } else {
                Airing air=new Airing(group.elementAt(0));
                String borderclassname=air.getBorderClassName();
                String bgclassname=air.getBgClassName(false);
                groupkey=Translate.encode((String)groupkey);

                out.println("<table width=\"100%\" class=\"epgcell\"><tr>\n"+
                        "   <td class=\"epgcellborder "+borderclassname+"\">\n"+
                        "       <table class=\""+bgclassname+"\" id=\"group"+groupnum+"_summ\"><tr>");
                if ( withCheckbox ){
                    out.println("      <td class='checkbox'><input type='checkbox'  onchange=\"javascript:setGroupChecked('group"+groupnum+"',this.checked)\"/></td>");
                }
                String sizeStr="";
                if ( showFileSize ){ 
                    // calculate group size
                    try {
                        long totalSize=0;
                        for (Iterator<?> it=group.iterator();it.hasNext();)
                        {
                            Object mf=it.next();
                            
                                Long size=(Long)SageApi.Api("GetSize",mf);
                                if ( size!=null)
                                    totalSize+=size.longValue();
                        }
                        if (totalSize > 100000000l)
                            sizeStr=fmt.format(totalSize/1000000000.0)+"GB";
                        else 
                            sizeStr=fmt.format(totalSize/1000000)+"MB";
                    } catch (Exception e ){}
                }
                out.println("      <td class=\"titlecell\" onclick=\"showDetail('group"+groupnum+"')\"><div class=\"\">\n"+
                        "       <a href=\"javascript:NullFunc()\">"+groupkey+" - "+group.size()+" shows "+sizeStr+"</a>\n"+
                        "       </div></td>\n");
                

               out.println(
                        "       <td class=\"channelcell\"><div class=\"\"><a href=\"javascript:NullFunc()\"><img class=\"folderimg\" src=\"folder.gif\" alt=\"folder\"/>\n"+
                        "       </a></div></td></tr></table>\n"+
                        "   <div class=\"Details\" id=\"group"+groupnum+"\">");
                for ( Iterator<?> j = group.iterator(); j.hasNext();){
                    Airing airing=new Airing(j.next());
                    airing.printAiringTableCell(req,out,withCheckbox,usechannellogos,showMarkers,showRatings,showEpisodeID,showFileSize,null,null,null);
                }
                out.println("</div></td></tr></table>");
                if ( keyset.size()>1)
                    out.print("<script type=\"text/javascript\">document.getElementById(\"group"+groupnum+"\").style.display='none';</script>");
                out.println("");


            }
        }
        PrintPageRange(out, req, pagenum, numpages);
        out.println("<div class=\"exphideall\"><a href=\"javascript:expandAll("+keyset.size()+")\">[Expand all Folders]</a>\r\n"+
                "<a href=\"javascript:hideAll("+keyset.size()+")\">[Hide all Folders]</a>\r\n");
        if ( withCheckbox ){
            out.println("<a href=\"javascript:checkAll(true)\">[Select all]</a>\r\n" +
            "<a href=\"javascript:checkAll(false)\">[Unselect all]</a>");
        }
        if ( withOptions )
            out.println("<a onclick=\"javascript:showOptions()\" href=\"#options\">[Show Options]</a>\r\n");
        out.println("</div>");
        out.println("</div>");//airingsList
    }
}