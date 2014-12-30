package org.moca.activity;

import org.moca.Constants;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.text.method.DialerKeyListener;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;

/**
 * Settings creates the settings window for specifying the Moca Dispatch server
 * URLs and the phone name.
 * 
 * If a user does not specify their own values, default values are used. Most of
 * these are stored in Constants. The default phone name is the phone's number.
 * 
 * String values are stored as preferences and can be retrieved as follows:
 * PreferenceManager.getDefaultSharedPreferences(c).getString("key name")
 */
public class Settings extends PreferenceActivity {
	
	public static final String TAG = Settings.class.toString();
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setPreferenceScreen(createPreferenceHierarchy());
	}

	private PreferenceScreen createPreferenceHierarchy() {
		
		// TODO Eliminate programmatic generation of the preference items -- put
		// all this in an XML and inflate it.

		// TODO Also, all the key values for these preferences should be
		// constants! They are littered everywhere in the code!

		// Root
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(
				this);
		
		// System Config Prefs
		PreferenceCategory dialogBasedPrefCat = new PreferenceCategory(this);
		dialogBasedPrefCat.setTitle("Sana Configuration");
		root.addPreference(dialogBasedPrefCat);

		// Moca Dispatch Server URL
		EditTextPreference mdsUrl = new EditTextPreference(this);
		mdsUrl.setDialogTitle("Mobile Dispatch Server URL");
		mdsUrl.setKey(Constants.PREFERENCE_MDS_URL);
		mdsUrl.setTitle("Mobile Dispatch Server URL");
		mdsUrl.setSummary("IP address of MDS, do NOT add a trailing /");
		mdsUrl.setDefaultValue(Constants.DEFAULT_DISPATCH_SERVER);
		dialogBasedPrefCat.addPreference(mdsUrl);

		// Phone name
		String phoneNum = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
				.getLine1Number();
		Log.i(TAG, "Phone number of this phone: " + phoneNum);
		if (phoneNum == null || phoneNum.equals("")) phoneNum = "5555555555";
		EditTextPreference phoneName = new EditTextPreference(this);
		phoneName.setDialogTitle("Phone Name");
		phoneName.setKey(Constants.PREFERENCE_PHONE_NAME);
		phoneName.setTitle("Phone Name");
		phoneName.setSummary("(typically the phone number)");
		// default value is the phone number of the phone
		phoneName.setDefaultValue(phoneNum);
		dialogBasedPrefCat.addPreference(phoneName);

		// Initial packet size
		EditTextPreference initialPacketSize = new EditTextPreference(this);
		initialPacketSize.setDialogTitle("Starting packet size in KB");
		initialPacketSize.setKey("s_packet_init_size");
		initialPacketSize.setTitle("Initial Packet Size");
		initialPacketSize.setSummary("(should be lower in poor coverage areas)");
		// default value is the phone number of the phone
		initialPacketSize.setDefaultValue(Integer
				.toString(Constants.DEFAULT_INIT_PACKET_SIZE));
		initialPacketSize.getEditText().setKeyListener(new DigitsKeyListener());
		dialogBasedPrefCat.addPreference(initialPacketSize);

		// Binary file location
		EditTextPreference binaryFileLocation = new EditTextPreference(this);
		binaryFileLocation.setDialogTitle("External Device File Folder");
		binaryFileLocation.setKey("s_binary_file_path");
		binaryFileLocation.setTitle("External Device File Folder");
		binaryFileLocation
				.setSummary("Folder where binary files for upload are stored");
		binaryFileLocation.setDefaultValue(Constants.DEFAULT_BINARY_FILE_FOLDER);
		dialogBasedPrefCat.addPreference(binaryFileLocation);

		// How often the database gets refreshed
		EditTextPreference databaseRefresh = new EditTextPreference(this);
		databaseRefresh.setDialogTitle("OpenMRS Database Refresh Interval");
		databaseRefresh.setKey(Constants.PREFERENCE_DATABASE_UPLOAD);
		databaseRefresh.setTitle("OpenMRS Database Refresh Interval");
		databaseRefresh.setDefaultValue(Integer.toString(Constants.DEFAULT_DATABASE_UPLOAD));
		databaseRefresh.setSummary("Interval at which the OpenMRS database will be cached, in hours");
		databaseRefresh.getEditText().setKeyListener(new DigitsKeyListener());
		dialogBasedPrefCat.addPreference(databaseRefresh);
		
		// Health worker username for OpenMRS
		EditTextPreference emrUsername = new EditTextPreference(this);
		emrUsername.setDialogTitle("Username");
		emrUsername.setKey(Constants.PREFERENCE_EMR_USERNAME);
		emrUsername.setTitle("Username");
		emrUsername.setSummary("Username for medical records system");
		emrUsername.setDefaultValue(Constants.DEFAULT_USERNAME);
		dialogBasedPrefCat.addPreference(emrUsername);
		

		// Health worker password for OpenMRS
		EditTextPreference emrPassword = new EditTextPreference(this);
		emrPassword.setDialogTitle("Password");
		emrPassword.setKey(Constants.PREFERENCE_EMR_PASSWORD);
		emrPassword.setTitle("Password");
		emrPassword.setSummary("Password for medical records system");
		emrPassword.setDefaultValue(Constants.DEFAULT_PASSWORD);
		dialogBasedPrefCat.addPreference(emrPassword);
		emrPassword.getEditText().setTransformationMethod(
				new PasswordTransformationMethod());
		
		// Proxy host settings
		EditTextPreference proxyHost = new EditTextPreference(this);
		proxyHost.setDialogTitle("Proxy Hostname");
		proxyHost.setKey(Constants.PREFERENCE_PROXY_HOST);
		proxyHost.setTitle("Proxy Hostname");
		proxyHost.setSummary("Enter the hostname of a proxy to use if required (ex: 10.10.1.100). Leave blank if not required.");
		proxyHost.setDefaultValue("");
		dialogBasedPrefCat.addPreference(proxyHost);
		
		// Proxy port settings
		EditTextPreference proxyPort = new EditTextPreference(this);
		proxyPort.setDialogTitle("Proxy Port");
		proxyPort.setKey(Constants.PREFERENCE_PROXY_PORT);
		proxyPort.setTitle("Proxy Port");
		proxyPort.setSummary("Enter the port number of your proxy if required (ex: 9401). Leave blank if not required.");
		proxyPort.setDefaultValue("");
		proxyPort.getEditText().setKeyListener(new DialerKeyListener());
		dialogBasedPrefCat.addPreference(proxyPort);
		
		
		// Estimated network bandwidth
		EditTextPreference estimatedNetworkBandwidth = new EditTextPreference(this);
		estimatedNetworkBandwidth.setDialogTitle("Network Bandwith (in kbps)");
		estimatedNetworkBandwidth.setKey("s_network_bandwidth");
		estimatedNetworkBandwidth.setTitle("Estimated Network Bandwidth (in kbps)");
		estimatedNetworkBandwidth.setSummary("Used for calculating network timeouts.");
		estimatedNetworkBandwidth
				.setDialogMessage("The network bandwidth value will be used to calculate appropriate timeouts for uploading. Enter in kilobytes per second.");
		estimatedNetworkBandwidth.setDefaultValue(Float
				.toString(Constants.ESTIMATED_NETWORK_BANDWIDTH));
		estimatedNetworkBandwidth.getEditText().setKeyListener(new DigitsKeyListener());
		dialogBasedPrefCat.addPreference(estimatedNetworkBandwidth);
		
		// Image downscale factor
		EditTextPreference imageDownscale = new EditTextPreference(this);
		imageDownscale.setDialogTitle("Image downscale factor");
		imageDownscale.setKey(Constants.PREFERENCE_IMAGE_SCALE);
		imageDownscale.setTitle("Image downscale factor");
		imageDownscale.setSummary("Scales down pictures taken with the camera.");
		imageDownscale.setDefaultValue(Integer
				.toString(Constants.IMAGE_SCALE_FACTOR));
		imageDownscale.getEditText().setKeyListener(new DigitsKeyListener());
		dialogBasedPrefCat.addPreference(imageDownscale);

		
		// Whether barcode reading is enabled on the phone
		/*CheckBoxPreference barcodeEnabled = new CheckBoxPreference(this);
		barcodeEnabled.setKey(Constants.PREFERENCE_BARCODE_ENABLED);
		barcodeEnabled.setTitle("Enable barcode reading");
		barcodeEnabled.setSummary("Enable barcode reading of patient and physician ids");
		barcodeEnabled.setDefaultValue(false);
		dialogBasedPrefCat.addPreference(barcodeEnabled);*/
		
		// Whether to enable upload hacks for strict carriers
		CheckBoxPreference enableUploadHack = new CheckBoxPreference(this);
		enableUploadHack.setKey(Constants.PREFERENCE_UPLOAD_HACK);
		enableUploadHack.setTitle("Enable Upload Hack");
		enableUploadHack.setSummary("Enable a hack to send images as text to the MDS. This is a workaround for cell phone carriers which block file uploads");
		enableUploadHack.setDefaultValue(false);
		dialogBasedPrefCat.addPreference(enableUploadHack);
		
		return root;
	}
}
