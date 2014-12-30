package org.moca;


/**
 * Defaults and constants. Many of these are overridden if a user changes 
 * the default settings from the settings menu.
 */
public class Constants {
	
    public static final String PHONE_ID = "";
    
    public static final String VALIDATE_CREDENTIALS_PATTERN = "/json/validate/credentials/";
    public static final String PROCEDURE_SUBMIT_PATTERN = "/json/procedure/submit/";
    public static final String BINARY_SUBMIT_PATTERN = "/json/binary/submit/";
    public static final String BINARYCHUNK_SUBMIT_PATTERN = "/json/binarychunk/submit/";
    public static final String BINARYCHUNK_HACK_SUBMIT_PATTERN = "/json/textchunk/submit/";
    public static final String DATABASE_DOWNLOAD_PATTERN = "/json/patient/list/";
    public static final String USERINFO_DOWNLOAD_PATTERN = "/json/patient/";
    public static final String EVENTLOG_SUBMIT_PATTERN = "/json/eventlog/submit/";
    
    public static final int DEFAULT_INIT_PACKET_SIZE = 20; // in KB
    
    public static final int MIN_PACKET_SIZE = 1; // at this point we just give up
    public static final String DEFAULT_BINARY_FILE_FOLDER = "/sdcard/"; // use "/sdcard/dcim/Camera/" for easy testing w/ Android camera app
    public static final int DEFAULT_POLL_PERIOD = 60; // in seconds

    public static final int ESTIMATED_TO_MIN_BANDWIDTH_FACTOR = 1000; // this number is too high to make packetization worthwhile
    
    /** DEFAULT PREFERENCE VALUES */
    public static final String DEFAULT_DISPATCH_SERVER = "http://demo.sanamobile.org/mds";
    public static final String DEFAULT_USERNAME = "admin";
    public static final String DEFAULT_PASSWORD = "Sanamobile1";
    public static final int IMAGE_SCALE_FACTOR = 1; // in KB
    
    /** PREFERENCES */
    public static final String PREFERENCE_PHONE_NAME = "s_phone_name";
    public static final String PREFERENCE_MDS_URL = "moca_dispatch_server_url";
    public static final String PREFERENCE_EMR_USERNAME = "s_username";
    public static final String PREFERENCE_EMR_PASSWORD = "s_password";
    public static final String PREFERENCE_IMAGE_SCALE = "s_pic_scale";
    public static final String PREFERENCE_UPLOAD_HACK = "s_upload_hack";
    public static final String PREFERENCE_BARCODE_ENABLED = "s_barcode_enabled";
    public static final String PREFERENCE_PROXY_HOST = "s_proxy_host";
    public static final String PREFERENCE_PROXY_PORT = "s_proxy_port";
    
    public static final int DEFAULT_DATABASE_UPLOAD = 1;
    public static final String PREFERENCE_DATABASE_UPLOAD = "s_database_refresh_period";
    public static final int USER_INFO_TIMEOUT_PERIOD = 2; //in seconds
    
    // chunksize / estimated bytes per second = average time to upload
    // Timeout = 4 * average time to upload = 4 * chunksize / estimate bps
    
    // Some data rates:
    // GPRS: 56-114 kbit/s   7-14.25 kb/sec
    // EDGE: max 236.8 kbkit/s
    
    // Calculated for mean GPRS speed.
    public static final float ESTIMATED_NETWORK_BANDWIDTH = 10625;
    
    public static int getTimeoutForBandwidth(float bandwidth, int bytes) {
    	return (int)(((1.0 * 1000 * ESTIMATED_TO_MIN_BANDWIDTH_FACTOR) / bandwidth) * bytes);
    }
}
