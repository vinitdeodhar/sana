package org.moca.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

public final class MocaDB {
    public static final String PROCEDURE_AUTHORITY = "org.moca.provider.Procedure";
    public static final String SAVED_PROCEDURE_AUTHORITY = "org.moca.provider.SavedProcedure";
    public static final String IMAGE_AUTHORITY = "org.moca.provider.Image";
    public static final String SOUND_AUTHORITY = "org.moca.provider.Sound";
    public static final String NOTIFICATION_AUTHORITY = "org.moca.provider.Notification";
    public static final String PATIENT_AUTHORITY = "org.moca.provider.Patient";
    public static final String EVENT_AUTHORITY = "org.moca.provider.Event";
    
    public static final String DATABASE_NAME = "moca.db";
    public static final int DATABASE_VERSION = 2; // Reset this to 1 before release
    
    public static final class ProcedureSQLFormat implements BaseColumns {
        private ProcedureSQLFormat() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PROCEDURE_AUTHORITY + "/procedures");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.moca.procedure";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.moca.procedure";

        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        public static final String TITLE = "title";
        
        public static final String AUTHOR = "author";
        
        public static final String GUID = "guid";

        public static final String PROCEDURE = "procedure";

        public static final String CREATED_DATE = "created";

        public static final String MODIFIED_DATE = "modified";
    }
    
    public static final class SavedProcedureSQLFormat implements BaseColumns {
        
        private SavedProcedureSQLFormat() {
        }
        
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + SAVED_PROCEDURE_AUTHORITY + "/savedProcedures");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.moca.savedProcedure";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.moca.savedProcedure";

        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
        public static final String QUEUE_SORT_ORDER = "upload_queue ASC";
        
        // COLUMNS
        
        // the guid of this procedure (randomly generated on insert)
        public static final String GUID = "GUID";

        // foreign key of the procedure we created this from
        public static final String PROCEDURE_ID = "procedure_id";
        
        // The JSON data representing the state of this procedure
        public static final String PROCEDURE_STATE = "procedure";
        
        // We are done inputting data and want to upload it to the MDS now
        public static final String FINISHED = "finished";
        
        // This procedure's text/state has been uploaded to the MDS
		// successfully. This doesn't mean its binaries have been -- only the
		// text.
        public static final String UPLOADED = "uploaded";
        
        // Status of the procedure in the upload queue
        // For use in SavedProcedureList to show each procedure's status
        // 0 - Was never put into queue, or "Not Uploaded"
        // 1 - Still in the queue waiting to be sent
        // 2 - Upload Successful - has been sent to the MDS, no longer in queue
        // 3 - Upload in progress
        // 4 - In the queue but waiting for connectivity to upload
        // 5 - Upload failed
        // 6 - Upload stalled - username/password incorrect
        public static final String UPLOAD_STATUS = "upload_queue_status";
        
        // Keeps track of the background upload queue
        // >=0 -- In queue, shows position in line
        // =-1 -- Not in queue (either never added or upload finished)
        public static final String UPLOAD_QUEUE = "upload_queue";

        public static final String CREATED_DATE = "created";

        public static final String MODIFIED_DATE = "modified";
    }

    public static final class ImageSQLFormat implements BaseColumns {
        private ImageSQLFormat() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + IMAGE_AUTHORITY + "/images");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.moca.image";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.moca.image";

        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        // COLUMNS
        
        public static final String SAVED_PROCEDURE_ID = "procedure";
        
        public static final String ELEMENT_ID = "element_id";
        
        public static final String FILE_URI = "file_uri";
        
        // Is the file written completely to storage yet?
        public static final String FILE_VALID = "file_valid";

        public static final String FILE_SIZE = "file_size";
        
        public static final String UPLOAD_PROGRESS = "upload_progress";
        
        public static final String UPLOADED = "uploaded";

        public static final String CREATED_DATE = "created";

        public static final String MODIFIED_DATE = "modified";
    }

    public static final class SoundSQLFormat implements BaseColumns {
        private SoundSQLFormat() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + SOUND_AUTHORITY + "/sounds");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.moca.sound";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.moca.sound";

        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
        // COLUMNS

        public static final String ELEMENT_ID = "element_id";

        public static final String SAVED_PROCEDURE_ID = "procedure";
        
        public static final String FILE_URI = "file_uri";
        
        // Is the file written completely to storage yet?
//        public static final String FILE_VALID = "file_valid";
//
//        public static final String FILE_SIZE = "file_size";
//        
        public static final String UPLOAD_PROGRESS = "upload_progress";
        
        public static final String UPLOADED = "uploaded";
        
        public static final String CREATED_DATE = "created";

        public static final String MODIFIED_DATE = "modified";
    }
    
    public static final class NotificationSQLFormat implements BaseColumns {
        private NotificationSQLFormat() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + NOTIFICATION_AUTHORITY + "/notifications");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.moca.notification";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.moca.notification";

        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
        // COLUMNS
        
        public static final String NOTIFICATION_GUID = "notification_guid";

        public static final String PROCEDURE_ID = "procedure_id";

        public static final String PATIENT_ID = "patient_id";
        
        public static final String MESSAGE = "message";
        
        public static final String FULL_MESSAGE = "full_message";
        
        public static final String DOWNLOADED = "downloaded";
        
        public static final String CREATED_DATE = "created";

        public static final String MODIFIED_DATE = "modified";
    }
    
    public static final class PatientSQLFormat implements BaseColumns {
    	private PatientSQLFormat() {	
    	}
    	
    	public static final Uri CONTENT_URI = Uri.parse("content://"
    			+ PATIENT_AUTHORITY + "/patients");
    	
    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.moca.patient";
    	
    	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.moca.patient";
    	
    	public static final String DEFAULT_SORT_ORDER = "modified DESC";
    	
    	//COLUMNS
    	
    	public static final String PATIENT_ID = "patient_id";
    	
    	public static final String PATIENT_DOB = "patient_dob";
    	
    	public static final String PATIENT_FIRSTNAME = "patient_firstname";
    	
    	public static final String PATIENT_LASTNAME = "patient_lastname";
    	
    	public static final String PATIENT_GENDER = "patient_gender";
    	
    }
    
    public static final class EventSQLFormat implements BaseColumns {
    	private EventSQLFormat() {
    		
    	}
    	
    	public static final Uri CONTENT_URI = Uri.parse("content://" + EVENT_AUTHORITY + "/events");
    	
    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.moca.event";
    	
    	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.moca.event";
    	
    	public static final String DEFAULT_SORT_ORDER = "modified DESC";
    	
    	/*
    	 * This content provider is for recording random events we want to keep track of in the app. These could be:
    	 * 
    	 * - Exceptions or crashes
    	 * - User actions (for performance measurements)
    	 * - Background process actions (failed uploads, successful uploads, credential results)
    	 * - Device actions (battery updates, GPS locations, network signal strength, network status changes)
    	 * 
    	 * So this could just be type + data. Also it could include some basic references like 
    	 * - Patient reference
    	 * - Encounter reference
    	 * - Procedure reference
    	 */
    	
    	public enum EventType {
    		// Run-time exceptions
    		EXCEPTION,
    		OUT_OF_MEMORY,
    		
    		// Encounter events
    		
    		// ProcedureRunner activity started (onCreate)
    		ENCOUNTER_ACTIVITY_START_OR_RESUME, // confirmed
    		
    		// Called when we the loading task is started
    		ENCOUNTER_LOAD_STARTED, // confirmed
    		// Called when the loading task finished successfully. The UI should now be present.
    		ENCOUNTER_LOAD_FINISHED, // confirmed
    		// Called when the load task failed to load a procedure
    		ENCOUNTER_LOAD_FAILED, // confirmed
    		
    		// When we are loading a saved encounter
    		ENCOUNTER_LOAD_SAVED, // confirmed
    		// When we are loading a new encounter
    		ENCOUNTER_LOAD_NEW_ENCOUNTER, // confirmed
    		// When we are warm-booting (e.g. orientation change, app goes to background, etc)
    		ENCOUNTER_LOAD_HOTLOAD, // confirmed
    		
    		// When the lookup service starts (a modal dialog is present for the duration of this)
    		ENCOUNTER_LOOKUP_PATIENT_START, // confirmed
    		// When the lookup service successfully found the patient
    		ENCOUNTER_LOOKUP_PATIENT_SUCCESS, // confirmed
    		// When the lookup service failed to find the patient
    		ENCOUNTER_LOOKUP_PATIENT_FAILED, // confirmed
    		
    		// When the user saved and quit 
    		ENCOUNTER_SAVE_QUIT, // confirmed
    		// When the user added the case to the upload queue and exited.
    		ENCOUNTER_SAVE_UPLOAD, // confirmed
    		// When the user discarded (deleted) the form and quit
    		ENCOUNTER_DISCARD, // confirmed
    		// when the user exits by some other action like hitting Back on the first page.
    		ENCOUNTER_EXIT_NO_SAVE, // confirmed
    		
    		// When the user jumped to a question
    		ENCOUNTER_JUMP_TO_QUESTION, // confirmed
    		// When the user advanced to the next question
    		ENCOUNTER_NEXT_PAGE, // confirmed
    		// When the user went back to the previous page.
    		ENCOUNTER_PREVIOUS_PAGE, // confirmed
    		// When the page validation failed.
    		ENCOUNTER_PAGE_VALIDATION_FAILED, // confirmed
    		
    		
    		
    		// MDS Events
    		MDS_UPLOAD_START,
    		MDS_UPLOAD_PROCEDURE_START,
    		MDS_UPLOAD_PROCEDURE_FINISH,
    		MDS_UPLOAD_PROCEDURE_FAILED,
    		MDS_UPLOAD_BINARY_START,
    		MDS_UPLOAD_BINARY_FINISH,
    		MDS_UPLOAD_BINARY_FAILED,
    		MDS_UPLOAD_BINARY_CHUNK_START,
    		MDS_UPLOAD_BINARY_CHUNK_FINISH,
    		MDS_UPLOAD_BINARY_CHUNK_FAILED,
    		MDS_UPLOAD_FAILED,
    		MDS_UPLOAD_SUCCESS,
    		MDS_CREDENTIALS_VALIDATED,
    		MDS_SYNC_START,
    		MDS_SYNC_FAILED,
    		MDS_SYNC_FINISH,
    		
    		// Network Events
    		NET_REQUEST_TIMEOUT,
    		NET_RADIO_STATUS_CHANGE,
    		NET_RADIO_SIGNAL_STRENGTH,
    		NET_TRANSFER_RATE,
    		
    		// Phone Events
    		PHONE_BATTERY_LEVEL,
    		PHONE_GPS_LOCATION,
    		PHONE_CPU_LOAD,
    		PHONE_MEMORY_USAGE,
    		
    		UNSPECIFIED
    	}
    	
    	// COLUMNS
    	
    	public static final String EVENT_TYPE = "event_type";
    	
    	public static final String EVENT_VALUE = "event_value";
    	
    	public static final String PATIENT_REFERENCE = "patient_reference";
    	
    	public static final String ENCOUNTER_REFERENCE = "encounter_reference";
    	
    	public static final String USER_REFERENCE = "user_reference";
    	
    	public static final String UPLOADED = "uploaded";
    	
    	public static final String CREATED_DATE = "created";

        public static final String MODIFIED_DATE = "modified";
    	
    }
    
    public static final class DoctorGroupSQLFormat implements BaseColumns {
    	private DoctorGroupSQLFormat() {	
    	}
    	
    	//public static final Uri CONTENT_URI = Uri.parse("content://"
    		//	+ DOCTOR_GROUP_AUTHORITY + "/doctorGroups");
    	
    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.moca.doctorGroup";
    	
    	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.moca.doctorGroup";
    	
    	public static final String DEFAULT_SORT_ORDER = "modified DESC";
    	
    	//COLUMNS
    	
    	public static final String DOCTOR_GROUP_ID = "doctor_group_id";
    	    	
    	public static final String DOCTOR_GROUP_NAME = "doctor_group_name";
    }
    
    /**
     * This class helps open, create, and upgrade the database file.
     */
    public static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, MocaDB.DATABASE_NAME, null, MocaDB.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            ProcedureProvider.onCreateDatabase(db);
            SavedProcedureProvider.onCreateDatabase(db);
            ImageProvider.onCreateDatabase(db);
            SoundProvider.onCreateDatabase(db);
            NotificationProvider.onCreateDatabase(db);
            PatientProvider.onCreateDatabase(db);
            //DoctorGroupProvider.onCreateDatabase(db);
            EventProvider.onCreateDatabase(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            ProcedureProvider.onUpgradeDatabase(db, oldVersion, newVersion);
            SavedProcedureProvider.onUpgradeDatabase(db, oldVersion, newVersion);
            ImageProvider.onUpgradeDatabase(db, oldVersion, newVersion);
            SoundProvider.onUpgradeDatabase(db, oldVersion, newVersion);
            NotificationProvider.onUpgradeDatabase(db, oldVersion, newVersion);
            PatientProvider.onUpgradeDatabase(db, oldVersion, newVersion);
            //DoctorGroupProvider.onUpgradeDatabase(db, oldVersion, newVersion);
            EventProvider.onUpgradeDatabase(db, oldVersion, newVersion);
        }
    }
    
}
