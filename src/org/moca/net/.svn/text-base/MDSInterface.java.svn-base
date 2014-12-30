package org.moca.net;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.moca.Constants;
import org.moca.db.Event;
import org.moca.db.MocaDB.ImageSQLFormat;
import org.moca.db.MocaDB.ProcedureSQLFormat;
import org.moca.db.MocaDB.SavedProcedureSQLFormat;
import org.moca.db.MocaDB.SoundSQLFormat;
import org.moca.procedure.Procedure;
import org.moca.procedure.ProcedureElement;
import org.moca.procedure.ProcedureParseException;
import org.moca.procedure.ProcedureElement.ElementType;
import org.moca.util.MocaUtil;
import org.moca.util.UserDatabase;
import org.xml.sax.SAXException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * Interface for uploading to the Moca Dispatch Server.
 * 
 * This is where all of the packetization and http posting occurs.  Other than
 * some database interactions, it is fairly independent of Android.
 * 
 * The process for uploading a procedure is as follows (item number two takes
 * place on a remote server, the other steps take place in the code in this
 * source file):
 * 
 * 1) Post question/response pairs from completed procedure via http, tagging it
 *    with procedure, patient, and phone IDs.
 * 2) Moca Dispatch Server (MDS) parses the questions to see if they include any
 *    binary elements (i.e. a page in the procedure that asks to take a
 *    picture). If there are pending binary uploads, MDS knows to expect them
 *    and does not send the completed upload to OpenMRS until all parts are
 *    received.
 * 3) For each binary element, Moca uploads chunks of the element to the
 *    MDS. The size of these chunks starts at a default size. Each chunk is
 *    tagged with a procedure, patient, and phone ID as well as an element
 *    identifier and the start and end byte numbers (corresponding to the chunk
 *    location).
 * 4) If the first chunk successfully uploads, the chunk size for the next chunk
 *    transmission doubles. If the post fails, the chunk size halves.
 * 5) If the chunk size falls below a default "give up" threshold, the procedure
 *    is tagged as not- finished-uploading, and Moca waits to transmit the rest
 *    of the completed procedure at a later time. If the entire binary element
 *    is successfully transmitted, it moves on to the next element.
 * 6) It repeats steps 3-5 for subsequent elements, but instead of starting at
 *    the default chunk size for each transmission, it now has knowledge about
 *    the connection quality and uses the last successful transmission size from
 *    the last binary element as a starting point.
 */
public class MDSInterface {
	public static final String TAG = MDSInterface.class.toString();

	public static String[] savedProcedureProjection = new String[] {
		SavedProcedureSQLFormat._ID, SavedProcedureSQLFormat.PROCEDURE_ID,
		SavedProcedureSQLFormat.PROCEDURE_STATE, SavedProcedureSQLFormat.FINISHED,
		SavedProcedureSQLFormat.GUID, SavedProcedureSQLFormat.UPLOADED };

	private static String constructValidateCredentialsURL(String mdsURL) {
		return mdsURL + Constants.VALIDATE_CREDENTIALS_PATTERN;
	}

	private static String constructProcedureSubmitURL(String mdsURL) {
		return mdsURL + Constants.PROCEDURE_SUBMIT_PATTERN;
	}

	private static String constructBinaryChunkSubmitURL(String mdsURL) {
		return mdsURL + Constants.BINARYCHUNK_SUBMIT_PATTERN;
	}

	private static String constructBinaryChunkHackSubmitURL(String mdsURL) {
		return mdsURL + Constants.BINARYCHUNK_HACK_SUBMIT_PATTERN;
	}

	private static String constructDatabaseDownloadURL(String mdsURL) {
		return mdsURL + Constants.DATABASE_DOWNLOAD_PATTERN;
	}
	
	private static String constructUserInfoURL(String mdsURL, String id) {
		return mdsURL + Constants.USERINFO_DOWNLOAD_PATTERN + id + "/";
	}
	
	private static String constructEventLogUrl(String mdsUrl) {
		return mdsUrl + Constants.EVENTLOG_SUBMIT_PATTERN;
	}
	
	private static String checkMDSUrl(String mdsUrl) {
		if ("http://moca.media.mit.edu/mds".equals(mdsUrl)) {
			return "http://demo.sanamobile.org/mds";
		}
		return mdsUrl;
	}


	/**
	 * Posts the text responses from a procedure to the Moca Dispatch Server.selection
	 * 
	 * We don't packetize the raw text responses since, generally speaking, the total
	 * transmission size will be fairly small (probably less than the default starting 
	 * packet size).
	 * 
	 * @return true if upload succeeds, otherwise false
	 */
	private static boolean postResponses(Context c, String savedProcedureGuid, String jsonResponses) {
		boolean result = false;

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
		String mdsURL = preferences.getString(Constants.PREFERENCE_MDS_URL,
				Constants.DEFAULT_DISPATCH_SERVER);
		mdsURL = checkMDSUrl(mdsURL);
		String phoneId = preferences.getString("s_phone_name", Constants.PHONE_ID);
		String username = preferences.getString(Constants.PREFERENCE_EMR_USERNAME, Constants.DEFAULT_USERNAME);
		String password = preferences.getString(Constants.PREFERENCE_EMR_PASSWORD, Constants.DEFAULT_PASSWORD);
		String sBandwidth = preferences.getString("s_network_bandwidth", "");
		String proxyHost = preferences.getString(Constants.PREFERENCE_PROXY_HOST, "");
		String sProxyPort = preferences.getString(Constants.PREFERENCE_PROXY_PORT, "0");
		int proxyPort = 0;
		try {
			if (!"".equals(sProxyPort)) 
				proxyPort = Integer.parseInt(sProxyPort);
		} catch(NumberFormatException e) {
			Log.w(TAG, "Invalid proxy port: " + sProxyPort);
		}

		float bandwidth = Constants.ESTIMATED_NETWORK_BANDWIDTH;
		try{
			bandwidth = Float.parseFloat(sBandwidth);
		} catch(NumberFormatException e) {

		}


		// Estimate bytes of this request.
		int bytes = jsonResponses.length() + savedProcedureGuid.length()
		+ "0".length() + "procedure_guid".length()
		+ "savedproc_guid".length() + "phone".length()
		+ "username".length() + "password".length()
		+ "responses".length() + username.length()
		+ password.length() + phoneId.length();

		int timeout = Constants.getTimeoutForBandwidth(bandwidth, bytes);
		Log.i(TAG, "Setting timeout to " + timeout + " for an estimated " + bytes + " byte upload.");

		PostMethod post = new PostMethod(constructProcedureSubmitURL(mdsURL));
		post.addParameter("savedproc_guid", savedProcedureGuid);
		post.addParameter("procedure_guid", Integer.toString(0));
		post.addParameter("phone", phoneId);
		post.addParameter("username", username);
		post.addParameter("password", password);
		post.addParameter("responses", jsonResponses);

		HttpClient client = new HttpClient();
		
		// If there's a proxy enabled, use it.
		if (!"".equals(proxyHost) && proxyPort != 0) {
			Log.i(TAG, "Setting proxy to " + proxyHost + ":" + proxyPort);
			HostConfiguration hc = new HostConfiguration();
			hc.setProxy(proxyHost, (int)proxyPort);
			client.setHostConfiguration(hc);
		}

		try {
			int status = client.executeMethod(post); 
			Log.i(TAG, "postResponses got response code " +  status);

			char buf[] = new char[20560];
			Reader reader = new InputStreamReader(post.getResponseBodyAsStream());
			int total = reader.read(buf, 0, 20560);
			String responseString = new String(buf);
			Log.i(TAG, "Received from MDS:" + responseString);
			Gson gson = new Gson();
			MDSResult response = gson.fromJson(responseString, MDSResult.class);

			result = response.succeeded();
			Log.i(TAG, "MDS reports " + (result ? "success" : "failure") + " for procedure text for " + savedProcedureGuid);

		} catch (IOException e1) {
			Log.e(TAG, e1.toString());
			result = false;
		} catch (JsonParseException e) {
			Log.e(TAG, "postResponses: While parsing MDS response, got JSON parse exception:" + e);
			result = false;
		} finally {
			post.releaseConnection();
		}
		return result;
	}

	private static boolean postBinaryAsEncodedText(Context c, String savedProcedureId, String elementId, String fileGuid, 
			ElementType type, int fileSize, int start, int end, byte byte_data[]) {
		boolean result = false;

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
		String mdsUrl = preferences.getString(Constants.PREFERENCE_MDS_URL, 
				Constants.DEFAULT_DISPATCH_SERVER); 
		mdsUrl = checkMDSUrl(mdsUrl);
		String sBandwidth = preferences.getString("s_network_bandwidth", "");
		String proxyHost = preferences.getString(Constants.PREFERENCE_PROXY_HOST, "");
		String sProxyPort = preferences.getString(Constants.PREFERENCE_PROXY_PORT, "0");
		int proxyPort = 0;
		try {
			if (!"".equals(sProxyPort)) 
				proxyPort = Integer.parseInt(sProxyPort);
		} catch(NumberFormatException e) {
			Log.w(TAG, "Invalid proxy port: " + sProxyPort);
		}
		
		float bandwidth = Constants.ESTIMATED_NETWORK_BANDWIDTH;
		try{
			bandwidth = Float.parseFloat(sBandwidth);
		} catch(NumberFormatException e) {
		}

		// Estimate bytes of this request.
		int bytes = "procedure_guid".length() + "element_id".length()
		+ "binary_guid".length() + "element_type".length()
		+ "file_size".length() + "byte_start".length()
		+ "byte_end".length() + "byte_data".length()
		+ savedProcedureId.length() + elementId.length()
		+ fileGuid.length() + type.toString().length()
		+ Integer.toString(fileSize).length()
		+ Integer.toString(start).length()
		+ Integer.toString(end).length()
		+ type.getFilename().length() + byte_data.length;

		int timeout = Constants.getTimeoutForBandwidth(bandwidth, bytes);

		Log.i(TAG, "Setting timeout to " + timeout + " for an estimated " + bytes + " byte upload.");

		PostMethod post = new PostMethod(constructBinaryChunkHackSubmitURL(mdsUrl));

		post.addParameter(new NameValuePair("procedure_guid", savedProcedureId));
		post.addParameter(new NameValuePair("element_id", elementId));
		post.addParameter(new NameValuePair("binary_guid", fileGuid));
		post.addParameter(new NameValuePair("element_type", type.toString()));
		post.addParameter(new NameValuePair("file_size", Integer.toString(fileSize)));
		post.addParameter(new NameValuePair("byte_start", Integer.toString(start)));
		post.addParameter(new NameValuePair("byte_end", Integer.toString(end)));

		// Encode byte_data in Base64
		byte[] encoded_data = new Base64().encode(byte_data);
		post.addParameter(new NameValuePair("byte_data", new String(encoded_data)));

		HttpClient client = new HttpClient();
		
		// If there's a proxy enabled, use it.
		if (!"".equals(proxyHost) && proxyPort != 0) {
			Log.i(TAG, "Setting proxy to " + proxyHost + ":" + proxyPort);
			HostConfiguration hc = new HostConfiguration();
			hc.setProxy(proxyHost, (int)proxyPort);
			client.setHostConfiguration(hc);
		}

		try {
			int status = client.executeMethod(post); 
			Log.i(TAG, "postBinaryChunkHack got response code " +  status);

			Reader reader = new InputStreamReader(post.getResponseBodyAsStream());
			Gson gson = new Gson();
			MDSResult response = gson.fromJson(reader, MDSResult.class);
			Log.i(TAG, "MDS reports " + (response.succeeded() ? "success" : "failure") + " for " + savedProcedureId + ":" + fileGuid );
			result = response.succeeded();
			/*resultData = response.getData();

            if (!result && resultData == "Incorrect openMRS Password!") {
            	Intent passwordFeedback = new Intent(c, ProcedureRunner.class);
            	passwordFeedback.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            	passwordFeedback.addFlags(1);
            	((Activity) c).startActivity(passwordFeedback);
            }*/
		} catch(FileNotFoundException e) {
			Log.e(TAG, "postBinary got exception : " + e.toString());
			result = false;
		} catch(Exception e) {
			Log.e(TAG, "postBinary got exception : " + e.toString());
			result = false;
		} finally {
			// TODO Maybe we could save this across uploads?
			post.releaseConnection();
		}
		return result;
	}

	private static class BytePartSource implements PartSource {
		private String filename;
		private byte[] data;

		public BytePartSource(byte[] data, String filename) {
			this.data = data;
			this.filename = filename;
		}

		public InputStream createInputStream() throws IOException {
			return new ByteArrayInputStream(data);
		}

		public String getFileName() {
			return filename;
		}

		public long getLength() {
			return data.length;
		}

	}

	private static boolean postBinaryAsFile(Context c, String savedProcedureId, String elementId, String fileGuid, 
			ElementType type, int fileSize, int start, int end, byte byte_data[]) {
		boolean result = false;

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
		String mdsUrl = preferences.getString(Constants.PREFERENCE_MDS_URL, 
				Constants.DEFAULT_DISPATCH_SERVER);
		mdsUrl = checkMDSUrl(mdsUrl);
		String sBandwidth = preferences.getString("s_network_bandwidth", "");
		String proxyHost = preferences.getString(Constants.PREFERENCE_PROXY_HOST, "");
		String sProxyPort = preferences.getString(Constants.PREFERENCE_PROXY_PORT, "0");
		int proxyPort = 0;
		try {
			if (!"".equals(sProxyPort)) 
				proxyPort = Integer.parseInt(sProxyPort);
		} catch(NumberFormatException e) {
			Log.w(TAG, "Invalid proxy port: " + sProxyPort);
		}
		
		float bandwidth = Constants.ESTIMATED_NETWORK_BANDWIDTH;
		try{
			bandwidth = Float.parseFloat(sBandwidth);
		} catch(NumberFormatException e) {
		}

		// Estimate bytes of this request.
		int bytes = "procedure_guid".length() + "element_id".length()
		+ "binary_guid".length() + "element_type".length()
		+ "file_size".length() + "byte_start".length()
		+ "byte_end".length() + "byte_data".length()
		+ savedProcedureId.length() + elementId.length()
		+ fileGuid.length() + type.toString().length()
		+ Integer.toString(fileSize).length()
		+ Integer.toString(start).length()
		+ Integer.toString(end).length()
		+ type.getFilename().length() + byte_data.length;

		int timeout = Constants.getTimeoutForBandwidth(bandwidth, bytes);

		Log.i(TAG, "Setting timeout to " + timeout + " for an estimated " + bytes + " byte upload.");

		PostMethod post = new PostMethod(constructBinaryChunkSubmitURL(mdsUrl));

		Part[] parts = {
				new StringPart("procedure_guid", savedProcedureId),
				new StringPart("element_id", elementId),
				new StringPart("binary_guid", fileGuid),
				new StringPart("element_type", type.toString()),
				new StringPart("file_size", Integer.toString(fileSize)),
				new StringPart("byte_start", Integer.toString(start)),
				new StringPart("byte_end", Integer.toString(end)),
				new FilePart("byte_data", new BytePartSource(byte_data, type.getFilename())),
		};

		post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

		HttpClient client = new HttpClient();
		
		// If there's a proxy enabled, use it.
		if (!"".equals(proxyHost) && proxyPort != 0) {
			Log.i(TAG, "Setting proxy to " + proxyHost + ":" + proxyPort);
			HostConfiguration hc = new HostConfiguration();
			hc.setProxy(proxyHost, (int)proxyPort);
			client.setHostConfiguration(hc);
		}

		try {
			int status = client.executeMethod(post); 
			Log.i(TAG, "postBinaryChunkHack got response code " +  status);

			InputStream responseStream = post.getResponseBodyAsStream();
			Reader reader = new InputStreamReader(responseStream);
			Gson gson = new Gson();
			MDSResult response = gson.fromJson(reader, MDSResult.class);
			Log.i(TAG, "MDS reports " + (response.succeeded() ? "success" : "failure") + " for " + savedProcedureId + ":" + fileGuid );

			result = response.succeeded();
			Log.i(TAG, "result: " + result);

		} catch(FileNotFoundException e) {
			Log.e(TAG, "postBinary got exception : " + e.toString());
			result = false;
		} catch(Exception e) {
			Log.e(TAG, "postBinary got exception : " + e.toString());
			result = false;
		} finally {
			post.releaseConnection();
		}
		return result;
	}

	/**
	 * Posts a single chunk of a binary file.
	 * 
	 * @param c current context
	 * @param savedProcedureId
	 * @param elementId
	 * @param type binary type (ie picture, sound, etc.)
	 * @param start first byte index in binary file (since this presumably is a chunk of a larger file)
	 * @param end last byte index in binary file of the chunk being uploaded
	 * @param byte_data a byte array containing the file chunk data
	 * @return true on successful upload, otherwise false
	 */
	private static boolean postBinary(Context c, String savedProcedureId, String elementId, String fileGuid, 
			ElementType type, int fileSize, int start, int end, byte byte_data[]) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
		boolean hacksMode = preferences.getBoolean(Constants.PREFERENCE_UPLOAD_HACK, false);

		if(hacksMode) {
			return postBinaryAsEncodedText(c, savedProcedureId, elementId, fileGuid, type, fileSize, start, end, byte_data);
		} else {
			return postBinaryAsFile(c, savedProcedureId, elementId, fileGuid, type, fileSize, start, end, byte_data);
		}
	}
	
	public static boolean isProcedureAlreadyUploaded(Uri uri, Context context) {
		Cursor cursor = context.getContentResolver().query(uri, savedProcedureProjection, null,
				null, null);
		// First get the saved procedure...
		cursor.moveToFirst();
		int procedureId = cursor.getInt(1);
		String answersJson = cursor.getString(2);
		boolean savedProcedureUploaded = cursor.getInt(5) != 0;
		cursor.deactivate();

		Uri procedureUri = ContentUris.withAppendedId(ProcedureSQLFormat.CONTENT_URI, procedureId);
		Log.i(TAG, "Getting procedure " + procedureUri.toString());
		cursor = context.getContentResolver().query(procedureUri, new String[] { ProcedureSQLFormat.PROCEDURE }, null, null, null);
		cursor.moveToFirst();
		String procedureXml = cursor.getString(0);
		cursor.deactivate();
		
		if (!savedProcedureUploaded) return false;

		Map<String, Map<String,String>> elementMap = null;
		try {
			Procedure p = Procedure.fromXMLString(procedureXml);
			p.setInstanceUri(uri);

			JSONTokener tokener = new JSONTokener(answersJson);
			JSONObject answersDict = new JSONObject(tokener);

			Map<String,String> answersMap = new HashMap<String,String>();
			Iterator<?> it = answersDict.keys();
			while(it.hasNext()) {
				String key = (String)it.next();
				answersMap.put(key, answersDict.getString(key));
				Log.i(TAG, "onCreate() : answer '" + key + "' : '" + answersDict.getString(key) +"'");
			}
			Log.i(TAG, "onCreate() : restoreAnswers");
			p.restoreAnswers(answersMap);
			elementMap = p.toElementMap();

		} catch (IOException e2) {
			Log.e(TAG, e2.toString());
		} catch (ParserConfigurationException e2) {
			Log.e(TAG, e2.toString());
		} catch (SAXException e2) {
			Log.e(TAG, e2.toString());
		} catch (ProcedureParseException e2) {
			Log.e(TAG, e2.toString());
		} catch (JSONException e) {
			Log.e(TAG, e.toString());
		}

		if(elementMap == null) {
			Log.i(TAG, "Could not read questions and answers from " + uri + ". Not uploading.");
			return false;
		}

		class ElementAnswer {
			public String answer;
			public String type;
			public ElementAnswer(String id, String answer, String type) {
				this.answer = answer;
				this.type = type;
			}
		}

		int totalBinaries = 0;
		List<ElementAnswer> binaries = new ArrayList<ElementAnswer>();
		for(Entry<String,Map<String,String>> e : elementMap.entrySet()) {
			
			String id = e.getKey();
			String type = e.getValue().get("type");
			String answer = e.getValue().get("answer");

			// Find elements that require binary uploads
			if(type.equals(ElementType.PICTURE.toString()) ||
					type.equals(ElementType.BINARYFILE.toString()) ||
					type.equals(ElementType.SOUND.toString())) {
				binaries.add(new ElementAnswer(id, answer, type));
				if(!"".equals(answer)) {
					String[] ids = answer.split(",");
					totalBinaries += ids.length;
				}
			}
		}
		// upload each binary file
		for(ElementAnswer e : binaries) {

			if("".equals(e.answer))
				continue;

			String[] ids = e.answer.split(",");

			for(String binaryId : ids) {
				Uri binUri = null;
				ElementType type = ElementType.INVALID;
				try {
					type = ElementType.valueOf(e.type);
				} catch(IllegalArgumentException ex) {
				}

				if (type == ElementType.PICTURE) {
					binUri = ContentUris.withAppendedId(ImageSQLFormat.CONTENT_URI, Long.parseLong(binaryId));	
				} else if (type == ElementType.SOUND) {
					binUri = ContentUris.withAppendedId(SoundSQLFormat.CONTENT_URI, Long.parseLong(binaryId));
				} else if (type == ElementType.BINARYFILE) {
					binUri = Uri.fromFile(new File(e.answer));
					// We can't tell if a BINARYFILE has been uploaded before.
					// Maybe if we grab the mtime/filesize on the file and store
					// it when we upload it.
				}

				try {
					Log.i(TAG, "Checking if " + binUri + " has been uploaded");
					// reset the new packet size each time to the last successful transmission size
					boolean alreadyUploaded = false;
					Cursor cur;
					switch(type) {
					case PICTURE:
						cur = context.getContentResolver().query(binUri, new String[] { ImageSQLFormat.UPLOADED }, null, null, null);
						cur.moveToFirst();
						alreadyUploaded = cur.getInt(0) != 0;
						if (!alreadyUploaded) return false;
						cur.deactivate();
						break;
					case SOUND:
						cur = context.getContentResolver().query(binUri, new String[] { SoundSQLFormat.UPLOADED }, null, null, null);
						cur.moveToFirst();
						alreadyUploaded = cur.getInt(0) != 0;
						if (!alreadyUploaded) return false;
						cur.deactivate();
						break;
					case BINARYFILE:
					default:
						// Can't do anything since its not in the DB. Sigh.
						break;
					}
				} catch (Exception x) {
					Log.i(TAG, "Error checking if the binary files have been uploaded: " + x.toString());
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Send the entire completed procedure to the Moca Dispatch Server (MDS)
	 * This procedure sends the answer/response pairs and all the binary data (sounds, 
	 * pictures, etc.) to the MDS in a packetized fashion.
	 * 
	 * @param uri uri of procedure in database
	 * @param context current context
	 * @return true if upload was successful, false if not
	 */
	public static boolean postProcedureToDjangoServer(Uri uri, Context context) {
		Log.i(TAG, "In Post procedure to Django server for background uploading service.");
		Cursor cursor = context.getContentResolver().query(uri, savedProcedureProjection, null,
				null, null);
		// First get the saved procedure...
		cursor.moveToFirst();
		int savedProcedureId = cursor.getInt(0);
		int procedureId = cursor.getInt(1);
		String answersJson = cursor.getString(2);
		boolean finished = cursor.getInt(3) != 0;
		String savedProcedureGUID = cursor.getString(4);
		boolean savedProcedureUploaded = cursor.getInt(5) != 0;
		cursor.deactivate();

		Uri procedureUri = ContentUris.withAppendedId(ProcedureSQLFormat.CONTENT_URI, procedureId);
		Log.i(TAG, "Getting procedure " + procedureUri.toString());
		cursor = context.getContentResolver().query(procedureUri, new String[] { ProcedureSQLFormat.TITLE, ProcedureSQLFormat.PROCEDURE }, null, null, null);
		cursor.moveToFirst();
		String procedureTitle = cursor.getString(cursor.getColumnIndex(ProcedureSQLFormat.TITLE));
		String procedureXml = cursor.getString(cursor.getColumnIndex(ProcedureSQLFormat.PROCEDURE));
		cursor.deactivate();

		//Log.i(TAG, "Procedure " + procedureXml);

		if(!finished) {
			Log.i(TAG, "Not finished. Not uploading. (just kidding)" + uri.toString());
			//return false;
		}
		Map<String, Map<String,String>> elementMap = null;
		try {
			Procedure p = Procedure.fromXMLString(procedureXml);
			p.setInstanceUri(uri);

			JSONTokener tokener = new JSONTokener(answersJson);
			JSONObject answersDict = new JSONObject(tokener);

			Map<String,String> answersMap = new HashMap<String,String>();
			Iterator<?> it = answersDict.keys();
			while(it.hasNext()) {
				String key = (String)it.next();
				answersMap.put(key, answersDict.getString(key));
				Log.i(TAG, "onCreate() : answer '" + key + "' : '" + answersDict.getString(key) +"'");
			}
			Log.i(TAG, "onCreate() : restoreAnswers");
			p.restoreAnswers(answersMap);
			elementMap = p.toElementMap();

		} catch (IOException e2) {
			Log.e(TAG, e2.toString());
		} catch (ParserConfigurationException e2) {
			Log.e(TAG, e2.toString());
		} catch (SAXException e2) {
			Log.e(TAG, e2.toString());
		} catch (ProcedureParseException e2) {
			Log.e(TAG, e2.toString());
		} catch (JSONException e) {
			Log.e(TAG, e.toString());
		}

		if(elementMap == null) {
			Log.i(TAG, "Could not read questions and answers from " + uri + ". Not uploading.");
			return false;
		}
		
		// Add in procedureTitle as a fake answer
		Map<String,String> titleMap = new HashMap<String,String>();
		titleMap.put("answer", procedureTitle);
		titleMap.put("id", "procedureTitle");
		titleMap.put("type", "HIDDEN");
		elementMap.put("procedureTitle", titleMap);
		
		Map<String,String> enrolledMap = new HashMap<String,String>();
		enrolledMap.put("answer", "Yes");
		enrolledMap.put("id", "patientEnrolled");
		enrolledMap.put("type", ProcedureElement.ElementType.RADIO.toString());
		enrolledMap.put("question", "Does the patient already have an ID card?");
		elementMap.put("patientEnrolled", enrolledMap);

		class ElementAnswer {
			public String id;
			public String answer;
			public String type;
			public ElementAnswer(String id, String answer, String type) {
				this.id = id;
				this.answer = answer;
				this.type = type;
			}
		}

		JSONObject jsono = new JSONObject();
		int totalBinaries = 0;
		ArrayList<ElementAnswer> binaries = new ArrayList<ElementAnswer>();
		for(Entry<String,Map<String,String>> e : elementMap.entrySet()) {
			try {
				jsono.put(e.getKey(), new JSONObject(e.getValue()));
			} catch (JSONException e1) {
				Log.e(TAG, "Could not convert map " + e.getValue().toString() + " to JSON");
			}

			String id = e.getKey();
			String type = e.getValue().get("type");
			String answer = e.getValue().get("answer");
			
			if (id == null || type == null || answer == null)
				continue;

			// Find elements that require binary uploads
			if(type.equals(ElementType.PICTURE.toString()) ||
					type.equals(ElementType.BINARYFILE.toString()) ||
					type.equals(ElementType.SOUND.toString())) {
				binaries.add(new ElementAnswer(id, answer, type));
				if(!"".equals(answer)) {
					String[] ids = answer.split(",");
					totalBinaries += ids.length;
				}
			}
		}

		Log.i(TAG, "About to post responses.");

		if(savedProcedureUploaded) {
			Log.i(TAG, "Responses have already been sent to MDS, not posting.");
		} else {
			// upload the question and answer pairs text, without using packetization

			String json = jsono.toString();
			Log.i(TAG, "json string: " + json);

			int tries = 0;
			final int MAX_TRIES = 5;
			while(tries < MAX_TRIES) {
				if (postResponses(context, savedProcedureGUID, json)) {
					// Mark the procedure text as uploaded in the database
					ContentValues cv = new ContentValues();
					cv.put(SavedProcedureSQLFormat.UPLOADED, true);
					context.getContentResolver().update(uri, cv, null, null);
					Log.i(TAG, "Responses were uploaded successfully.");
					break;
				}
				tries++;
			}

			if(tries == MAX_TRIES) {
				Log.e(TAG, "Could not post responses, bailing.");
				return false;
			}

		}


		Log.i(TAG, "Posted responses, now sending " + totalBinaries + " binaries.");


		// lookup starting packet size
		int newPacketSize;
		try {
			newPacketSize = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("s_packet_init_size", Integer.toString(Constants.DEFAULT_INIT_PACKET_SIZE)));
		} catch (NumberFormatException e) {
			newPacketSize = Constants.DEFAULT_INIT_PACKET_SIZE;
		}
		// adjust from KB to bytes
		newPacketSize *= 1000;

		int totalProgress = 1+totalBinaries;
		int thisProgress = 2;

		// upload each binary file
		for(ElementAnswer e : binaries) {

			if("".equals(e.answer))
				continue;

			String[] ids = e.answer.split(",");

			for(String binaryId : ids) {


				Uri binUri = null;
				ElementType type = ElementType.INVALID;
				try {
					type = ElementType.valueOf(e.type);
				} catch(IllegalArgumentException ex) {

				}

				if (type == ElementType.PICTURE) {
					binUri = ContentUris.withAppendedId(ImageSQLFormat.CONTENT_URI, Long.parseLong(binaryId));	
				} else if (type == ElementType.SOUND) {
					binUri = ContentUris.withAppendedId(SoundSQLFormat.CONTENT_URI, Long.parseLong(binaryId));
				} else if (type == ElementType.BINARYFILE) {
					binUri = Uri.fromFile(new File(e.answer));
					// We can't tell if a BINARYFILE has been uploaded before.
					// Maybe if we grab the mtime/filesize on the file and store
					// it when we upload it.
				}

				try {
					Log.i(TAG, "Uploading " + binUri);
					// reset the new packet size each time to the last successful transmission size

					newPacketSize = transmitBinary(context, savedProcedureGUID, e.id, binaryId, type, binUri, newPacketSize);

					// Delete the file!
					switch(type) {
					case PICTURE:
					case SOUND:
						//This was deleting the pictures after upload - should not happen, leave commented out!
						//context.getContentResolver().delete(binUri, null, null);
						break;
					default:
					}
				} catch (Exception x) {
					Log.i(TAG, "Uploading " + binUri + " failed : " + x.toString());
					return false;
				}
				thisProgress++;
			}
		}
		// TODO Tag entire procedure in db as done transmitting
		return true;   
	}
	
	/**
	 * Sends an entire binary file in a packetized fashion. This method is where the automatic 
	 * ramping packetization takes place.
	 * Does uploading in the background
	 * 
	 * @param c current context
	 * @param savedProcedureId
	 * @param elementId
	 * @param type binary type (ie picture, sound, etc.)
	 * @param binaryUri uri of the file to be transmitted
	 * @param startPacketSize the starting packet size for each chunk; this will be throttled up or down depending on connection strength
	 * @return the last successful chunk transmission size on success so that it can be used for future transmissions as the startPacketSize
	 * @throws Exception on upload failure
	 */
	private static int transmitBinary(Context c, String savedProcedureId, String elementId, String binaryGuid, ElementType type, Uri binaryUri, int startPacketSize) throws Exception {
		int packetSize, fileSize;
		ContentValues cv = new ContentValues();

		packetSize = startPacketSize;

		boolean alreadyUploaded = false;
		int currPosition = 0;
		Cursor cur;
		switch(type) {
		case PICTURE:
			cur = c.getContentResolver().query(binaryUri, new String[] { ImageSQLFormat.UPLOADED, ImageSQLFormat.UPLOAD_PROGRESS }, null, null, null);
			cur.moveToFirst();
			alreadyUploaded = cur.getInt(0) != 0;
			currPosition = cur.getInt(1);
			cur.deactivate();
			break;
		case SOUND:
			cur = c.getContentResolver().query(binaryUri, new String[] { SoundSQLFormat.UPLOADED, SoundSQLFormat.UPLOAD_PROGRESS }, null, null, null);
			cur.moveToFirst();
			alreadyUploaded = cur.getInt(0) != 0;
			currPosition = cur.getInt(1);
			cur.deactivate();
			break;
		case BINARYFILE:
		default:
			// Can't do anything since its not in the DB. Sigh.
			break;

		}

		if(alreadyUploaded) {
			Log.i(TAG, binaryUri + " was already uploaded. Skipping.");
			return startPacketSize;
		}



		InputStream is = c.getContentResolver().openInputStream(binaryUri);
		fileSize = is.available();

		// Skip forward by the progress we've made previously.
		is.skip(currPosition);

		int progress = (int)(100.0 * currPosition / fileSize);

		int bytesRemaining = fileSize - currPosition;
		Log.i(TAG, "transmitBinary uploading " + binaryUri + " " + bytesRemaining + " total bytes remaining. Starting at " + packetSize + " packet size");

		// reference packet rate byte/msec
		double basePacketRate = 0.0;
		while(bytesRemaining > 0) {
			// get starting time of packet transmission
			long transmitStartTime = new Date().getTime();
			// if transmission rate is acceptable (comparison between currPacketRate and basePacketRate)
			boolean efficient = false;

			int bytesToRead = Math.min(packetSize, bytesRemaining);
			byte[] chunk = new byte[bytesToRead];
			int bytesRead = is.read(chunk, 0, bytesToRead);

			boolean success = false;
			while(!success) {
				Log.i(TAG, "Trying to upload " + bytesRead + " bytes for " + savedProcedureId + ":" + elementId + ".");
				success = postBinary(c, savedProcedureId, elementId, binaryGuid, type, fileSize, currPosition, currPosition+bytesRead, chunk);

				efficient = false;
				// new rate is compared to 80% of previous rate
				basePacketRate *= 0.8;
				if(success) {
					long transmitEndTime = new Date().getTime();
					// get new packet rate
					double currPacketRate = (double)packetSize/(double)(transmitEndTime-transmitStartTime);
					Log.i(TAG, "packet rate = (current) " + currPacketRate + ", (base) " + basePacketRate);
					if(currPacketRate > basePacketRate) {
						basePacketRate = currPacketRate;
						efficient = true;
					}
				}

				if(efficient) {
					packetSize *= 2;
					Log.i(TAG, "Shifting packet size *2 =" + packetSize);
				} else {
					packetSize /= 2;
					Log.i(TAG, "Shifting packet size /2 =" + packetSize);
				}

				if(packetSize < Constants.MIN_PACKET_SIZE * 1000) {
					// TODO(rryan) : fail at some point
					is.close();
					throw new IOException("Could not upload " + binaryUri +". failed after " + (fileSize-bytesRemaining) + " bytes.");
				}
			}

			bytesRemaining -= bytesRead;
			currPosition += bytesRead;

			progress = (int)(100.0 * currPosition / fileSize);

			// write current progress to database
			cv.clear();
			switch(type) {
			case PICTURE:
				cv.put(ImageSQLFormat.UPLOAD_PROGRESS, currPosition);
				c.getContentResolver().update(binaryUri, cv, null, null);
				break;
			case SOUND:
				cv.put(SoundSQLFormat.UPLOAD_PROGRESS, currPosition);
				c.getContentResolver().update(binaryUri, cv, null, null);
				break;
			}

		}

		// Mark file as uploaded in the database
		cv.clear();
		switch(type) {
		case PICTURE:
			cv.put(ImageSQLFormat.UPLOADED, true);
			c.getContentResolver().update(binaryUri, cv, null, null);
			break;
		case SOUND:
			cv.put(SoundSQLFormat.UPLOADED, true);
			c.getContentResolver().update(binaryUri, cv, null, null);
			break;
		}

		is.close();

		return packetSize;
	}

	public static boolean validateCredentials(Context c) throws IOException {
		Log.i(TAG, "validateCredentials was called");
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(c);
		String username = preferences.getString(
				Constants.PREFERENCE_EMR_USERNAME, Constants.DEFAULT_USERNAME);
		String password = preferences.getString(
				Constants.PREFERENCE_EMR_PASSWORD, Constants.DEFAULT_PASSWORD);
		String mdsURL = preferences.getString(Constants.PREFERENCE_MDS_URL,
				Constants.DEFAULT_DISPATCH_SERVER);
		String proxyHost = preferences.getString(Constants.PREFERENCE_PROXY_HOST, "");
		String sProxyPort = preferences.getString(Constants.PREFERENCE_PROXY_PORT, "0");
		int proxyPort = 0;
		try {
			if (!"".equals(sProxyPort)) 
				proxyPort = Integer.parseInt(sProxyPort);
		} catch(NumberFormatException e) {
			Log.w(TAG, "Invalid proxy port: " + sProxyPort);
		}
		
		mdsURL = checkMDSUrl(mdsURL);
		String resultData = "";
		boolean result = true;
		JSONObject jsono = new JSONObject();
		try {
			jsono.put("username", username).put("password", password);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PostMethod post = new PostMethod(
				constructValidateCredentialsURL(mdsURL));
		post.addParameter("username", username);
		post.addParameter("password", password);
		Log.i(TAG, "sending to MDS: username: " + username + ", password: "
				+ password);
		HttpClient client = new HttpClient();
		
		// If there's a proxy enabled, use it.
		if (!"".equals(proxyHost) && proxyPort != 0) {
			Log.i(TAG, "Setting proxy to " + proxyHost + ":" + proxyPort);
			HostConfiguration hc = new HostConfiguration();
			hc.setProxy(proxyHost, (int)proxyPort);
			client.setHostConfiguration(hc);
		} else {
			Log.i(TAG, "Not using a proxy, " + proxyHost + ":"+ proxyPort);
		}

		try {
			int status = client.executeMethod(post);
			Log.i(TAG, "validateCredentials got response code " + status);

			Reader reader = new InputStreamReader(post
					.getResponseBodyAsStream());
			Gson gson = new Gson();
			MDSResult response = gson.fromJson(reader, MDSResult.class);

			// Use this response to validate the password
			resultData = response.getData();
			result = response.succeeded();
			Log.i(TAG, "MDS reports " + (result ? "success" : "failure")
					+ " for credentials");
			Log.i(TAG, "Response data: " + resultData);
			// if (!result && resultData == "Incorrect openMRS Password!")
			// badPasswordFlag = true;

		} catch (IOException e1) {
			Log.e(TAG, "While checking credentials got IOException: " + e1.toString());
			result = false;
			throw e1; // Throw it up so that the layer above knows we failed and didn't get a definite no.
		} catch (JsonParseException e) {
			Log.e(TAG, "validateCredentials error while parsing MDS response: " + e);
			result = false;
		} catch (Exception e) {
			Log.e(TAG, "validateCredentials got exception: " + e.toString());
		} finally {
			post.releaseConnection();
		}
		Log.i(TAG, "" + "Result: " + result);
		return result;
	}

	// Sync patient database on phone with MRS
	public static boolean updatePatientDatabase(Context c, ContentResolver cr) {
		Log.i(TAG, "updating the database to sync with openmrs");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
		String password = preferences.getString(Constants.PREFERENCE_EMR_PASSWORD, Constants.DEFAULT_PASSWORD);
		String username = preferences.getString(Constants.PREFERENCE_EMR_USERNAME, Constants.DEFAULT_USERNAME);
		String mdsURL = preferences.getString(Constants.PREFERENCE_MDS_URL,
				Constants.DEFAULT_DISPATCH_SERVER);
		String proxyHost = preferences.getString(Constants.PREFERENCE_PROXY_HOST, "");
		String sProxyPort = preferences.getString(Constants.PREFERENCE_PROXY_PORT, "0");
		int proxyPort = 0;
		try {
			if (!"".equals(sProxyPort)) 
				proxyPort = Integer.parseInt(sProxyPort);
		} catch(NumberFormatException e) {
			Log.w(TAG, "Invalid proxy port: " + sProxyPort);
		}
		
		mdsURL = checkMDSUrl(mdsURL);

		PostMethod post = new PostMethod(constructDatabaseDownloadURL(mdsURL));
		post.addParameter("username", username);
		post.addParameter("password", password);
		Log.i(TAG, "sending to MDS for database download: username: " + username + ", password: " + password);
		HttpClient client = new HttpClient();
		
		// If there's a proxy enabled, use it.
		if (!"".equals(proxyHost) && proxyPort != 0) {
			Log.i(TAG, "Setting proxy to " + proxyHost + ":" + proxyPort);
			HostConfiguration hc = new HostConfiguration();
			hc.setProxy(proxyHost, (int)proxyPort);
			client.setHostConfiguration(hc);
		}
		
		try {
			int status = client.executeMethod(post);
			Log.i(TAG, "updateDatabase got response code " +  status);
			Reader reader = new InputStreamReader(post.getResponseBodyAsStream());
			Gson gson = new Gson();
			MDSResult response = gson.fromJson(reader, MDSResult.class);
			String toparse = response.getData();
			MocaUtil.clearPatientData(c);
			
			//the following line needs to be uncommented eventually
			UserDatabase.addDataToUsers(cr, toparse);
			Log.i(TAG, "our database looks like this: " + toparse);
		} catch (Exception e) {
			Log.i(TAG, "Caught an exception while trying to download patient data from the MDS: " + e.toString());
			Log.i(TAG, e.getStackTrace().toString());
			return false; 
		}
		return true;
	}
	
	public static String getUserInfo(Context c, String userid) {
	
		String info = null;
		Log.i(TAG, "getting specific patient info from openmrs");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
		String password = preferences.getString(Constants.PREFERENCE_EMR_PASSWORD, Constants.DEFAULT_PASSWORD);
		String username = preferences.getString(Constants.PREFERENCE_EMR_USERNAME, Constants.DEFAULT_USERNAME);
		String mdsURL = preferences.getString(Constants.PREFERENCE_MDS_URL,
				Constants.DEFAULT_DISPATCH_SERVER);
		String proxyHost = preferences.getString(Constants.PREFERENCE_PROXY_HOST, "");
		String sProxyPort = preferences.getString(Constants.PREFERENCE_PROXY_PORT, "0");
		int proxyPort = 0;
		try {
			if (!"".equals(sProxyPort)) 
				proxyPort = Integer.parseInt(sProxyPort);
		} catch(NumberFormatException e) {
			Log.w(TAG, "Invalid proxy port: " + sProxyPort);
		}
		
		mdsURL = checkMDSUrl(mdsURL);
		Log.i(TAG, "our url is " + constructUserInfoURL(mdsURL,userid));
		PostMethod post = new PostMethod(constructUserInfoURL(mdsURL, userid));
		post.addParameter("username", username);
		post.addParameter("password", password);
		Log.i(TAG, "sending to MDS for user info download: username: " + username + ", password: " + password);
		HttpClient client = new HttpClient();
		
		// If there's a proxy enabled, use it.
		if (!"".equals(proxyHost) && proxyPort != 0) {
			Log.i(TAG, "Setting proxy to " + proxyHost + ":" + proxyPort);
			HostConfiguration hc = new HostConfiguration();
			hc.setProxy(proxyHost, (int)proxyPort);
			client.setHostConfiguration(hc);
		}

		try {
			int status = client.executeMethod(post); 
			Log.i(TAG, "postResponses got response code " +  status);
			Reader reader = new InputStreamReader(post.getResponseBodyAsStream());
			Gson gson = new Gson();
			MDSResult response = gson.fromJson(reader, MDSResult.class);
			info = response.getData();
			Log.i(TAG, "our user info looks like this: " + info);
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			Log.i(TAG, e.getStackTrace().toString());
		}
		
		return info;
	}
	
	public static boolean isNewPatientIdValid(Context c, String userid) {
		Log.i(TAG, "Checking if new patient Id is available to use");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
		String password = preferences.getString(Constants.PREFERENCE_EMR_PASSWORD, Constants.DEFAULT_PASSWORD);
		String username = preferences.getString(Constants.PREFERENCE_EMR_USERNAME, Constants.DEFAULT_USERNAME);
		String mdsURL = preferences.getString(Constants.PREFERENCE_MDS_URL,
				Constants.DEFAULT_DISPATCH_SERVER);
		String proxyHost = preferences.getString(Constants.PREFERENCE_PROXY_HOST, "");
		String sProxyPort = preferences.getString(Constants.PREFERENCE_PROXY_PORT, "0");
		
		int proxyPort = 0;
		try {
			if (!"".equals(sProxyPort)) 
				proxyPort = Integer.parseInt(sProxyPort);
		} catch(NumberFormatException e) {
			Log.w(TAG, "Invalid proxy port: " + sProxyPort);
		}
		
		mdsURL = checkMDSUrl(mdsURL);
		Log.i(TAG, "our url is " + constructUserInfoURL(mdsURL,userid));
		PostMethod post = new PostMethod(constructUserInfoURL(mdsURL, userid));
		post.addParameter("username", username);
		post.addParameter("password", password);
		Log.i(TAG, "sending to MDS for user info download: username: " + username + ", password: " + password);
		HttpClient client = new HttpClient();
		
		// If there's a proxy enabled, use it.
		if (!"".equals(proxyHost) && proxyPort != 0) {
			Log.i(TAG, "Setting proxy to " + proxyHost + ":" + proxyPort);
			HostConfiguration hc = new HostConfiguration();
			hc.setProxy(proxyHost, (int)proxyPort);
			client.setHostConfiguration(hc);
		}

		try {
			int status = client.executeMethod(post); 
			Log.i(TAG, "isNewPatientValid got response code " +  status);
			Reader reader = new InputStreamReader(post.getResponseBodyAsStream());
			Gson gson = new Gson();
			MDSResult response = gson.fromJson(reader, MDSResult.class);
			String data = response.getData();
			Log.i(TAG, "data in response: "+ data);
			if (data != null && !"".equals(data)) {
				Log.i(TAG, "Response failed - id already in use.");
				return false;
			}
			Log.i(TAG, "Response succeeded - Id is not in use and is valid");
			return true;
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			Log.i(TAG, e.getStackTrace().toString());
		}
		return true;
	}
	
	
	public static boolean submitEvents(Context c, List<Event> eventsList) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
		String phoneId = preferences.getString("s_phone_name", Constants.PHONE_ID);
		String password = preferences.getString(Constants.PREFERENCE_EMR_PASSWORD, Constants.DEFAULT_PASSWORD);
		String username = preferences.getString(Constants.PREFERENCE_EMR_USERNAME, Constants.DEFAULT_USERNAME);
		String mdsURL = preferences.getString(Constants.PREFERENCE_MDS_URL,
				Constants.DEFAULT_DISPATCH_SERVER);
		String proxyHost = preferences.getString(Constants.PREFERENCE_PROXY_HOST, "");
		String sProxyPort = preferences.getString(Constants.PREFERENCE_PROXY_PORT, "0");
		
		Log.i(TAG, "Submitting " + eventsList.size() + " events to the MDS");
		
		if (eventsList.size() == 0) {
			return true;
		}
		
		int proxyPort = 0;
		try {
			if (!"".equals(sProxyPort)) 
				proxyPort = Integer.parseInt(sProxyPort);
		} catch(NumberFormatException e) {
			Log.w(TAG, "Invalid proxy port: " + sProxyPort);
		}
		mdsURL = checkMDSUrl(mdsURL);
	
		Gson g = new Gson();
		PostMethod post = new PostMethod(constructEventLogUrl(mdsURL));
		
		post.addParameter("username", username);
		post.addParameter("password", password);
		post.addParameter("client_id", phoneId);
		post.addParameter("events", g.toJson(eventsList));
		
		HttpClient client = new HttpClient();
		
		// If there's a proxy enabled, use it.
		if (!"".equals(proxyHost) && proxyPort != 0) {
			Log.i(TAG, "Setting proxy to " + proxyHost + ":" + proxyPort);
			HostConfiguration hc = new HostConfiguration();
			hc.setProxy(proxyHost, (int)proxyPort);
			client.setHostConfiguration(hc);
		}
		
		try {
			int status = client.executeMethod(post);
			Log.i(TAG, "submitEvents got response code " +  status);
			Reader reader = new InputStreamReader(post.getResponseBodyAsStream());
			
			MDSResult response = g.fromJson(reader, MDSResult.class);
			String data = response.getData();
			
			if (response.succeeded()) {
				Log.i(TAG, "MDS reports success: " + data);
				return true;
			} else {
				Log.e(TAG, "MDS reports failure: " + data);
				return false;
			}
		} catch (Exception e) {
			Log.e(TAG, "While submitting events, got exception: " + e.toString());
			e.printStackTrace();
		} finally {
			post.releaseConnection();
		}
		
		return false;
	}
}
