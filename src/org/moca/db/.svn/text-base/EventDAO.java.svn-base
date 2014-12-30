package org.moca.db;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.moca.db.MocaDB.EventSQLFormat;
import org.moca.db.MocaDB.EventSQLFormat.EventType;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

public class EventDAO {
	private static final String TAG = EventDAO.class.getName();

	public static void registerEvent(Context c, EventType type, String value, String encounterRef, String patientRef, String userRef) {
		Log.i(TAG, "Event " + type + " Value: '" + value + "' Encounter: '" + encounterRef + "' Patient: '" + patientRef + "' User: '" + userRef + "'");
		ContentValues cv = new ContentValues();
		
		cv.put(EventSQLFormat.EVENT_TYPE, type.toString());
		cv.put(EventSQLFormat.EVENT_VALUE, value);
		
		if (encounterRef != null)
			cv.put(EventSQLFormat.ENCOUNTER_REFERENCE, encounterRef);
		if (patientRef != null)
			cv.put(EventSQLFormat.PATIENT_REFERENCE, patientRef);
		if (userRef != null)
			cv.put(EventSQLFormat.USER_REFERENCE, userRef);
		
		c.getContentResolver().insert(EventSQLFormat.CONTENT_URI, cv);
	}
	
	public static void registerEvent(Context c, EventType type, String value) {
		registerEvent(c, type, value, "", "", "");
	}
	
	public static void registerEvent(Context c, EventType type) {
		registerEvent(c, type, "", "", "", "");
	}
	
	public static String getStackTrace(Throwable throwable) {
	    Writer writer = new StringWriter();
	    PrintWriter printWriter = new PrintWriter(writer);
	    throwable.printStackTrace(printWriter);
	    return writer.toString();
	}
	
	public static void logException(Context c, Throwable e) {
		logException(c, e, "", "", "");
	}
	
	public static void logException(Context c, Throwable e, String encounterRef, String patientRef, String userRef) {
		String stackTrace = getStackTrace(e);
		
		EventType et = EventType.EXCEPTION;
		if (e instanceof OutOfMemoryError) {
			et = EventType.OUT_OF_MEMORY;
		}
		
		registerEvent(c, et, stackTrace, encounterRef, patientRef, userRef);
	}
}
