<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>Denied</title>

  <style type="text/css">
      body {
          background-image: url(images/logout/wfBigBG.jpg);
      }
  </style>

  <script type="text/javascript">
    Ext.BLANK_IMAGE_URL = '';

    Ext.onReady(function() {
      //check current location to see if we failed from /owf/admin if so try to redirect to main OWF
      if (window.location.href.match(new RegExp('^.*${request.contextPath}\/admin[\/\?]?.*$'))) {
        Ext.MessageBox.alert('Authorization Error',
                'You are not authorized to access this page. You will be redirected to your default dashboard.',
                function redirectToOzone() {
                  window.location.href = '${request.contextPath}'
                });
      }
      else {
        //failed while accessing something else just show a error dialog
        Ext.MessageBox.show({
            title: 'Authorization Error',
            modal: true,
            closable: false,
            msg: 'You are not authorized to access this page.'
        });
      }
    });
  </script>

</head>
<body>
</body>

</html>
