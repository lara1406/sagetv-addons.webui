<%
/*
*      Copyright 2012 Battams, Derek
*      
*      This is a modified version of the code; original code by nielm, et al.
*
*       Licensed under the Apache License, Version 2.0 (the "License");
*       you may not use this file except in compliance with the License.
*       You may obtain a copy of the License at
*
*          http://www.apache.org/licenses/LICENSE-2.0
*
*       Unless required by applicable law or agreed to in writing, software
*       distributed under the License is distributed on an "AS IS" BASIS,
*       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*       See the License for the specific language governing permissions and
*       limitations under the License.
*/
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
        <head>
                <title>${request.getAttribute('title') ?: 'SageTV Web Interface'}</title>
                <link rel="stylesheet"  type="text/css" media="all" href="/sage/sage_all.css"/>
                <link rel="stylesheet"  type="text/css" media="print" href="/sage/sage_print.css"/>
                <link rel="stylesheet"  type="text/css" media="handheld" href="/sage/sage_handheld.css"/>
                <link rel="Shortcut Icon" href="/sage/favicon.ico" type="image/x-icon"/>
                <link rel="stylesheet" type="text/css" href="/sage/ddsmoothmenu.css" />
				<link rel="stylesheet" type="text/css" href="/sage/ddsmoothmenu-v.css" />

				<script type="text/javascript" src="/sage/jquery.min.js"></script>
				<script type="text/javascript" src="/sage/ddsmoothmenu.js">
					/***********************************************
					* Smooth Navigational Menu- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
					* This notice MUST stay intact for legal use
					* Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code
					***********************************************/
				</script>

				<script type="text/javascript">
					ddsmoothmenu.init({
						mainmenuid: "smoothmenu1", //menu DIV id
						orientation: 'h', //Horizontal or vertical menu: Set to "h" or "v"
						classname: 'ddsmoothmenu', //class added to menu's outer DIV
						//customtheme: ["#1c5a80", "#18374a"],
						contentsource: ["menuContainer", "/sage/menu.groovy"]
					})
				</script>
                
                <script type="text/javascript" src="/sage/sage.js"></script>
                <% for(def url : request.getAttribute('scripts')) { %>
                	<script type="text/javascript" src="$url"></script>
                <% } %>
        </head>
        <body>
        		<div id="menuContainer"></div>
                <div id="title">
                        <h1><a href="/sage/index.html" title="home"><img id="logoimg" src="/sage/sagelogo.gif" alt="SageTV logo" title="Home Screen" border="0"/></a>${request.getAttribute('pageTitle') ?: ''}</h1>
                </div>
                <div id="content">