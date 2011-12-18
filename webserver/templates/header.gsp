<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
        <head>
                <title>${request.getAttribute('title') ?: 'SageTV Web Interface'}</title>
                <link rel="stylesheet"  type="text/css" media="all" href="/sage/sage_all.css"/>
                <link rel="stylesheet"  type="text/css" media="print" href="/sage/sage_print.css"/>
                <link rel="stylesheet"  type="text/css" media="handheld" href="/sage/sage_handheld.css"/>
                <link rel="Shortcut Icon" href="/sage/favicon.ico" type="image/x-icon"/>
                <script type="text/javascript" src="/sage/sage.js"></script>
        </head>
        <body>
                <div id="title">
                        <h1><a href="/sage/index.html" title="home"><img id="logoimg" src="/sage/sagelogo.gif" alt="SageTV logo" title="Home Screen" border="0"/></a>${request.getAttribute('pageTitle') ?: ''}</h1>
                </div>
                <div id="content">