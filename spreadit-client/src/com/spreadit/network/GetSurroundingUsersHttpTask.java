package com.spreadit.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.util.Log;

class GetSurroundingUsersHttpTask extends AsyncTask<String, String, String> {
	/*
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	public AsyncResponse delegate=null;

	@Override
	protected String doInBackground(String... params)
	{
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(params[0]);
		HttpResponse response;

		String responseString = null;
		try {
			List<NameValuePair> requestParams = new LinkedList<NameValuePair>();
			requestParams.add(new BasicNameValuePair("server_id", params[1]));

			response = httpclient.execute(httpget);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
			} else {
				// Closes the connection.
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (ClientProtocolException e) {
			// TODO Handle problems..

		} catch (IOException e) {
			// TODO Handle problems..
		}
		return responseString;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		// Do anything with response..
		Log.d("tag", "Get surrounding users received");
		delegate.processGetSurroundingUsersFinish(result);		
	}


}
