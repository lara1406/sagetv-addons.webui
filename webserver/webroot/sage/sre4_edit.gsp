<@ webserver/templates/header.gsp @>
<%
/*
*      Copyright 2011 Battams, Derek
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
        import sagex.api.*
        def airing = request.getAttribute('airing')
%>
<h2>${AiringAPI.GetAiringTitle(airing)}: ${ShowAPI.GetShowEpisode(airing)}</h2>
<form method="POST" action="sre4.groovy" enctype="application/x-www-form-urlencoded">
        <input type="hidden" name="a" value="update" />
        <input type="hidden" name="id" value="${params['id']}" />
        <span><p>Title</p><p><input type="text" name="title" value="${AiringAPI.GetAiringTitle(airing)}" /></p></span>
        <span><p>Team 1</p><p><input type="text" name="team1_old" value="${request.getAttribute('team1') ?: ''}" /> becomes <input type="text" name="team1_new" value="${request.getAttribute('team1') ?: ''}" /></p></span>
        <span><p>Team 2</p><p><input type="text" name="team2_old" value="${request.getAttribute('team2') ?: ''}" /> becomes <input type="text" name="team2_new" value="${request.getAttribute('team2') ?: ''}" /></p></span>
        <% if(Boolean.parseBoolean(request.getAttribute('global'))) { %>
        <span>Add global override? <input type="checkbox" name="global" /></span>
        <% } %>
        <input type="submit" name="submit" value="Update" />
</form>
<@ webserver/templates/footer.gsp @>
        