package com.monopoly.domke.sebastian.monopoly.database;

import android.net.ParseException;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class WebDatabaseHandler {
	
	private String result = null;
	private InputStream is = null;
	private StringBuilder sb = null;
	private DatabaseHandler db;
	public boolean verbundenSite = false;
	public boolean verbundenTraining = false;
	private String resultSessionBean;
	
	//Uri des Webservice um eine SessionBean zu erhalten
	private final String sessionBeanUri = "http://bastiswebservice.dnsdynamic.com:8080/temwebservice/user";
	//Uri des Webservice um die Datenbankeintr�ge der Site Inductions des Mitarbeiters zu erhalten 
	private final String getSiteInductionUri = "http://bastiswebservice.dnsdynamic.com:8080/temwebservice/site/list";
	//Uri des Webservice um die Datenbankeintr�ge der Training Courses des Mitarbeiters zu erhalten 
	private final String getTrainingCoursesUri = "http://bastiswebservice.dnsdynamic.com:8080/temwebservice/training/list";
	
	public WebDatabaseHandler(DatabaseHandler dbHandler) {
		db = dbHandler;
	}
	
	/*
	 * Verbindung zum Webservice mit dem Benutzernamen und Passwort des Mitarbeiters
	 * 
	 * @param user = Benutzername des Mitarbeiters
	 * 
	 * @param pw = Passwort des Mitarbeiters
	 * 
	 * @return true = SessionBean des Webservice erhalten
	 * 
	 * @return false = Verbindung zum Webservice fehlgeschlagen
	 */
	public boolean sessionBean(String user, String pw){
		
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

		    HttpPost post = new HttpPost(sessionBeanUri);

		    // Setup form data
		    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    nameValuePairs.add(new BasicNameValuePair("user", user));
		    nameValuePairs.add(new BasicNameValuePair("pw", pw));
		    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		    // Execute request
		    HttpResponse response = httpclient.execute(post);

		    //Response of the Webservice
		    resultSessionBean = EntityUtils.toString(response.getEntity());
		    
	}
	catch (UnsupportedEncodingException e) {
            // writing error to Log
		Log.e("log_tag", "SocketTimeoutException (SessionBean)" + e.toString());
            return false;
        }
	catch (ClientProtocolException e) {
        // writing exception to log
		Log.e("log_tag", "SocketTimeoutException (SessionBean)" + e.toString());
        return false;
    }
	catch (SocketTimeoutException e)
	{
		Log.e("log_tag", "SocketTimeoutException (SessionBean)" + e.toString());
		return false;
	}
	catch (ConnectTimeoutException e)
	{
		Log.e("log_tag", "ConnectTimeoutException (SessionBean)" + e.toString());
		return false;
	}
	
	catch (Exception e) {
		Log.e("log_tag", "Error in http connection (SessionBean)" + e.toString());
		return false;
	}
		return true;
		
	}
	
	/*
	 * Abruf der Site Inductions �ber den Webservice
	 * 
	 * @return true = Datenbankabfrage erfolgreich
	 * 
	 * @return false = Datenbankabfrage fehlgeschlagen
	 */
	public boolean getSiteInduction(){

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
				
				DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
				
				HttpGet getRequest = new HttpGet(
					getSiteInductionUri);
				getRequest.addHeader("authtoken", resultSessionBean);
		 
				HttpResponse response = httpClient.execute(getRequest);
		 
				
				if (response.getStatusLine().getStatusCode() != 200) {
					throw new RuntimeException("Failed : HTTP error code : "
					   + response.getStatusLine().getStatusCode());
				}
				
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
				
				verbundenSite=true;
			} 
			 catch (UnsupportedEncodingException e) {
		            // writing error to Log
		            e.printStackTrace();
		            verbundenSite=false;
		            return false;
		        }
			catch (ClientProtocolException e) {
	            // writing exception to log
	            e.printStackTrace();
	            verbundenSite=false;
	            return false;
	        }
			catch (SocketTimeoutException e)
			{
				Log.e("log_tag", "SocketTimeoutException" + e.toString());
				verbundenSite=false;
				return false;
			}
			catch (ConnectTimeoutException e)
			{
				Log.e("log_tag", "ConnectTimeoutException" + e.toString());
				verbundenSite=false;
				return false;
			}
			
			catch (Exception e) {
				Log.e("log_tag", "Error in http connection" + e.toString());
				verbundenSite=false;
				return false;
			}
			
			if(verbundenSite == true){
			// convert response to string
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "utf-8"), 8);
				sb = new StringBuilder();
				sb.append(reader.readLine() + "\n");
				String line = "0";
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
			} catch (Exception e) {
				Log.e("log_tag", "Error converting result " + e.toString());
				return false;
			}
			

			// paring data (JsonObject to siteInduction)

			try {
				JSONArray jArray = new JSONArray(result);
				JSONObject json_data = null;
				for (int i = 0; i < jArray.length(); i++) {
					json_data = jArray.getJSONObject(i);

					/*
					db.addSiteInduction(new SiteInduction(json_data
							.getInt("_site_induction_id"), json_data.getString("_site_name"),
							convertBoolean(json_data.getBoolean("_site_induction_complete")),
							convertBoolean(json_data.getBoolean("_site_induction_required")),
							json_data.getString("_induction_held_until"),
							json_data.getString("_site_position")));
							*/

				}
			} catch (JSONException e1) {
				return false;
			} catch (ParseException e1) {
				e1.printStackTrace();
				return false;
			}
		}
		db.close();
		return true;
	}

	
	public boolean getTrainingCourses() {

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
				
				DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
				HttpGet getRequest = new HttpGet(
					getTrainingCoursesUri);
				getRequest.addHeader("authtoken", resultSessionBean);
				
				HttpResponse response = httpClient.execute(getRequest);
		 
				
				if (response.getStatusLine().getStatusCode() != 200) {
					throw new RuntimeException("Failed : HTTP error code : "
					   + response.getStatusLine().getStatusCode());
				}
				
				
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
				
				verbundenSite=true;
			} catch (UnsupportedEncodingException e) {
	            // writing error to Log
	            e.printStackTrace();
	            verbundenSite=false;
	            return false;
	        }
		catch (ClientProtocolException e) {
            // writing exception to log
            e.printStackTrace();
            verbundenSite=false;
            return false;
        }
		catch (SocketTimeoutException e)
		{
			Log.e("log_tag", "SocketTimeoutException" + e.toString());
			verbundenSite=false;
			return false;
		}
		catch (ConnectTimeoutException e)
		{
			Log.e("log_tag", "ConnectTimeoutException" + e.toString());
			verbundenSite=false;
			return false;
		}
		
		catch (Exception e) {
			Log.e("log_tag", "Error in http connection" + e.toString());
			verbundenSite=false;
			return false;
		}

			if(verbundenSite == true){
			// convert response to string
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "utf-8"), 8);
				sb = new StringBuilder();
				sb.append(reader.readLine() + "\n");
				String line = "0";
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
			} catch (Exception e) {
				Log.e("log_tag", "Error converting result " + e.toString());
				return false;
			}

			// paring data (JsonObject to trainingCourses)

			try {
				JSONArray jArray = new JSONArray(result);
				JSONObject json_data = null;
				for (int i = 0; i < jArray.length(); i++) {
					json_data = jArray.getJSONObject(i);

					/*
					db.addTrainingCourse(new TrainingCourses(json_data
							.getInt("_training_courses_id"), json_data.getString("_course_name"),
							convertBoolean(json_data.getBoolean("_required")), json_data
									.getString("_completed_date"), json_data
									.getString("_expire_date"), json_data
									.getString("_renewable_date"), json_data
									.getString("_certificate_location"),
							json_data.getString("_course_description"), convertBoolean(json_data
									.getBoolean("_refresh_period_month")), json_data
									.getString("_training_course_country")));
					*/
				}
			} catch (JSONException e1) {
				return false;
			} catch (ParseException e1) {
				e1.printStackTrace();
				return false;
			}
		}
		db.close();
		return true;
	}
	
	public String convertBoolean(boolean value){
		
		if(value == true){
			return "Yes";
		}
		else if(value == false){
			return "No";
		}
		else{
			return "";
		}
	}
}
