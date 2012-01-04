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
import net.sf.sageplugins.webserver.Version
%>
                        <hr/>
                        <p>Page generated at: ${new Date()}
                        <br/>Sage Webserver version ${Version.VERSION}

                        <br/><a href="http://validator.w3.org/check?uri=referer"><img  src="valid-xhtml10.gif"  alt="Valid XHTML 1.0!" height="31" width="88" /></a>

                        <a href="http://jigsaw.w3.org/css-validator/"><img  src="valid-css.gif"  alt="Valid CSS2!" height="31" width="88" /></a>
                        <a href="https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=ZPBAK9WRNH2BQ"><img src="//www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif" /></a>
</p>
                </div>
                <!-- start menu bar -->
                <script language="JavaScript" type="text/javascript"><!--//     var MENU_ITEMS = null; //--></script>
                <script language="JavaScript" type="text/javascript" src="/sage/menu_core.js"></script>
                <script language="JavaScript" type="text/javascript" src="/sage/menu_items.js"></script>
                <script language="JavaScript" type="text/javascript" src="/sage/menu_style.js"></script>
                <script language="JavaScript" type="text/javascript">
                <!--//
                        if ( MENU_ITEMS==null ) {
                                alert("Error in menu_items.js - check syntax");
                        } else {
                                doMenu(MENU_ITEMS, MENU_POS);
                        }
                //-->
                </script>
        </body>
</html>
