<@ webserver/header.gsp @>
<%
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
<@ webserver/footer.gsp @>
        