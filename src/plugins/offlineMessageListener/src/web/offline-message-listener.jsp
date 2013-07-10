<%@ page
   import="org.jivesoftware.openfire.XMPPServer,com.angelystor.openfire.plugin.OfflineMessageListenerPlugin,
           org.jivesoftware.util.ParamUtils,
           java.util.HashMap,
           java.util.Map"
   errorPage="error.jsp"%>

<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt"%>

<%
	boolean save = request.getParameter("save") != null;	
	boolean enabled = ParamUtils.getBooleanParameter(request, "enabled", false);
	String secretKey = ParamUtils.getParameter(request, "secret_key");
	String postURL = ParamUtils.getParameter(request, "post_url");
    String appKey = ParamUtils.getParameter(request, "app_key");
    String appSecret = ParamUtils.getParameter(request, "app_secret");
    
	OfflineMessageListenerPlugin plugin = (OfflineMessageListenerPlugin) XMPPServer.getInstance().getPluginManager().getPlugin("offlinemessagelistener");

	Map<String, String> errors = new HashMap<String, String>();	
	
	if (save) 
	{
	  if (postURL == null || postURL.trim().length() < 1) 
	  {
	     errors.put("missingPostURL", "missingPostURL");
	  }
       
	  if (errors.size() == 0) 
	  {
	     plugin.setEnabled(enabled);
	     plugin.setSecretKey(secretKey);
	     plugin.setURL(postURL);
	     plugin.setAppKey(appKey);
	     plugin.setAppSecret(appSecret);
           
	     response.sendRedirect("offline-message-listener.jsp?settingsSaved=true");
	     return;
	  }		
	}
    
	enabled = plugin.isEnabled();
	secretKey = plugin.getSecretKey();
	postURL = plugin.getURL();
	appKey = plugin.getAppKey();
	appSecret = plugin.getAppSecret();
%>

<html>
	<head>
	  <title><fmt:message key="offline.title" /></title>
	  <meta name="pageID" content="offline-form"/>
	</head>
	<body>

    <form action="offline-message-listener.jsp?save" method="post">

    <div class="jive-contentBoxHeader"><fmt:message key="offline.options" /></div>
    <div class="jive-contentBox">
   
        <% if (ParamUtils.getBooleanParameter(request, "settingsSaved")) { %>
   
        <div class="jive-success">
            <table cellpadding="0" cellspacing="0" border="0">
            <tbody>
              <tr>
                 <td class="jive-icon"><img src="images/success-16x16.gif" width="16" height="16" border="0"></td>
                 <td class="jive-icon-label"><fmt:message key="offline.saved.success" /></td>
              </tr>
            </tbody>
            </table>
        </div>
   
        <% } %>
   
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
        <tbody>
          <tr>
             <td width="1%" align="center" nowrap><input type="checkbox" name="enabled" <%=enabled ? "checked" : "" %>></td>
             <td width="99%" align="left"><fmt:message key="offline.enable" /></td>
          </tr>
        </tbody>
        </table>
   
       <br><br>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
        <tbody>
         <tr>
             <td width="20%" valign="top"><fmt:message key="offline.ua.app_key" />:&nbsp;</td>
             <td><input size="50" type="text" name="app_key" value="<%= appKey %>"></td>
          </tr>
          <tr>
             <td valign="top"><fmt:message key="offline.ua.app_secret" />:&nbsp;</td>
             <td><input size="50" type="text" name="app_secret" value="<%= appSecret %>"></td>
          </tr>        
          <tr>
             <td valign="top"><fmt:message key="offline.secret_key" />:&nbsp;</td>
             <td><input size="25" type="text" name="secret_key" value="<%= secretKey %>"></td>
          </tr>
          <tr>
             <td valign="top"><fmt:message key="offline.post_url" />:&nbsp;</td>
             <td><input size="50" type="text" name="post_url" value="<%= postURL %>"></td>
             <% if (errors.containsKey("missingPostURL")) { %>
                <span class="jive-error-text"><fmt:message key="offline.post_url.missing" /></span>
             <% } %> 
          </tr>
        </tbody>
        </table>
    </div>
    <input type="submit" value="<fmt:message key="offline.button.save" />"/>
    </form>

    </body>
</html>
