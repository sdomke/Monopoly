package com.monopoly.domke.sebastian.monopoly.helper;

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.util.Log;

public class SendSiteRequest {

	//Uri des Webservice um sich f�r eine Site Attendance anzumelden
	private final String sendRequestSiteAttendanceUri = "http://bastiswebservice.dnsdynamic.com:8080/temwebservice/requestsite/attendance";
	//Uri des Webservice um sich von einer Site Attendance abzumelden
	private final String sendRequestSiteDepartureUri = "http://bastiswebservice.dnsdynamic.com:8080/temwebservice/requestsite/departure";
	
	public SendSiteRequest(){
		
	}
	
	/*
	 * Anmelden einer Site Attendance �ber den Webservice
	 * 
	 * @param site = Gewaehlte Site
	 * 
	 * @param kID = K-ID des Mitarbeiters
	 * 
	 * @param time = Geplante Aufenthaltsdauer auf der Site
	 * 
	 * @param freeText = Optionale Anmerkungen des Mitarbeiters
	 * 
	 * @return true = Senden der Site Attendance an den Webservice erfolgreich
	 * 
	 * @return false = Senden der Site Attendance an den Webservice nicht erfolgreich
	 */
	public boolean sendRequestSiteAttendance(String site, String kId, String time, String freeText){
		JSONObject json = new JSONObject();
		
		try {
			
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used. 
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			
			DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);

		    HttpPost post = new HttpPost(sendRequestSiteAttendanceUri);
		    
		    //F�llen des JsonObjects mit den gewaehlten Informationen f�r den Site Attendance Request
		    json.put("site", site);
            json.put("kId", kId);
            json.put("time", time);
            json.put("freeText", freeText);
            
            StringEntity se = new StringEntity( json.toString());  
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);

		    // Execute request
		    HttpResponse response = httpclient.execute(post);

		    //Antwort des Webservice auf den Site Attendance Request
		    if(response!=null){
		    String resultSessionBean = EntityUtils.toString(response.getEntity());
		    Log.i("sendRequestSiteAttend", "Result request: " + resultSessionBean);
		    //Communicator.siteAttendance=true;
		    }
		    //Logging.info("sendRequestSiteAttendance", "Request Site Attendance complete");
	}
	catch (UnsupportedEncodingException e) {
		Log.e("log_tag", "SocketTimeoutException (SessionBean)" + e.toString());
		//Logging.error("SessionBean Error", "Session not successfully started.", e);

		return false;
        }
	catch (ClientProtocolException e) {
		Log.e("log_tag", "SocketTimeoutException (SessionBean)" + e.toString());
		//Logging.error("SessionBean Error", "Session not successfully started.", e);
        return false;
    }
	catch (SocketTimeoutException e)
	{
		Log.e("log_tag", "SocketTimeoutException (SessionBean)" + e.toString());
		//Logging.error("SessionBean Error", "Session not successfully started.", e);
		return false;
	}
	catch (ConnectTimeoutException e)
	{
		Log.e("log_tag", "ConnectTimeoutException (SessionBean)" + e.toString());
		//Logging.error("SessionBean Error", "Session not successfully started.", e);
		return false;
	}
	
	catch (Exception e) {
		Log.e("log_tag", "Error in http connection (SessionBean)" + e.toString());
		//Logging.error("SessionBean Error", "Session not successfully started.", e);
		return false;
	}
		return true;
		
	}
	
	/*
	 * Anmelden einer Site Attendance �ber den Webservice
	 * 
	 * @param site = Gewaehlte Site
	 * 
	 * @param kID = K-ID des Mitarbeiters
	 * 
	 * @param time = Geplante Aufenthaltsdauer auf der Site
	 * 
	 * @param freeText = Optionale Anmerkungen des Mitarbeiters
	 * 
	 * @return true = Senden der Site Attendance an den Webservice erfolgreich
	 * 
	 * @return false = Senden der Site Attendance an den Webservice nicht erfolgreich
	 */
	public boolean sendDepartureSiteAttendance(String site, String kId){
		JSONObject json = new JSONObject();
		
		try {
			
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used. 
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			
			DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);

		    HttpPost post = new HttpPost(sendRequestSiteDepartureUri);
		    
		    //F�llen des JsonObjekts mit den gewaehlten Informationen f�r den Site Departure Request
		    json.put("site", site);
            json.put("kId", kId);
            
            StringEntity se = new StringEntity( json.toString());  
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);

		    // Execute request
		    HttpResponse response = httpclient.execute(post);

		    //Antwort des Webservice auf den Site Departure Request
		    if(response!=null){
		    String resultSessionBean = EntityUtils.toString(response.getEntity());
		    Log.i("sendRequestSiteAttend", "Result request: " + resultSessionBean);
		    //Communicator.siteAttendance=false;
		    }
		    //Logging.info("sendRequestSiteAttendance", "Request Site Attendance complete");
	}
	catch (UnsupportedEncodingException e) {
		Log.e("log_tag", "SocketTimeoutException (SessionBean)" + e.toString());
		//Logging.error("SessionBean Error", "Session not successfully started.", e);
            
		return false;
        }
	catch (ClientProtocolException e) {
		Log.e("log_tag", "SocketTimeoutException (SessionBean)" + e.toString());
		//Logging.error("SessionBean Error", "Session not successfully started.", e);
        return false;
    }
	catch (SocketTimeoutException e)
	{
		Log.e("log_tag", "SocketTimeoutException (SessionBean)" + e.toString());
		//Logging.error("SessionBean Error", "Session not successfully started.", e);
		return false;
	}
	catch (ConnectTimeoutException e)
	{
		Log.e("log_tag", "ConnectTimeoutException (SessionBean)" + e.toString());
		//Logging.error("SessionBean Error", "Session not successfully started.", e);
		return false;
	}
	
	catch (Exception e) {
		Log.e("log_tag", "Error in http connection (SessionBean)" + e.toString());
		//Logging.error("SessionBean Error", "Session not successfully started.", e);
		return false;
	}
		return true;
	}
	
}
