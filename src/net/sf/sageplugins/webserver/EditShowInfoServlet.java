
package net.sf.sageplugins.webserver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
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
public class EditShowInfoServlet extends SageServlet {

    /** 
     * DEVELOPMENT use only -- allows editing of files from client instances 
     */
    private static final boolean OVERRIDE_CLIENT_CHECK=false;

    private static final String PARAM_EDIT_SHOW_CB="editShow";

    private static final String PARAM_SH_NAME="sh_ShowName";
    private static final String PARAM_SH_EPISODENAM="sh_EpisodeName";
    private static final String PARAM_SH_EPGID="sh_EpgId";
    private static final String PARAM_SH_GEN_EPGID_CB="sh_generateEpgId";
    private static final String PARAM_SH_FIRSTRUN="sh_FirstRun";
    private static final String PARAM_SH_CATEGORY="sh_Category";
    private static final String PARAM_SH_SUBCATEGORY="sh_SubCategory";
    private static final String PARAM_SH_DURATION="sh_Duration";
    private static final String PARAM_SH_DESC="sh_Desc";
    private static final String PARAM_SH_RATING="sh_Rating";
    private static final String PARAM_SH_ADVISORY="sh_Advisory";
    private static final String PARAM_SH_ROLES="sh_Roles";
    private static final String PARAM_SH_ORIGAIRDATE="sh_orig_air";
    private static final String PARAM_SH_YEAR="sh_Year";
    private static final String PARAM_SH_LANG="sh_Language";

    private static final String PARAM_EDIT_AIR_CB="editAiring";

    private static final String PARAM_AIR_STARTTIME="air_start";
    private static final String PARAM_AIR_CHANNELID="air_ChannelId";
    private static final String PARAM_AIR_DURATION="air_Duration";

    private static final String PARAM_AIR_HDTV="air_hdtv";
    private static final String PARAM_AIR_PARTNUM="air_partnum";
    private static final String PARAM_AIR_TOTALPART="air_totalparts";
    private static final String PARAM_AIR_RATING="air_rating";
    private static final String PARAM_AIR_STEREO="air_stereo";
    private static final String PARAM_AIR_CC="air_closedCaptioned";
    private static final String PARAM_AIR_SAP="air_sap";
    private static final String PARAM_AIR_SUBTITLE="air_subtitled";
    private static final String PARAM_AIR_PREM_FINALE="air_premFinale";



    private static final String PARAM_EDIT_FILE_CB="editFile";

    private static final String PARAM_FILE_STARTOFFSET="file_StartOffset";
    private static final String PARAM_FILE_OFFSETTYPE="file_OffsetType";




    /**
     * 
     */
    private static final long serialVersionUID = 2010468605042091757L;
    static final String[] Ratings=new String[]{
        "NR","AO","NC-17","R","PG-13","PG","G",
        "TVM","TVG","TV14","TVY7","TVPG","TVY"
    };
    static final String[] AdvisoryRatings=new String[]{
        "Graphic_Violence",
        "Violence",
        "Mild_Violence",
        "Graphic_Language",
        "Language",
        "Adult_Situations",
        "Strong_Sexual_Content",
        "Nudity",
        "Brief Nudity",
        "Rape"
    };

    static final String[] UntranslatedRoles=new String[]{
        "Actor","Lead_Actor","Supporting_Actor",
        "Actress","Lead_Actress","Supporting_Actress",
        "Guest","Guest_Star","Director","Producer",
        "Writer","Choreographer","Sports_Figure","Coach",
        "Host","Executive_Producer","Artist"
    };
    // shallow copy: values will be replaced by translated values
    static final String[] Roles=(String[])UntranslatedRoles.clone();
    
    static final String[] AirExtraInfo=new String[]{
        "Premiere",
        "Season_Premiere",
        "Series_Premiere",
        "Channel_Premiere",
        "Season_Finale",
        "Series_Finale",
    };

    static void TranslateArray(String[] array){
        for ( int i = 0 ; i < array.length; i ++ ){
            try {
                String translation=SageApi.StringApi("LocalizeString",new Object[]{array[i]});
                if ( translation.equals(array[i])) {
                    // no translation
                    translation=array[i].replace('_', ' ');
                }
                array[i]=translation;
            }catch (java.lang.reflect.InvocationTargetException e){
                System.out.println("Error translating \""+array[i]+"\" :"+e+"--"+e.getCause());
            }
        }
    }

    static { 
        // translate static strings
        TranslateArray(AdvisoryRatings);
        TranslateArray(Roles);
        TranslateArray(AirExtraInfo);
        TranslateArray(Ratings);
    }
    /**
     * 
     */
    public EditShowInfoServlet() {
        super();
    }
    private void editShow(HttpServletRequest req, HttpServletResponse resp,Airing airing, File file, PrintWriter out) throws Exception {
        printTitle(out,"Edit Show Info", SageServlet.isTitleBroken(req));
        out.println("<div id=\"content\">");

        // Ok, what do we have to do here...

        // 1) if edit show
        //        addshow()
        //        if different epgid
        //            then implicit set edit airing (using previous airing info if not set)    



        // 3) if set mediafile airing
        //        check airing existance
        //        implicit edit mediafile (using previous mf info if not set)

        //    else if edit airing 
        //         create airing
        //         implicit edit mediafile (using previous mf info if not set)



        // 4) if edit mediafile
        //         rename real file to .tmp
        //         delete file
        //         get airing starttime
        //         reset timestamp from airing
        //         rename file back from .tmp
        //         add mediafile
        //         if mf time != old mf time // file changed!
        //            rename real file to .tmp
        //            delete file
        //            get airing starttime
        //            reset timestamp from airing
        //            rename file back from .tmp
        //            add mediafile
        //         endif
        //         set mediafile airing



        boolean editShow=req.getParameter(PARAM_EDIT_SHOW_CB)!=null;
        boolean editAiring=req.getParameter(PARAM_EDIT_AIR_CB)!=null;
        boolean forceEditAiring=false;
        boolean editMediaFile=req.getParameter(PARAM_EDIT_FILE_CB)!=null;
        boolean forceEditMediaFile=false;

        final Object Show;
        String ShowID;

        if ( editShow ){
            ShowID=getDecodedParameter(req,PARAM_SH_EPGID); 
            if ( req.getParameter(PARAM_SH_GEN_EPGID_CB )!=null 
                    || ShowID.equalsIgnoreCase("NoShow")
                    || ShowID==null 
                    || ShowID.length()<3 ){
                do {
//                  // keep gen'd EPGID less than 12 chars
                    ShowID="EPex"+Integer.toHexString((int)(java.lang.Math.random()*0xFFFFFFF));
                } while (SageApi.Api("GetShowForExternalID",ShowID)!=null);

                // ShowID has changed -- force editAiring so that new Airing can be created for this show.
                forceEditAiring=true;
            }

            long duration=0;
            try { duration=Long.parseLong(req.getParameter(PARAM_SH_DURATION))*60*1000; }
            catch (NumberFormatException e){
                out.println("<h3>Failed when parsing integer value for Duration: "+req.getParameter(PARAM_SH_DURATION)+"</h3>");
                throw e;
            }
            boolean isFirstRun=req.getParameter(PARAM_SH_FIRSTRUN)!=null;
            long oad=0;
            try { oad=Utils.ParseDateTimeWidget(PARAM_SH_ORIGAIRDATE,req); }
            catch (NumberFormatException e){
                out.println("<h3>Failed when parsing Original Air Date: "+e.getMessage()+"</h3>");
                throw e;
            }
            String rating=getDecodedParameter(req,PARAM_SH_RATING);
            if ( rating.equals("") || rating.equalsIgnoreCase("none"))
                rating=null;

            // Parse Roles
            String rolesStr=getDecodedParameter(req,PARAM_SH_ROLES).trim();
            String[] rolesArr=rolesStr.split(";[ \r\n]*");
            String[] people=null;
            String[] roles=null;
            if (rolesArr.length>0){
                LinkedList<String> PeopleList=new LinkedList<String>();
                LinkedList<String> RolesList=new LinkedList<String>();
                for ( int i=0;i<rolesArr.length;i++){
                    String[] pair=rolesArr[i].trim().split(":[ \t]*");
                    if ( pair.length==2){
                        String person=pair[1].trim();
                        String role=pair[0].trim();
                        if ( person.length() >0 && role.length()>0){
                            int j;
                            for(j=0;j<Roles.length;j++){
                                if ( role.equalsIgnoreCase(Roles[j]) // local lang
                                     || role.equalsIgnoreCase(UntranslatedRoles[j]) // untranslated
                                     || role.equalsIgnoreCase(UntranslatedRoles[j].replace('_', ' '))) // en
                                {
                                    PeopleList.add(person);
                                    RolesList.add(Roles[j]);
                                    out.println("found:"+Roles[j]+":"+person+"<br/>");
                                    break;
                                }
                            }
                            if ( j==Roles.length){
                                out.println("<h3>Failed matching role:"+role+" for person:"+person+"<h3/>");
                                throw new NumberFormatException("Failed parsing roles list");
                            }
                        }
                    }
                }
                if ( PeopleList.size()>0){
                    people=PeopleList.toArray(new String[0]);
                    roles=RolesList.toArray(new String[0]);
                }
            }

            try { 
                Show=SageApi.Api("AddShow",new Object[] {
                        getDecodedParameter(req,PARAM_SH_NAME),//Title
                        new Boolean(isFirstRun),//IsFirstRun
                        getDecodedParameter(req,PARAM_SH_EPISODENAM),//Episode
                        getDecodedParameter(req,PARAM_SH_DESC),//Description
                        new Long(duration),//Duration
                        getDecodedParameter(req,PARAM_SH_CATEGORY),//Category
                        getDecodedParameter(req,PARAM_SH_SUBCATEGORY),//Sub-category
                        people,//PeopleList
                        roles,//Roles for PeopleList
                        rating,//Rated
                        req.getParameterValues(PARAM_SH_ADVISORY),//Expanded Rating
                        getDecodedParameter(req,PARAM_SH_YEAR),//Year
                        null,//ParentalRatings
                        null,//MiscList
                        ShowID,//ExternalID
                        getDecodedParameter(req,PARAM_SH_LANG),//Language
                        new Long(oad) //OriginalAirDate
                });
            } catch (InvocationTargetException e) {
                out.println("<p>Failed generating new Show: "+e.toString()+"</p>");
                out.close();
                return;
            }
            out.println("<p>Generated new Show: "+ShowID+"</p>");

        } // end EditShow
        else 
        {
            // show is unchanged
            Show=SageApi.Api("GetShow",airing.sageAiring);
            ShowID=(String)SageApi.Api("GetShowExternalID",Show);
        }


        final Object Airing;
        final Object origAiring;
        final Integer AiringID;
        final Object origAiringID;
        final long air_start;

        if ( forceEditAiring || editAiring ) {

            // airing info
            Integer channelID=null;
            long air_duration=0;
            boolean air_hdtv=false;
            int air_partnum=0;
            int air_totalparts=0;
            String air_rating=null;
            boolean air_stereo=false;
            boolean air_closedCaptioned=false;
            boolean air_sap=false;
            boolean air_subtitled=false;
            String air_premFinale=null;

            if ( editAiring ) {
                // check data
                try { air_start=Utils.ParseDateTimeWidget(PARAM_AIR_STARTTIME,req); }
                catch (NumberFormatException e){
                    out.println("<h3>Failed when parsing Airing Start Time: "+e.getMessage()+"</h3>");
                    throw e;
                }
                Object Channel=null;
                String channelStr=req.getParameter(PARAM_AIR_CHANNELID);
                if ( channelStr !=null && channelStr!="" && channelStr!="none"){
                    try { channelID=new Integer(channelStr); }
                    catch (NumberFormatException e){
                        out.println("<h3>Failed when parsing AirChannelID: "+e.getMessage()+"</h3>");
                        throw e;
                    }
                    Channel=SageApi.Api("GetChannelForStationID",channelID);
                    if ( Channel==null) {
                        out.println("<h3>Invalid AirChannelID: "+channelID+"</h3>");
                        throw new NumberFormatException("Invalid AirChannelID");
                    }
                }
    
                try { 
                    air_duration=Long.parseLong(req.getParameter(PARAM_AIR_DURATION))*60*1000;
                    if ( air_duration <=0 )
                        throw new NumberFormatException("Must be greater than 0");
                }
                catch (NumberFormatException e){
                    out.println("<h3>Failed when parsing integer value for Airing Duration: "+req.getParameter(PARAM_AIR_DURATION)+"</h3>");
                    throw e;
                }
    
                air_hdtv=req.getParameter(PARAM_AIR_HDTV)!=null;
    
                try { air_partnum=Integer.parseInt(req.getParameter(PARAM_AIR_PARTNUM)); }
                catch (NumberFormatException e){
                    out.println("<h3>Failed when parsing integer value for Airing Duration: "+req.getParameter(PARAM_AIR_PARTNUM)+"</h3>");
                    throw e;
                }
                try { air_totalparts=Integer.parseInt(req.getParameter(PARAM_AIR_TOTALPART)); }
                catch (NumberFormatException e){
                    out.println("<h3>Failed when parsing integer value for Airing Duration: "+req.getParameter(PARAM_AIR_TOTALPART)+"</h3>");
                    throw e;
                }
                air_rating=getDecodedParameter(req,PARAM_AIR_RATING);
                if ( air_rating.equals("") || air_rating.equalsIgnoreCase("none"))
                    air_rating=null;
    
                air_stereo=req.getParameter(PARAM_AIR_STEREO)!=null;
                air_closedCaptioned=req.getParameter(PARAM_AIR_CC)!=null;
                air_sap=req.getParameter(PARAM_AIR_SAP)!=null;
                air_subtitled=req.getParameter(PARAM_AIR_SUBTITLE)!=null;
                air_premFinale=req.getParameter(PARAM_AIR_PREM_FINALE);
                if ( air_premFinale!=null)
                    air_premFinale=URLDecoder.decode(air_premFinale,charset);
            } else {
                // get airing data from existing airing
                Object channel=SageApi.Api("GetChannel",airing.sageAiring);
                if ( channel != null )
                    channelID=(Integer)SageApi.Api("GetStationID",channel);
                
                Long AirStart=(Long)SageApi.Api("GetAiringStartTime",airing.sageAiring);
                if ( AirStart!=null)
                    air_start=AirStart.longValue();
                else
                    air_start=0;
                Long Duration=(Long)SageApi.Api("GetAiringDuration",airing.sageAiring);
                air_duration=0;
                if ( Duration != null )
                    air_duration=Duration.longValue();

                if ( SAGE_MAJOR_VERSION>4.99 ) 
                	air_hdtv=SageApi.booleanApi("IsAiringHDTV",new Object[]{airing.sageAiring});
                
                air_rating=SageApi.StringApi("GetParentalRating",new Object[]{airing.sageAiring});
                String extrainf=SageApi.StringApi("GetExtraAiringDetails",new Object[]{airing.sageAiring});
                if (extrainf==null )
                    extrainf="";
                try {
                    String[] extraInfArr=extrainf.split(", *");
                    int[] partofpartsret=getPartOfParts(extraInfArr);
                    if ( partofpartsret!=null ){
                        air_partnum=partofpartsret[0];
                        air_totalparts=partofpartsret[1];
                    }
                    java.util.List<String> extraInfList=java.util.Arrays.asList(extraInfArr);
                    air_stereo=extraInfList.contains(SageApi.StringApi("LocalizeString",new Object[]{"Stereo"}));
                    air_subtitled=extraInfList.contains(SageApi.StringApi("LocalizeString",new Object[]{"Subtitled"}));
                    air_closedCaptioned=extraInfList.contains(SageApi.StringApi("LocalizeString",new Object[]{"Closed_Captioned"}));
                    air_sap=extraInfList.contains(SageApi.StringApi("LocalizeString",new Object[]{"SAP"}));

                    for ( int airExtraInfoNum=0;airExtraInfoNum<AirExtraInfo.length; airExtraInfoNum++){
                        if ( extraInfList.contains(AirExtraInfo[airExtraInfoNum]))
                            air_premFinale=AirExtraInfo[airExtraInfoNum];
                    }

                }catch (Exception e){
                    log("failed to parse extra inf:\""+extrainf+"\"",e);
                }
            }
 
            try {
                if ( SAGE_MAJOR_VERSION >4.99 )
                    Airing=SageApi.Api("AddAiringDetailed",new Object[] {
                            ShowID,
                            channelID,
                            new Long(air_start),
                            new Long(air_duration),
                            new Integer(air_partnum),
                            new Integer(air_totalparts),
                            air_rating,
                            new Boolean(air_hdtv),
                            new Boolean(air_stereo),
                            new Boolean(air_closedCaptioned),
                            new Boolean(air_sap),
                            new Boolean(air_subtitled),
                            air_premFinale} );
                else
                    Airing=SageApi.Api("AddAiring",new Object[] {
                            ShowID,
                            channelID,
                            new Long(air_start),
                            new Long(air_duration)} );
                    
            } 
            catch (InvocationTargetException e) {
                out.println("<p>Failed generating new Airing<br/>for Show "+ShowID+" on chID "+channelID +" st="+air_start+" dur="+air_duration+"<br/>"+e.toString()+"</p>");
                out.close();
                return;
            }
            origAiring=SageApi.Api("GetMediaFileAiring",airing.sageAiring);
            origAiringID=(Integer)SageApi.Api("GetAiringID",new Object[]{origAiring});
            AiringID=(Integer)SageApi.Api("GetAiringID",new Object[]{Airing});

            if ( origAiringID.equals(AiringID)){
                out.println("<p>re-using original Airing ID: "+AiringID+"</p>");
                editAiring=false;
                forceEditAiring=false;
            } else {
                out.println("<p>Generated new Airing with ID: "+AiringID+"</p>");
                // new airing requires edit mediafile
                forceEditMediaFile=true;
            } 
        } // end of if editAiring
        else 
        {
            // use old airing
            Airing=SageApi.Api("GetMediaFileAiring",airing.sageAiring);
            AiringID=(Integer)SageApi.Api("GetAiringID",new Object[]{Airing});
            origAiring=SageApi.Api("GetMediaFileAiring",airing.sageAiring);
            origAiringID=(Integer)SageApi.Api("GetAiringID",new Object[]{origAiring});
            air_start=airing.getStartMillis();

        }

        // 3) if set mediafile airing
        //        check airing existance
        //        implicit edit mediafile (using previous mf info if not set)


        if ( editMediaFile || forceEditMediaFile ) {
            if ( ! SageApi.booleanApi("IsDVD",new Object[]{airing.sageAiring})){

                long fileStartOffset;
                if ( editMediaFile ){
                    fileStartOffset=0;
                    try { fileStartOffset=Long.parseLong(req.getParameter(PARAM_FILE_STARTOFFSET))*1000; }
                    catch (NumberFormatException e){
                        out.println("<h3>Failed when parsing FileStartOffset: "+e.getMessage()+"</h3>");
                        throw e;
                    }
                    if ( req.getParameter(PARAM_FILE_OFFSETTYPE).equalsIgnoreCase("earlier"))
                        fileStartOffset=-fileStartOffset;
                } else {
                    Long AirStart=(Long)SageApi.Api("GetAiringStartTime",airing.sageAiring);
                    long airstart=0;
                    if ( AirStart!=null)
                        airstart=AirStart.longValue();
                    Long FileStart=(Long)SageApi.Api("GetFileStartTime",airing.sageAiring);
                    long filestart=0;
                    if ( FileStart!=null)
                        filestart=FileStart.longValue();
                    fileStartOffset=(filestart-airstart);
                }

                Long FileDuration=(Long)SageApi.Api("GetDurationForSegment",new Object[]{airing.sageAiring,new Integer(0)});
                long fileduration=0;
                if ( FileDuration != null ){ 
                    fileduration=FileDuration.longValue();
                }

                String showName=(String)SageApi.Api("GetShowTitle",Airing);
                String episodeName=(String)SageApi.Api("GetShowEpisode",Airing);

                if ( ! editAiring){
                    // AiringID has not been changed.
                    // filename will be the same.
                    // We need to rescan the file by first using a tmp file

                    String tmpFileName=file.getParent()+
                    File.separator+showName.trim().replaceAll("[^A-Za-z0-9]","")+
                    "-"+episodeName.trim().replaceAll("[^A-Za-z0-9]","")+
                    "-"+AiringID+"-0-"+
                    "tmp."+
                    file.getName().replaceAll("^.*\\.([^.]*)$","$1");//extension
                    out.println("<p>Renaming: \""+Translate.encode(file.getPath())+"\" to \""+Translate.encode(tmpFileName)+"\"</p>");
                    File tmpFile=new File(tmpFileName);

                    if ( ! file.renameTo(tmpFile) ){
                        out.println("<h3>Failed to rename file -- file may be in use!</h3>");
                        throw new IOException("failed generating updated MediaFile");
                    }

                    Object tmpMediaFile=SageApi.Api("AddMediaFile",new Object[] {tmpFile.getAbsolutePath(),""});
                    if ( tmpMediaFile == null ){
                        out.println("<p>Failed to add new MediaFile to Sage</p>");
                        if ( ! tmpFile.renameTo(file) )
                            out.println("<h3>Failed to revert file name -- file now named "+Translate.encode(tmpFile.getAbsolutePath())+"</h3>");
                        throw new IOException("failed generating updated MediaFile");
                    }

                    // check duration
                    Long NewDuration=(Long)SageApi.Api("GetDurationForSegment",new Object[]{tmpMediaFile,new Integer(0)});
                    long newduration=0;
                    if ( NewDuration != null ){ 
                        newduration=NewDuration.longValue();
                    }
                    if ( newduration > 100*24*60*60*1000 ) {
                        out.println("<p>Error: failed getting new duration (got: "+newduration+"msecs)</p>");
                        if ( ! tmpFile.renameTo(file) )
                            out.println("<h3>Failed to revert file name -- file now named "+Translate.encode(tmpFile.getAbsolutePath())+"</h3>");
                        throw new IOException("failed length-parsing updated MediaFile");
                    }

                    if ( ! tmpFile.setLastModified(air_start+fileStartOffset+newduration)){
                        out.println("<p>Error: failed setting timestamp</p>");
                        if ( ! tmpFile.renameTo(file) )
                            out.println("<h3>Failed to revert file name -- file now named "+Translate.encode(tmpFile.getAbsolutePath())+"</h3>");
                        throw new IOException("failed creating updated MediaFile");
                    }

                    out.println("<p>deleteing old DB file entry</p>");
                    SageApi.Api("DeleteFile",airing.sageAiring);

                    if ( SageApi.Api("GetMediaFileForID",new Object[]{new Integer(airing.id)})!=null){
                        out.println("<h3>Failed to delete old DB file entry -- file may be in use!</h3>");
                        if ( ! tmpFile.renameTo(file) )
                            out.println("<h3>Failed to revert file name -- file now named "+Translate.encode(tmpFile.getAbsolutePath())+"</h3>");
                        throw new IOException("failed generating updated MediaFile");
                    }

                    String newFileName=file.getParent()+
                    File.separator+
                    showName.trim().replaceAll("[^A-Za-z0-9]","")+
                    "-"+episodeName.trim().replaceAll("[^A-Za-z0-9]","")+
                    "-"+AiringID+"-0."+
                    file.getName().replaceAll("^.*\\.([^.]*)$","$1");//extension
                    File newFile=new File(newFileName);
                    out.println("<p>Renaming: \""+Translate.encode(tmpFile.getPath())+"\" to \""+Translate.encode(newFileName)+"\"</p>");
                    if ( ! tmpFile.renameTo(newFile) ){
                        out.println("<h3>Failed to rename file -- file may be in use!</h3>");
                        out.println("<h3>cannot revert -- file now named "+Translate.encode(tmpFile.getAbsolutePath())+"</h3>");
                        throw new IOException("failed generating updated MediaFile");
                    }

                    Object newMediaFile=SageApi.Api("AddMediaFile",new Object[] {newFile.getPath(),""});
                    if ( newMediaFile == null ){
                        out.println("<p>Failed to add new MediaFile to Sage</p>");
                        throw new IOException("failed generating updated MediaFile");
                    }
                    out.println("<p>Added new MediaFile with ID: "+SageApi.IntApi("GetMediaFileID",new Object[]{newMediaFile})+"</p>");
                    if ( ! SageApi.booleanApi("SetMediaFileAiring",new Object[]{newMediaFile,Airing}) ){
                        out.println("<h3>Failed to link file to airing!</h3>");
                        out.println("<h3>cannot revert -- file now named "+Translate.encode(newFile.getAbsolutePath())+"</h3>");
                        throw new IOException("failed updating MediaFile");
                    }
                    out.println("<p>Sucessfully updated Airing information in MediaFile<br/>\r\n" +
                    "Note, This makes this file an Archived Recording</p>");
                    out.println("<p>View updated <a href=\"DetailedInfo?MediaFileId="+
                            SageApi.IntApi("GetMediaFileID",new Object[]{newMediaFile})+
                    "\">Detailed Info</a></p>");

                }  else {
                    // airing has been edited, mediafile has a new name.
                    // we can be a bit safter and keep the old MediaFile

                    String newFileName=file.getParent()+
                    File.separator+showName.trim().replaceAll("[^A-Za-z0-9]","")+
                    "-"+episodeName.trim().replaceAll("[^A-Za-z0-9]","")+
                    "-"+AiringID+"-0."+
                    file.getName().replaceAll("^.*\\.([^.]*)$","$1");//extension
                    out.println("<p>Renaming: \""+Translate.encode(file.getPath())+"\" to \""+Translate.encode(newFileName)+"\"</p>");
                    
                    long oldFileTimestamp=file.lastModified();

                    File newFile=new File(newFileName);
                    if ( ! file.renameTo(newFile) ){
                        out.println("<h3>Failed to rename file -- file may be in use!</h3>");
                        throw new IOException("failed generating updated MediaFile");
                    }
                    Object newMediaFile=null;
                    try {
                        do {
                            if ( ! newFile.setLastModified(air_start+fileStartOffset+fileduration)) {
                                out.println("<h3>Failed to update file timestamp</h3>");
                                newFile.renameTo(file);
                                throw new IOException("failed generating updated MediaFile");
                            }
                            // Add MediaFile
                            newMediaFile=SageApi.Api("AddMediaFile",new Object[] {newFile.getPath(),""});
                            if ( newMediaFile == null ){
                                out.println("<p>Failed to add new MediaFile to Sage</p>");
                                throw new IOException("failed generating updated MediaFile");
                            }
                            out.println("<p>Added new MediaFile with ID: "+SageApi.IntApi("GetMediaFileID",new Object[]{newMediaFile})+"</p>");
                            // check duration
                            Long NewDuration=(Long)SageApi.Api("GetDurationForSegment",new Object[]{newMediaFile,new Integer(0)});
                            long newduration=0;
                            if ( NewDuration != null ){ 
                                newduration=NewDuration.longValue();
                            }
                            if ( newduration > 100*24*60*60*1000 ) {
                                out.println("<p>Error: failed getting new duration (got: "+newduration+"secs)</p>");
                                throw new IOException("failed length-parsing updated MediaFile");
                            } else if ( Math.abs(newduration-fileduration)>5000 ){
                                out.println("<p>Warning: new mediaFile's duration of "+newduration/1000+" secs does not match original: "+fileduration/1000+" secs<br/>\r\n"+
                                "Possibly editted file -- regenerating mediafile with new timestamp</p>");
                                if ( ! newFile.renameTo(file) ){
                                    out.println("<h3>Failed to rename file: file currently named:<br/>" +
                                            Translate.encode(newFile.getPath())+"</h3>");
                                    throw new IOException("failed generating updated MediaFile");
                                }
                                SageApi.Api("DeleteFile",newMediaFile);
                                fileduration=newduration;
                                if ( ! file.renameTo(newFile) ){
                                    out.println("<p>Failed to rename file</p>");
                                    throw new IOException("failed generating updated MediaFile");
                                }
                            } else {
                                break;
                            }
                        } while(true);
                    } catch (Exception e) {
                        out.println("<p>Reverting to old file name</p>");
                        if ( ! newFile.renameTo(file) ){
                            out.println("<h3>Failed when reverting to old file name. : file currently named:<br/>" +
                                    Translate.encode(newFile.getPath())+"</h3>");
                        } else {
                            if (newMediaFile != null ){
                                SageApi.Api("DeleteFile",newMediaFile);
                            }
                        }
                        throw e;
                    }
                    // file has been renamed: delete old file
                    Object OldAiring=SageApi.Api("GetMediaFileAiring",airing.sageAiring);
                    SageApi.Api("DeleteFile",airing.sageAiring);
                    if (SageApi.booleanApi("SetMediaFileAiring",new Object[]{newMediaFile,Airing})){
                        out.println("<p>Sucessfully set updated Airing information in MediaFile<br/>\r\n" +
                        "Note, This makes this file an Archived Recording</p>");
                        out.println("<p>View updated <a href=\"DetailedInfo?MediaFileId="+
                                SageApi.IntApi("GetMediaFileID",new Object[]{newMediaFile})+
                        "\">Detailed Info</a></p>");
                    } else {
                        out.println("<h3>Error: failed setting airing on new MediaFile</h3>");
                        if ( ! newFile.renameTo(file) ){
                            out.println("<h3>Failed when reverting to old file name. Airing info may be lost</h3>");
                        } else {
                            SageApi.Api("DeleteFile",newMediaFile);
                            file.setLastModified(oldFileTimestamp);
                            Object oldMediaFile=SageApi.Api("AddMediaFile",new Object[] {file.getPath(),""});
                            if (! SageApi.booleanApi("SetMediaFileAiring",new Object[]{oldMediaFile,OldAiring})){
                                out.println("<h3>Failed when reverting to old airing data. Airing info may be lost</h3>");  
                            } else {
                            	out.println("<p>Reverted to original file name and airing info</p>");
                            }
                        }
                        throw new IOException("failed when setting airing info on new media file");
                    }
                } // end editAiring
            } // end ! DVD
            else {
                // DVDs can only have their Show set...
                if (SageApi.booleanApi("SetMediaFileShow",new Object[]{airing.sageAiring,Show})){
                    out.println("<p>Sucessfully set added Show information to DVD<br/>\r\n" +
                    "Note, This makes this file an Archived Recording</p>");
                    out.println("<p>View updated <a href=\"DetailedInfo?MediaFileId="+airing.id+
                    "\">Detailed Info</a></p>");
                } else {
                    out.println("<p>Error: failed adding Show info to DVD.</p>");
                }
            }
        } // end Edit Media File
        else {
            out.println("<p>View updated <a href=\"DetailedInfo?MediaFileId="+airing.id+
            "\">Detailed Info</a></p>");
        }
        printFooter(req,out);
        out.println("</div>");//content
        printMenu(req,out);
        out.println("</body></html>");
        out.close();
    }


    private void printForm(HttpServletRequest req, HttpServletResponse resp,Airing airing, PrintWriter out) throws Exception {

        // print form
        printTitle(out,"Edit Show Info", SageServlet.isTitleBroken(req));
        out.println("<div id=\"content\">");
        out.println("<h3>Warning: This is an experimental feature, and if things go wrong, you may lose the DB information for the show being edited.<br/>(The file itself should appear in the imported videos)</h3>");
        out.println("<form method='post' action='EditShowInfo' name='EditShowForm'>");
        out.println("<input type='hidden' name='MediaFileId' value='"+Integer.toString(airing.id)+"'/>");
        out.println("<table>");
        out.println("<tr><td colspan=\"2\"><h3><input type=\"checkbox\" onchange=\"SelectEditMode('sh_',this.checked)\" name=\""+PARAM_EDIT_SHOW_CB+"\"/>Edit Show Information</h3></td></tr>");
        out.println("<tr><td>Show Name:</td><td><input type=\"text\" cols=\"50\" name=\""+PARAM_SH_NAME+"\" value=\""+Translate.encode(airing.getTitle())+"\"></td></tr>");
        out.println("<tr><td>Episode Name:</td><td><input type=\"text\" cols=\"50\" name=\""+PARAM_SH_EPISODENAM+"\" value=\""+Translate.encode(airing.getEpisode())+"\"></td></tr>");

        String epgid=SageApi.StringApi("GetShowExternalID",new Object[]{airing.sageAiring});
        out.println("<tr><td>EPGID:</td><td><input type=\"text\" cols=\"25\"  name=\""+PARAM_SH_EPGID+"\" value=\""
                +Translate.encode(epgid)
                +"\"/>(EPGID for TV files must start with 'EP' 'SH' 'SP' or 'MV')");

        out.print("<br/><input type=\"checkbox\"  name=\""+PARAM_SH_GEN_EPGID_CB+"\" ");
        if ( ! epgid.startsWith("EP") || epgid.startsWith("MV") || epgid.startsWith("SP")){
            out.print("checked=\"checked\"");  
        }
        out.println("/>Auto-Generate New EPxxx EPGID (makes it a TV file and prevents overwrites when EPG updates)");
        out.println("</td></tr>");

        out.print("<tr><td>First Run:</td><td><input type=\"checkbox\" name=\""+PARAM_SH_FIRSTRUN+"\" ");
        if ( SageApi.booleanApi("IsShowFirstRun",new Object[]{airing.sageAiring}))
            out.print("checked=\"checked\"");
        out.println("></td></tr>");

        String s=SageApi.StringApi("GetShowCategory",new Object[]{airing.sageAiring});
        if ( s == null)
            s="";
        out.println("<tr><td>Category:</td><td><input type=\"text\" name=\""+PARAM_SH_CATEGORY+"\" value=\""+Translate.encode(s)+"\">");
        s=SageApi.StringApi("GetShowSubCategory",new Object[]{airing.sageAiring});
        if ( s == null)
            s="";
        out.println(" / <input type=\"text\" name=\""+PARAM_SH_SUBCATEGORY+"\" value=\""+Translate.encode(s)+"\"></td></tr>");
        Long Duration=(Long)SageApi.Api("GetShowDuration",airing.sageAiring);
        long duration=0;
        if ( Duration != null )
            duration=Duration.longValue();
        out.println("<tr><td>Duration:</td><td><input onchange=\"RemoveNonNumbers(this)\" type=\"text\" size=\"4\" name=\""+PARAM_SH_DURATION+"\" value=\""+Long.toString(duration/60000)+"\">mins</td></tr>");

        s=SageApi.StringApi("GetShowDescription",new Object[]{airing.sageAiring});
        if ( s == null)
            s="";
        out.println("<tr><td>Description:</td><td><textarea wrap=\"virtual\" cols=\"50\" rows=\"5\" name=\""+PARAM_SH_DESC+"\">"+Translate.encode(s)+"</textarea></td></tr>");

        out.println("<tr><td>Rating:</td><td><select name=\""+PARAM_SH_RATING+"\">");
        String rating=SageApi.StringApi("GetShowRated",new Object[]{airing.sageAiring});
        out.print("  <option name=\"none\" ");
        if ( rating==null || rating=="")
            out.print("selected=\"selected\"");
        out.println(">None</option>");

        for(int i=0;i<Ratings.length;i++){
            out.print("  <option name=\""+Ratings[i]+"\" ");
            if ( Ratings[i].equals(rating))
                out.print("selected=\"selected\"");
            out.println(">"+Ratings[i]+"</option>");
        }
        out.println("</select></td></tr>");

        String advRatings=(String)SageApi.Api("GetShowExpandedRatings",airing.sageAiring);
        if (advRatings==null)
            advRatings="";
        //System.out.println("advRatings="+advRatings.getClass().getName()+" - "+advRatings.toString());
        out.println("<tr><td>Advisories:<br/>(use CTRL-click<br/>to multi-select)</td><td><select name=\""+PARAM_SH_ADVISORY+"\" multiple=\"multiple\" size=\"3\">");
        for(int i=0;i<AdvisoryRatings.length;i++){
            out.print("  <option name=\""+AdvisoryRatings[i]+"\" ");
            if ( advRatings.indexOf(AdvisoryRatings[i])>=0){
                out.print("selected=\"selected\"");
            }
            out.println(">"+AdvisoryRatings[i]+"</option>");
        }
        out.print("</select></td></tr>");

        out.println("<tr><td>Roles:<br/>Semicolon-separated list<br/>" +
                "of Role:Person pairs<br/>" +
                "<a href=\"ListRoles.html\" target=\"_blank\">(List Roles)</a>" +
                "</td><td><textarea wrap=\"virtual\" cols=\"50\" rows=\"5\" name=\""+PARAM_SH_ROLES+"\">");
        for (int i=0;i<Roles.length;i++){
            String people=(String)SageApi.Api("GetPeopleInShowInRoles",new Object[]{airing.sageAiring,Roles[i]});
            if ( people != null && people.length()>0 ){
                String[] peoplearr=people.split(",[ \t]*");
                for (int j=0;j<peoplearr.length;j++)
                    out.println(Roles[i]+":"+peoplearr[j]+";");
            }
        }
        out.println("</textarea></td></tr>");


        out.println("<tr><td>OriginalAirDate:</td><td>");
        Long Oad=(Long)SageApi.Api("GetOriginalAiringDate",airing.sageAiring);
        long oad=0;
        if ( Oad!=null)
            oad=Oad.longValue();
        Utils.PrintDateTimeWidget(out,oad,PARAM_SH_ORIGAIRDATE);
        out.println("</td></tr>");
        String Yr=(String)SageApi.Api("GetShowYear",airing.sageAiring);
        if ( Yr==null)
            Yr="";
        out.println("<tr><td>Year:</td><td><input type=\"text\" onchange=\"RemoveNonNumbers(this)\" size=\"5\" name=\""+PARAM_SH_YEAR+"\" value=\""+Yr+"\"></td></tr>");         

        String Lang=(String)SageApi.Api("GetShowLanguage",airing.sageAiring);
        if ( Lang==null)
            Lang="";
        out.println("<tr><td>Language:</td><td><input type=\"text\" name=\""+PARAM_SH_LANG+"\" value=\""+Lang+"\"></td></tr>");

        if (! SageApi.booleanApi("IsDVD",new Object[]{airing.sageAiring})) {

            out.println("<tr><td colspan=\"2\"><hr/><h3><input type=\"checkbox\" onchange=\"SelectEditMode('air_',this.checked);\" name=\""+PARAM_EDIT_AIR_CB+"\"/>Edit Airing Information</h3>" +
            "(note, implicity rescans file) </td></tr>");
            out.println("<tr><td>Airing start time:</td><td>");
            Long AirStart=(Long)SageApi.Api("GetAiringStartTime",airing.sageAiring);
            long airstart=0;
            if ( AirStart!=null)
                airstart=AirStart.longValue();
            Utils.PrintDateTimeWidget(out,airstart,PARAM_AIR_STARTTIME);
            out.println("</td></tr>");

            Duration=(Long)SageApi.Api("GetAiringDuration",airing.sageAiring);
            duration=0;
            if ( Duration != null )
                duration=Duration.longValue();
            out.println("<tr><td>Airing Duration:</td><td><input onchange=\"RemoveNonNumbers(this)\" type=\"text\" size=\"4\" name=\""+PARAM_AIR_DURATION+"\" value=\""+Long.toString(duration/60000)+"\">mins</td></tr>");

            out.println("<tr><td>Channel:</td><td>");
            out.println("      <select name=\""+PARAM_AIR_CHANNELID+"\">");
            out.print("        <option value=\"0\" ");
            Object AirChannel=SageApi.Api("GetChannel",airing.sageAiring);
            if ( AirChannel==null )
                out.print("selected=\"selected\"");
            out.println(">None</option>");  
            Object channels=SageApi.Api("GetAllChannels");
            channels=SageApi.Api("FilterByBoolMethod",new Object[]{channels, "IsChannelViewable", Boolean.TRUE});
            channels=SageApi.Api("Sort",new Object[]{channels,Boolean.FALSE,"ChannelNumber"});

            for (int i =0; i<SageApi.Size(channels);i++){
                Object channel=SageApi.GetElement(channels,i);
                Object stationId=SageApi.Api("GetStationID",channel);
                Object channelnum=SageApi.Api("GetChannelNumber",channel);
                out.print("        <option value=\""+
                        stationId.toString()+"\" ");
                if ( AirChannel == channel)
                    out.print("selected=\"selected\"");
                out.println(">"+
                        channelnum.toString()+
                        " - "+
                        Translate.encode((String)SageApi.Api("GetChannelName",new Object[]{channel}))+
                "</option>");
            }
            out.println("      </select></td></tr>");

            out.print("<tr><td>HDTV airing:</td><td><input type=\"checkbox\" name=\""+PARAM_AIR_HDTV+"\" ");
            if ( SAGE_MAJOR_VERSION>4.99 &&
            	 SageApi.booleanApi("IsAiringHDTV",new Object[]{airing.sageAiring}))
                out.print("checked=\"checked\"");
            out.println("></td></tr>");

            out.println("<tr><td>Rating:</td><td><select name=\""+PARAM_AIR_RATING+"\">");
            rating=SageApi.StringApi("GetParentalRating",new Object[]{airing.sageAiring});
            out.print("  <option name=\"\" ");
            if ( rating==null || rating=="")
                out.print("selected=\"selected\"");
            out.println(">None</option>");

            for(int i=0;i<Ratings.length;i++){
                out.print("  <option name=\""+Ratings[i]+"\" ");
                if ( Ratings[i].equals(rating))
                    out.print("selected=\"selected\"");
                out.println(">"+Ratings[i]+"</option>");
            }
            out.println("</select></td></tr>");

            String extrainf=SageApi.StringApi("GetExtraAiringDetails",new Object[]{airing.sageAiring});
            if (extrainf==null )
                extrainf="";

            // look for part of parts and other extra inf
            int partnum=0;
            int totalparts=0;
            boolean Stereo=false;
            boolean ClosedCaptioning=false;
            boolean SAP=false;
            boolean Subtitled=false;
            java.lang.String airExtraInf=null;
            try {
                String[] extraInfArr=extrainf.split(", *");
                int[] partofpartsret=getPartOfParts(extraInfArr);
                if ( partofpartsret!=null ){
                    partnum=partofpartsret[0];
                    totalparts=partofpartsret[1];
                }
                java.util.List<String> extraInfList=java.util.Arrays.asList(extraInfArr);
                Stereo=extraInfList.contains(SageApi.StringApi("LocalizeString",new Object[]{"Stereo"}));
                Subtitled=extraInfList.contains(SageApi.StringApi("LocalizeString",new Object[]{"Subtitled"}));
                ClosedCaptioning=extraInfList.contains(SageApi.StringApi("LocalizeString",new Object[]{"Closed_Captioned"}));
                SAP=extraInfList.contains(SageApi.StringApi("LocalizeString",new Object[]{"SAP"}));

                for ( int airExtraInfoNum=0;airExtraInfoNum<AirExtraInfo.length; airExtraInfoNum++){
                    if ( extraInfList.contains(AirExtraInfo[airExtraInfoNum]))
                        airExtraInf=AirExtraInfo[airExtraInfoNum];
                }

            }catch (Exception e){
                log("failed to parse extra inf:\""+extrainf+"\"",e);
            }

            out.print("<tr><td>Part number: </td><td>" +
                    "<input onchange=\"RemoveNonNumbers(this)\" type=\"text\" size=\"4\" name=\""+PARAM_AIR_PARTNUM+"\" value=\""+partnum+"\"> of " +
                    "<input onchange=\"RemoveNonNumbers(this)\" type=\"text\" size=\"4\" name=\""+PARAM_AIR_TOTALPART+"\" value=\""+totalparts+"\"></td></tr>");

            out.print("<tr><td>Stereo:</td><td><input type=\"checkbox\" name=\""+PARAM_AIR_STEREO+"\" ");
            if ( Stereo)
                out.print("checked=\"checked\"");
            out.println("></td></tr>");

            out.print("<tr><td>Closed Captioned:</td><td><input type=\"checkbox\" name=\""+PARAM_AIR_CC+"\" ");
            if ( ClosedCaptioning)
                out.print("checked=\"checked\"");
            out.println("></td></tr>");

            out.print("<tr><td>Secondary Audio:</td><td><input type=\"checkbox\" name=\""+PARAM_AIR_SAP+"\" ");
            if ( SAP)
                out.print("checked=\"checked\"");
            out.println("></td></tr>");

            out.print("<tr><td>Subtitled:</td><td><input type=\"checkbox\" name=\""+PARAM_AIR_SUBTITLE+"\" ");
            if ( Subtitled)
                out.print("checked=\"checked\"");
            out.println("></td></tr>");            

            out.print("<tr><td>Premiere/Finale:</td><td>");
            out.println("      <select name=\""+PARAM_AIR_PREM_FINALE+"\">");
            out.print("        <option value=\"NONE\" ");
            if ( airExtraInf==null )
                out.print("selected=\"selected\"");
            out.println(">None</option>");
            for ( int airExtraInfoNum=0;airExtraInfoNum<AirExtraInfo.length; airExtraInfoNum++){
                out.print("        <option value=\""+Translate.encode(AirExtraInfo[airExtraInfoNum])
                        +"\" ");
                if ( airExtraInf!=null && airExtraInf.equals(AirExtraInfo[airExtraInfoNum]))
                    out.print("selected=\"selected\"");
                out.println(">"+Translate.encode(AirExtraInfo[airExtraInfoNum])+
                "</option>");
            }
            out.println("      </select></td></tr>");

            out.println("<tr><td colspan=\"2\"><hr/><h3><input type=\"checkbox\" onchange=\"SelectEditMode('file_',this.checked)\" name=\""+PARAM_EDIT_FILE_CB+"\"/>Edit File Information</h3></td></tr>");

            Long FileStart=(Long)SageApi.Api("GetFileStartTime",airing.sageAiring);
            long filestart=0;
            if ( FileStart!=null)
                filestart=FileStart.longValue();
            long fileoffset=(filestart-airstart);
            out.println("<tr><td>File start time:</td><td>"+
                    "<input type=\"text\" onchange=\"RemoveNonNumbers(this)\" size=\"5\" name=\""+PARAM_FILE_STARTOFFSET+"\" value=\""+Long.toString((Math.abs(fileoffset)+500)/1000)+"\">seconds");
            out.println("   <select name=\""+PARAM_FILE_OFFSETTYPE+"\">");
            out.print("   <option value=\"earlier\"");
            if ( fileoffset <=0)
                out.print(" selected=\"selected\"");
            out.println(">Earlier</option>");
            out.print("        <option value=\"later\"");
            if ( fileoffset >0)
                out.print(" selected=\"selected\"");
            out.println(">Later</option>");             
            out.println("   </select> than Airing Start Time</td></tr>");

            // file duration
            Duration=(Long)SageApi.Api("GetDurationForSegment",new Object[]{airing.sageAiring,new Integer(0)});

            if ( Duration == null || Duration.longValue()==0)
            {
                Duration=(Long)SageApi.Api("GetAiringDuration",airing.sageAiring);
            }

        }           
        out.println("<tr><td>File Duration:</td><td>"+(String)SageApi.Api("PrintDurationWithSeconds",Duration)+"</td></tr>");

        out.println("</table>");
        out.println("<p><input type=\"submit\" name='Update' value=\"Update\"/></p>");  
        out.println("</form>");
        out.println("<script type=\"text/javascript\">SelectEditMode('air_',false);SelectEditMode('file_',false);SelectEditMode('sh_',false);</script>");
        printFooter(req,out);
        out.println("</div>");//content
        printMenu(req,out);
        out.println("</body></html>");
        out.close();

    }
    /* (non-Javadoc)
     * @see net.sf.sageplugins.webserver.SageServlet#doServletGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doServletGet(HttpServletRequest req, HttpServletResponse resp)
    throws Exception {

        htmlHeaders(resp);
        noCacheHeaders(resp);
        PrintWriter out = getGzippedWriter(req,resp);
        // must catch and report all errors within Gzipped Writer
        try {
            xhtmlHeaders(out);
            out.println("<head>");
            jsCssImport(req, out);
            out.println("<title>Edit Show Info</title></head>");
            out.println("<body>");

            Airing airing=null;
            try {
                airing=new Airing(req);
            } catch ( Exception e) {

                printTitle(out,"Edit Show Info: Error", SageServlet.isTitleBroken(req));
                out.println("<div id=\"content\">");
                out.println("<h3>Unknown airing/media file ID passed</h3>");
                out.println("<p>"+e.toString()+"</p>");
                out.println("</div>");
                printMenu(req,out);
                out.println("</body></html>");
                out.close();
                return;
            }
            if ( ! OVERRIDE_CLIENT_CHECK && SageApi.booleanApi("IsClient",null) ) {
                printTitle(out,"Edit Show Info: Error", SageServlet.isTitleBroken(req));
                out.println("<div id=\"content\">");
                out.println("<h3>Cannot run in the context of a client</h3>");
                out.println("</div>");
                printMenu(req,out);
                out.println("</body></html>");
                out.close();
                return;
            }
            if ( airing.idType != Airing.ID_TYPE_MEDIAFILE){
                printTitle(out,"Edit Show Info: Error", SageServlet.isTitleBroken(req));
                out.println("<div id=\"content\">");
                out.println("<h3>No MediaFile passed</h3>");
                out.println("</div>");
                printMenu(req,out);
                out.println("</body></html>");
                out.close();
            }
            if ( ! SageApi.booleanApi("IsVideoFile",new Object[]{airing.sageAiring})){
                printTitle(out,"Edit Show Info: Error", SageServlet.isTitleBroken(req));
                out.println("<div id=\"content\">");
                out.println("<h3>Can only edit show info for video files</h3>");
                out.println("</div>");
                printMenu(req,out);
                out.println("</body></html>");
                out.close();
            }
            Object SegmentFiles=SageApi.Api("GetSegmentFiles",airing.sageAiring);
            if ( SageApi.Size(SegmentFiles)!=1){
                printTitle(out,"Edit Show Info: Error", SageServlet.isTitleBroken(req));
                out.println("<div id=\"content\">");
                out.println("<h3>MediaFile is segmented: cannot function on segmented media files</h3>");
                out.println("</div>");
                printMenu(req,out);
                out.println("</body></html>");
                out.close();
            }
            File file=(File)SageApi.GetElement(SegmentFiles,0);
            if (!file.exists() || !file.canWrite()) {
                printTitle(out,"Edit Show Info: Error", SageServlet.isTitleBroken(req));
                out.println("<div id=\"content\">");
                out.println("<h3>File: "+file.getPath()+" is not Writable</h3>");
                out.println("</div>");
                printMenu(req,out);
                out.println("</body></html>");
                out.close();
            }

            if ( req.getParameter("Update")==null){
                printForm(req,resp,airing,out);
            } else {
                editShow(req,resp,airing,file,out);
            }
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
    private String getDecodedParameter(HttpServletRequest req, String param) 
    throws Exception
    {
        String arg=req.getParameter(param);
        if ( arg==null)
            return "";
        arg=URLDecoder.decode(arg,charset);
        return arg;
    }

    /**
     * Parse an array of Air extra info to get 'Part n of m 
     * @param extraInfArr
     * @return int[2]
     */
    int[] getPartOfParts(String[] extraInfArr){

        try {
            String partofparts=SageApi.StringApi("LocalizeString", new Object[]{"Part_Of_Parts"});
            partofparts.replaceAll("\\{0\\}", "([0-9]+)");
            partofparts.replaceAll("\\{1\\}", "([0-9]+)");
            java.util.regex.Pattern p=java.util.regex.Pattern.compile(partofparts);
            for (int extraNum=0;extraNum<extraInfArr.length; extraNum++){
                java.util.regex.Matcher m=p.matcher(extraInfArr[extraNum]);
                if ( m.find()){
                    int[] retval=new int[2];
                    retval[0]=Integer.parseInt(m.group(1));
                    retval[1]=Integer.parseInt(m.group(2));
                    return retval;
                }
            }
        }catch (Exception e){
            log("failed to parse extra inf:\""+java.util.Arrays.asList(extraInfArr)+"\"",e);
        }
        return null;
    }
}

