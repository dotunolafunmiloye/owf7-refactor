<%@ page contentType="text/html; UTF-8" %>
<table style="height: 20px;	width: 100%;">
<tbody>
<tr>
<td style="background-color: yellow;font-size: 14px;text-align: center;	width: 100%;">
<b>${grailsApplication.config.bannerText?:'Configure banner using -Dozone.banner.text'}</b>
</td>
</tr>
</tbody>
</table>