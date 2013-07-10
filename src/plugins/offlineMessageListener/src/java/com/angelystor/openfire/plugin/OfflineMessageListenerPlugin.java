package com.angelystor.openfire.plugin;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.jivesoftware.openfire.OfflineMessageListener;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.util.JiveGlobals;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmpp.packet.Message;

/**
 * Plugin that hooks onto offline messages and forwards it to an external
 * web page
 *
 * @author angelystor
 */
public class OfflineMessageListenerPlugin implements Plugin, OfflineMessageListener
{
    private static Logger Log = Logger.getLogger(OfflineMessageListenerPlugin.class);

    private static final String SECRET_KEY = "plugin.offlinemessage.secret_key";
    private static final String POST_URL = "plugin.offlinemessage.post_url";
    private static final String ENABLED = "plugin.offlinemessage.enabled";
    private static final String UA_APP_KEY = "plugin.offlinemessage.ua.app_key";
    private static final String UA_APP_SECRET = "plugin.offlinemessage.ua.app_secret";    
    
    public OfflineMessageListenerPlugin()
    {
        
    }

    public void initializePlugin(PluginManager manager, File pluginDirectory) 
    {
        XMPPServer.getInstance().getOfflineMessageStrategy().addListener(this);
    }

    public void destroyPlugin() 
    {
        XMPPServer.getInstance().getOfflineMessageStrategy().removeListener(this);
    }

    /* (non-Javadoc)
     * @see org.jivesoftware.openfire.OfflineMessageListener#messageBounced(org.xmpp.packet.Message)
     */
    @Override
    public void messageBounced(Message message)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.jivesoftware.openfire.OfflineMessageListener#messageStored(org.xmpp.packet.Message)
     */
    @Override
    public void messageStored(Message message)
    {
        if (isEnabled())
        {
            if (message.getBody() != null)
            {
                Log.debug("Message: " + message);
                Log.debug("Sending push notification to " + message.getTo());
                                
                postToURL(message);
            }
        }
        
    }
    
    private void postToURL(Message message)
    {                
        PostMethod post = new PostMethod(getURL());
        
        NameValuePair[] data = 
            {
                new NameValuePair("access_token", getSecretKey()),
                new NameValuePair("to", message.getTo().getNode()),
                new NameValuePair("body", formatPushMessage(message)),
                new NameValuePair("from", message.getFrom().getNode())
            };
        Log.info("Posting to " + getURL());
        for (NameValuePair p : data)
        {
            Log.info(p);
        }
        post.setRequestBody(data);
        /*// because my java is crud and doesn't take UA cert !?!?
        String jsonPayload = null;
        try
        {
            jsonPayload = createUAPacket(message);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
            Log.error(e);
        }
        
        if (jsonPayload == null)
        {
            Log.info("Error in creating json payload from " + message);
            return;
        }
        
        try
        {
            post.setRequestEntity(new StringRequestEntity(jsonPayload, "application/json", "UTF-8"));
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
            Log.info(e);
        }
        */
        
        // execute method and handle any error responses.
        HttpClient httpClient = new HttpClient();
        try
        {
            UsernamePasswordCredentials creds = new UsernamePasswordCredentials(getAppKey(), getAppSecret());
            
            httpClient.getParams().setAuthenticationPreemptive(true);
            httpClient.getState().setCredentials(AuthScope.ANY, creds);
                       
            post.addRequestHeader("Proxy-Authorization", BasicScheme.authenticate(creds, "UTF-8"));
            post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            //post.addRequestHeader("Content-Type", "application/json; charset=UTF-8");


            int statusCode = httpClient.executeMethod(post);
            Log.info("Response: " + statusCode);
            
        }
        catch(HttpException e1)
        {
            Log.info(e1);
            e1.printStackTrace();
        }
        catch(IOException e1)
        {
            Log.info(e1);
            e1.printStackTrace();
        }
    }
    
    private String formatPushMessage(Message message)
    {
        StringBuffer sb = new StringBuffer();
        Log.info("Message: " + message);

        sb.append(message.getFrom().getNode());
        sb.append(": ");
        
        String body = message.getBody();
        
        if (body.length() > 80 - sb.length())
        {
            body = body.substring(0, 80 - sb.length() - 3);
            sb.append(body);
            sb.append("...");
        }
        else
        {
            sb.append(body);
        }
        
        return sb.toString();
    }
    
    private String createUAPacket(Message message) throws JSONException
    {        
        JSONObject root = new JSONObject();
        JSONObject aps = new JSONObject();
        aps.put("alert", formatPushMessage(message));
        aps.put("sound", "cat.caf");
        aps.put("badge", "+1");
        
        root.put("aps", aps);
        JSONArray aliases = new JSONArray();
        aliases.put(message.getTo().getNode());
        root.put("aliases", aliases);
        Log.info("Root:" + root);
        return root.toString();
    }
    
    public String getSecretKey()
    {
        return JiveGlobals.getProperty(SECRET_KEY, "Secret Key");
    }
    
    public void setSecretKey(String key)
    {
        JiveGlobals.setProperty(SECRET_KEY, key);
    }
    
    public String getURL()
    {
        return JiveGlobals.getProperty(POST_URL, "URL to post to");
    }
    
    public void setURL(String url)
    {
        JiveGlobals.setProperty(POST_URL, url);
    }
    
    public boolean isEnabled()
    {
        return JiveGlobals.getBooleanProperty(ENABLED);
    }
    
    public void setEnabled(boolean enabled)
    {
        JiveGlobals.setProperty(ENABLED, enabled ? "true" : "false");
    }
    
    public String getAppKey()
    {
        return JiveGlobals.getProperty(UA_APP_KEY, "Urban Airship Application Key");
    }
    
    public void setAppKey(String key)
    {
        JiveGlobals.setProperty(UA_APP_KEY, key);
    }
    
    public String getAppSecret()
    {
        return JiveGlobals.getProperty(UA_APP_SECRET, "Urban Airship Application Secret");
    }
    
    public void setAppSecret(String key)
    {
        JiveGlobals.setProperty(UA_APP_SECRET, key);
    }
    
}
