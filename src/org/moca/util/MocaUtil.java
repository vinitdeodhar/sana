package org.moca.util;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.moca.R;
import org.moca.db.MocaDB.ImageSQLFormat;
import org.moca.db.MocaDB.NotificationSQLFormat;
import org.moca.db.MocaDB.PatientSQLFormat;
import org.moca.db.MocaDB.ProcedureSQLFormat;
import org.moca.db.MocaDB.SavedProcedureSQLFormat;
import org.moca.db.MocaDB.SoundSQLFormat;
import org.moca.procedure.Procedure;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;


public class MocaUtil {
	public static final String TAG = MocaUtil.class.toString();

    private static final String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String randomString(String prefix, int length) {
    	return randomString(prefix, length, alphabet);
    }
    
    public static String randomString(String prefix, int length, String alphabet) {
        StringBuilder sb = new StringBuilder(prefix);
        Random r = new Random();
        int alphabetlength = alphabet.length();
        
        for(int i=0; i<length; i++) {           
            sb.append(alphabet.charAt(r.nextInt(alphabetlength-1)));
        }
        
        return sb.toString();
    } 
    
    public static void errorAlert(Context context, String message) {
        createDialog(context, "Error", message).show();
    }
    
    public static AlertDialog createDialog(Context context, String title, String message) {
        Builder dialogBuilder = new Builder(context);
        dialogBuilder.setPositiveButton(context.getResources().getString(R.string.general_ok), null);
        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        return dialogBuilder.create();
    }
    
    public static String getNodeAttributeOrDefault(Node node, String name, String defaultValue) {
        NamedNodeMap attributes = node.getAttributes();
        Node valueNode = attributes.getNamedItem(name);
        String value = defaultValue;
        if(valueNode != null) 
            value = valueNode.getNodeValue();
        return value;
    }
    
    public static <ExceptionType extends Exception> String getNodeAttributeOrFail(Node node, String name, ExceptionType e) throws ExceptionType {
        NamedNodeMap attributes = node.getAttributes();
        Node valueNode = attributes.getNamedItem(name);
        if(valueNode == null)
            throw e;
        return valueNode.getNodeValue();
    }
    

    /**
     * Utility method for deleting all the elements from a given content URI. You have to provide the name of the primary key column.
     * @param ctx the context whose content resolver to use to lookup the URI
     * @param contentUri the content URI to delete all the items from
     * @param idColumn the column of the primary key for the URI
     */
    private static void deleteContentUri(Context ctx, Uri contentUri, String idColumn) {
    	ctx.getContentResolver().delete(contentUri, null, null);
    }
    
    /**
     * Deleted all Procedures, SavedProcedures, Images, Sounds, and Notifications from the database.
     * @param ctx
     */
    public static void clearDatabase(Context ctx) {
    	deleteContentUri(ctx, ProcedureSQLFormat.CONTENT_URI, ProcedureSQLFormat._ID);
    	deleteContentUri(ctx, SavedProcedureSQLFormat.CONTENT_URI, SavedProcedureSQLFormat._ID);
    	deleteContentUri(ctx, ImageSQLFormat.CONTENT_URI, ImageSQLFormat._ID);
    	deleteContentUri(ctx, SoundSQLFormat.CONTENT_URI, SoundSQLFormat._ID);
    	deleteContentUri(ctx, NotificationSQLFormat.CONTENT_URI, NotificationSQLFormat._ID);
    }
    
    public static void clearPatientData(Context ctx) {
    	deleteContentUri(ctx, PatientSQLFormat.CONTENT_URI, PatientSQLFormat._ID);
    }
    
    
    private static void insertProcedure(Context ctx, int id) {
        
        String title = MocaUtil.randomString("Procedure ", 10);
        String author = "";
        String xml;
        try {
            InputStream rs = ctx.getResources().openRawResource(id);
            byte[] data = new byte[rs.available()];
            rs.read(data);
            xml = new String(data);
            
            //Insert "Find Patient" pages in front of every procedure
            //Convert xml to string
            int idFindPatient = R.raw.findpatient;
            InputStream rsFindPatient = ctx.getResources().openRawResource(idFindPatient);
            byte[] dataFindPatient = new byte[rsFindPatient.available()];
            rsFindPatient.read(dataFindPatient);
            String originalXMLFindPatient = new String(dataFindPatient);
            
            //Remove the Procedure XML header/footer from findpatient.xml
            String findPatientHeader = "<Procedure title=\"Find Patient\">";
            String findPatientFooter = "<//Procedure>";
            String xmlFindPatient = "";
            int strLength = originalXMLFindPatient.length();
            xmlFindPatient = originalXMLFindPatient.substring(findPatientHeader.length()+1,strLength-findPatientFooter.length()-1);            
            
            //Modify the procedure xml
            //Insert the "Find Patient" pages after the Procedure tag and before the procedure's pages
            String startProcHeader = "<Procedure title=";
            String xmlFullProcedure = xml;
            if(xml.startsWith(startProcHeader))
            {	
            	int endOfProcedureTag = xml.indexOf(">")+1;
            	//Procedure Tag
            	String xmlHeader = xml.substring(0,endOfProcedureTag);
            	//Rest of Procedure
            	String xmlRestOfProcedure = xml.substring(endOfProcedureTag+1);
            	//New Complete Procedure with Find Patient XML
            	xmlFullProcedure = xmlHeader + xmlFindPatient + xmlRestOfProcedure;
            }
                        
            Procedure p = Procedure.fromXMLString(xmlFullProcedure);
            title = p.getTitle();
            author = p.getAuthor();
            
            ContentValues cv = new ContentValues();
            cv.put(ProcedureSQLFormat.TITLE, title);
            cv.put(ProcedureSQLFormat.AUTHOR, author);
             
            cv.put(ProcedureSQLFormat.PROCEDURE, xmlFullProcedure);
            ctx.getContentResolver().insert(ProcedureSQLFormat.CONTENT_URI, cv);
        } catch(Exception e) {
            Log.e(TAG, "Couldn't add procedure id=" + id + ", title = " + title + ", to db. Exception : " + e.toString());
        }
    }
    
    /**
     * Loading Moca with XML-described procedures is currently hard-coded. New files can be 
     * added or removed here.
     */
    public static void loadDefaultDatabase(Context ctx) {
      /*
      insertProcedure(ctx, R.raw.bronchitis);
      insertProcedure(ctx, R.raw.cervicalcancer);
      insertProcedure(ctx, R.raw.surgery_demo);
      
      insertProcedure(ctx, R.raw.tbcontact);
      insertProcedure(ctx, R.raw.multiupload_test); 
      */
    	
    	insertProcedure(ctx, R.raw.upload_test);
    	insertProcedure(ctx, R.raw.hiv);
        insertProcedure(ctx, R.raw.cervicalcancer);
        insertProcedure(ctx, R.raw.prenatal);
        insertProcedure(ctx, R.raw.surgery);
        insertProcedure(ctx, R.raw.derma);
        insertProcedure(ctx, R.raw.teleradiology);
        insertProcedure(ctx, R.raw.ophthalmology);
        insertProcedure(ctx, R.raw.tbcontact2);
        insertProcedure(ctx, R.raw.tbpatient);
	insertProcedure(ctx, R.raw.cvd_protocol);
	insertProcedure(ctx, R.raw.oral_cancer);
	insertProcedure(ctx, R.raw.pediatric);

        //insertProcedure(ctx, R.raw.audio_upload_test);
    }

    /** 
     * Returns true 
     * @param c - The current context
     * @return true if Android has either a wifi or cellular connection active
     */
	public static boolean checkConnection(Context c) {
		try {
			TelephonyManager telMan = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
			WifiManager wifiMan = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
			
			if (telMan != null && wifiMan != null) {
				int dataState = telMan.getDataState();
				if (dataState == TelephonyManager.DATA_CONNECTED || (wifiMan.isWifiEnabled() && wifiMan.pingSupplicant()))
					return true;
			}
			
			return false;
		}
		catch (Exception e) {
			Log.e(TAG, "Exception in checkConnection(): " + e.toString());
			return false;
		}
	}
	
	public static AlertDialog createAlertMessage(Context c, String alertMessage) {
		return createAlertMessage(c, alertMessage, null);
	}
	
	public static AlertDialog createAlertMessage(Context c, String alertMessage, DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setMessage(alertMessage).setCancelable(false)
				.setPositiveButton(c.getResources().getString(R.string.general_ok), listener);
		AlertDialog alert = builder.create();
		alert.show();
		return alert;
	}
	
	/**
	 * Format a list of primary keys into a SQLite-formatted list of ids. 
	 * 
	 * Ex 1,2,3 is formatted as (1,2,3)
	 */
	public static String formatPrimaryKeyList(List<Long> idList) {
		StringBuilder sb = new StringBuilder("(");
		Iterator<Long> it = idList.iterator();
		while (it.hasNext()) {
			sb.append(Long.toString(it.next()));
			if (it.hasNext()) {
				sb.append(",");
			}
		}
		sb.append(")");
		return sb.toString();
	}
}
