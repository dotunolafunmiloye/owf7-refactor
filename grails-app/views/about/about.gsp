<!DOCTYPE html>
<!-- Yes, the image doesn't actually exist.  The pic is added via the CSS class. -->
<html>
<head></head>
<body>
<img src="./images/brand-logo.png" class="aboutImage" />
<p>${message}</p>
<p>${support}</p>
<p id="aboutInfo">
Version: ${appVersion}, Grails Version: ${grailsVersion}
<br>
<g:if test="${build_number != null}">Build Number: ${build_number}<br></g:if>
Build Date: ${today}<br>
<g:if test="${commit != null}">Commit: ${commit}</g:if>
</p>
</body>
</html>