//==============================================================================
// Parse URL into object-map
//==============================================================================
var url = new Object();
{ 
	var qs = location.search.substring(1);
	var nv = qs.split('&');
	for(i = 0; i < nv.length; i++)
	{
	  eq = nv[i].indexOf('=');
	  url[nv[i].substring(0,eq).toLowerCase().replace("nol_","")] = decodeURIComponent(nv[i].substring(eq + 1));
	}
}


//==============================================================================
// try to determine file type.... 
//==============================================================================
var fileext="unknown";
// first use filename hint
if ( url.filename && url.filename.lastIndexOf(".") >0 )	{
	fileext=url.filename.substring(url.filename.lastIndexOf(".")+1,url.filename.length).toLowerCase();
} else if ( url.clipurl ) {
	// look for a filename in clipurl
	if ( url.clipurl.toLowerCase().match("\.[a-z0-9]+$") ) {
		fileext=url.clipurl.substring(url.clipurl.lastIndexOf(".")+1,url.clipurl.length).toLowerCase();
	}
}


function HtmlIze(text) {
	return text.replace(/&/g, "&amp;").replace(/</g,"&lt;").replace(/>/g, "&gt;").replace(/\+/g, " ");
}

function MakeAbsoluteURL(clipurl) {
	if ( clipurl.match("^[a-z]+://") == null )
		clipurl=location.href.substr(0,location.href.indexOf('/',8))+clipurl;
	return clipurl;
}

function MakeMediaFilePlaylist(mediafileid,pltype,fntype) {
	if ( url.transcodeopts!=null )
		return MakeAbsoluteURL("/sagepublic/PlaylistGenerator?Command=Generate&pltype="+pltype+"&fntype="+fntype+"&MediaFileId="+mediafileid+"&TranscodeOpts="+encodeURIComponent(url.transcodeopts));
	else
		return MakeAbsoluteURL("/sagepublic/PlaylistGenerator?Command=Generate&pltype="+pltype+"&fntype="+fntype+"&MediaFileId="+mediafileid);
}

function MakeURLPlaylist(url,pltype) {
	return MakeAbsoluteURL("/sagepublic/PlaylistGenerator?Command=Generate&fntype=url&pltype="+pltype+"&Url="+encodeURIComponent(url));
}


//==============================================================================
// Browser Detect
//==============================================================================
var detect = navigator.userAgent.toLowerCase();
var OS,browser,version,total,thestring;

if (checkIt('konqueror'))
{
	browser = "Konqueror";
	OS = "Linux";
}
else if (checkIt('safari')) browser = "Safari"
else if (checkIt('omniweb')) browser = "OmniWeb"
else if (checkIt('opera')) browser = "Opera"
else if (checkIt('webtv')) browser = "WebTV";
else if (checkIt('icab')) browser = "iCab"
else if (checkIt('msie')) browser = "Internet Explorer"
else if (checkIt('firefox')) browser = "Firefox"
else if (!checkIt('compatible'))
{
	browser = "Mozilla"
	version = detect.charAt(8);
}
else browser = "An unknown browser";

if (!version) version = detect.charAt(place + thestring.length);

if (!OS)
{
	if (checkIt('linux')) OS = "Linux";
	else if (checkIt('x11')) OS = "Unix";
	else if (checkIt('mac')) OS = "Mac"
	else if (checkIt('win')) OS = "Windows"
	else OS = "an unknown operating system";
}

function checkIt(string)
{
	place = detect.indexOf(string) + 1;
	thestring = string;
	return place;
}

//alert(browser+version);
