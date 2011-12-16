/********************************************************************
 * SageCommands in JS -- load an image+refresh (or jump to error page)
 ********************************************************************/
function AiringCommand(command,targetHint,deleteReturnTo) {
   CmdImage=new Image();
   url=unescape(window.location.href);
   var qs = location.search.substring(1);
   var path=location.href;
   if ( qs != null && qs.length >1 ) {
		path=location.href.substring(0,location.href.indexOf(location.search))
		// remove any random=
		qs=qs.replace(/&*random=[0-9]+/g,"");
		// add a random=
		if ( qs.length > 1 )
		        qs=qs+"&";
		qs=qs+"random="+Math.floor(Math.random()*999999);
   } else {
		qs="random="+Math.floor(Math.random()*999999);
   }
   path=path+"?"+qs;

   
   if ( deleteReturnTo!=null ){
                path=path+'&deleteReturnTo='+deleteReturnTo;
   }
   if ( targetHint != null ) {
                path=path+'#'+targetHint;
   }

   CmdImage.onload=new Function('window.location.replace( "'+path+'" );');
   CmdImage.onerror=new Function('window.location="'+command+'";');
   CmdImage.src=command+"&RetImage=yes";
   return false;
}

/*
 edit show info
 */
function SelectEditMode(mode,enabled)
{
   for (var i = 0; i < document.EditShowForm.elements.length; i++) {
    if(document.EditShowForm.elements[i].name.match("^"+mode)){
      document.EditShowForm.elements[i].disabled = !enabled;
    }
  }
}

/********************************************************************
 * cookie handling
 ********************************************************************/

function GetCookie(name)
{
    var dc = document.cookie;
    var prefix = name + "=";
    var begin = dc.indexOf("; " + prefix);
    if (begin == -1)
    {
        begin = dc.indexOf(prefix);
        if (begin != 0) return null;
    }
    else
    {
        begin += 2;
    }
    var end = document.cookie.indexOf(";", begin);
    if (end == -1)
    {
        end = dc.length;
    }
    return unescape(dc.substring(begin + prefix.length, end));
}

function DeleteOptionsCookie(name)
{
    if (GetCookie(name))
    {
        document.cookie = name + "=" + 
            "; expires=Thu, 01-Jan-70 00:00:01 GMT";
    }
}
function SetCookie(name, value)
{
    document.cookie= name + "=" + escape(value) +
        "; expires=Tue, 01-Jan-2030 00:00:01 GMT";
}
function SetSessionCookie(name, value){
     document.cookie = name + "=" + escape(value);
}
/****************************************************************
 * multiple selection of checkboxes
 ****************************************************************/
function checkAll(state) {
  for (var i = 0; i < document.AiringsForm.elements.length; i++) {
    if(document.AiringsForm.elements[i].type == 'checkbox'){
      document.AiringsForm.elements[i].checked = state;
    }
  }
}
function checkAllSystemMessages(state) {
  for (var i = 0; i < document.AlertsForm.elements.length; i++) {
    if(document.AlertsForm.elements[i].type == 'checkbox'){
      document.AlertsForm.elements[i].checked = state;
    }
  }
}
function checkGroupChecked(checkbox) {
  
  var detail=checkbox.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode;
  var summ=detail.parentNode.firstChild;
  if ( detail.className!='Details' )
  	return;
  while ( summ != null 
          && ( summ.nodeType != 1
          || summ.tagName!='TABLE' 
          || !summ.id.match('^group.*_summ$')) ) {
  	summ=summ.nextSibling;
  }
  if ( summ==null )
	  return;
  var checked=true;
  var children=detail.childNodes;
  for (var child=0; child<children.length; child++) {
  	  if ( children[child].tagName=='DIV') {
	  	  var child_cb=children[child].getElementsByTagName("input")[0];
	      if ( child_cb !=null )
	         checked=checked&&child_cb.checked;
	  }
  }
  if ( summ.getElementsByTagName("input")[0]!=null)
	 summ.getElementsByTagName("input")[0].checked=checked;
  
}
function setGroupChecked(groupname,checked) {
  
  var detail=document.getElementById(groupname);
  var children=detail.childNodes;
  for (var child=0; child<children.length; child++) {
  	  if ( children[child].tagName=='DIV') {
	  	  var child_cb=children[child].getElementsByTagName("input")[0];
	  	  
	      if ( child_cb !=null )
	         child_cb.checked=checked;
	  }
  }  
}

/***************************************************************
 * javascript for showing/hiding folders of mediafiles
 **************************************************************/

function NullFunc() {
}

function hideAll(numgroups) {
	for (var i=1; i<=numgroups; i++) { 
		var detail=document.getElementById('group'+i);
		if (detail != null )
			detail.style.display='none';
	}
}
function expandAll(numgroups) {
	for (var i=1; i<=numgroups; i++) { 
		var detail=document.getElementById('group'+i);
		if (detail != null )
			detail.style.display='block';
	}
}
function hideAllMessageDetails(nummessages) {
	for (var i=1; i<=nummessages; i++) { 
		var detail=document.getElementById('message'+i+'detail');
		if (detail != null )
			detail.style.display='none';
	}
}
function expandAllMessageDetails(nummessages) {
	for (var i=1; i<=nummessages; i++) { 
		var detail=document.getElementById('message'+i+'detail');
		if (detail != null )
			detail.style.display='block';
	}
}
function showDetail(itemId) {
	var detail=document.getElementById(itemId);
	var summary=document.getElementById(itemId+'_summ');
	if ( detail.style.display=='block' ) {
		detail.style.display='none';
	} else {
		detail.style.display='block'
	}
	
	//summary.style.display = 'none';
	//detail.style.display='block';
}
function confirmAction(msg,action) {
	if (  confirm(msg) ){
		window.location=action;
	}
}
/*****************************************************************/
function showOptions() {
	var options=document.getElementById('options');
	if(options) {
		options.style.display='';
	}
}
function hideOptions() {
	var options=document.getElementById('options');
	if(options) {
		options.style.display='none';
	}
}	
/*****************************************************************/
function RemoveNonNumbers(ta) {
    origVal=ta.value; newVal='';
    for (i=0;i<origVal.length;i++) { 
        if (origVal.charAt(i)>='0' 
            && origVal.charAt(i)<='9')
           newVal += origVal.charAt(i);
    }
    ta.value=newVal;
}
/*****************************************************************/
function doMenu(MENU_ITEMS, MENU_POS) {
	if ( MENU_ITEMS==null ) { 
	   alert("Error in menu_items.js - check syntax");
	} else {
		// check for required JS functionality
		if ( document.body && document.body.style && document.getElementById ) {
			mymenu=new menu (MENU_ITEMS, MENU_POS);
		} else {
			// fallback menu based on nested <ul>
			document.write("<hr/>")
			printMenuList(MENU_ITEMS,"<h3>","</h3>");
			document.write("<hr/>")
		}
	}
}
function printMenuList(MENU_ITEMS,headeron,headeroff) {
	document.writeln("<ul>")
	for ( var i=0;i<MENU_ITEMS.length;i++) {
		if ( MENU_ITEMS[i]!=null) {
			if ( MENU_ITEMS[i][0]!=null ) {
				document.write("<li>"+headeron)
				if ( MENU_ITEMS[i][1] != null ){
					document.write('<a href="'+MENU_ITEMS[i][1]+'"' );
					if ( MENU_ITEMS[i][2] != null && MENU_ITEMS[i][2].tw != null )
						document.write('target="'+MENU_ITEMS[i][2].tw+'"' );
					if ( MENU_ITEMS[i][2] != null && MENU_ITEMS[i][2].title != null )
						document.write('title="'+MENU_ITEMS[i][2].title+'"' );
					document.write('>');
				}
				document.write(MENU_ITEMS[i][0]);
				if ( MENU_ITEMS[i][1] != null ){
					document.write("</a>");
				}
				document.write(headeroff+"</li>");
			}
			if ( MENU_ITEMS[i][3] != null ) {
				printMenuList(MENU_ITEMS[i].slice(3),"","");
			}
		}
	}
	document.writeln("</ul>")
}	